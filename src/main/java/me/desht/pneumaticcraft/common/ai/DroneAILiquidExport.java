package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;

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
        if (drone.getFluidTank().getFluidAmount() == 0) {
            drone.addDebugEntry("gui.progWidget.liquidExport.debug.emptyDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te != null) {
                FluidStack exportedFluid = drone.getFluidTank().drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                if (!exportedFluid.isEmpty() && ((ILiquidFiltered) progWidget).isFluidValid(exportedFluid.getFluid())) {
                    for (Direction side : Direction.VALUES) {
                        if (ISidedWidget.checkSide(progWidget, side) && trySide(te, side, exportedFluid, simulate)) return true;
                    }
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.filledToMax", pos);
                } else {
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.noValidFluid");
                }
            } else if (((ILiquidExport) progWidget).isPlacingFluidBlocks()
                    && (!((ICountWidget) progWidget).useCount() || getRemainingCount() >= BUCKET_VOLUME)) {
                LazyOptional<IFluidHandler> cap = drone.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                if (FluidUtils.tryPourOutFluid(cap, drone.world(), pos, false, false, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE)) {
                    if (!simulate) {
                        decreaseCount(BUCKET_VOLUME);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private boolean trySide(TileEntity te, Direction side, FluidStack fluidToExport, boolean simulate) {
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).map(fluidHandler -> {
            int filledAmount = fluidHandler.fill(fluidToExport, FluidAction.SIMULATE);
            if (filledAmount > 0) {
                if (((ICountWidget) progWidget).useCount()) {
                    filledAmount = Math.min(filledAmount, getRemainingCount());
                }
                if (!simulate) {
                    decreaseCount(fluidHandler.fill(drone.getFluidTank().drain(filledAmount, FluidAction.EXECUTE), FluidAction.EXECUTE));
                }
                return true;
            }
            return false;
        }).orElse(false);
    }
}
