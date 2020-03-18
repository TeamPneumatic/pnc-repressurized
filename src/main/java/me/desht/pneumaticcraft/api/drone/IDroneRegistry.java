package me.desht.pneumaticcraft.api.drone;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;

/**
 * Retrieve an instance of this via {@link PneumaticRegistry.IPneumaticCraftInterface#getDroneRegistry()}.
 */
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
     * This will add a custom puzzle piece that has only an Area whitelist/blacklist parameter (similar to a GoTo piece).
     * It will do the specified behaviour. This can be used, for example, to create energy import/export widgets for a
     * custom energy type (i.e. other than Forge Energy).
     * <p>This <strong>must</strong> be called
     * from a registry event handler for {@link RegistryEvent.Register&lt;ProgWidgetType&gt;} to ensure registration
     * is done at the right time - do not call it directly from elsewhere.
     *
     * @param event the Forge registry event
     * @param interactor the custom interactor object
     */
    void registerCustomBlockInteractor(RegistryEvent.Register<ProgWidgetType<?>> event, ICustomBlockInteract interactor);

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
     * @param globalPos global position to deliver items to
     * @param deliveredStacks stacks that are delivered by the drone; when no stacks, or more than 65 stacks are given, this will generate an IllegalArgumentException.
     * @return the drone; you can use this to set a custom name for example (defaults to "Amadron Delivery Drone").
     */
    CreatureEntity deliverItemsAmazonStyle(GlobalPos globalPos, ItemStack... deliveredStacks);

    /**
     * The opposite of deliverItemsAmazonStyle. Will retrieve the queried items from an inventory at the specified location.
     *
     * @param globalPos the global position to retrieve items from
     * @param queriedStacks the stacks to retrieve
     * @return the drone
     */
    CreatureEntity retrieveItemsAmazonStyle(GlobalPos globalPos, ItemStack... queriedStacks);

    /**
     * Similar to deliverItemsAmazonStyle, but with Fluids. Will spawn in a Drone that will fill an IFluidHandler at the
     * given block position. If the fluid doesn't fit or there isn't a IFluidHandler, the fluid <em>will be lost</em>.
     *
     * @param globalPos global position to deliver the fluid to
     * @param deliveredFluid the fluid to deliver
     * @return the drone
     */
    CreatureEntity deliverFluidAmazonStyle(GlobalPos globalPos, FluidStack deliveredFluid);

    /**
     * The opposite of deliverFluidAmazonStyle. Will retrieve the queried fluid from an IFluidHandler at the specified location.
     *
     * @param globalPos global block position to retrieve fluid from
     * @param queriedFluid the fluid to retrieve
     * @return the drone
     */
    CreatureEntity retrieveFluidAmazonStyle(GlobalPos globalPos, FluidStack queriedFluid);
}
