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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

/**
 * Implement this and register it with {@link IDroneRegistry#registerCustomBlockInteractor(ICustomBlockInteract)}.
 * This will add a puzzle piece that has only an Area white- and blacklist parameter (similar to a Goto piece).
 * This could be used to create custom energy import/export widgets, for example.
 */
public interface ICustomBlockInteract {
    /**
     * Get a unique name for this puzzle piece. This will be an ID in the "pneumaticcraft:" namespace so it's recommended
     * to prefix the string with your mod ID, or some other unique string, to avoid clashes.
     *
     * @return a unique ID
     */
    String getID();

    /**
     * Get the puzzle piece texture. Should be a multiple of 80x64 (width x height). I'd recommend starting
     * out by copying the
     * <a href="https://github.com/TeamPneumatic/pnc-repressurized/blob/master/src/main/resources/assets/pneumaticcraft/textures/items/progwidgets/goto_piece.png">Go To widget texture</a>
     *
     * @return a resource location for the texture to be used
     */
    ResourceLocation getTexture();

    /**
     * The actual interaction.
     * <p>
     * For each blockpos in the specified area, the drone will visit that block (ordered from closest to furthest). It
     * will call this method with {@code simulate} = true. If this method returns true, the drone will navigate to this
     * location, and call this method again with {@code simulate} = false. It will keep doing this until this method
     * returns false.
     * <p>
     * In the puzzle piece GUI, players can specify a 'use count' and fill in the maximum count they want
     * to use. When {@link IBlockInteractHandler#useCount()} returns true, and {@code simulate} is false, you must only
     * import/export up to {@link IBlockInteractHandler#getRemainingCount()}, and you must notify the transferred amount
     * by doing {@link IBlockInteractHandler#decreaseCount(int)}.
     *
     * @param pos current visited location
     * @param drone a reference to the drone object
     * @param interactHandler object you can use to get accessible sides and give feedback about counts
     * @param simulate  true when determining whether the drone should navigate to this block,
     *                  false when next to this block
     * @return true if the interaction was (would be) successful
     */
    boolean doInteract(BlockPos pos, IDrone drone, IBlockInteractHandler interactHandler, boolean simulate);

    /**
     * Used for crafting, categorizes the puzzle piece.
     *
     * @return a color
     */
    DyeColor getColor();
}
