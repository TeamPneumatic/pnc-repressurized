package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.PNCConfig;
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
import net.minecraft.util.math.Vec3d;
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
    private final Stack<IProgWidget> jumpBackWidgets = new Stack<>();//Used to jump back to a for each widget.

    private static final int MAX_JUMP_STACK_SIZE = 100;

    public DroneAIManager(IDroneBase drone) {
        theProfiler = drone.world().getProfiler();
        this.drone = drone;
        if (!drone.world().isRemote) {
            // we normally don't get called clientside, but The One Probe can do it
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
        GlobalVariableManager.getInstance().writeItemVars(tag);

        return tag;
    }

    public void readFromNBT(CompoundNBT tag) {
        coordinateVariables.clear();
        ListNBT tagList = tag.getList("coords", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT t = tagList.getCompound(i);
            coordinateVariables.put(t.getString("key"), NBTUtil.readBlockPos(t.getCompound("pos")));
        }

        GlobalVariableManager.readItemVars(tag, itemVariables);
    }

    @Override
    public BlockPos getCoordinate(String varName) {
        BlockPos pos;
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.CoordinateVariable.Drone event = new SpecialVariableRetrievalEvent.CoordinateVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            pos = event.getCoordinate();
        } else if (varName.startsWith("#")) {
            pos = GlobalVariableManager.getInstance().getPos(varName.substring(1));
        } else {
            pos = coordinateVariables.get(varName);
        }
        return pos != null ? pos : BlockPos.ZERO;
    }

    public void setCoordinate(String varName, BlockPos coord) {
        if (varName.startsWith("#")) {
            GlobalVariableManager.getInstance().set(varName.substring(1), coord);
        } else if (!varName.startsWith("$")) coordinateVariables.put(varName, coord);
    }

    @Nonnull
    public ItemStack getStack(String varName) {
        ItemStack item;
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.ItemVariable.Drone event = new SpecialVariableRetrievalEvent.ItemVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            item = event.getItem();
        } else if (varName.startsWith("#")) {
            item = GlobalVariableManager.getInstance().getItem(varName.substring(1));
        } else {
            item = itemVariables.getOrDefault(varName, ItemStack.EMPTY);
        }
        return item;
    }

    public void setItem(String varName, @Nonnull ItemStack item) {
        if (varName.startsWith("#")) {
            GlobalVariableManager.getInstance().set(varName.substring(1), item);
        } else if (!varName.startsWith("$")) itemVariables.put(varName, item);
    }

    private void updateWidgetFlow() {
        boolean isExecuting = false;
        for (EntityAITaskEntry entry : executingTaskEntries) {
            if (curWidgetAI == entry.goal) {
                isExecuting = true;
                break;
            }
        }
        if (!isExecuting && curActiveWidget != null && (curWidgetTargetAI == null || !curWidgetTargetAI.shouldExecute())) {
            IProgWidget widget = curActiveWidget.getOutputWidget(drone, progWidgets);
            if (widget != null) {
                if (curActiveWidget.getOutputWidget() != widget) {
                    if (addJumpBackWidget(curActiveWidget)) return;
                }
                setActiveWidget(widget);
            } else {
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
            for (IProgWidget widget : progWidgets) {
                if (widget instanceof ProgWidgetStart) {
                    setActiveWidget(widget);
                    return;
                }
            }
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
                    if (first) {
                        // only a start widget?
                        return;
                    } else {
                        if (stopWhenEndReached) {
                            // stop executing
                            setActiveWidget(null);
                        } else {
                            // return to the start widget
                            gotoFirstWidget();
                        }
                        return;
                    }
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
        Iterator iterator = taskEntries.iterator();

        while (iterator.hasNext()) {
            EntityAITaskEntry entityaitaskentry = (EntityAITaskEntry) iterator.next();
            Goal entityaibase1 = entityaitaskentry.goal;

            if (entityaibase1 == goal) {
                if (executingTaskEntries.contains(entityaitaskentry)) {
                    entityaibase1.resetTask();
                    executingTaskEntries.remove(entityaitaskentry);
                }

                iterator.remove();
            }
        }
    }
    
    private void pickupItemsIfMagnet() {
        int magnetUpgrades = drone.getUpgrades(EnumUpgrade.MAGNET);
        if (magnetUpgrades > 0) {
            int range = Math.min(6, 1 + magnetUpgrades);
            int rangeSq = range * range;
            Vec3d v = drone.getDronePos();
            AxisAlignedBB aabb = new AxisAlignedBB(v.x, v.y, v.z, v.x, v.y, v.z).grow(range);
            List<ItemEntity> items = drone.world().getEntitiesWithinAABB(ItemEntity.class, aabb,
                    item -> item != null
                            && item.isAlive()
                            && !item.cannotPickup()
                            && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                            && drone.getDronePos().squareDistanceTo(item.getPositionVector()) <= rangeSq);

            for (ItemEntity item : items) {
                DroneEntityAIPickupItems.tryPickupItem(drone, item);
            }
        }
    }

    public void onUpdateTasks() {
        pickupItemsIfMagnet();
        
        if (PNCConfig.Common.Advanced.stopDroneAI) return;

        if (!drone.isAIOverriden()) {
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

                        entityaitaskentry.goal.resetTask();
                        executingTaskEntries.remove(entityaitaskentry);
                    }

                    if (canUse(entityaitaskentry) && entityaitaskentry.goal.shouldExecute()) {
                        arraylist.add(entityaitaskentry);
                        executingTaskEntries.add(entityaitaskentry);
                    }
                }
                updateWidgetFlow();
            } else {
                iterator = executingTaskEntries.iterator();

                while (iterator.hasNext()) {
                    entityaitaskentry = iterator.next();

                    if (!entityaitaskentry.goal.shouldContinueExecuting()) {
                        entityaitaskentry.goal.resetTask();
                        iterator.remove();
                    }
                }
            }

            theProfiler.startSection("goalStart");
            iterator = arraylist.iterator();

            while (iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                theProfiler.startSection(entityaitaskentry.goal.getClass().getSimpleName());
                entityaitaskentry.goal.startExecuting();
                theProfiler.endSection();
            }

            theProfiler.endSection();
            theProfiler.startSection("goalTick");
            iterator = executingTaskEntries.iterator();

            while (iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                entityaitaskentry.goal.tick();
            }

            theProfiler.endSection();
        } else {//drone charging ai is running
            if (!wasAIOveridden && curWidgetTargetAI != null) {
                drone.getTargetAI().removeGoal(curWidgetTargetAI);
            }
            wasAIOveridden = true;
            for (EntityAITaskEntry ai : executingTaskEntries) {
                ai.goal.resetTask();
            }
            executingTaskEntries.clear();
            drone.setDugBlock(null);
        }
    }

    /**
     * Determine if a specific AI Task should continue being executed.
     */
    private boolean canContinue(EntityAITaskEntry par1EntityAITaskEntry) {
        theProfiler.startSection("canContinue");
        boolean flag = par1EntityAITaskEntry.goal.shouldContinueExecuting();
        theProfiler.endSection();
        return flag;
    }

    /**
     * Determine if a specific AI Task can be executed, which means that all running higher (= lower int value) priority
     * tasks are compatible with it or all lower priority tasks can be interrupted.
     */
    private boolean canUse(EntityAITaskEntry par1EntityAITaskEntry) {
        theProfiler.startSection("canUse");

        for (EntityAITaskEntry entry : taskEntries) {
            if (entry != par1EntityAITaskEntry) {
                if (par1EntityAITaskEntry.priority >= entry.priority) {
                    if (executingTaskEntries.contains(entry) && !areTasksCompatible(par1EntityAITaskEntry, entry)) {
                        theProfiler.endSection();
                        return false;
                    }
                } else if (executingTaskEntries.contains(entry) && !entry.goal.isPreemptible()) {
                    theProfiler.endSection();
                    return false;
                }
            }
        }

        theProfiler.endSection();
        return true;
    }

    /**
     * Returns whether two EntityAITaskEntries can be executed concurrently
     */
    private boolean areTasksCompatible(EntityAITaskEntry e1, EntityAITaskEntry e2) {
        EnumSet flags = e2.goal.getMutexFlags();
        return e1.goal.getMutexFlags().stream().noneMatch(flags::contains);
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

    public class EntityAITaskEntry {
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
