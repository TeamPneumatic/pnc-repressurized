/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is derived from Minecraft's {link EntityAITasks} class. As the original class would need quite a few
 * accesstransformers or reflection calls to do what I want, I've copied most of that class in here.
 */

public class DroneAIManager implements IVariableProvider {
    /**
     * A list of EntityAITaskEntrys in EntityAITasks.
     */
    private final List<EntityAITaskEntry> taskEntries = new ArrayList<>();

    /**
     * A list of EntityAITaskEntrys that are currently being executed.
     */
    private final List<EntityAITaskEntry> executingTaskEntries = new ArrayList<>();

    /**
     * Instance of Profiler.
     */
    private final IProfiler theProfiler;
    private int tickCount;
    static final int TICK_RATE = 3;

    private final IDroneBase drone;
    private List<IProgWidget> progWidgets;
    private IProgWidget curActiveWidget;
    private Goal curWidgetAI;
    private Goal curWidgetTargetAI;
    private boolean stopWhenEndReached;
    private boolean wasAIOveridden;
    private String currentLabel = "Main";//Holds the name of the last label that was jumped to.

    private Map<String, BlockPos> coordinateVariables = new HashMap<>();
    private Map<String, ItemStack> itemVariables = new HashMap<>();
    private final Deque<IProgWidget> jumpBackWidgets = new ArrayDeque<>(); // A jump-back stack

    private static final int MAX_JUMP_STACK_SIZE = 100;

    public DroneAIManager(IDroneBase drone) {
        theProfiler = drone.world().getProfiler();
        this.drone = drone;
        if (!drone.world().isClientSide) {
            // we don't do much clientside, but instances can be created (programmable controller, The One Probe...)
            // don't try to set the widgets clientside because there aren't any and that messes up entity tracker info
            setWidgets(drone.getProgWidgets());
        }
    }

    public DroneAIManager(IDroneBase drone, List<IProgWidget> progWidgets) {
        theProfiler = drone.world().getProfiler();
        this.drone = drone;
        stopWhenEndReached = true;
        setWidgets(progWidgets);
    }

    public void dontStopWhenEndReached() {
        stopWhenEndReached = false;
    }

    public void setWidgets(List<IProgWidget> progWidgets) {
        this.progWidgets = progWidgets;
        if (progWidgets.isEmpty()) {
            setActiveWidget(null);
        } else {
            for (IProgWidget widget : progWidgets) {
                if (widget instanceof IVariableWidget) {
                    ((IVariableWidget) widget).setAIManager(this);
                }
            }
            gotoFirstWidget();
        }
    }

    void connectVariables(DroneAIManager subAI) {
        subAI.coordinateVariables = coordinateVariables;
        subAI.itemVariables = itemVariables;
    }

    public void clearVariables() {
        coordinateVariables.clear();
        itemVariables.clear();
    }

    public boolean isIdling() {
        return curWidgetAI == null;
    }

    Goal getCurrentAI() {
        return curWidgetAI;
    }

    public IDroneBase getDrone() {
        return drone;
    }

    public CompoundNBT writeToNBT(CompoundNBT tag) {
        ListNBT tagList = new ListNBT();
        for (Map.Entry<String, BlockPos> entry : coordinateVariables.entrySet()) {
            CompoundNBT t = new CompoundNBT();
            t.putString("key", entry.getKey());
            t.put("pos", NBTUtil.writeBlockPos(entry.getValue()));
            tagList.add(t);
        }
        tag.put("coords", tagList);

        ListNBT tagList2 = new ListNBT();
        for (Map.Entry<String, ItemStack> entry : itemVariables.entrySet()) {
            CompoundNBT t = new CompoundNBT();
            t.putString("key", entry.getKey());
            t.put("item", entry.getValue().serializeNBT());
            tagList2.add(t);
        }
        tag.put("items", tagList2);

        return tag;
    }

    public void readFromNBT(CompoundNBT tag) {
        coordinateVariables.clear();
        ListNBT tagList = tag.getList("coords", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT t = tagList.getCompound(i);
            coordinateVariables.put(t.getString("key"), NBTUtil.readBlockPos(t.getCompound("pos")));
        }

        ListNBT tagList2 = tag.getList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList2.size(); i++) {
            CompoundNBT t = tagList2.getCompound(i);
            itemVariables.put(t.getString("key"), ItemStack.of(t.getCompound("item")));
        }
    }

    @Override
    public boolean hasCoordinate(String varName) {
        return getCoordinateInternal(varName) != null;
    }

    @Override
    public BlockPos getCoordinate(String varName) {
        BlockPos pos = getCoordinateInternal(varName);
        return pos != null ? pos : BlockPos.ZERO;
    }

    private BlockPos getCoordinateInternal(String varName) {
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.CoordinateVariable.Drone event = new SpecialVariableRetrievalEvent.CoordinateVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            return event.getCoordinate();
        } else if (varName.startsWith("#")) {
            return GlobalVariableManager.getInstance().getPos(varName.substring(1));
        } else {
            return coordinateVariables.get(varName);
        }
    }

    public void setCoordinate(String varName, BlockPos coord) {
        if (varName.startsWith("#")) {
            GlobalVariableManager.getInstance().set(varName.substring(1), coord);
        } else if (varName.startsWith("%")) {
            GlobalVariableManager.getInstance().set(drone.getOwnerUUID(), varName, coord);
        } else if (!varName.startsWith("$")) {
            coordinateVariables.put(varName, coord);
            drone.onVariableChanged(varName, true);
        }
    }

    @Override
    public boolean hasStack(String varName) {
        return !getStack(varName).isEmpty();
    }

    @Override
    public ItemStack getStack(String varName) {
        ItemStack item;
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.ItemVariable.Drone event = new SpecialVariableRetrievalEvent.ItemVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            item = event.getItem();
        } else if (varName.startsWith("#")) {
            item = GlobalVariableManager.getInstance().getItem(varName.substring(1));
        } else if (varName.startsWith("%")) {
            item = GlobalVariableManager.getInstance().getStack(drone.getOwnerUUID(), varName.substring(1));
        } else {
            item = itemVariables.getOrDefault(varName, ItemStack.EMPTY);
        }
        return item;
    }

    public void setItem(String varName, @Nonnull ItemStack item) {
        if (varName.startsWith("#")) {
            GlobalVariableManager.getInstance().set(varName.substring(1), item);
        } else if (!varName.startsWith("$")) {
            itemVariables.put(varName, item);
            drone.onVariableChanged(varName, false);
        }
    }

    private void updateWidgetFlow() {
        // is the current widget still in the executing list?
        boolean isExecuting = executingTaskEntries.stream().anyMatch(entry -> curWidgetAI == entry.goal);

        if (!isExecuting && curActiveWidget != null && (curWidgetTargetAI == null || !curWidgetTargetAI.canUse())) {
            // move on to the next widget in the program
            drone.resetAttackCount();
            IProgWidget widget = curActiveWidget.getOutputWidget(drone, progWidgets);
            if (widget != null) {
                // we've jumped to a widget that isn't the direct descendant of the previous (jump, foreach...)
                if (curActiveWidget.getOutputWidget() != widget && addJumpBackWidget(curActiveWidget)) {
                    return;
                }
                setActiveWidget(widget);
            } else {
                // end of the program!
                if (stopWhenEndReached) {
                    setActiveWidget(null);
                } else {
                    gotoFirstWidget();
                }
            }
        }
        if (curActiveWidget == null && !stopWhenEndReached) {
            gotoFirstWidget();
        }
    }

    private void gotoFirstWidget() {
        setLabel("Main");
        if (!jumpBackWidgets.isEmpty()) {
            setActiveWidget(jumpBackWidgets.pop());
        } else {
            progWidgets.stream()
                    .filter(widget -> widget instanceof ProgWidgetStart)
                    .findFirst()
                    .ifPresent(this::setActiveWidget);
        }
    }

    private void setActiveWidget(IProgWidget widget) {
        Goal targetAI = null;
        Goal ai = null;
        if (widget != null) {
            boolean first = widget instanceof ProgWidgetStart;
            targetAI = widget.getWidgetTargetAI(drone, widget);
            ai = widget.getWidgetAI(drone, widget);
            Set<IProgWidget> visitedWidgets = new HashSet<>();//Prevent endless loops
            while (!visitedWidgets.contains(widget) && targetAI == null && ai == null) {
                visitedWidgets.add(widget);
                IProgWidget oldWidget = widget;
                widget = widget.getOutputWidget(drone, progWidgets);
                if (widget == null) {
                    // reached the last widget in the line
                    if (!first) {
                        if (stopWhenEndReached) {
                            // stop executing
                            setActiveWidget(null);
                        } else {
                            // return to the start widget
                            gotoFirstWidget();
                        }
                    }
                    return;
                } else if (oldWidget.getOutputWidget() != widget) {
                    // we jumped to a "subroutine"
                    if (addJumpBackWidget(oldWidget)) return;
                }
                targetAI = widget.getWidgetTargetAI(drone, widget);
                ai = widget.getWidgetAI(drone, widget);
            }
            drone.setActiveProgram(widget);
        } else {
            setLabel("Stopped");
        }

        curActiveWidget = widget;
        if (curWidgetAI != null) removeGoal(curWidgetAI);
        if (curWidgetTargetAI != null) drone.getTargetAI().removeGoal(curWidgetTargetAI);
        if (ai != null) addGoal(2, ai);
        if (targetAI != null) drone.getTargetAI().addGoal(2, targetAI);
        curWidgetAI = ai;
        curWidgetTargetAI = targetAI;
    }

    private boolean addJumpBackWidget(IProgWidget widget) {
        if (widget instanceof IJumpBackWidget) {
            if (jumpBackWidgets.size() >= MAX_JUMP_STACK_SIZE) {
                drone.overload("jumpStackTooLarge", MAX_JUMP_STACK_SIZE);
                jumpBackWidgets.clear();
                setActiveWidget(null);
                return true;
            } else {
                jumpBackWidgets.push(widget);
            }
        }
        return false;
    }

    public List<EntityAITaskEntry> getRunningTasks() {
        return taskEntries;
    }

    public Goal getTargetAI() {
        return curWidgetTargetAI;
    }

    /**
     * START EntityAITasks code
     */

    private void addGoal(int priority, Goal goal) {
        taskEntries.add(new EntityAITaskEntry(priority, goal));
    }

    /**
     * removes the indicated task from the entity's AI tasks.
     */
    private void removeGoal(Goal goal) {
        Iterator<EntityAITaskEntry> iterator = taskEntries.iterator();

        while (iterator.hasNext()) {
            EntityAITaskEntry entityaitaskentry = iterator.next();
            Goal entityaibase1 = entityaitaskentry.goal;

            if (entityaibase1 == goal) {
                if (executingTaskEntries.contains(entityaitaskentry)) {
                    entityaibase1.stop();
                    executingTaskEntries.remove(entityaitaskentry);
                }

                iterator.remove();
            }
        }
    }
    
    private void pickupItemsIfMagnet() {
        int magnetUpgrades = drone.getUpgrades(EnumUpgrade.MAGNET);
        // drone must also have an active program
        if (magnetUpgrades > 0 && !drone.getProgWidgets().isEmpty()) {
            int range = Math.min(6, 1 + magnetUpgrades);
            int rangeSq = range * range;
            Vector3d v = drone.getDronePos();
            AxisAlignedBB aabb = new AxisAlignedBB(v.x, v.y, v.z, v.x, v.y, v.z).inflate(range);
            List<ItemEntity> items = drone.world().getEntitiesOfClass(ItemEntity.class, aabb,
                    item -> item != null
                            && item.isAlive()
                            && !item.hasPickUpDelay()
                            && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                            && drone.getDronePos().distanceToSqr(item.position()) <= rangeSq);
            items.forEach(item -> DroneEntityAIPickupItems.tryPickupItem(drone, item));
        }
    }

    public void onUpdateTasks() {
        if (ConfigHelper.common().advanced.stopDroneAI.get()) return;

        pickupItemsIfMagnet();

        if (!drone.isAIOverridden()) {
            if (wasAIOveridden && curWidgetTargetAI != null) drone.getTargetAI().addGoal(2, curWidgetTargetAI);
            wasAIOveridden = false;
            ArrayList<EntityAITaskEntry> arraylist = new ArrayList<>();
            Iterator<EntityAITaskEntry> iterator;
            EntityAITaskEntry entityaitaskentry;

            if (tickCount++ % TICK_RATE == 0) {
                iterator = taskEntries.iterator();

                while (iterator.hasNext()) {
                    entityaitaskentry = iterator.next();
                    boolean flag = executingTaskEntries.contains(entityaitaskentry);

                    if (flag) {
                        if (canUse(entityaitaskentry) && canContinue(entityaitaskentry)) {
                            continue;
                        }

                        entityaitaskentry.goal.stop();
                        executingTaskEntries.remove(entityaitaskentry);
                    }

                    if (canUse(entityaitaskentry) && entityaitaskentry.goal.canUse()) {
                        arraylist.add(entityaitaskentry);
                        executingTaskEntries.add(entityaitaskentry);
                    }
                }
                updateWidgetFlow();
            } else {
                iterator = executingTaskEntries.iterator();

                while (iterator.hasNext()) {
                    entityaitaskentry = iterator.next();

                    if (!entityaitaskentry.goal.canContinueToUse()) {
                        entityaitaskentry.goal.stop();
                        iterator.remove();
                    }
                }
            }

            theProfiler.push("goalStart");
            iterator = arraylist.iterator();

            while (iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                theProfiler.push(entityaitaskentry.goal.getClass().getSimpleName());
                entityaitaskentry.goal.start();
                theProfiler.pop();
            }

            theProfiler.pop();
            theProfiler.push("goalTick");
            iterator = executingTaskEntries.iterator();

            while (iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                entityaitaskentry.goal.tick();
            }

            theProfiler.pop();
        } else {//drone charging ai is running
            if (!wasAIOveridden && curWidgetTargetAI != null) {
                drone.getTargetAI().removeGoal(curWidgetTargetAI);
            }
            wasAIOveridden = true;
            for (EntityAITaskEntry ai : executingTaskEntries) {
                ai.goal.stop();
            }
            executingTaskEntries.clear();
            drone.setDugBlock(null);
        }
    }

    /**
     * Determine if a specific AI Task should continue being executed.
     */
    private boolean canContinue(EntityAITaskEntry par1EntityAITaskEntry) {
        theProfiler.push("canContinue");
        boolean flag = par1EntityAITaskEntry.goal.canContinueToUse();
        theProfiler.pop();
        return flag;
    }

    /**
     * Determine if a specific AI Task can be executed, which means that all running higher (= lower int value) priority
     * tasks are compatible with it or all lower priority tasks can be interrupted.
     */
    private boolean canUse(EntityAITaskEntry par1EntityAITaskEntry) {
        theProfiler.push("canUse");

        for (EntityAITaskEntry entry : taskEntries) {
            if (entry != par1EntityAITaskEntry) {
                if (par1EntityAITaskEntry.priority >= entry.priority) {
                    if (executingTaskEntries.contains(entry) && !areTasksCompatible(par1EntityAITaskEntry, entry)) {
                        theProfiler.pop();
                        return false;
                    }
                } else if (executingTaskEntries.contains(entry) && !entry.goal.isInterruptable()) {
                    theProfiler.pop();
                    return false;
                }
            }
        }

        theProfiler.pop();
        return true;
    }

    /**
     * Returns whether two EntityAITaskEntries can be executed concurrently
     */
    private boolean areTasksCompatible(EntityAITaskEntry e1, EntityAITaskEntry e2) {
        EnumSet<Goal.Flag> flags = e2.goal.getFlags();
        return e1.goal.getFlags().stream().noneMatch(flags::contains);
    }

    public void setLabel(String label) {
        currentLabel = label;
        drone.updateLabel();
    }

    public String getLabel() {
        if (curWidgetAI instanceof DroneAIExternalProgram) {
            return ((DroneAIExternalProgram) curWidgetAI).getRunningAI().getLabel() + " --> " + currentLabel;
        } else {
            return currentLabel;
        }
    }

    public static class EntityAITaskEntry {
        /**
         * The EntityAIBase object.
         */
        public final Goal goal;
        /**
         * Priority of the EntityAIBase
         */
        public final int priority;

        EntityAITaskEntry(int priority, Goal goal) {
            this.priority = priority;
            this.goal = goal;
        }
    }

}
