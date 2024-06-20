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

package me.desht.pneumaticcraft.api.semiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.EntityCapability;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a "semiblock" - an attachable gadget which sits on a real block, such as a logistics frame or
 * crop support.
 * @implNote While semiblocks are implemented as entities, this is an implementation detail which should not be relied
 * upon any more than strictly necessary.
 */
public interface ISemiBlock {
    /**
     * Get a unique ID for this semiblock, which should match the corresponding item's registry name.
     * @return a semiblock ID
     */
    ResourceLocation getSemiblockId();

    /**
     * Get the displayed name for this semiblock.
     *
     * @return the name
     */
    Component getSemiblockDisplayName();

    /**
     * Get the world this semiblock is in.
     * @return the world
     */
    Level getWorld();

    /**
     * Get the block position this entity occupies.
     *
     * @return the block pos
     */
    BlockPos getBlockPos();

    /**
     * Get the block entity at the semiblock's position.  This is cached for performance.
     * @return the block entity, or null if there is none
     */
    BlockEntity getCachedTileEntity();

    /**
     * Written to the dropped item (under the "EntityTag" subtag) when the semiblock is broken, to persisted entity
     * data by {@code Entity#addAdditionalSaveData()}, and displayed by info mods such as TOP or Waila. Use this method
     * rather than {@code addAdditionalSaveData()} for fields that either need to be serialized to the dropped item, or
     * displayed on TOP/Waila.
     * <p>
     *
     * @param tag      NBT tag to write data to
     * @param provider
     * @implNote Data written to itemstacks is automatically applied to newly-spawned entities by
     * {@link net.minecraft.world.entity.EntityType#updateCustomEntityTag(Level, Player, Entity, CustomData)} when the
     * semiblock entity is spawned from an item (i.e. placed by a player).
     */
    CompoundTag serializeNBT(CompoundTag tag, HolderLookup.Provider provider);

    /**
     * Implement tick logic here. Always be sure to call {@code super.tick()} in subclass overrides!
     */
    void tick();

    /**
     * Check if this semiblock is still valid, i.e. the underlying entity is still alive.
     *
     * @return true if this semiblock is valid, false otherwise
     */
    boolean isValid();

    /**
     * Add any dropped items from this semiblock to the given list.  By default, just the semiblock item itself is
     * added, but this can be overridden to drop extra items if needed.
     */
    NonNullList<ItemStack> getDrops();

    /**
     * Check if this semiblock can be placed here.
     * @param facing the side of the block against which it is placed
     * @return true if the semiblock is placeable here, false otherwise
     */
    boolean canPlace(Direction facing);

    /**
     * Called immediately after the semiblock entity has been added to the world.
     * @param player player who is placing the semiblock
     * @param stack itemstack used to create the entity
     * @param facing the side of the block which was clicked to place the entity
     */
    default void onPlaced(Player player, ItemStack stack, Direction facing) {
    }

    /**
     * Called when a semiblock is right-clicked with a Logistics Configurator
     * @param player the player
     * @param side the side of the block being clicked
     * @return true if something was done, false if the semiblock doesn't care about being clicked
     */
    default boolean onRightClickWithConfigurator(Player player, Direction side) {
        return false;
    }

    /**
     * Check if this semiblock can coexist with the other semiblock, in the same block pos. By default this is
     * true if at least one of the semiblocks is a {@link IDirectionalSemiblock} and both semiblocks have a different
     * side.
     * @param otherSemiblock the other semiblock
     * @return true if they can coexist, false otherwise
     */
    boolean canCoexist(ISemiBlock otherSemiblock);

    /**
     * Get the tracking for this semiblock; this should only be used for network sync purposes, and is subject to change
     * on a world reload.
     * @see ISemiBlock#byTrackingId(Level, int)
     * @return the underlying entity's ID, or -1 if the entity has not been added to the world
     */
    int getTrackingId();

    /**
     * Called when an entity has caused this semiblock to be removed; usually, but necessarily, a player wrenching or
     * hitting it.
     *
     * @param entity the killer entity
     */
    void killedByEntity(Entity entity);

    /**
     * Add tooltip information for this semiblock. This info is used by info mods such as Waila or TOP.
     *
     * @param consumer the component consumer
     * @param player the player looking at the entity or item
     * @param tag NBT data as saved by {@link ISemiBlock#serializeNBT(CompoundTag, HolderLookup.Provider)}
     * @param extended true if extended data should be shown
     */
    default void addTooltip(Consumer<Component> consumer, Player player, CompoundTag tag, boolean extended) {
    }

    /**
     * Write this semiblock to network buffer for network sync purposes.
     * @param payload the buffer
     */
    void writeToBuf(RegistryFriendlyByteBuf payload);

    /**
     * Read this semiblock from network buffer for network sync purposes.
     * @param payload the buffer
     */
    void readFromBuf(RegistryFriendlyByteBuf payload);

    /**
     * A color in ARGB format.  Used for various things: GUI/item/render tinting, as well as TOP colour coding.
     * @return a color for this semiblock type
     */
    default int getColor() { return 0xFF808080; }

    <T> Optional<T> getSemiblockCapability(EntityCapability<T,Direction> capability, Direction direction);

    <T> Optional<T> getSemiblockCapability(EntityCapability<T,Void> capability);

    /**
     * Retrieve a semiblock by tracking ID.  This is only intended to be used for network sync purposes and is
     * subject to change on a world reload.
     * @see ISemiBlock#getTrackingId()
     * @param world the world
     * @param id the tracking ID
     * @return a semiblock, or null if ID is not valid
     */
    static ISemiBlock byTrackingId(Level world, int id) {
        Entity e = world.getEntity(id);
        return e instanceof ISemiBlock ? (ISemiBlock) e : null;
    }
}
