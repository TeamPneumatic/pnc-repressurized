package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLiquidImport;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;

public class DroneAILiquidImport<W extends ProgWidgetInventoryBase & ILiquidFiltered> extends DroneAIImExBase<W> {

    public DroneAILiquidImport(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return emptyTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return emptyTank(pos, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    private boolean shouldVoidExcess() {
        return progWidget instanceof ProgWidgetLiquidImport && ((ProgWidgetLiquidImport) progWidget).shouldVoidExcess();
    }

    private boolean emptyTank(BlockPos pos, boolean simulate) {
        if (!shouldVoidExcess() && drone.getFluidTank().getFluidAmount() == drone.getFluidTank().getCapacity()) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.liquidImport.debug.fullDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te != null) {
                boolean didWork = false;
                for (Direction side : Direction.VALUES) {
                    if (progWidget.isSideSelected(side)) {
                        didWork = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)
                                .map(handler -> tryImportFluid(handler, simulate)).orElse(false);
                        if (didWork) break;
                    }
                }
                if (didWork) return true;
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.liquidImport.debug.emptiedToMax", pos);
            }

            // try to pick up a bucket of fluid from the world
            if (!progWidget.useCount() || getRemainingCount() >= BUCKET_VOLUME) {
                LazyOptional<IFluidHandler> cap = drone.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                FluidState state = drone.world().getFluidState(pos);
                BlockState blockState = drone.world().getBlockState(pos);
                if (state.isSource() && progWidget.isFluidValid(state.getFluid())) {
                    FluidStack stack = new FluidStack(state.getFluid(), BUCKET_VOLUME);
                    if (shouldVoidExcess() || drone.getFluidTank().fill(stack, FluidAction.SIMULATE) == BUCKET_VOLUME) {
                        if (!simulate) {
                            if (blockState.getBlock() instanceof IBucketPickupHandler) {
                                ((IBucketPickupHandler) blockState.getBlock()).pickupFluid(drone.world(), pos, blockState);
                                decreaseCount(BUCKET_VOLUME);
                                drone.getFluidTank().fill(stack, FluidAction.EXECUTE);
                                return true;
//                                FluidUtils.tryPickupFluid(cap, drone.world(), pos, false, FluidAction.EXECUTE);
                            }
                        }
                    }
                    return true;
                }
            }

            return false;
        }
    }

    private boolean tryImportFluid(IFluidHandler sourceHandler, boolean simulate) {
        FluidStack importedFluid = sourceHandler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        if (!importedFluid.isEmpty() && progWidget.isFluidValid(importedFluid.getFluid())) {
            int filledAmount = drone.getFluidTank().fill(importedFluid, FluidAction.SIMULATE);
            if (filledAmount > 0) {
                if (progWidget.useCount())
                    filledAmount = Math.min(filledAmount, getRemainingCount());
                if (!simulate) {
                    decreaseCount(drone.getFluidTank().fill(sourceHandler.drain(filledAmount, FluidAction.EXECUTE), FluidAction.EXECUTE));
                }
                return true;
            } else {
                return shouldVoidExcess();
            }
        }
        return false;
    }
}
