package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.Itemss;

public class ContainerAmadronAddTrade extends ContainerPneumaticBase{
    private final InventoryBasic inv = new InventoryBasic("amadron", true, 2);

    public ContainerAmadronAddTrade(){
        super(null);
        addSlotToContainer(new SlotUntouchable(inv, 0, 10, 90));
        addSlotToContainer(new SlotUntouchable(inv, 1, 86, 90));
    }

    public void setStack(int index, ItemStack stack){
        inv.setInventorySlotContents(index, stack);
    }

    public ItemStack getStack(int index){
        return inv.getStackInSlot(index);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Itemss.amadronTablet;
    }
}
