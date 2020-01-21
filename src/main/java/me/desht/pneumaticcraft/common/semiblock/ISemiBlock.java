package me.desht.pneumaticcraft.common.semiblock;

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

import java.util.List;

public interface ISemiBlock {
    /**
     * Get an ID for this semiblock, which should match the corresponding item's registry name.
     * @return a semiblock ID
     */
    ResourceLocation getId();

    ITextComponent getDisplayName();

    TileEntity getCachedTileEntity();

    World getWorld();

    /**
     * Get the block position this entity occupies.
     *
     * @return the block pos
     */
    BlockPos getBlockPos();

    /**
     * Written to the dropped item (under the "EntityTag" subtag) when the semiblock is broken, to persisted entity
     * data by {@code Entity#writeAdditional()}, and displayed by info mods such as TOP or Waila. Use this method
     * rather than {@code writeAdditional()} for fields that either need to be serialized to the dropped item, or
     * displayed on TOP/Waila.
     * <p>
     * Note that data written to itemstacks is automatically applied to newly-spawned entities by
     * {@link EntityType#applyItemNBT(World, PlayerEntity, Entity, CompoundNBT)} when the
     * entity is spawned from an item (i.e. placed by a player).
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
     * @return true if something was done, false if the semiblock isn't interested
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
     * Get the tracking for this semiblock; should only be used for syncing purposes.
     *
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
     * Add tooltip information for this semiblock. This info is for display by info mods (TOP, Waila...)
     *
     * @param curInfo append info to this list
     * @param player the player looking at the entity or item
     * @param tag NBT data as saved by {@link ISemiBlock#serializeNBT(CompoundNBT)}
     * @param extended true if extended data should be shown
     */
    default void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
    }

    void writeToBuf(PacketBuffer payload);

    void readFromBuf(PacketBuffer payload);

    /**
     * A color in ARGB format.  Used for various things: GUI/item/render tinting, as well as TOP colour coding.
     * @return a color for this semiblock type
     */
    default int getColor() { return 0xFF808080; }

    static ISemiBlock byTrackingId(World world, int id) {
        Entity e = world.getEntityByID(id);
        return e instanceof ISemiBlock ? (ISemiBlock) e : null;
    }
}
