package igwmod.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * This class is derived from Vanilla's ContainerCreative class.
 */
class ContainerBlockWiki extends Container{

    public void updateStacks(List<LocatedStack> stacks, List<IPageLink> pageLinks){
        int invSize = 0;
        for(LocatedStack stack : stacks)
            if(stack.y >= GuiWiki.MIN_TEXT_Y * GuiWiki.TEXT_SCALE && 16 + stack.y <= GuiWiki.MAX_TEXT_Y * GuiWiki.TEXT_SCALE) invSize++;
        for(IPageLink link : pageLinks)
            if(link instanceof LocatedStack) invSize++;
        InventoryBasic inventory = new InventoryBasic("tmp", true, invSize);
        inventorySlots = new ArrayList();
        inventoryItemStacks = NonNullList.create();
        int curSlot = 0;
        for(LocatedStack stack : stacks) {
            if(stack.y >= GuiWiki.MIN_TEXT_Y * GuiWiki.TEXT_SCALE && 16 + stack.y <= GuiWiki.MAX_TEXT_Y * GuiWiki.TEXT_SCALE) {
                addSlotToContainer(new Slot(inventory, curSlot, stack.x, stack.y){
                    @Override
                    public boolean isItemValid(ItemStack par1ItemStack){
                        return false;
                    }
                });                
                if(stack.stack.getItemDamage() == 32767)//TODO better way to handle wildcard value.
                {
                    stack.stack.setItemDamage(0);
                }
                inventory.setInventorySlotContents(curSlot++, stack.stack);
            }
        }
        for(IPageLink pageLink : pageLinks) {
            if(pageLink instanceof LocatedStack) {
                LocatedStack stack = (LocatedStack)pageLink;
                addSlotToContainer(new Slot(inventory, curSlot, stack.x, stack.y){
                    @Override
                    public boolean isItemValid(ItemStack par1ItemStack){
                        return false;
                    }
                });
                inventory.setInventorySlotContents(curSlot++, stack.stack);
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer){
        return true;
    }

    
    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){
        return null;
    }

    @Override
    public void putStackInSlot(int par1, ItemStack par2ItemStack){} //override this to do nothing, as NEI tries to place items in this container which makes it crash.
}
