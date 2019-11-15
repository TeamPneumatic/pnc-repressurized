package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.util.Direction;

public interface ISidedWidget {
    void setSides(boolean[] sides);

    boolean[] getSides();

    static boolean checkSide(ISidedWidget progWidget, Direction side) {
        return progWidget.getSides()[side.getIndex()];
    }
}
