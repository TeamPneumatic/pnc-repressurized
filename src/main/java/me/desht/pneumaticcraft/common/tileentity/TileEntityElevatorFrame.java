package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import net.minecraftforge.items.IItemHandler;

public class TileEntityElevatorFrame extends TileEntityBase {
    //TODO redo elevator frames

    public TileEntityElevatorFrame() {
        super(ModTileEntities.ELEVATOR_FRAME.get());
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }
}
