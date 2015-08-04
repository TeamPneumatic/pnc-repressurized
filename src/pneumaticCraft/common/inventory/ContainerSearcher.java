package pneumaticCraft.common.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pneumaticCraft.client.gui.GuiSearcher;
import pneumaticCraft.common.item.Itemss;

public class ContainerSearcher extends Container{
    /** the list of items in this container */
    public List itemList = new ArrayList();
    private GuiSearcher gui;

    public void init(GuiSearcher gui){
        this.gui = gui;
        for(int i = 0; i < 6; ++i) {
            for(int j = 0; j < 8; ++j) {
                addSlotToContainer(new Slot(gui.getInventory(), i * 8 + j, 8 + j * 18, 52 + i * 18));
            }
        }

        addSlotToContainer(new Slot(gui.getInventory(), 48, 124, 25));
        scrollTo(0.0F);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return player.getCurrentArmor(3) != null && player.getCurrentArmor(3).getItem() == Itemss.pneumaticHelmet;
    }

    /**
     * Updates the gui slots ItemStack's based on scroll position.
     */
    public void scrollTo(float par1){
        int i = itemList.size() / 8 - 6 + 1;
        int j = (int)(par1 * i + 0.5D);

        if(j < 0) {
            j = 0;
        }

        for(int k = 0; k < 6; ++k) {
            for(int l = 0; l < 8; ++l) {
                int i1 = l + (k + j) * 8;

                if(i1 >= 0 && i1 < itemList.size()) {
                    gui.getInventory().setInventorySlotContents(l + k * 8, (ItemStack)itemList.get(i1));
                } else {
                    gui.getInventory().setInventorySlotContents(l + k * 8, (ItemStack)null);
                }
            }
        }
    }

    public boolean hasMoreThan1PageOfItemsInList(){
        return itemList.size() > 48;
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
    public void putStacksInSlots(ItemStack[] p_75131_1_){}

    @Override
    public void putStackInSlot(int par1, ItemStack par2ItemStack){} //override this to do nothing, as NEI tries to place items in this container which makes it crash.

}
