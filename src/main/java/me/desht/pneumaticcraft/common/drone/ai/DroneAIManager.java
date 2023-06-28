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

package me.desht.pneumaticcraft.common.drone.ai;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is derived from Minecraft's {link EntityAITasks} class. As the original class would need quite a few
 * accesstransformers or reflection calls to do what I want, I've copied most of that class in here.
 */

public class DroneAIManager implements IVariableProvider {
    public static final int TICK_RATE = 3;

    private final List<WrappedGoal> goals = new ArrayList<>();
    private final List<WrappedGoal> executingGoals = new ArrayList<>();
    private final ProfilerFiller theProfiler;
    private int tickCount;
    private final IDroneBase drone;
    private List<IProgWidget> progWidgets;
    private IProgWidget activeWidget;
    private IProgWidget startWidget;  // cache to reduce search time; this one is referenced a lot
    private Goal currentGoal;
    private Goal currentTargetingGoal;
    private boolean stopWhenEndReached;
    private boolean wasAIOveridden;
    private String currentLabel = "Main"; // Holds the name of the last label that was jumped to.

    private Map<String, BlockPos> coordinateVariables = new HashMap<>();
    private Map<String, ItemStack> itemVariables = new HashMap<>();
    private final Deque<IProgWidget> jumpBackWidgets = new ArrayDeque<>(); // Used to jump back to a foreach widget.

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
        this.progWidgets = ImmutableList.copyOf(progWidgets);
        this.jumpBackWidgets.clear();
        if (progWidgets.isEmpty()) {
            setActiveWidget(null);
            startWidget = null;
        } else {
            for (IProgWidget widget : progWidgets) {
                if (widget instanceof ProgWidgetStart) {
                    startWidget = widget;
                } else if (widget instanceof IVariableWidget v) {
                    v.setAIManager(this);
                }
            }
            restartProgram();
        }
    }

    void connectVariables(DroneAIManager subManager) {
        subManager.coordinateVariables = coordinateVariables;
        subManager.itemVariables = itemVariables;
    }

    public boolean isIdling() {
        return currentGoal == null;
    }

    public Goal getCurrentGoal() {
        return currentGoal;
    }

    public IDroneBase getDrone() {
        return drone;
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag tagList = new ListTag();
        for (Map.Entry<String, BlockPos> entry : coordinateVariables.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putString("key", entry.getKey());
            t.put("pos", NbtUtils.writeBlockPos(entry.getValue()));
            tagList.add(t);
        }
        tag.put("coords", tagList);

        ListTag tagList2 = new ListTag();
        for (Map.Entry<String, ItemStack> entry : itemVariables.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putString("key", entry.getKey());
            t.put("item", entry.getValue().serializeNBT());
            tagList2.add(t);
        }
        tag.put("items", tagList2);

        return tag;
    }

    public void readFromNBT(CompoundTag tag) {
        coordinateVariables.clear();
        ListTag tagList = tag.getList("coords", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag t = tagList.getCompound(i);
            coordinateVariables.put(t.getString("key"), NbtUtils.readBlockPos(t.getCompound("pos")));
        }

        ListTag tagList2 = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList2.size(); i++) {
            CompoundTag t = tagList2.getCompound(i);
            itemVariables.put(t.getString("key"), ItemStack.of(t.getCompound("item")));
        }
    }

    @Override
    public Optional<BlockPos> getCoordinate(UUID id, String varName) {
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.CoordinateVariable.Drone event = new SpecialVariableRetrievalEvent.CoordinateVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            return Optional.ofNullable(event.getCoordinate());
        } else if (varName.startsWith("%") || varName.startsWith("#")) {
            return Optional.ofNullable(GlobalVariableHelper.getPos(drone.getOwnerUUID(), varName));
        } else {
            return Optional.ofNullable(coordinateVariables.get(varName));
        }
    }

    @Override
    public ItemStack getStack(UUID id, String varName) {
        ItemStack item;
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.ItemVariable.Drone event = new SpecialVariableRetrievalEvent.ItemVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            item = event.getItem();
        } else if (varName.startsWith("#") || varName.startsWith("%")) {
            item = GlobalVariableHelper.getStack(drone.getOwnerUUID(), varName);
        } else {
            item = itemVariables.getOrDefault(varName, ItemStack.EMPTY);
        }
        return item;
    }

    public void setCoordinate(String varName, BlockPos coord) {
        if (varName.startsWith("%") || varName.startsWith("#")) {
            GlobalVariableHelper.setPos(drone.getOwnerUUID(), varName, coord);
        } else if (!varName.startsWith("$")) {
            coordinateVariables.put(varName, coord);
            drone.onVariableChanged(varName, true);
        }
    }

    public void setStack(String varName, @Nonnull ItemStack item) {
        if (varName.startsWith("#")) {
            GlobalVariableHelper.setStack(drone.getOwnerUUID(), varName, item);
        } else if (!varName.startsWith("$")) {
            itemVariables.put(varName, item);
            drone.onVariableChanged(varName, false);
        }
    }

    private void updateWidgetFlow() {
        // is the current widget still in the executing list?
        boolean isExecuting = executingGoals.stream().anyMatch(entry -> currentGoal == entry.goal);

        if (!isExecuting && activeWidget != null && (currentTargetingGoal == null || !currentTargetingGoal.canUse())) {
            // move on to the next widget in the program
            drone.resetAttackCount();
            IProgWidget widget = activeWidget.getOutputWidget(drone, progWidgets);
            if (widget != null) {
                // we've jumped to a widget that isn't the direct descendant of the previous (jump, foreach...)
                if (activeWidget.getOutputWidget() != widget && addJumpBackWidget(activeWidget)) {
                    return;
                }
                setActiveWidget(widget);
            } else {
                // end of the program!
                if (stopWhenEndReached && jumpBackWidgets.isEmpty()) {
                    setActiveWidget(null);
                } else {
                    restartProgram();
                }
            }
        }
        if (activeWidget == null && !stopWhenEndReached) {
            restartProgram();
        }
    }

    /**
     * Move execution back to the Start widget, or if we're in a Foreach subroutine, back to the Foreach widget
     */
    private void restartProgram() {
        setLabel("Main");
        setActiveWidget(jumpBackWidgets.isEmpty() ? startWidget : jumpBackWidgets.pop());
    }

    private void setActiveWidget(IProgWidget widget) {
        Goal targetGoal = null;
        Goal goal = null;
        if (widget != null) {
            boolean isStartWidget = widget == startWidget;
            targetGoal = widget.getWidgetTargetAI(drone, widget);
            goal = widget.getWidgetAI(drone, widget);
            Set<IProgWidget> visitedWidgets = new HashSet<>();//Prevent endless loops
            while (!visitedWidgets.contains(widget) && targetGoal == null && goal == null) {
                visitedWidgets.add(widget);
                IProgWidget oldWidget = widget;
                widget = widget.getOutputWidget(drone, progWidgets);
                if (widget == null) {
                    // reached the last widget in the line
                    if (!isStartWidget) {
                        if (stopWhenEndReached && jumpBackWidgets.isEmpty()) {
                            // stop executing
                            setActiveWidget(null);
                        } else {
                            // return to the start widget (or
                            restartProgram();
                        }
                    }
                    return;
                } else if (oldWidget.getOutputWidget() != widget) {
                    // we jumped to a "subroutine"
                    if (addJumpBackWidget(oldWidget)) return;
                }
                targetGoal = widget.getWidgetTargetAI(drone, widget);
                goal = widget.getWidgetAI(drone, widget);
            }
            drone.setActiveProgram(widget);
        } else {
            setLabel("Stopped");
        }

        activeWidget = widget;
        if (currentGoal != null) removeGoal(currentGoal);
        if (currentTargetingGoal != null) drone.getTargetAI().removeGoal(currentTargetingGoal);
        if (goal != null) addGoal(2, goal);
        if (targetGoal != null) drone.getTargetAI().addGoal(2, targetGoal);
        currentGoal = goal;
        currentTargetingGoal = targetGoal;
    }

    private boolean addJumpBackWidget(IProgWidget widget) {
        // note: this returns true if there was a problem adding the widget, false if all OK
        if (widget instanceof IJumpBackWidget) {
            if (jumpBackWidgets.size() >= MAX_JUMP_STACK_SIZE) {
                drone.overload("jumpStackTooLarge", MAX_JUMP_STACK_SIZE);
                jumpBackWidgets.clear();
                setActiveWidget(null);
                return true;
            }
            jumpBackWidgets.push(widget);
        }
        return false;
    }

    public List<WrappedGoal> getRunningTasks() {
        return goals;
    }

    public Goal getTargetAI() {
        return currentTargetingGoal;
    }

    private void addGoal(int priority, Goal goal) {
        goals.add(new WrappedGoal(priority, goal));
    }

    /**
     * removes the indicated task from the entity's AI tasks.
     */
    private void removeGoal(Goal goal) {
        Iterator<WrappedGoal> iterator = goals.iterator();
        while (iterator.hasNext()) {
            WrappedGoal wrappedGoal = iterator.next();
            if (wrappedGoal.goal() == goal) {
                if (executingGoals.contains(wrappedGoal)) {
                    wrappedGoal.goal().stop();
                    executingGoals.remove(wrappedGoal);
                }
                iterator.remove();
            }
        }
    }
    
    private void pickupItemsIfMagnet() {
        int magnetUpgrades = drone.getUpgrades(ModUpgrades.MAGNET.get());
        // drone must also have an active program
        if (magnetUpgrades > 0 && !drone.getProgWidgets().isEmpty()) {
            int range = Math.min(6, 1 + magnetUpgrades);
            int rangeSq = range * range;
            Vec3 v = drone.getDronePos();
            AABB aabb = new AABB(v.x, v.y, v.z, v.x, v.y, v.z).inflate(range);
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
        if (ConfigHelper.common().drones.stopDroneAI.get()) return;

        pickupItemsIfMagnet();

        if (!drone.isAIOverridden()) {
            if (wasAIOveridden && currentTargetingGoal != null) {
                drone.getTargetAI().addGoal(2, currentTargetingGoal);
            }
            wasAIOveridden = false;
            List<WrappedGoal> newGoalList = new ArrayList<>();
            Iterator<WrappedGoal> iterator;
            WrappedGoal currentGoal;

            if (tickCount++ % TICK_RATE == 0) {
                iterator = goals.iterator();
                while (iterator.hasNext()) {
                    currentGoal = iterator.next();
                    if (executingGoals.contains(currentGoal)) {
                        if (canUse(currentGoal) && canContinue(currentGoal)) {
                            continue;
                        }
                        currentGoal.goal.stop();
                        executingGoals.remove(currentGoal);
                    }
                    if (canUse(currentGoal) && currentGoal.goal.canUse()) {
                        newGoalList.add(currentGoal);
                        executingGoals.add(currentGoal);
                    }
                }
                updateWidgetFlow();
            } else {
                // just continue running the currently-executing goal, if possible
                iterator = executingGoals.iterator();
                while (iterator.hasNext()) {
                    currentGoal = iterator.next();
                    if (!currentGoal.goal.canContinueToUse()) {
                        currentGoal.goal.stop();
                        iterator.remove();
                    }
                }
            }

            theProfiler.push("goalStart");
            iterator = newGoalList.iterator();
            while (iterator.hasNext()) {
                currentGoal = iterator.next();
                theProfiler.push(currentGoal.goal.getClass().getSimpleName());
                currentGoal.goal.start();
                theProfiler.pop();
            }
            theProfiler.pop();

            theProfiler.push("goalTick");
            iterator = executingGoals.iterator();
            while (iterator.hasNext()) {
                currentGoal = iterator.next();
                currentGoal.goal.tick();
            }
            theProfiler.pop();
        } else {
            // AI overridden - going to charging station, hacked, ...
            if (!wasAIOveridden && currentTargetingGoal != null) {
                drone.getTargetAI().removeGoal(currentTargetingGoal);
            }
            wasAIOveridden = true;
            executingGoals.forEach(wrappedGoal -> wrappedGoal.goal.stop());
            executingGoals.clear();
            drone.setDugBlock(null);
        }
    }

    /**
     * Determine if a specific AI Task should continue being executed.
     */
    private boolean canContinue(WrappedGoal par1WrappedGoal) {
        theProfiler.push("canContinue");
        boolean flag = par1WrappedGoal.goal.canContinueToUse();
        theProfiler.pop();
        return flag;
    }

    /**
     * Determine if a specific AI Task can be executed, which means that all running higher (= lower int value) priority
     * tasks are compatible with it or all lower priority tasks can be interrupted.
     */
    private boolean canUse(WrappedGoal par1WrappedGoal) {
        theProfiler.push("canUse");

        for (WrappedGoal entry : goals) {
            if (entry != par1WrappedGoal) {
                if (par1WrappedGoal.priority >= entry.priority) {
                    if (executingGoals.contains(entry) && !areTasksCompatible(par1WrappedGoal, entry)) {
                        theProfiler.pop();
                        return false;
                    }
                } else if (executingGoals.contains(entry) && !entry.goal.isInterruptable()) {
                    theProfiler.pop();
                    return false;
                }
            }
        }

        theProfiler.pop();
        return true;
    }

    /**
     * Returns whether two goals can be executed concurrently
     */
    private boolean areTasksCompatible(WrappedGoal e1, WrappedGoal e2) {
        EnumSet<Goal.Flag> flags = e2.goal.getFlags();
        return e1.goal.getFlags().stream().noneMatch(flags::contains);
    }

    public void setLabel(String label) {
        currentLabel = label;
        drone.updateLabel();
    }

    public String getLabel() {
        return currentGoal instanceof DroneAIExternalProgram ext ?
                ext.getRunningAI().getLabel() + "/" + currentLabel :
                currentLabel;
    }

    public List<IProgWidget> widgets() {
        return progWidgets;
    }

    public record WrappedGoal(int priority, Goal goal) {
    }
}
