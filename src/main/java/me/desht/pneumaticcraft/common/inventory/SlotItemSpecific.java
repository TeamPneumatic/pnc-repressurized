package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

class SlotItemSpecific extends SlotItemHandler {
    private Item itemAllowed;
    private int oreDictEntry;
    private boolean dye;

    SlotItemSpecific(IItemHandler handler, Item itemAllowed, int index, int x, int y) {
        super(handler, index, x, y);
        this.itemAllowed = itemAllowed;
    }

    SlotItemSpecific(IItemHandler handler, String oreDictKeyAllowed, int index, int x, int y) {
        super(handler, index, x, y);
        oreDictEntry = OreDictionary.getOreID(oreDictKeyAllowed);
        dye = oreDictKeyAllowed.equals("dye");
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for
     * the armor slots.
     */
    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        if (itemAllowed != null) {
            Item item = stack.isEmpty() ? null : stack.getItem();
            return item == itemAllowed;
        } else {
            int[] ids = OreDictionary.getOreIDs(stack);
            for (int id : ids) {
                if (id == oreDictEntry) return true;
                if (dye && TileEntityPlasticMixer.getDyeIndex(stack) >= 0) return true;
            }
            return false;
        }
    }

}
