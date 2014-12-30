package pneumaticCraft.common.ai;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public abstract class DroneAIBlockCondition extends DroneAIBlockInteraction{

    private boolean result;
    private boolean aborted;

    public DroneAIBlockCondition(EntityDrone drone, ProgWidgetAreaItemBase widget){
        super(drone, 0, widget);
    }

    @Override
    public boolean shouldExecute(){
        if(aborted) {
            return false;
        } else {
            if(super.shouldExecute()) {
                result = ((ICondition)widget).isAndFunction();//set the initial value, so it can be modified by the 'evaluate' method later.
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean continueExecuting(){
        if(aborted) return false;
        return super.continueExecuting();
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        if(evaluate(pos) != ((ICondition)widget).isAndFunction()) {
            result = !result;
            abort();
        }
        return false;
    }

    @Override
    protected boolean shouldAbort(){
        return aborted;
    }

    protected abstract boolean evaluate(ChunkPosition pos);

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return false;
    }

    protected void abort(){
        aborted = true;
    }

    public boolean getResult(){
        return result;
    }

}
