package pneumaticCraft.common.ai;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public abstract class DroneAIBlockCondition extends DroneAIBlockInteraction{

    private boolean result;

    public DroneAIBlockCondition(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
    }

    @Override
    public boolean shouldExecute(){
        if(super.shouldExecute()) {
            result = ((ICondition)widget).isAndFunction();//set the initial value, so it can be modified by the 'evaluate' method later.
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        if(evaluate(pos) != ((ICondition)widget).isAndFunction()) {
            result = !result;
            if(result) {
                drone.addDebugEntry("gui.progWidget.blockCondition.debug.blockMatches", pos);
            } else {
                drone.addDebugEntry("gui.progWidget.blockCondition.debug.blockDoesNotMatch", pos);
            }
            abort();
        }
        return false;
    }

    @Override
    protected void addEndingDebugEntry(){
        if(result) {
            drone.addDebugEntry("gui.progWidget.blockCondition.debug.allBlocksMatch");
        } else {
            drone.addDebugEntry("gui.progWidget.blockCondition.debug.noBlocksMatch");
        }
    }

    protected abstract boolean evaluate(ChunkPosition pos);

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return false;
    }

    public boolean getResult(){
        return result;
    }

}
