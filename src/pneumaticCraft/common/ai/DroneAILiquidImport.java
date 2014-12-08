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

public class DroneAILiquidImport extends DroneAIImExBase{

    public DroneAILiquidImport(EntityDrone drone, double speed, ProgWidgetAreaItemBase widget){
        super(drone, speed, widget);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        return emptyTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return emptyTank(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean emptyTank(ChunkPosition pos, boolean simulate){
        TileEntity te = drone.worldObj.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        if(te instanceof IFluidHandler) {
            IFluidHandler tank = (IFluidHandler)te;
            for(int i = 0; i < 6; i++) {
                if(((ISidedWidget)widget).getSides()[i]) {
                    FluidStack importedFluid = tank.drain(ForgeDirection.getOrientation(i), Integer.MAX_VALUE, false);
                    if(importedFluid != null && ((ILiquidFiltered)widget).isFluidValid(importedFluid.getFluid())) {
                        int filledAmount = drone.getTank().fill(importedFluid, false);
                        if(filledAmount > 0) {
                            if(((ICountWidget)widget).useCount()) filledAmount = Math.min(filledAmount, getRemainingCount());
                            if(!simulate) {
                                decreaseCount(drone.getTank().fill(tank.drain(ForgeDirection.getOrientation(i), filledAmount, true), true));
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
