package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerInventorySearcher extends Container{
    public ContainerInventorySearcher(InventoryPlayer inventoryPlayer){
        // Add the player's inventory slots to the container
        for(int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for(int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 48 + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for(int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, 106));
        }
    }

    public void init(IInventory inv){
        addSlotToContainer(new Slot(inv, 0, 80, 23));
    }

    @Override
    protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer){}

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){
        return null;
    }

    @Override
    public void putStackInSlot(int par1, ItemStack par2ItemStack){} //override this to do nothing, as NEI tries to place items in this container which makes it crash.

    @Override
    public void putStacksInSlots(ItemStack[] p_75131_1_){}

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer){
        return true;
    }

}
