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

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.*;

import static net.minecraft.util.Direction.*;

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
        switch (axis) {
            case X:
                return dir.getAxis() == Axis.X ? dir : rotateX(dir);
            case Y:
                return dir.getAxis() == Axis.Y ? dir : dir.getClockWise();
            case Z:
                return dir.getAxis() == Axis.Z ? dir : rotateZ(dir);
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    private static Direction rotateX(Direction dir) {
        switch (dir) {
            case NORTH:
                return DOWN;
            case SOUTH:
                return UP;
            case UP:
                return NORTH;
            case DOWN:
                return SOUTH;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
        }
    }

    private static Direction rotateZ(Direction dir) {
        switch (dir) {
            case EAST:
                return DOWN;
            case WEST:
                return UP;
            case UP:
                return EAST;
            case DOWN:
                return WEST;
            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
        }
    }
}
