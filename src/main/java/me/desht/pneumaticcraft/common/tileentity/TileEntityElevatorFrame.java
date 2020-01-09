package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileEntityElevatorFrame extends TileEntityBase {
    //TODO redo elevator frames

    public TileEntityElevatorFrame() {
        super(ModTileEntities.ELEVATOR_FRAME);
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }
}
