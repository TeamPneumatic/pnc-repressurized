package me.desht.pneumaticcraft.api.semiblock;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

/**
 * Represents a "semiblock" - an attachable gadget which sits on a real block, such as a logistics frame or
 * crop support.
 * @implNote While semiblocks are implemented as entities, this is an implementation detail which should not be relied
 * upon any more than strictly necessary.
 */
public interface ISemiBlock extends ICapabilityProvider {
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
    ITextComponent getDisplayName();

    /**
     * Get the world this semiblock is in.
     * @return the world
     */
    World getWorld();

    /**
     * Get the block position this entity occupies.
     *
     * @return the block pos
     */
    BlockPos getBlockPos();

    /**
     * Get the tile entity at the semiblock's position.  This is cached for performance.
     * @return the tile entity, or null if there is none
     */
    TileEntity getCachedTileEntity();

    /**
     * Written to the dropped item (under the "EntityTag" subtag) when the semiblock is broken, to persisted entity
     * data by {@code Entity#writeAdditional()}, and displayed by info mods such as TOP or Waila. Use this method
     * rather than {@code writeAdditional()} for fields that either need to be serialized to the dropped item, or
     * displayed on TOP/Waila.
     * <p>
     * @implNote Data written to itemstacks is automatically applied to newly-spawned entities by
     * {@link EntityType#applyItemNBT(World, PlayerEntity, Entity, CompoundNBT)} when the
     * semiblock entity is spawned from an item (i.e. placed by a player).
     * @param tag NBT tag to write data to
     */
    CompoundNBT serializeNBT(CompoundNBT tag);

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
    default void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
    }

    /**
     * Called when a semiblock is right-clicked with a Logistics Configurator
     * @param player the player
     * @param side the side of the block being clicked
     * @return true if something was done, false if the semiblock doesn't care about being clicked
     */
    default boolean onRightClickWithConfigurator(PlayerEntity player, Direction side) {
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
     * @see ISemiBlock#byTrackingId(World, int)
     * @return the underlying entity's ID, or -1 if the entity has not been added to the world
     */
    int getTrackingId();

    /**
     * Mark this semiblock for removal.
     *
     * @param player player who is removing the semiblock
     */
    void removeSemiblock(PlayerEntity player);

    /**
     * Add tooltip information for this semiblock. This info is used by info mods such as Waila or TOP.
     *
     * @param curInfo append info to this list
     * @param player the player looking at the entity or item
     * @param tag NBT data as saved by {@link ISemiBlock#serializeNBT(CompoundNBT)}
     * @param extended true if extended data should be shown
     */
    default void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
    }

    /**
     * Write this semiblock to network buffer for network sync purposes.
     * @param payload the buffer
     */
    void writeToBuf(PacketBuffer payload);

    /**
     * Read this semiblock from network buffer for network sync purposes.
     * @param payload the buffer
     */
    void readFromBuf(PacketBuffer payload);

    /**
     * A color in ARGB format.  Used for various things: GUI/item/render tinting, as well as TOP colour coding.
     * @return a color for this semiblock type
     */
    default int getColor() { return 0xFF808080; }

    /**
     * Retrieve a semiblock by tracking ID.  This is only intended to be used for network sync purposes and is
     * subject to change on a world reload.
     * @see ISemiBlock#getTrackingId()
     * @param world the world
     * @param id the tracking ID
     * @return a semiblock, or null if ID is not valid
     */
    static ISemiBlock byTrackingId(World world, int id) {
        Entity e = world.getEntity(id);
        return e instanceof ISemiBlock ? (ISemiBlock) e : null;
    }
}
