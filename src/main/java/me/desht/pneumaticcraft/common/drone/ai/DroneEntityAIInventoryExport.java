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
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DroneEntityAIInventoryExport extends DroneAIImportExportBase<ProgWidgetInventoryBase> {

    public DroneEntityAIInventoryExport(IDrone drone, ProgWidgetInventoryBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return export(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return export(pos, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    private boolean export(BlockPos pos, boolean simulate) {
        BlockEntity te = drone.getDroneLevel().getBlockEntity(pos);
        if (te != null) {
            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(i);
                if (!droneStack.isEmpty()) {
                    if (progWidget.isItemValidForFilters(droneStack)) {
                        for (Direction dir : DirectionUtil.VALUES) {
                            if (progWidget.isSideSelected(dir)) {
                                droneStack = droneStack.copy();
                                int oldCount = droneStack.getCount();
                                if (progWidget.useCount()) {
                                    droneStack.setCount(Math.min(droneStack.getCount(), getRemainingCount()));
                                }
                                ItemStack remainder = IOHelper.insert(te, droneStack.copy(), dir, simulate);
                                int stackSize = drone.getInv().getStackInSlot(i).getCount() - (remainder.isEmpty() ? droneStack.getCount() : droneStack.getCount() - remainder.getCount());
                                droneStack.setCount(stackSize);
                                int exportedItems = oldCount - stackSize;
                                if (!simulate) {
                                    drone.getInv().setStackInSlot(i, stackSize > 0 ? droneStack : ItemStack.EMPTY);
                                    decreaseCount(exportedItems);
                                }
                                if (simulate && exportedItems > 0) return true;
                            }
                        }
                        if (droneStack.isEmpty() && !simulate) {
                            drone.addAirToDrone(-PneumaticValues.DRONE_USAGE_INV);
                        }
                        else drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventoryExport.debug.filledToMax", pos);
                    } else {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventoryExport.debug.stackdoesntPassFilter", pos);
                    }
                }
            }
        } else {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventory.debug.noInventory", pos);
        }
        return false;
    }
}
