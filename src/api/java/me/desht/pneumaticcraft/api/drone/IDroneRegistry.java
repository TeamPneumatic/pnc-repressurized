package me.desht.pneumaticcraft.api.drone;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityCreature;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public interface IDroneRegistry {
    /**
     * Normally drones will pathfind through any block that doesn't have any collisions (Block#getBlocksMovement returns true).
     * With this method you can register custom blocks to allow the drone to pathfind through them. If the block requires any special
     * handling, like allow pathfinding on certain conditions, you can pass a IPathFindHandler with the registry.
     *
     * @param block the block to allow pathfinding through
     * @param handler can be null, to always allow pathfinding through this block.
     */
    void addPathfindableBlock(Block block, IPathfindHandler handler);

    /**
     * This will add a puzzle piece that has only a Area white- and blacklist parameter (similar to a GoTo piece).
     * It will do the specified behaviour. This can be used to create energy import/export widgets.  This must be called
     * from an event handler for {@link me.desht.pneumaticcraft.api.event.PuzzleRegistryEvent} to ensure registration
     * is done at the right time - don't call it directly from a (pre/post) init handler.
     *
     * @param interactor the custom interactor object
     */
    void registerCustomBlockInteractor(ICustomBlockInteract interactor);

    /**
     * Will spawn in a Drone a distance away from the given coordinate. When there is an inventory at the given block
     * position, the drone will export the items there. If there is no inventory or items don't fit, the drone will
     * travel to 5 blocks above the specified Y level, and drop the deliveredStacks. When there isn't a clear path for
     * the items to fall these 5 blocks the Drone will deliver at a Y level above the specified Y that <em>is</em>
     * clear. If no clear blocks can be found (when there are only solid blocks), the Drone will drop the items very
     * high up in the air instead.
     * <p>
     * When a player attempts to catch the drone (by wrenching it), the drone will only the drop the items that it was
     * delivering (or none if it dropped those items already). The Drone itself never will be dropped.
     *
     * @param world the world
     * @param pos position to deliver items to
     * @param deliveredStacks stacks that are delivered by the drone; when no stacks, or more than 65 stacks are given, this will generate an IllegalArgumentException.
     * @return the drone; you can use this to set a custom name for example (defaults to "Amadron Delivery Drone").
     */
    EntityCreature deliverItemsAmazonStyle(World world, BlockPos pos, ItemStack... deliveredStacks);

    /**
     * The opposite of deliverItemsAmazonStyle. Will retrieve the queried items from an inventory at the specified location.
     *
     * @param world the world
     * @param pos the position to retrieve items from
     * @param queriedStacks the stacks to retrieve
     * @return the drone
     */
    EntityCreature retrieveItemsAmazonStyle(World world, BlockPos pos, ItemStack... queriedStacks);

    /**
     * Similar to deliverItemsAmazonStyle, but with Fluids. Will spawn in a Drone that will fill an IFluidHandler at the
     * given block position. If the fluid doesn't fit or there isn't a IFluidHandler, the fluid <em>will be lost</em>.
     *
     * @param world the world
     * @param pos the position to delivery the fluid to
     * @param deliveredFluid the fluid to deliver
     * @return the drone
     */
    EntityCreature deliverFluidAmazonStyle(World world, BlockPos pos, FluidStack deliveredFluid);

    /**
     * The opposite of deliverFluidAmazonStyle. Will retrieve the queried fluid from an IFluidHandler at the specified location.
     *
     * @param world the world
     * @param pos the block position to retrieve fluid from
     * @param queriedFluid the fluid to retrieve
     * @return the drone
     */
    EntityCreature retrieveFluidAmazonStyle(World world, BlockPos pos, FluidStack queriedFluid);
}
