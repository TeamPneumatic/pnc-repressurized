package pneumaticCraft.common.inventory;

import net.minecraft.inventory.IInventory;
import pneumaticCraft.common.item.Itemss;

public class SlotUpgrade extends SlotItemSpecific{
    public SlotUpgrade(IInventory par2IInventory, int par3, int par4, int par5){
        super(par2IInventory, Itemss.machineUpgrade, par3, par4, par5);
    }
}
