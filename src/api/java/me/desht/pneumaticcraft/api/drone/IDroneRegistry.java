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
     * @param block
     * @param handler can be null, to always allow pathfinding through this block.
     */
    void addPathfindableBlock(Block block, IPathfindHandler handler);

    /**
     * This will add a puzzle piece that has only a Area white- and blacklist parameter (similar to a GoTo piece).
     * It will do the specified behaviour. This can be used to create energy import/export widgets.
     *
     * @param interactor
     */
    void registerCustomBlockInteractor(ICustomBlockInteract interactor);

    /**
     * Will spawn in a Drone a distance away from the given coordinate. When there is an inventory at the given x,y,z the drone will export the items in there. If there isn't or items don't fit, the drone will travel to 5 blocks above the specified
     * y level, and drop the deliveredStacks. When there isn't a clear path for the items to fall these 5 blocks the Drone will deliver at a
     * y level above the specified y that _is_ clear. If no clear blocks can be found (when there are only solid blocks), the Drone will
     * drop the items very high up in the air instead, and drop them there.
     * <p>
     * When the Drone is tried to be caught by a player (by wrenching it), the drone will only the drop the items that it was delivering (or
     * none if it dropped those items already). The Drone itself never will be dropped.
     *
     * @param pos
     * @param deliveredStacks stacks that are delivered by the drone. When no stacks, or more than 65 stacks are given, this will generate an IllegalArgumentException.
     * @return the drone. You can use this to set a custom name for example (defaults to "Amadron Delivery Drone").
     */
    EntityCreature deliverItemsAmazonStyle(World world, BlockPos pos, ItemStack... deliveredStacks);

    /**
     * The opposite of deliverItemsAmazonStyle. Will retrieve the queried items from an inventory at the specified location.
     *
     * @param world
     * @param pos
     * @param queriedStacks
     * @return
     */
    EntityCreature retrieveItemsAmazonStyle(World world, BlockPos pos, ItemStack... queriedStacks);

    /**
     * Similar to deliverItemsAmazonStyle, but with Fluids. Will spawn in a Drone that will fill an IFluidHandler at the given x,y,z. If the fluid doesn't fit or there isn't a IFluidHandler, the fluid is lost.
     *
     * @param world
     * @param pos
     * @param deliveredFluid
     * @return
     */
    EntityCreature deliverFluidAmazonStyle(World world, BlockPos pos, FluidStack deliveredFluid);

    /**
     * The opposite of deliverFluidAmazonStyle. Will retrieve the queried fluid from an IFluidHandler at the specified location.
     *
     * @param world
     * @param pos
     * @param queriedStacks
     * @return
     */
    EntityCreature retrieveFluidAmazonStyle(World world, BlockPos pos, FluidStack queriedFluid);
}
