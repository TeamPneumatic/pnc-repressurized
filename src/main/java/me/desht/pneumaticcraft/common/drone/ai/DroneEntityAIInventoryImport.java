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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class DroneEntityAIInventoryImport extends DroneAIImportExportBase<ProgWidgetInventoryBase> {

    public DroneEntityAIInventoryImport(IDrone drone, ProgWidgetInventoryBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return importItems(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return importItems(pos, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    private boolean importItems(BlockPos pos, boolean simulate) {
        BlockEntity te = drone.getDroneLevel().getBlockEntity(pos);
        boolean imported = false;
        for (Direction dir : DirectionUtil.VALUES) {
            if (progWidget.isSideSelected(dir)) {
                imported = IOHelper.getInventoryForBlock(te, dir).map(inv -> tryImport(inv, pos, simulate)).orElse(false);
                if (imported) break;
            }
        }
        return imported;
    }

    private boolean tryImport(IItemHandler inv, BlockPos pos, boolean simulate) {
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (progWidget.isItemValidForFilters(stack)) {
                    ItemStack importedStack = inv.extractItem(i, stack.getCount(), true);
                    if (importedStack.isEmpty()) {
                        continue;
                    }
                    importedStack = importedStack.copy();
                    if (progWidget.useCount()) {
                        importedStack.setCount(Math.min(importedStack.getCount(), getRemainingCount()));
                    }
                    ItemStack remainder = ItemHandlerHelper.insertItem(drone.getInv(), importedStack, simulate);
                    int removedItems = importedStack.getCount() - remainder.getCount();
                    if (!simulate) {
                        inv.extractItem(i, removedItems, false);
                        decreaseCount(removedItems);
                        drone.addAirToDrone(-PneumaticValues.DRONE_USAGE_INV);
                        if (progWidget.useCount() && getRemainingCount() <= 0) {
                            return false;
                        }
                    } else if (removedItems > 0) {
                        return true;
                    } else {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventoryImport.debug.filledToMax", pos);
                    }
                } else {
                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventoryImport.debug.stackdoesntPassFilter", pos);
                }
            }
        }
        return false;
    }

}
