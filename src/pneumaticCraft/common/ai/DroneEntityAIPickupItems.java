package pneumaticCraft.common.ai;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import pneumaticCraft.common.EventHandlerPneumaticCraft;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public class DroneEntityAIPickupItems extends EntityAIBase{
    private final EntityDrone drone;
    private final double speed;
    private final ProgWidgetAreaItemBase itemPickupWidget;
    private EntityItem curPickingUpEntity;
    private final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;

    public DroneEntityAIPickupItems(EntityDrone drone, double speed, ProgWidgetAreaItemBase progWidgetPickupItem){
        this.drone = drone;
        this.speed = speed;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        itemPickupWidget = progWidgetPickupItem;
        theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        List<Entity> pickableItems = itemPickupWidget.getEntitiesInArea(drone.worldObj, new IEntitySelector(){
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
                        int x = MathHelper.floor_double(ent.posX);
                        int y = MathHelper.floor_double(ent.posY);
                        int z = MathHelper.floor_double(ent.posZ);
                        if(drone.isBlockValidPathfindBlock(x, y, z) && drone.getNavigator().tryMoveToEntityLiving(ent, speed)) {
                            curPickingUpEntity = (EntityItem)ent;
                            return true;
                        }
                    }
                }
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
        if(curPickingUpEntity.getDistanceToEntity(drone) < 1.5) {
            ItemStack stack = curPickingUpEntity.getEntityItem();
            if(itemPickupWidget.isItemValidForFilters(stack)) {
                new EventHandlerPneumaticCraft().onPlayerPickup(new EntityItemPickupEvent(drone.getFakePlayer(), curPickingUpEntity));//not posting the event globally, as I don't have a way of handling a canceled event.
                int stackSize = stack.stackSize;
                ItemStack remainder = PneumaticCraftUtils.exportStackToInventory(drone.getInventory(), stack, ForgeDirection.UP);//side doesn't matter, drones aren't ISided.
                if(remainder == null) {
                    drone.onItemPickup(curPickingUpEntity, stackSize);
                    curPickingUpEntity.setDead();
                }
            }
            return false;
        }
        return !drone.getNavigator().noPath();
    }
}
