package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.progwidgets.ILiquidExport;
import me.desht.pneumaticcraft.common.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
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

public class DroneAILiquidExport<W extends ProgWidgetInventoryBase & ILiquidFiltered & ILiquidExport> extends DroneAIImExBase<W> {

    private enum FillStatus { OK, NO_HANDLER, NO_SPACE }

    public DroneAILiquidExport(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return fillTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return fillTank(pos, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    private boolean fillTank(BlockPos pos, boolean simulate) {
        if (drone.getFluidTank().getFluidAmount() == 0) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.liquidExport.debug.emptyDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.world().getBlockEntity(pos);
            if (te != null) {
                FluidStack exportedFluid = drone.getFluidTank().drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                if (!exportedFluid.isEmpty() && progWidget.isFluidValid(exportedFluid.getFluid())) {
                    FillStatus status = FillStatus.NO_HANDLER;
                    for (Direction side : DirectionUtil.VALUES) {
                        if (progWidget.isSideSelected(side)) {
                            status = trySide(te, side, exportedFluid, simulate);
                            if (status == FillStatus.OK) return true;
                        }
                    }
                    if (status == FillStatus.NO_SPACE) {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.liquidExport.debug.filledToMax", pos);
                        return false;
                    }
                } else {
                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.liquidExport.debug.noValidFluid");
                    return false;
                }
            }
            // drop through to here if there was no TE or a TE had no valid fluid handler

            if (progWidget.isPlacingFluidBlocks() && (!progWidget.useCount() || getRemainingCount() >= BUCKET_VOLUME)) {
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

    private FillStatus trySide(TileEntity te, Direction side, FluidStack fluidToExport, boolean simulate) {
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).map(fluidHandler -> {
            int filledAmount = fluidHandler.fill(fluidToExport, FluidAction.SIMULATE);
            if (filledAmount > 0) {
                if (((ICountWidget) progWidget).useCount()) {
                    filledAmount = Math.min(filledAmount, getRemainingCount());
                }
                if (!simulate) {
                    decreaseCount(fluidHandler.fill(drone.getFluidTank().drain(filledAmount, FluidAction.EXECUTE), FluidAction.EXECUTE));
                }
                return FillStatus.OK;
            }
            return FillStatus.NO_SPACE;
        }).orElse(FillStatus.NO_HANDLER);
    }
}
