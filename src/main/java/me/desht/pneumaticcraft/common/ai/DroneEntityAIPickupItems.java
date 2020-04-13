package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import java.util.EnumSet;
import java.util.List;

public class DroneEntityAIPickupItems extends Goal {
    private final IDroneBase drone;
    private final ProgWidgetAreaItemBase itemPickupWidget;
    private ItemEntity curPickingUpEntity;
    private final DistanceEntitySorter theNearestAttackableTargetSorter;

    public DroneEntityAIPickupItems(IDroneBase drone, ProgWidgetAreaItemBase progWidgetPickupItem) {
        this.drone = drone;
        setMutexFlags(EnumSet.allOf(Flag.class)); // so it won't run along with other AI tasks.
        itemPickupWidget = progWidgetPickupItem;
        theNearestAttackableTargetSorter = new DistanceEntitySorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        List<Entity> pickableItems = itemPickupWidget.getEntitiesInArea(drone.world(), entity -> entity instanceof ItemEntity && entity.isAlive());

        if (pickableItems.isEmpty()) {
            drone.addDebugEntry("gui.progWidget.itemPickup.debug.noItems");
            return false;
        }
        pickableItems.sort(theNearestAttackableTargetSorter);
        for (Entity ent : pickableItems) {
            ItemStack stack = ((ItemEntity) ent).getItem();
            if (itemPickupWidget.isItemValidForFilters(stack)) {
                if (IOHelper.insert(drone, stack, null, true).isEmpty()) {
                    if (drone.getPathNavigator().moveToEntity(ent)) {
                        curPickingUpEntity = (ItemEntity) ent;
                        return true;
                    }
                } else {
                    drone.addDebugEntry("gui.progWidget.inventoryImport.debug.filledToMax");
                }
            } else {
                drone.addDebugEntry("gui.progWidget.itemPickup.debug.itemNotValid");
            }
        }
        return false;

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
        if (!curPickingUpEntity.isAlive()) return false;
        if (curPickingUpEntity.getPositionVector().squareDistanceTo(drone.getDronePos()) < 2.25) {
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

        ItemStack remainder = IOHelper.insert(drone, stack, Direction.UP, false);
        int collected = stackSize - remainder.getCount();
        if (collected > 0) {
            drone.onItemPickupEvent(itemEntity, collected);
        }
        if (remainder.isEmpty()) {
            itemEntity.remove();
        } else if (collected > 0) {
            itemEntity.setItem(remainder);
        }
    }
}
