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
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;

/**
 * This represents the drone's AI interaction handler. DON'T IMPLEMENT THIS, just use it when passed to
 * {@link ICustomBlockInteract#doInteract(BlockPos, IDrone, IBlockInteractHandler, boolean)}.
 */
@ApiStatus.NonExtendable
public interface IBlockInteractHandler {
    /**
     * Returns a boolean[6] of all sides, in order D,U,N,S,W,E; you can use {@link Direction#get3DDataValue()} to get
     * an index into this array to get the facing direction, or you use the {@link #isSideAccessible(Direction)}
     * convenience method.
     *
     * @return an array of booleans indexed by the Direction index
     */
    boolean[] getSides();

    /**
     * This is controlled by players setting the "Use Count" checkbox in the widget GUI.  When true, the drone is trying
     * to transfer a specific quantity of the resource in question (items/fluid/energy...).
     *
     * @return true if a specific quantity should be imported/exported
     */
    boolean useCount();

    /**
     * When {@link #useCount()} returns true, this should be called to notify the amount actually transferred.
     *
     * @param count the amount to notify
     */
    void decreaseCount(int count);

    /**
     * When {@link #useCount()} returns true, transfer only up to this much of the resource in question.
     *
     * @return the maximum amount to attempt to transfer
     */
    int getRemainingCount();

    /**
     * When invoked, the drone will abort searching the area. Could be used to abort early when full of RF energy for
     * example, when importing RF.  (It's useless to search any further)
     */
    void abort();

    /**
     * Convenience method to check if a given face is accessible.
     *
     * @param face the face to check
     * @return true if the face is accessible
     */
    default boolean isSideAccessible(Direction face) {
        return getSides()[face.get3DDataValue()];
    }
}
