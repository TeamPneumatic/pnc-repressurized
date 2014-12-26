package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.profiler.Profiler;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;

/**
 * This class is derived from Minecraft's {link EntityAITasks} class. As the original class would need quite a few
 * accesstransformers or reflection calls to do what I want, I've copied most of that class in here.
 */

public class DroneAIManager{
    /** A list of EntityAITaskEntrys in EntityAITasks. */
    public List<EntityAITaskEntry> taskEntries = new ArrayList<EntityAITaskEntry>();

    /** A list of EntityAITaskEntrys that are currently being executed. */
    private final List<EntityAITaskEntry> executingTaskEntries = new ArrayList<EntityAITaskEntry>();

    /** Instance of Profiler. */
    private final Profiler theProfiler;
    private int tickCount;
    public static final int TICK_RATE = 3;
    private static final int MIN_CYCLE_TIME = 40;//lag prevention, prevents drones from cycling very quick through their AI tasks.
    private int cycleTimeCounter;

    private final EntityDrone drone;
    private IProgWidget curActiveWidget;
    private EntityAIBase curWidgetAI;
    private EntityAIBase curWidgetTargetAI;

    public DroneAIManager(Profiler par1Profiler, EntityDrone drone){
        theProfiler = par1Profiler;
        this.drone = drone;
    }

    private void updateWidgetFlow(){
        boolean isExecuting = false;
        cycleTimeCounter++;
        for(EntityAITaskEntry entry : executingTaskEntries) {
            if(curWidgetAI == entry.action) {
                isExecuting = true;
                break;
            }
        }
        if(!isExecuting && curActiveWidget != null && (curWidgetTargetAI == null || !curWidgetTargetAI.shouldExecute())) {
            IProgWidget widget = curActiveWidget.getOutputWidget(drone, drone.progWidgets);
            if(widget != null) {
                setActiveWidget(widget);
            } else {
                /* if(cycleTimeCounter < MIN_CYCLE_TIME) {
                     if(curWidgetAI != null) removeTask(curWidgetAI);
                     if(curWidgetTargetAI != null) drone.targetTasks.removeTask(curWidgetTargetAI);
                     curWidgetAI = null;
                     curWidgetTargetAI = null;
                 } else {*/
                gotoFirstWidget();
                cycleTimeCounter = 0;
                // }
            }
        }
    }

    public void gotoFirstWidget(){
        for(IProgWidget widget : drone.progWidgets) {
            if(widget instanceof ProgWidgetStart) {
                setActiveWidget(widget);
                return;
            }
        }
    }

    private void setActiveWidget(IProgWidget widget){
        boolean first = widget instanceof ProgWidgetStart;
        EntityAIBase targetAI = widget.getWidgetTargetAI(drone, widget);
        EntityAIBase ai = widget.getWidgetAI(drone, widget);
        while(targetAI == null && ai == null) {
            widget = widget.getOutputWidget(drone, drone.progWidgets);
            if(widget == null) {
                if(first) {
                    return;
                } else {
                    gotoFirstWidget();
                    return;
                }
            }
            targetAI = widget.getWidgetTargetAI(drone, widget);
            ai = widget.getWidgetAI(drone, widget);
        }

        curActiveWidget = widget;
        drone.setActiveProgram(widget);
        if(curWidgetAI != null) removeTask(curWidgetAI);
        if(curWidgetTargetAI != null) drone.targetTasks.removeTask(curWidgetTargetAI);
        if(ai != null) addTask(2, ai);
        if(targetAI != null) drone.targetTasks.addTask(2, targetAI);
        curWidgetAI = ai;
        curWidgetTargetAI = targetAI;
    }

    public List<EntityAITaskEntry> getRunningTasks(){
        return taskEntries;
    }

    public EntityAIBase getTargetAI(){
        return curWidgetTargetAI;
    }

    /**
     * 
     *
     *
     *START EntityAITasks code
     *
     *
     *
     */

    public void addTask(int par1, EntityAIBase par2EntityAIBase){
        taskEntries.add(new EntityAITaskEntry(par1, par2EntityAIBase));
    }

    /**
     * removes the indicated task from the entity's AI tasks.
     */
    public void removeTask(EntityAIBase par1EntityAIBase){
        Iterator iterator = taskEntries.iterator();

        while(iterator.hasNext()) {
            EntityAITaskEntry entityaitaskentry = (EntityAITaskEntry)iterator.next();
            EntityAIBase entityaibase1 = entityaitaskentry.action;

            if(entityaibase1 == par1EntityAIBase) {
                if(executingTaskEntries.contains(entityaitaskentry)) {
                    entityaibase1.resetTask();
                    executingTaskEntries.remove(entityaitaskentry);
                }

                iterator.remove();
            }
        }
    }

    public void onUpdateTasks(){
        if(!drone.chargeAI.isExecuting && drone.gotoOwnerAI == null) {
            ArrayList<EntityAITaskEntry> arraylist = new ArrayList<EntityAITaskEntry>();
            Iterator<EntityAITaskEntry> iterator;
            EntityAITaskEntry entityaitaskentry;

            if(tickCount++ % TICK_RATE == 0) {
                iterator = taskEntries.iterator();

                while(iterator.hasNext()) {
                    entityaitaskentry = iterator.next();
                    boolean flag = executingTaskEntries.contains(entityaitaskentry);

                    if(flag) {
                        if(canUse(entityaitaskentry) && canContinue(entityaitaskentry)) {
                            continue;
                        }

                        entityaitaskentry.action.resetTask();
                        executingTaskEntries.remove(entityaitaskentry);
                    }

                    if(canUse(entityaitaskentry) && entityaitaskentry.action.shouldExecute()) {
                        arraylist.add(entityaitaskentry);
                        executingTaskEntries.add(entityaitaskentry);
                    }
                }
                updateWidgetFlow();
            } else {
                iterator = executingTaskEntries.iterator();

                while(iterator.hasNext()) {
                    entityaitaskentry = iterator.next();

                    if(!entityaitaskentry.action.continueExecuting()) {
                        entityaitaskentry.action.resetTask();
                        iterator.remove();
                    }
                }
            }

            theProfiler.startSection("goalStart");
            iterator = arraylist.iterator();

            while(iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                theProfiler.startSection(entityaitaskentry.action.getClass().getSimpleName());
                entityaitaskentry.action.startExecuting();
                theProfiler.endSection();
            }

            theProfiler.endSection();
            theProfiler.startSection("goalTick");
            iterator = executingTaskEntries.iterator();

            while(iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                entityaitaskentry.action.updateTask();
            }

            theProfiler.endSection();
        } else {//drone charging ai is running
            for(EntityAITaskEntry ai : executingTaskEntries) {
                ai.action.resetTask();
            }
            executingTaskEntries.clear();
            drone.setDugBlock(0, 0, 0);
        }
    }

    /**
     * Determine if a specific AI Task should continue being executed.
     */
    private boolean canContinue(EntityAITaskEntry par1EntityAITaskEntry){
        theProfiler.startSection("canContinue");
        boolean flag = par1EntityAITaskEntry.action.continueExecuting();
        theProfiler.endSection();
        return flag;
    }

    /**
     * Determine if a specific AI Task can be executed, which means that all running higher (= lower int value) priority
     * tasks are compatible with it or all lower priority tasks can be interrupted.
     */
    private boolean canUse(EntityAITaskEntry par1EntityAITaskEntry){
        theProfiler.startSection("canUse");
        Iterator iterator = taskEntries.iterator();

        while(iterator.hasNext()) {
            EntityAITaskEntry entityaitaskentry1 = (EntityAITaskEntry)iterator.next();

            if(entityaitaskentry1 != par1EntityAITaskEntry) {
                if(par1EntityAITaskEntry.priority >= entityaitaskentry1.priority) {
                    if(executingTaskEntries.contains(entityaitaskentry1) && !areTasksCompatible(par1EntityAITaskEntry, entityaitaskentry1)) {
                        theProfiler.endSection();
                        return false;
                    }
                } else if(executingTaskEntries.contains(entityaitaskentry1) && !entityaitaskentry1.action.isInterruptible()) {
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
    private boolean areTasksCompatible(EntityAITaskEntry par1EntityAITaskEntry, EntityAITaskEntry par2EntityAITaskEntry){
        return (par1EntityAITaskEntry.action.getMutexBits() & par2EntityAITaskEntry.action.getMutexBits()) == 0;
    }

    public class EntityAITaskEntry{
        /**
         * The EntityAIBase object.
         */
        public EntityAIBase action;
        /**
         * Priority of the EntityAIBase
         */
        public int priority;

        public EntityAITaskEntry(int par2, EntityAIBase par3EntityAIBase){
            priority = par2;
            action = par3EntityAIBase;
        }
    }
}
