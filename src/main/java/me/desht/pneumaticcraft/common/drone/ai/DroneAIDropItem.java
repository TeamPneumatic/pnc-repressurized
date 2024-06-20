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
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.IItemDropper;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class DroneAIDropItem<W extends ProgWidgetInventoryBase & IItemDropper> extends DroneAIImportExportBase<W> {
    private final Set<BlockPos> visitedPositions = new HashSet<>();

    public DroneAIDropItem(IDrone drone, W widget) {
        super(drone, widget);
    }

    @Override
    public boolean canUse() {
        boolean shouldExecute = false;
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (progWidget.isItemValidForFilters(stack)) {
                shouldExecute = super.canUse();
                break;
            }
        }
        return shouldExecute;
    }

    @Override
    protected boolean moveIntoBlock() {
        return true;
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return !visitedPositions.contains(pos);//another requirement is that the drone can navigate to this exact block, but that's handled by the pathfinder.
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        visitedPositions.add(pos);
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (progWidget.isItemValidForFilters(stack)) {
                if (useCount() && getRemainingCount() < stack.getCount()) {
                    stack = stack.split(getRemainingCount());
                    decreaseCount(getRemainingCount());
                } else {
                    decreaseCount(stack.getCount());
                    drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                }
                ItemEntity item = new ItemEntity(drone.getDroneLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                if (progWidget.dropStraight()) {
                    item.setDeltaMovement(0, 0, 0);
                }
                if (progWidget.hasPickupDelay()) {
                    item.setPickUpDelay(40);
                }
                drone.getDroneLevel().addFreshEntity(item);
                if (useCount() && getRemainingCount() == 0) break;
            }
        }
        return false;
    }
}
