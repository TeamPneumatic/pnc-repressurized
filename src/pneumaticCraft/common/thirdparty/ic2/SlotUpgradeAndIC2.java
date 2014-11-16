package pneumaticCraft.common.thirdparty.ic2;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.util.PneumaticCraftUtils;

class SlotUpgradeAndIC2 extends Slot{

    SlotUpgradeAndIC2(IInventory par2IInventory, int par3, int par4, int par5){
        super(par2IInventory, par3, par4, par5);
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for
     * the armor slots.
     */
    @Override
    public boolean isItemValid(ItemStack par1ItemStack){
        Item item = par1ItemStack == null ? null : par1ItemStack.getItem();
        return item == Itemss.machineUpgrade || PneumaticCraftUtils.isIC2Upgrade(item);
    }
}
