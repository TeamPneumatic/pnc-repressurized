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

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a semiblock which sits on the side of an actual block, rather than occupying the same space.
 * E.g. Transfer Gadgets and Logistics Frames are directional, but Crop Supports and Heat Frames are not.
 */
public interface IDirectionalSemiblock {
    Direction getSide();

    void setSide(Direction direction);

    @Nullable
    static Direction getDirection(ISemiBlock semiBlock) {
        return semiBlock instanceof IDirectionalSemiblock d ? d.getSide() : null;
    }
}
