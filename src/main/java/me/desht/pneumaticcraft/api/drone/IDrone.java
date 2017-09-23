package me.desht.pneumaticcraft.api.drone;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

//import net.minecraftforge.common.IExtendedEntityProperties;

public interface IDrone extends IPressurizable, ICapabilityProvider {
    /**
     * @return amount of inserted upgrades in the drone
     */
    int getUpgrades(Item upgrade);

    World world();

    IFluidTank getTank();

//    public IInventory getInv();
IItemHandlerModifiable getInv();

    Vec3d getDronePos();

    IPathNavigator getPathNavigator();

    void sendWireframeToClient(BlockPos pos);

    EntityPlayerMP getFakePlayer();

    boolean isBlockValidPathfindBlock(BlockPos pos);

    void dropItem(ItemStack stack);

    void setDugBlock(BlockPos pos);

    EntityAITasks getTargetAI();

//    public IExtendedEntityProperties getProperty(String key);
//
//    public void setProperty(String key, IExtendedEntityProperties property);

    void setEmittingRedstone(EnumFacing orientation, int emittingRedstone);

    void setName(String string);

    void setCarryingEntity(Entity entity);

    List<Entity> getCarryingEntities();
//    public Entity getCarryingEntity();

    boolean isAIOverriden();

    void onItemPickupEvent(EntityItem curPickingUpEntity, int stackSize);
}
