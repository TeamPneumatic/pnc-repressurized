package igwmod.gui;

import java.awt.Rectangle;

public class ReservedSpace implements IReservedSpace{
    private final Rectangle reservedSpace;

    public ReservedSpace(Rectangle reservedSpace){
        this.reservedSpace = reservedSpace;
    }

    @Override
    public Rectangle getReservedSpace(){
        return reservedSpace;
    }

}
