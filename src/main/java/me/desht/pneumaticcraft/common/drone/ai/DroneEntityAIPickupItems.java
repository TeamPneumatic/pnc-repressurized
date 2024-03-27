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
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.IItemPickupWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.EnumSet;
import java.util.List;

public class DroneEntityAIPickupItems extends Goal {
    private final IDroneBase drone;
    private final ProgWidgetAreaItemBase itemPickupWidget;
    private ItemEntity curPickingUpEntity;
    private final DistanceEntitySorter theNearestAttackableTargetSorter;
    private final boolean canSteal;

    public DroneEntityAIPickupItems(IDroneBase drone, ProgWidgetAreaItemBase progWidgetPickupItem) {
        this.drone = drone;
        setFlags(EnumSet.allOf(Flag.class)); // so it won't run along with other AI tasks.
        itemPickupWidget = progWidgetPickupItem;
        theNearestAttackableTargetSorter = new DistanceEntitySorter(drone);
        if (progWidgetPickupItem instanceof IItemPickupWidget) {
            canSteal = ((IItemPickupWidget) itemPickupWidget).canSteal();
        } else {
            throw new IllegalArgumentException("expecting a IItemPickupWidget!");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {
        List<Entity> pickableItems = itemPickupWidget.getEntitiesInArea(drone.world(), entity -> entity instanceof ItemEntity && entity.isAlive());

        if (pickableItems.isEmpty()) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.itemPickup.debug.noItems");
            return false;
        }
        pickableItems.sort(theNearestAttackableTargetSorter);
        for (Entity ent : pickableItems) {
            if (ent.getPersistentData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT) && !canSteal) {
                continue;
            }
            ItemStack stack = ((ItemEntity) ent).getItem();
            if (itemPickupWidget.isItemValidForFilters(stack)) {
                if (ItemHandlerHelper.insertItem(drone.getInv(), stack, true).isEmpty()) {
                    return tryMoveToItem(ent);
                } else {
                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventoryImport.debug.filledToMax");
                }
            } else {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.itemPickup.debug.itemNotValid");
            }
        }
        return false;

    }

    // different order to Direction.values() - UP first as it's the most likely, and DOWN last as it's the least
    private static final Direction[] DIRECTIONS = new Direction[] {
            Direction.UP, Direction.NORTH, Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.DOWN
    };

    private boolean tryMoveToItem(Entity ent) {
        if (!drone.isBlockValidPathfindBlock(ent.blockPosition())) {
            // the item's in some block space that the drone can't pathfind to (e.g. bamboo, stairs...)
            // maybe we can find a clear adjacent block?
            for (Direction d : DIRECTIONS) {
                BlockPos pos2 = ent.blockPosition().relative(d);
                if (drone.isBlockValidPathfindBlock(pos2)
                        && drone.getPathNavigator().moveToXYZ(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5)) {
                    curPickingUpEntity = (ItemEntity) ent;
                    return true;
                }
            }
        } else {
            if (drone.getPathNavigator().moveToEntity(ent)) {
                curPickingUpEntity = (ItemEntity) ent;
                return true;
            } else {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.general.debug.cantNavigate");
            }
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean canContinueToUse() {
        if (!curPickingUpEntity.isAlive()) return false;
        if (curPickingUpEntity.position().distanceToSqr(drone.getDronePos()) < 4) {
            ItemStack stack = curPickingUpEntity.getItem();
            if (itemPickupWidget.isItemValidForFilters(stack)) {
                tryPickupItem(drone, curPickingUpEntity);
            }
            return false;
        }
        return !drone.getPathNavigator().hasNoPath();
    }

    static void tryPickupItem(IDrone drone, ItemEntity itemEntity){
        ItemStack stack = itemEntity.getItem();
        int stackSize = stack.getCount();

        ItemStack remainder = ItemHandlerHelper.insertItem(drone.getInv(), stack, false);
        int collected = stackSize - remainder.getCount();
        if (collected > 0) {
            drone.onItemPickupEvent(itemEntity, collected);
        }
        if (remainder.isEmpty()) {
            itemEntity.discard();
        } else if (collected > 0) {
            itemEntity.setItem(remainder);
        }
    }
}
