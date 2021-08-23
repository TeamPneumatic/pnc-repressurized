package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.util.Direction;

public interface ISidedWidget {
    boolean[] ALL_SIDES = new boolean[] { true, true, true, true, true, true };

    void setSides(boolean[] sides);

    boolean[] getSides();

    default boolean isSideSelected(Direction side) {
        return getSides()[side.get3DDataValue()];
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
