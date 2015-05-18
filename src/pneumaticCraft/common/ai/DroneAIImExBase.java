package pneumaticCraft.common.ai;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.api.drone.IBlockInteractHandler;
import pneumaticCraft.common.progwidgets.ICountWidget;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public abstract class DroneAIImExBase extends DroneAIBlockInteraction implements IBlockInteractHandler{
    private int transportCount;

    public DroneAIImExBase(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
        transportCount = ((ICountWidget)widget).getCount();
    }

    @Override
    public boolean shouldExecute(){
        boolean countReached = transportCount <= 0;
        transportCount = ((ICountWidget)widget).getCount();
        if(countReached && useCount()) return false;
        return super.shouldExecute();
    }

    @Override
    public void decreaseCount(int count){
        transportCount -= count;
    }

    @Override
    public int getRemainingCount(){
        return transportCount;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return !useCount() || transportCount > 0;
    }

    @Override
    public boolean[] getSides(){
        return ((ISidedWidget)widget).getSides();
    }

    @Override
    public boolean useCount(){
        return ((ICountWidget)widget).useCount();
    }

}
