package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class DroneEntityAIPickupItems extends EntityAIBase {
    private final IDroneBase drone;
    private final ProgWidgetAreaItemBase itemPickupWidget;
    private EntityItem curPickingUpEntity;
    private final DistanceEntitySorter theNearestAttackableTargetSorter;

    public DroneEntityAIPickupItems(IDroneBase drone, ProgWidgetAreaItemBase progWidgetPickupItem) {
        this.drone = drone;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        itemPickupWidget = progWidgetPickupItem;
        theNearestAttackableTargetSorter = new DistanceEntitySorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        List<Entity> pickableItems = itemPickupWidget.getEntitiesInArea(drone.world(), entity -> entity instanceof EntityItem && entity.isEntityAlive());

        if (pickableItems.isEmpty()) {
            drone.addDebugEntry("gui.progWidget.itemPickup.debug.noItems");
            return false;
        }
        pickableItems.sort(theNearestAttackableTargetSorter);
        for (Entity ent : pickableItems) {
            ItemStack stack = ((EntityItem) ent).getItem();
            if (itemPickupWidget.isItemValidForFilters(stack)) {
                if (IOHelper.insert(drone, stack, null, true).isEmpty()) {
                    if (drone.getPathNavigator().moveToEntity(ent)) {
                        curPickingUpEntity = (EntityItem) ent;
                        return true;
                    }
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
        if (curPickingUpEntity.isDead) return false;
        if (new Vec3d(curPickingUpEntity.posX, curPickingUpEntity.posY, curPickingUpEntity.posZ).squareDistanceTo(drone.getDronePos()) < 2.25) {
            ItemStack stack = curPickingUpEntity.getItem();
            if (itemPickupWidget.isItemValidForFilters(stack)) {
                tryPickupItem(drone, curPickingUpEntity);
            }
            return false;
        }
        return !drone.getPathNavigator().hasNoPath();
    }

    static void tryPickupItem(IDrone drone, EntityItem itemEntity){
        ItemStack stack = itemEntity.getItem();
        int stackSize = stack.getCount();

        ItemStack remainder = PneumaticCraftUtils.exportStackToInventory(drone, stack, EnumFacing.UP);
        int collected = stackSize - remainder.getCount();
        if (collected > 0) {
            drone.onItemPickupEvent(itemEntity, collected);
        }
        if (remainder.isEmpty()) {
            itemEntity.setDead();
        } else if (collected > 0) {
            itemEntity.setItem(remainder);
        }
    }
}
