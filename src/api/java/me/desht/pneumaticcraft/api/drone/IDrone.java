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

import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Represents a drone or drone-like object (e.g. a Programmable Controller).
 * <p>
 * Do not implement this class yourself! Instances of it are available via drone events ({@link AmadronRetrievalEvent},
 * {@link DroneConstructingEvent}, {@link DroneSuicideEvent}), and via {@code getDrone(...)} methods in
 * {@link IDroneRegistry}.
 */
public interface IDrone extends ICapabilityProvider {
    /**
     * Get a count of the installed upgrades of the given type.
     *
     * @param upgrade the upgrade to check
     * @return amount of installed upgrades in the drone
     */
    int getUpgrades(PNCUpgrade upgrade);

    /**
     * Get the drone's world.
     *
     * @return a world
     */
    Level world();

    /**
     * Get the drone's fluid tank.  Note that this is also accessible via the
     * {@link net.minecraftforge.common.capabilities.ForgeCapabilities#FLUID_HANDLER}
     * capability, which should be used in preference.
     *
     * @return a fluid tank
     */
    IFluidTank getFluidTank();

    /**
     * Get the drone's inventory.  Note that this is also accessible via the
     * {@link net.minecraftforge.common.capabilities.ForgeCapabilities#ITEM_HANDLER} capability,
     * which should be used in preference.
     *
     * @return an inventory
     */
    IItemHandlerModifiable getInv();

    /**
     * Get the drone's exact position.
     *
     * @return the entity position
     */
    Vec3 getDronePos();

    /**
     * Get the position of the drone's controller. For actual drone entities, this will always be {@code BlockPos.ZERO}.
     * If the drone is actually a Programmable Controller, it will be the controller's block position.
     */
    BlockPos getControllerPos();

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
     * @return a vanilla GoalSelector object
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
    void setName(Component string);

    /**
     * Make the drone pick up the given entity.  The given entity will be set as a rider of the drone, but it will
     * not have any control over the drone (in fact, its AI is disabled while being carried).
     *
     * @param entity an entity to pick up
     */
    void setCarryingEntity(Entity entity);

    /**
     * Get the list of entities currently carried by this drone.
     * Note: although this method returns a list, drones currently support only a single carried entity.
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
    boolean isAIOverridden();

    /**
     * Called when a drone is picking up an item.
     *
     * @param curPickingUpEntity the item entity
     * @param stackSize the size of the itemstack in the item entity
     */
    void onItemPickupEvent(ItemEntity curPickingUpEntity, int stackSize);

    /**
     * Retrieve the owning player of this drone; i.e. the player who deployed the drone. A drone deployed by a
     * Dispenser does not have a valid player owner.
     *
     * @return the owning player; will be null if the owner is offline or the drone was not player-deployed
     */
    Player getOwner();

    /**
     * Get the UUID of the drone's owner.  This will be non-null even if the owning player is offline. A drone
     * deployed by a Dispenser has an arbitrary owner UUID that was assigned to it when it was spawned.
     *
     * @return the owner's UUID
     */
    @Nonnull
    UUID getOwnerUUID();

    /**
     * Get the blockpos at which this drone was deployed (by player or dispenser).  For the Programmable Controller,
     * this will simply return the controller's block position.
     *
     * @return the deployment pos
     */
    BlockPos getDeployPos();
}
