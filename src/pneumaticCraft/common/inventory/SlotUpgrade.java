package pneumaticCraft.common.inventory;

import net.minecraft.inventory.IInventory;
import pneumaticCraft.common.item.Itemss;

class SlotUpgrade extends SlotItemSpecific{
    SlotUpgrade(IInventory par2IInventory, int par3, int par4, int par5){
        super(par2IInventory, Itemss.machineUpgrade, par3, par4, par5);
    }
}
