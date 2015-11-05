package pneumaticCraft.common.ai;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.progwidgets.ProgWidgetForEachCoordinate;

public class DroneAIForEachCoordinate extends DroneAIBlockInteraction<ProgWidgetForEachCoordinate>{

    private ChunkPosition curCoord;

    public DroneAIForEachCoordinate(IDroneBase drone, ProgWidgetForEachCoordinate widget){
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        if(widget.isValidPosition(pos)) {
            curCoord = pos;
            abort();
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return false;
    }

    public ChunkPosition getCurCoord(){
        return curCoord;
    }

    @Override
    protected void addEndingDebugEntry(){

    }
}
