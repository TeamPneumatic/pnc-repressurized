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

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ILiquidExport;
import me.desht.pneumaticcraft.common.drone.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;

public class DroneAILiquidExport<W extends ProgWidgetInventoryBase & ILiquidFiltered & ILiquidExport> extends DroneAIImportExportBase<W> {

    private enum FillStatus { OK, NO_HANDLER, NO_SPACE }

    public DroneAILiquidExport(IDrone drone, W widget) {
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
            BlockEntity te = drone.getDroneLevel().getBlockEntity(pos);
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
            // drop through to here if there was no BE or a BE had no valid fluid handler

            if (progWidget.isPlacingFluidBlocks() && (!progWidget.useCount() || getRemainingCount() >= BUCKET_VOLUME)) {
                if (FluidUtils.tryPourOutFluid(drone.getFluidTank(), drone.getDroneLevel(), pos, false, false,
                        simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE)) {
                    if (!simulate) {
                        decreaseCount(BUCKET_VOLUME);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private FillStatus trySide(BlockEntity te, Direction side, FluidStack fluidToExport, boolean simulate) {
        return IOHelper.getFluidHandlerForBlock(te, side).map(fluidHandler -> {
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
