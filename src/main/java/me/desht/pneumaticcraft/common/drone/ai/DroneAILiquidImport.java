/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetLiquidImport;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;

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
            BlockEntity te = drone.world().getBlockEntity(pos);
            if (te != null) {
                boolean didWork = false;
                for (Direction side : DirectionUtil.VALUES) {
                    if (progWidget.isSideSelected(side)) {
                        didWork = IOHelper.getFluidHandlerForBlock(te, side)
                                .map(handler -> tryImportFluid(handler, simulate)).orElse(false);
                        if (didWork) break;
                    }
                }
                if (didWork) return true;
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.liquidImport.debug.emptiedToMax", pos);
            }

            // try to pick up a bucket of fluid from the world
            if (!progWidget.useCount() || getRemainingCount() >= BUCKET_VOLUME) {
                FluidState state = drone.world().getFluidState(pos);
                BlockState blockState = drone.world().getBlockState(pos);
                if (state.isSource() && progWidget.isFluidValid(state.getType())) {
                    FluidStack stack = new FluidStack(state.getType(), BUCKET_VOLUME);
                    if (shouldVoidExcess() || drone.getFluidTank().fill(stack, FluidAction.SIMULATE) == BUCKET_VOLUME) {
                        if (!simulate) {
                            if (blockState.getBlock() instanceof BucketPickup pickup) {
                                pickup.pickupBlock(drone.getFakePlayer(), drone.world(), pos, blockState);
                                decreaseCount(BUCKET_VOLUME);
                                drone.getFluidTank().fill(stack, FluidAction.EXECUTE);
                                return true;
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
