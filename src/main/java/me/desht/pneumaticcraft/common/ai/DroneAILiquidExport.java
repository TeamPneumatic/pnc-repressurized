package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.*;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class DroneAILiquidExport extends DroneAIImExBase<ProgWidgetInventoryBase> {

    public DroneAILiquidExport(IDroneBase drone, ProgWidgetInventoryBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return fillTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return fillTank(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean fillTank(BlockPos pos, boolean simulate) {
        if (drone.getTank().getFluidAmount() == 0) {
            drone.addDebugEntry("gui.progWidget.liquidExport.debug.emptyDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te != null) {
                FluidStack exportedFluid = drone.getTank().drain(Integer.MAX_VALUE, false);
                if (exportedFluid != null && ((ILiquidFiltered) progWidget).isFluidValid(exportedFluid.getFluid())) {
                    for (Direction side : Direction.VALUES) {
                        if (ISidedWidget.checkSide(progWidget, side) && trySide(te, side, exportedFluid, simulate)) return true;
                    }
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.filledToMax", pos);
                } else {
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.noValidFluid");
                }
            } else if (((ILiquidExport) progWidget).isPlacingFluidBlocks() && (!((ICountWidget) progWidget).useCount() || getRemainingCount() >= 1000)) {
                Block fluidBlock = drone.getTank().getFluid().getFluid().getBlock();
                if (drone.getTank().getFluidAmount() >= 1000 && fluidBlock != null && drone.world().isAirBlock(pos)) {
                    if (!simulate) {
                        decreaseCount(1000);
                        drone.getTank().drain(1000, true);
                        drone.world().setBlockState(pos, fluidBlock.getDefaultState()); //TODO 1.8 test
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private boolean trySide(TileEntity te, Direction side, FluidStack fluidToExport, boolean simulate) {
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).map(tank -> {
            int filledAmount = tank.fill(fluidToExport, false);
            if (filledAmount > 0) {
                if (((ICountWidget) progWidget).useCount()) {
                    filledAmount = Math.min(filledAmount, getRemainingCount());
                }
                if (!simulate) {
                    decreaseCount(tank.fill(drone.getTank().drain(filledAmount, true), true));
                }
                return true;
            }
            return false;
        }).orElse(false);
    }
}
