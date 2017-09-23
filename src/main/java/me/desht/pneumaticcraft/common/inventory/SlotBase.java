package me.desht.pneumaticcraft.common.inventory;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class SlotBase extends SlotItemHandler {

    SlotBase(IItemHandler handler, int slotIndex, int posX, int posY) {
        super(handler, slotIndex, posX, posY);
    }

    public boolean canShift() {
        return true;
    }

    // SlotItemHander's implementation isItemValid() should be good enough
//    @Override
//    public boolean isItemValid(ItemStack stack) {
//        return inventory.isItemValidForSlot(getSlotIndex(), stack);
//    }
}
