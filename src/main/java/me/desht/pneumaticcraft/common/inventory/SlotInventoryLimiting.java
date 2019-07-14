package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotInventoryLimiting extends SlotItemHandler {

    public SlotInventoryLimiting(IItemHandler handler, int slotIndex, int posX, int posY) {
        super(handler, slotIndex, posX, posY);
    }

    public SlotInventoryLimiting(TileEntityPneumaticBase te, int slotIndex, int posX, int posY) {
        this(te.getInventoryCap(), slotIndex, posX, posY);
    }

}
