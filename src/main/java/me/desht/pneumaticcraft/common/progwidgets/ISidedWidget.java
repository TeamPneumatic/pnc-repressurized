package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.util.Direction;

public interface ISidedWidget {
    void setSides(boolean[] sides);

    boolean[] getSides();

    static boolean checkSide(ProgWidget progWidget, Direction side) {
        return progWidget instanceof ISidedWidget && ((ISidedWidget) progWidget).getSides()[side.getIndex()];
    }
}
