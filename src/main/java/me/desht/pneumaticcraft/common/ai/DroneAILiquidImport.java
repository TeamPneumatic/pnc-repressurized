package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class DroneAILiquidImport extends DroneAIImExBase<ProgWidgetInventoryBase> {

    public DroneAILiquidImport(IDroneBase drone, ProgWidgetInventoryBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return emptyTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return emptyTank(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean emptyTank(BlockPos pos, boolean simulate) {
        if (drone.getTank().getFluidAmount() == drone.getTank().getCapacity()) {
            drone.addDebugEntry("gui.progWidget.liquidImport.debug.fullDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te != null) {
                boolean didWork = false;
                for (Direction side : Direction.VALUES) {
                    if (ISidedWidget.checkSide(progWidget, side)) {
                        didWork = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)
                                .map(handler -> tryImportFluid(handler, simulate)).orElse(false);
                        if (didWork) break;
                    }
                }
                if (didWork) return true;
                drone.addDebugEntry("gui.progWidget.liquidImport.debug.emptiedToMax", pos);
            }

            // fall through to fluid-in-world check here; it's possible for a fluid block to be a TE (with no
            // fluid capability) and also a fluid block which can be drained directly
            if (!((ICountWidget) progWidget).useCount() || getRemainingCount() >= Fluid.BUCKET_VOLUME) {
                FluidStack fluidStack = FluidUtils.getFluidAt(drone.world(), pos, false);
                if (fluidStack != null && fluidStack.amount == Fluid.BUCKET_VOLUME
                        && ((ILiquidFiltered) progWidget).isFluidValid(fluidStack.getFluid())
                        && drone.getTank().fill(fluidStack, false) == Fluid.BUCKET_VOLUME) {
                    if (!simulate) {
                        decreaseCount(Fluid.BUCKET_VOLUME);
                        FluidStack fluidStack1 = FluidUtils.getFluidAt(drone.world(), pos, true);
                        drone.getTank().fill(fluidStack1, true);
                    }
                    return true;
                }
            }

            return false;
        }
    }

    private boolean tryImportFluid(IFluidHandler sourceHandler, boolean simulate) {
        FluidStack importedFluid = sourceHandler.drain(Integer.MAX_VALUE, false);
        if (importedFluid != null && ((ILiquidFiltered) progWidget).isFluidValid(importedFluid.getFluid())) {
            int filledAmount = drone.getTank().fill(importedFluid, false);
            if (filledAmount > 0) {
                if (((ICountWidget) progWidget).useCount())
                    filledAmount = Math.min(filledAmount, getRemainingCount());
                if (!simulate) {
                    decreaseCount(drone.getTank().fill(sourceHandler.drain(filledAmount, true), true));
                }
                return true;
            }
        }
        return false;
    }
}
