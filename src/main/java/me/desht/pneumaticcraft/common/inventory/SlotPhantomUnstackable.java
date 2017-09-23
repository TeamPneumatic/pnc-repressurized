package me.desht.pneumaticcraft.common.inventory;

import net.minecraftforge.items.IItemHandler;

public class SlotPhantomUnstackable extends SlotPhantom {

    SlotPhantomUnstackable(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

}
