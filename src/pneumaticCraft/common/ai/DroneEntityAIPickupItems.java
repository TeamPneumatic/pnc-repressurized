package pneumaticCraft.common.ai;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import pneumaticCraft.common.EventHandlerPneumaticCraft;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class DroneEntityAIPickupItems extends EntityAIBase{
    private final IDroneBase drone;
    private final ProgWidgetAreaItemBase itemPickupWidget;
    private EntityItem curPickingUpEntity;
    private final DistanceEntitySorter theNearestAttackableTargetSorter;

    public DroneEntityAIPickupItems(IDroneBase drone, ProgWidgetAreaItemBase progWidgetPickupItem){
        this.drone = drone;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        itemPickupWidget = progWidgetPickupItem;
        theNearestAttackableTargetSorter = new DistanceEntitySorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        List<Entity> pickableItems = itemPickupWidget.getEntitiesInArea(drone.getWorld(), new IEntitySelector(){
            @Override
            public boolean isEntityApplicable(Entity entity){
                return entity instanceof EntityItem && entity.isEntityAlive();
            }
        });

        Collections.sort(pickableItems, theNearestAttackableTargetSorter);
        for(Entity ent : pickableItems) {
            ItemStack stack = ((EntityItem)ent).getEntityItem();
            if(itemPickupWidget.isItemValidForFilters(stack)) {
                for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                    ItemStack droneStack = drone.getInventory().getStackInSlot(i);
                    if(droneStack == null || droneStack.isItemEqual(stack) && droneStack.stackSize < droneStack.getMaxStackSize()) {
                        if(drone.getPathNavigator().moveToEntity(ent)) {
                            curPickingUpEntity = (EntityItem)ent;
                            return true;
                        }
                    }
                }
            } else {
                drone.addDebugEntry("gui.progWidget.itemPickup.debug.itemNotValid");
            }
        }
        return false; // 

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
        if(curPickingUpEntity.isDead) return false;
        if(Vec3.createVectorHelper(curPickingUpEntity.posX, curPickingUpEntity.posY, curPickingUpEntity.posZ).distanceTo(drone.getPosition()) < 1.5) {
            ItemStack stack = curPickingUpEntity.getEntityItem();
            if(itemPickupWidget.isItemValidForFilters(stack)) {
                new EventHandlerPneumaticCraft().onPlayerPickup(new EntityItemPickupEvent(drone.getFakePlayer(), curPickingUpEntity));//not posting the event globally, as I don't have a way of handling a canceled event.
                int stackSize = stack.stackSize;
                ItemStack remainder = PneumaticCraftUtils.exportStackToInventory(drone.getInventory(), stack, ForgeDirection.UP);//side doesn't matter, drones aren't ISided.
                if(remainder == null) {
                    drone.onItemPickupEvent(curPickingUpEntity, stackSize);
                    curPickingUpEntity.setDead();
                }
            }
            return false;
        }
        return !drone.getPathNavigator().hasNoPath();
    }
}
