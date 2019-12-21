package me.desht.pneumaticcraft.api.drone;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

/**
 * Represents a drone or drone-like entity (e.g. a Programmable Controller).
 * <p>
 * Do not implement this class yourself!
 */
public interface IDrone extends ICapabilityProvider {
    /**
     * Get the number of installed upgrades of the given item type.
     *
     * @return amount of installed upgrades in the drone
     */
    int getUpgrades(IItemRegistry.EnumUpgrade upgrade);

    /**
     * Get the drone's world.
     *
     * @return a world
     */
    World world();

    /**
     * Get the drone's fluid tank.Note that this is also accessible via the
     * {@link net.minecraftforge.fluids.capability.CapabilityFluidHandler#FLUID_HANDLER_CAPABILITY}
     * capability.
     *
     * @return a fluid tank
     */
    IFluidTank getFluidTank();

    /**
     * Get the drone's inventory.  Note that this is also accessible via the
     * {@link net.minecraftforge.items.CapabilityItemHandler#ITEM_HANDLER_CAPABILITY} capability.
     *
     * @return an inventory
     */
    IItemHandlerModifiable getInv();

    /**
     * Get the drone's exact position.
     *
     * @return the entity position
     */
    Vec3d getDronePos();

    /**
     * Get the drone's path navigator object.
     *
     * @return the path navigator
     */
    IPathNavigator getPathNavigator();

    /**
     * Send a position to the client to be rendered by the Pneumatic Helmet with Entity Tracker.  This is used
     * to highlight blacklisted block positions.
     *
     * @param pos a block position
     */
    void sendWireframeToClient(BlockPos pos);

    /**
     * Get the fake player object for the drone.  This will always return null if called client-side.
     *
     * @return a fake player
     */
    FakePlayer getFakePlayer();

    /**
     * Is the given position valid for pathfinding purposes?  i.e. may the drone move through or to it?
     * <p>
     * Custom pathfinding functionality may be added via {@link IDroneRegistry#addPathfindableBlock(Block, IPathfindHandler)}.
     *
     * @param pos a block position
     * @return true if the position is valid, false otherwise
     */
    boolean isBlockValidPathfindBlock(BlockPos pos);

    /**
     * Cause the drone to drop the given item at its position.  The specified item does not need to be in the drone's
     * inventory, nor will it be removed from the drone's inventory if present.
     *
     * @param stack the item stack to drop
     */
    void dropItem(ItemStack stack);

    /**
     * Mark the given block as being dug.
     *
     * @param pos a block position
     */
    void setDugBlock(BlockPos pos);

    /**
     * Get the drone's current collection of tasks.
     *
     * @return a vanilla EntityAITasks object
     */
    GoalSelector getTargetAI();

    /**
     * Make the drone emit redstone in the given direction from its current position.
     *
     * @param orientation the direction to emit
     * @param emittingRedstone the redstone level
     */
    void setEmittingRedstone(Direction orientation, int emittingRedstone);

    /**
     * Set the drone's custom name.
     *
     * @param string a custom name
     */
    void setName(ITextComponent string);

    /**
     * Make the drone pick up the given entity.  The given entity will be set as a rider of the drone.
     *
     * @param entity an entity to pick up
     */
    void setCarryingEntity(Entity entity);

    /**
     * Get the list of entities currently carried by this drone.
     *
     * @return a list of entities
     */
    List<Entity> getCarryingEntities();

    /**
     * Has the drone's normal tasks been overridden by special circumstances?  E.g. drone hacked, or heading to a
     * charging station.
     *
     * @return true if the drone's normal AI has been overridden
     */
    boolean isAIOverriden();

    /**
     * Called when a drone is picking up an item.
     *
     * @param curPickingUpEntity the item entity
     * @param stackSize the size of the itemstack in the item entity
     */
    void onItemPickupEvent(ItemEntity curPickingUpEntity, int stackSize);

    /**
     * Retrieve the owning player of this drone.
     *
     * @return the owning player; may be null if the owner is offline
     */
    PlayerEntity getOwner();
}
