package pneumaticCraft.common.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;

class SlotItemSpecific extends Slot{
    private Item itemAllowed;
    private int oreDictEntry;
    private boolean dye;

    SlotItemSpecific(IInventory par2IInventory, Item itemAllowed, int par3, int par4, int par5){
        super(par2IInventory, par3, par4, par5);
        this.itemAllowed = itemAllowed;
    }

    SlotItemSpecific(IInventory par2IInventory, String oreDictKeyAllowed, int par3, int par4, int par5){
        super(par2IInventory, par3, par4, par5);
        oreDictEntry = OreDictionary.getOreID(oreDictKeyAllowed);
        dye = oreDictKeyAllowed.equals("dye");
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for
     * the armor slots.
     */
    @Override
    public boolean isItemValid(ItemStack par1ItemStack){
        if(itemAllowed != null) {
            Item item = par1ItemStack == null ? null : par1ItemStack.getItem();
            return item == itemAllowed;
        } else {
            int[] ids = OreDictionary.getOreIDs(par1ItemStack);
            for(int id : ids) {
                if(id == oreDictEntry) return true;
                if(dye && TileEntityPlasticMixer.getDyeIndex(par1ItemStack) >= 0) return true;
            }
            return false;
        }
    }

}
