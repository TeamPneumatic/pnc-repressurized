package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.MinecraftForge;
import pneumaticCraft.api.drone.SpecialVariableRetrievalEvent;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.IVariableWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;
import pneumaticCraft.common.remote.GlobalVariableManager;

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

    private final EntityDrone drone;
    private List<IProgWidget> progWidgets;
    private IProgWidget curActiveWidget;
    private EntityAIBase curWidgetAI;
    private EntityAIBase curWidgetTargetAI;
    private boolean stopWhenEndReached;

    private final Map<String, ChunkPosition> coordinateVariables = new HashMap<String, ChunkPosition>();

    public DroneAIManager(EntityDrone drone){
        theProfiler = drone.worldObj.theProfiler;
        this.drone = drone;
        setWidgets(drone.progWidgets);
    }

    public DroneAIManager(EntityDrone drone, List<IProgWidget> progWidgets){
        theProfiler = drone.worldObj.theProfiler;
        this.drone = drone;
        stopWhenEndReached = true;
        setWidgets(progWidgets);
    }

    public void setWidgets(List<IProgWidget> progWidgets){
        this.progWidgets = progWidgets;
        for(IProgWidget widget : progWidgets) {
            if(widget instanceof IVariableWidget) {
                ((IVariableWidget)widget).setAIManager(this);
            }
        }
        gotoFirstWidget();
    }

    public boolean isIdling(){
        return curWidgetAI == null;
    }

    public EntityAIBase getCurrentAI(){
        return curWidgetAI;
    }

    public void writeToNBT(NBTTagCompound tag){
        NBTTagList tagList = new NBTTagList();
        for(Map.Entry<String, ChunkPosition> entry : coordinateVariables.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("key", entry.getKey());
            t.setInteger("x", entry.getValue().chunkPosX);
            t.setInteger("y", entry.getValue().chunkPosY);
            t.setInteger("z", entry.getValue().chunkPosZ);
            tagList.appendTag(t);
        }
        tag.setTag("coords", tagList);
    }

    public void readFromNBT(NBTTagCompound tag){
        coordinateVariables.clear();
        NBTTagList tagList = tag.getTagList("coords", 10);
        for(int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound t = tagList.getCompoundTagAt(i);
            coordinateVariables.put(t.getString("key"), new ChunkPosition(t.getInteger("x"), t.getInteger("y"), t.getInteger("z")));
        }
    }

    public ChunkPosition getCoordinate(String varName){
        ChunkPosition pos;
        if(varName.startsWith("$")) {
            SpecialVariableRetrievalEvent event = new SpecialVariableRetrievalEvent(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            pos = event.coordinate;
        } else if(varName.startsWith("#")) {
            pos = GlobalVariableManager.getPos(varName.substring(1));
        } else {
            pos = coordinateVariables.get(varName);
        }
        return pos != null ? pos : new ChunkPosition(0, 0, 0);
    }

    public void setCoordinate(String varName, ChunkPosition coord){
        if(varName.startsWith("#")) {
            GlobalVariableManager.set(varName.substring(1), coord);
        } else if(!varName.startsWith("$")) coordinateVariables.put(varName, coord);
    }

    private void updateWidgetFlow(){
        boolean isExecuting = false;
        for(EntityAITaskEntry entry : executingTaskEntries) {
            if(curWidgetAI == entry.action) {
                isExecuting = true;
                break;
            }
        }
        if(!isExecuting && curActiveWidget != null && (curWidgetTargetAI == null || !curWidgetTargetAI.shouldExecute())) {
            IProgWidget widget = curActiveWidget.getOutputWidget(drone, progWidgets);
            if(widget != null) {
                setActiveWidget(widget);
            } else {
                if(stopWhenEndReached) {
                    setActiveWidget(null);
                } else {
                    gotoFirstWidget();
                }
            }
        }
        if(curActiveWidget == null && !stopWhenEndReached) {
            gotoFirstWidget();
        }
    }

    private void gotoFirstWidget(){
        for(IProgWidget widget : progWidgets) {
            if(widget instanceof ProgWidgetStart) {
                setActiveWidget(widget);
                return;
            }
        }
    }

    private void setActiveWidget(IProgWidget widget){
        EntityAIBase targetAI = null;
        EntityAIBase ai = null;
        if(widget != null) {
            boolean first = widget instanceof ProgWidgetStart;
            targetAI = widget.getWidgetTargetAI(drone, widget);
            ai = widget.getWidgetAI(drone, widget);
            while(targetAI == null && ai == null) {
                widget = widget.getOutputWidget(drone, progWidgets);
                if(widget == null) {
                    if(first) {
                        return;
                    } else {
                        if(stopWhenEndReached) {
                            setActiveWidget(null);
                        } else {
                            gotoFirstWidget();
                        }
                        return;
                    }
                }
                targetAI = widget.getWidgetTargetAI(drone, widget);
                ai = widget.getWidgetAI(drone, widget);
            }
            drone.setActiveProgram(widget);
        }

        curActiveWidget = widget;
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
