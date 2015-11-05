package pneumaticCraft.common.ai;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.progwidgets.ICountWidget;
import pneumaticCraft.common.progwidgets.ILiquidFiltered;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.util.FluidUtils;

public class DroneAILiquidImport extends DroneAIImExBase{

    public DroneAILiquidImport(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
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
        if(drone.getTank().getFluidAmount() == drone.getTank().getCapacity()) {
            drone.addDebugEntry("gui.progWidget.liquidImport.debug.fullDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
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
                drone.addDebugEntry("gui.progWidget.liquidImport.debug.emptiedToMax", pos);
            } else if(!((ICountWidget)widget).useCount() || getRemainingCount() >= 1000) {
                Fluid fluid = FluidRegistry.lookupFluidForBlock(drone.getWorld().getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ));
                if(fluid != null && ((ILiquidFiltered)widget).isFluidValid(fluid) && drone.getTank().fill(new FluidStack(fluid, 1000), false) == 1000 && FluidUtils.isSourceBlock(drone.getWorld(), pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ)) {
                    if(!simulate) {
                        decreaseCount(1000);
                        drone.getTank().fill(new FluidStack(fluid, 1000), true);
                        drone.getWorld().setBlockToAir(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
