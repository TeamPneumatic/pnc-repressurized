package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.util.Direction;

public interface ISidedWidget {
    void setSides(boolean[] sides);

    boolean[] getSides();

    default boolean isSideSelected(Direction side) {
        return getSides()[side.getIndex()];
    }
}
