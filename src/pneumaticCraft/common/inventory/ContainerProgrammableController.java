package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;

public class ContainerProgrammableController extends ContainerPneumaticBase<TileEntityProgrammableController>{

    public ContainerProgrammableController(InventoryPlayer inventoryPlayer, final TileEntityProgrammableController te){
        super(te);
        // add the upgrade slots
        addSlotToContainer(new Slot(te, 0, 10, 15){//326
            @Override
            public boolean isItemValid(ItemStack stack){
                return te.isItemValidForSlot(0, stack);
            }
        });

        // Add the player's inventory slots to the container
        for(int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for(int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 84 + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for(int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, 142));
        }
    }

    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();

    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){

        ItemStack var3 = null;
        Slot var4 = (Slot)inventorySlots.get(par2);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(par2 == 0) {
                if(!mergeItemStack(var5, 1, 36, false)) return null;
                var4.onSlotChange(var5, var3);
            } else if(te.isItemValidForSlot(0, var3)) {
                if(!mergeItemStack(var5, 0, 1, false)) return null;
                var4.onSlotChange(var5, var3);
            }
            if(var5.stackSize == 0) {
                var4.putStack((ItemStack)null);
            } else {
                var4.onSlotChanged();
            }

            if(var5.stackSize == var3.stackSize) return null;

            var4.onPickupFromSlot(par1EntityPlayer, var5);
        }

        return var3;
    }

}
