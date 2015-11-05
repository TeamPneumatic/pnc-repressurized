package pneumaticCraft.common.ai;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.progwidgets.ICountWidget;
import pneumaticCraft.common.progwidgets.ILiquidExport;
import pneumaticCraft.common.progwidgets.ILiquidFiltered;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public class DroneAILiquidExport extends DroneAIImExBase{

    public DroneAILiquidExport(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        return fillTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return fillTank(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean fillTank(ChunkPosition pos, boolean simulate){
        if(drone.getTank().getFluidAmount() == 0) {
            drone.addDebugEntry("gui.progWidget.liquidExport.debug.emptyDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            if(te instanceof IFluidHandler) {
                IFluidHandler tank = (IFluidHandler)te;

                FluidStack exportedFluid = drone.getTank().drain(Integer.MAX_VALUE, false);
                if(exportedFluid != null && ((ILiquidFiltered)widget).isFluidValid(exportedFluid.getFluid())) {
                    for(int i = 0; i < 6; i++) {
                        if(((ISidedWidget)widget).getSides()[i]) {
                            int filledAmount = tank.fill(ForgeDirection.getOrientation(i), exportedFluid, false);
                            if(filledAmount > 0) {
                                if(((ICountWidget)widget).useCount()) filledAmount = Math.min(filledAmount, getRemainingCount());
                                if(!simulate) {
                                    decreaseCount(tank.fill(ForgeDirection.getOrientation(i), drone.getTank().drain(filledAmount, true), true));
                                }
                                return true;
                            }
                        }
                    }
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.filledToMax", pos);
                } else {
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.noValidFluid");
                }
            } else if(((ILiquidExport)widget).isPlacingFluidBlocks() && (!((ICountWidget)widget).useCount() || getRemainingCount() >= 1000)) {
                Block fluidBlock = drone.getTank().getFluid().getFluid().getBlock();
                if(drone.getTank().getFluidAmount() >= 1000 && fluidBlock != null && drone.getWorld().isAirBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ)) {
                    if(!simulate) {
                        decreaseCount(1000);
                        drone.getTank().drain(1000, true);
                        drone.getWorld().setBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, fluidBlock);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
