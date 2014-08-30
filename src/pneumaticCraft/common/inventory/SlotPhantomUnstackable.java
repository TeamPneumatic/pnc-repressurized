package pneumaticCraft.common.inventory;

import net.minecraft.inventory.IInventory;

public class SlotPhantomUnstackable extends SlotPhantom{

    SlotPhantomUnstackable(IInventory par2iInventory, int par3, int par4, int par5){
        super(par2iInventory, par3, par4, par5);
    }

    @Override
    public int getSlotStackLimit(){
        return 1;
    }

}
