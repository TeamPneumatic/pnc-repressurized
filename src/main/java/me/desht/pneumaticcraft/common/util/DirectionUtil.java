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

/**
 * rotateAround() disappeared from Direction in 1.15
 */
public class DirectionUtil {
    // since this is private in Direction...
    public static final Direction[] VALUES = new Direction[] {
            DOWN, UP, NORTH, SOUTH, WEST, EAST
    };

    // this may return to Direction.HORIZONTALS one day (like in 1.12.2) but for now...
    public static final Direction[] HORIZONTALS = new Direction[] {
            NORTH, SOUTH, WEST, EAST
    };

    public static Direction rotateAround(Direction dir, Direction.Axis axis) {
        return switch (axis) {
            case X -> dir.getAxis() == Axis.X ? dir : rotateX(dir);
            case Y -> dir.getAxis() == Axis.Y ? dir : dir.getClockWise();
            case Z -> dir.getAxis() == Axis.Z ? dir : rotateZ(dir);
        };
    }

    private static Direction rotateX(Direction dir) {
        return switch (dir) {
            case NORTH -> DOWN;
            case SOUTH -> UP;
            case UP -> NORTH;
            case DOWN -> SOUTH;
            case EAST, WEST -> throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
        };
    }

    private static Direction rotateZ(Direction dir) {
        return switch (dir) {
            case EAST -> DOWN;
            case WEST -> UP;
            case UP -> EAST;
            case DOWN -> WEST;
            case NORTH, SOUTH -> throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
        };
    }

    public static boolean getDirectionBit(int val, Direction dir) {
        return (val & (1 << dir.get3DDataValue())) != 0;
    }

    public static byte setDirectionBit(int val, Direction dir, boolean set) {
        return set ?
                (byte) (val | (1 << dir.get3DDataValue())) :
                (byte) (val & ~(1 << dir.get3DDataValue()));
    }
}
