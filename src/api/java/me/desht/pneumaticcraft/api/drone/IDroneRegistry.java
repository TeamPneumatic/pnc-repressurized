/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.drone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;

/**
 * Retrieve an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getDroneRegistry()}.
 */
public interface IDroneRegistry {
    /**
     * Normally drones will pathfind through any block that doesn't have any collisions.
     * With this method you can register custom blocks to allow the drone to pathfind through them. If the block
     * requires any special handling, e.g. to allow pathfinding under certain conditions, you can pass a IPathFindHandler
     * with the registry.
     *
     * @param block the block to allow pathfinding through
     * @param handler can be null, to always allow pathfinding through this block.
     */
    void addPathfindableBlock(Block block, IPathfindHandler handler);

    /**
     * Register a custom puzzle piece that has only an Area whitelist/blacklist parameter (similar to a Go To piece).
     * It will do the specified behaviour. This can be used, for example, to create energy import/export widgets for a
     * custom energy type (i.e. other than Forge Energy).
     * <p>
     * <strong>Important:</strong> this must be called from your mod constructor, i.e. <em>before</em> registries are
     * frozen, since it adds a deferred registration entry.
     *
     * @param interactor the custom interactor object
     */
    void registerCustomBlockInteractor(ICustomBlockInteract interactor);

    /**
     * Will spawn in a Drone a distance away from the given coordinate. When there is an inventory at the given block
     * position, the drone will export the items there. If there is no inventory or items don't fit, the drone will
     * travel to 5 blocks above the specified Y level, and drop the deliveredStacks. When there isn't a clear path for
     * the items to fall, the Drone will deliver at a Y level above the specified Y that <em>is</em>
     * clear. If no clear blocks can be found (when there are only solid blocks), the Drone will drop the items very
     * high up in the air instead.
     * <p>
     * When a player attempts to catch the drone (by wrenching it), the drone will only the drop the items that it was
     * delivering (or none if it dropped those items already). The Drone itself will never be dropped.
     *
     * @param globalPos global position to deliver items to
     * @param deliveredStacks stacks to be delivered by the drone
     * @return the drone; you can use this to set a custom name for example (defaults to "Amadron Delivery Drone").
     * @throws IllegalArgumentException if the array of ItemStacks is empty or contains more than 36 separate stacks
     */
    PathfinderMob deliverItemsAmazonStyle(GlobalPos globalPos, ItemStack... deliveredStacks);

    /**
     * The opposite of deliverItemsAmazonStyle. Will retrieve the queried items from an inventory at the specified location.
     *
     * @param globalPos the global position to retrieve items from
     * @param queriedStacks the stacks to retrieve
     * @return the drone
     */
    PathfinderMob retrieveItemsAmazonStyle(GlobalPos globalPos, ItemStack... queriedStacks);

    /**
     * Similar to deliverItemsAmazonStyle, but with Fluids. Will spawn in a Drone that will fill an IFluidHandler at the
     * given block position. If the fluid doesn't fit or there isn't a IFluidHandler, the fluid <em>will be lost</em>.
     *
     * @param globalPos global position to deliver the fluid to
     * @param deliveredFluid the fluid to deliver
     * @return the drone
     * @throws IllegalArgumentException if the FluidStack contains more than 576,000 mB of fluid (the maximum a fully
     * upgraded drone can carry)
     */
    PathfinderMob deliverFluidAmazonStyle(GlobalPos globalPos, FluidStack deliveredFluid);

    /**
     * The opposite of deliverFluidAmazonStyle. Will retrieve the queried fluid from an IFluidHandler at the specified location.
     *
     * @param globalPos global block position to retrieve fluid from
     * @param queriedFluid the fluid to retrieve
     * @return the drone
     */
    PathfinderMob retrieveFluidAmazonStyle(GlobalPos globalPos, FluidStack queriedFluid);

    /**
     * Get the {@link IDrone} API object for the given entity ID
     * @param level the level
     * @param entityID the entity ID
     * @return an IDrone object, or {@code Optional.empty()} if the entity isn't a drone
     */
    Optional<IDrone> getDrone(Level level, int entityID);

    /**
     * Get the {@link IDrone} API object for the block entity at the given position
     * @param level the level
     * @param pos the block entity's position
     * @return an IDrone object, or {@code Optional.empty()} if the block entity at the given pos isn't a Programmable Controller
     */
    Optional<IDrone> getDrone(Level level, BlockPos pos);
}
