package pneumaticCraft.common.ai;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ICountWidget;
import pneumaticCraft.common.progwidgets.ILiquidFiltered;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public abstract class DroneAIImExBase extends DroneAIBlockInteraction{
    private int exportCount;

    public DroneAIImExBase(EntityDrone drone, double speed, ProgWidgetAreaItemBase widget){
        super(drone, speed, widget);
        exportCount = ((ICountWidget)widget).getCount();
    }

    @Override
    public boolean shouldExecute(){
        boolean countReached = exportCount <= 0;
        exportCount = ((ICountWidget)widget).getCount();
        if(countReached && ((ICountWidget)widget).useCount()) return false;
        return super.shouldExecute();
    }

    protected void decreaseCount(int count){
        exportCount -= count;
    }

    protected int getRemainingCount(){
        return exportCount;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return !((ICountWidget)widget).useCount() || exportCount > 0;
    }

}
