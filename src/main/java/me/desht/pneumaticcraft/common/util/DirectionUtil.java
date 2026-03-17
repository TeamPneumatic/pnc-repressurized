/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import net.minecraft.core.Direction;

import static net.minecraft.core.Direction.*;

public class DirectionUtil {
    // since this is private in Direction...
    public static final Direction[] VALUES = new Direction[] {
            DOWN, UP, NORTH, SOUTH, WEST, EAST
    };

    public static boolean getDirectionBit(int val, Direction dir) {
        return (val & (1 << dir.get3DDataValue())) != 0;
    }

    public static byte setDirectionBit(int val, Direction dir, boolean set) {
        return set ?
                (byte) (val | (1 << dir.get3DDataValue())) :
                (byte) (val & ~(1 << dir.get3DDataValue()));
    }
}
