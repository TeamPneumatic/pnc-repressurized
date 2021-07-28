package me.desht.pneumaticcraft.api.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Represents an item which can store & provide one or more block positions.  An example would be the GPS Tool (one
 * position), the GPS Area Tool (possibly many positions) or the Amadron Tablet (two positions).
 */
public interface IPositionProvider {
    /**
     * Get block position data from the given ItemStack.  It is up to the implementor to decide how the block positions
     * should be stored on the itemstack and in what order they should be returned.
     *
     * @param playerId the player, for player-global variable context (may be null)
     * @param stack the itemstack
     * @return a list of block positions that has been retrieved from the itemstack
     */
    @Nonnull
    List<BlockPos> getStoredPositions(UUID playerId, @Nonnull ItemStack stack);

    /**
     * Color that should be used to highlight the stored block positions if & when they are rendered on-screen.
     *
     * @param index the index in the list returned by getStoredPositions()
     * @return a color in ARGB format, or 0 to skip rendering completely
     */
    default int getRenderColor(int index) { return 0xFFFFFF00; }
    
    /**
     * Whether or not the rendered positions should be visible through the world.
     * 
     * @return true if visible through the world, false if not.
     */
    default boolean disableDepthTest() { return true; }

    /**
     * Gets the raw stored positions in this provider. E.g. for the GPS Area Tool, just the two clicked
     * positions, not the whole set of positions defined by the tool's area type.
     *
     * @param player the player, for player-global variable context
     * @param stack the itemstack
     * @return the raw positions stored on the itemstack
     */
    default List<BlockPos> getRawStoredPositions(PlayerEntity player, ItemStack stack) {
        return getStoredPositions(player.getUniqueID(), stack);
    }

    /**
     * If the item stores any global variables which the client needs to know about (e.g. for area rendering), override
     * this method to sync their values to the client. This method is called server-side when an item in any player's
     * inventory (which implements {@link IPositionProvider}) changes in any way.
     * <p>
     * See {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#syncGlobalVariable(ServerPlayerEntity, String)}}
     * for a convenience method to send the necessary sync packet.
     *
     * @param player the player to sync to
     * @param stack the itemstack
     */
    default void syncVariables(ServerPlayerEntity player, ItemStack stack) {
    }
}
