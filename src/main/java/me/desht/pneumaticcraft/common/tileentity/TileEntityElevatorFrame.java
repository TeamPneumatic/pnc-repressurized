package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileEntityElevatorFrame extends TileEntityBase {
    //TODO redo elevator frames

    public TileEntityElevatorFrame() {
        super(ModTileEntityTypes.ELEVATOR_FRAME);
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }
}
