package me.desht.pneumaticcraft.api.drone;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Implement this and register it with {@link IDroneRegistry#registerCustomBlockInteractor(ICustomBlockInteract)}.
 * This will add a puzzle piece that has only a Area white- and blacklist parameter (similar to a GoTo piece).
 * It will do the specified behaviour. This can be used to create energy import/export widgets, for example.
 */
public interface ICustomBlockInteract {

    /**
     * Get a unique name for this puzzle piece. This will be used when serializing the piece.
     *
     * @return a unique string ID
     */
    String getName();

    /**
     * Should return the puzzle piece texture. Should be a multiple of 80x64 (width x height). I'd recommend starting
     * out with copying the Go To widget texture.
     *
     * @return a resource location for the texture to be used
     */
    ResourceLocation getTexture();

    /**
     * The actual interaction.
     * <p>
     * For every position in the specified area, the drone will visit every block (ordered from closest to furthest). It
     * will call this method with {@code simulate} = true. If this method returns true, the drone will navigate to this
     * location, and call this method again with {@code simulate} = false It will keep doing this until this method
     * returns false.
     * <p>
     * In the puzzle piece GUI, players can specify a 'use count' and fill in the maximum count they want
     * to use. When {@link IBlockInteractHandler#useCount()} returns true, and {@code simulate} is false, you must only
     * import/export up to {@link IBlockInteractHandler#getRemainingCount()}, and you must notify the transferred amount
     * by doing {@link IBlockInteractHandler#decreaseCount(int)}.
     *
     * @param pos current visited location
     * @param drone a reference to the drone object
     * @param interactHandler object you can use to use to get accessible sides and give feedback about counts.
     * @param simulate  true when trying to figure out whether or not the drone should navigate to this block,
     *                  false when next to this block.
     * @return true if the interaction was (would be) successful
     */
    boolean doInteract(BlockPos pos, IDrone drone, IBlockInteractHandler interactHandler, boolean simulate);

    /**
     * Used for crafting, categorizes the puzzle piece.  The return is the same index as used by Minecraft dyes
     * (e.g. 0 = black, 1 = red...)
     *
     * @return a color index
     */
    int getCraftingColorIndex();
}
