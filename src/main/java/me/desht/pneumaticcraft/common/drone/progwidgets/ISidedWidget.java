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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.Direction;

public interface ISidedWidget {
    boolean[] ALL_SIDES = new boolean[] { true, true, true, true, true, true };

    void setSides(boolean[] sides);

    boolean[] getSides();

    default boolean isSideSelected(Direction side) {
        return getSides()[side.get3DDataValue()];
    }

    default void setSideSelected(Direction side, boolean set) {
        boolean[] sides = getSides();
        sides[side.get3DDataValue()] = set;
        setSides(sides);
    }

    static Direction getDirForSides(boolean[] sides) {
        for (int i = 0; i < sides.length; i++) {
            if (sides[i]) {
                return Direction.from3DDataValue(i);
            }
        }
        Log.error("[ISidedWidget] sides array contains all false values (default: down) !");
        return Direction.DOWN;
    }

    static boolean[] getSidesFromDir(Direction dir) {
        boolean[] dirs = new boolean[6];
        dirs[dir.ordinal()] = true;
        return dirs;
    }
}
