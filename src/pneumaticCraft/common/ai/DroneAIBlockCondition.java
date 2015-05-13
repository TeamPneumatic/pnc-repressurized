package pneumaticCraft.common.ai;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public abstract class DroneAIBlockCondition extends DroneAIBlockInteraction{

    private boolean result;

    public DroneAIBlockCondition(IDrone drone, ProgWidgetAreaItemBase widget){
        super(drone, 0, widget);
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
            abort();
        }
        return false;
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
