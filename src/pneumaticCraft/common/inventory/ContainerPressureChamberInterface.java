package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberInterface;

public class ContainerPressureChamberInterface extends ContainerPneumaticBase<TileEntityPressureChamberInterface>{

    public ContainerPressureChamberInterface(InventoryPlayer inventoryPlayer, TileEntityPressureChamberInterface te){
        super(te);

        // add the transfer slot.
        addSlotToContainer(new SlotUntouchable(te, 0, 66, 35));

        // add the upgrade slots
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                addSlotToContainer(new SlotUpgrade(te, i * 2 + j + 1, 20 + j * 18, 26 + i * 18));
            }
        }
        // add the export filter slots
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                addSlotToContainer(new SlotPhantomUnstackable(te, i * 3 + j + 5, 115 + j * 18, 25 + i * 18));
            }
        }

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

    /**
     * @param itemStack
     *            ItemStack to merge into inventory
     * @param start
     *            minimum slot to attempt fill
     * @param end
     *            maximum slot to attempt fill
     * @param backwards
     *            go backwards
     * @return true if stacks merged successfully public boolean
     *         mergeItemStack(itemStack, start, end, backwards)
     */

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){

        ItemStack var3 = null;
        Slot var4 = (Slot)inventorySlots.get(par2);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(par2 < 14) {
                if(!mergeItemStack(var5, 14, 50, false)) return null;
                var4.onSlotChange(var5, var3);
            } else {
                if(var3.getItem() == Itemss.machineUpgrade) {
                    if(!mergeItemStack(var5, 1, 5, false)) return null;
                } else {
                    if(par2 < 41) {
                        if(!mergeItemStack(var5, 41, 50, false)) return null;
                    } else {
                        if(!mergeItemStack(var5, 14, 41, false)) return null;
                    }
                }
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

    /**
     * Source: Buildcraft
     */
    @Override
    public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player){
        Slot slot = slotNum < 0 ? null : (Slot)inventorySlots.get(slotNum);
        if(slot instanceof IPhantomSlot) {
            return slotClickPhantom(slot, mouseButton, modifier, player);
        }
        return super.slotClick(slotNum, mouseButton, modifier, player);
    }

    private ItemStack slotClickPhantom(Slot slot, int mouseButton, int modifier, EntityPlayer player){
        ItemStack stack = null;

        if(mouseButton == 2) {
            if(((IPhantomSlot)slot).canAdjust()) {
                slot.putStack(null);
            }
        } else if(mouseButton == 0 || mouseButton == 1) {
            InventoryPlayer playerInv = player.inventory;
            slot.onSlotChanged();
            ItemStack stackSlot = slot.getStack();
            ItemStack stackHeld = playerInv.getItemStack();

            if(stackSlot != null) {
                stack = stackSlot.copy();
            }

            if(stackSlot == null) {
                if(stackHeld != null && slot.isItemValid(stackHeld)) {
                    fillPhantomSlot(slot, stackHeld, mouseButton, modifier);
                }
            } else if(stackHeld == null) {
                adjustPhantomSlot(slot, mouseButton, modifier);
                slot.onPickupFromSlot(player, playerInv.getItemStack());
            } else if(slot.isItemValid(stackHeld)) {
                if(canStacksMerge(stackSlot, stackHeld)) {
                    adjustPhantomSlot(slot, mouseButton, modifier);
                } else {
                    fillPhantomSlot(slot, stackHeld, mouseButton, modifier);
                }
            }
        }
        return stack;
    }

    public boolean canStacksMerge(ItemStack stack1, ItemStack stack2){
        if(stack1 == null || stack2 == null) return false;
        if(!stack1.isItemEqual(stack2)) return false;
        if(!ItemStack.areItemStackTagsEqual(stack1, stack2)) return false;
        return true;

    }

    protected void adjustPhantomSlot(Slot slot, int mouseButton, int modifier){
        if(!((IPhantomSlot)slot).canAdjust()) {
            return;
        }
        ItemStack stackSlot = slot.getStack();
        int stackSize;
        if(modifier == 1) {
            stackSize = mouseButton == 0 ? (stackSlot.stackSize + 1) / 2 : stackSlot.stackSize * 2;
        } else {
            stackSize = mouseButton == 0 ? stackSlot.stackSize - 1 : stackSlot.stackSize + 1;
        }

        if(stackSize > slot.getSlotStackLimit()) {
            stackSize = slot.getSlotStackLimit();
        }

        stackSlot.stackSize = stackSize;

        if(stackSlot.stackSize <= 0) {
            slot.putStack((ItemStack)null);
        }
    }

    protected void fillPhantomSlot(Slot slot, ItemStack stackHeld, int mouseButton, int modifier){
        if(!((IPhantomSlot)slot).canAdjust()) {
            return;
        }
        int stackSize = mouseButton == 0 ? stackHeld.stackSize : 1;
        if(stackSize > slot.getSlotStackLimit()) {
            stackSize = slot.getSlotStackLimit();
        }
        ItemStack phantomStack = stackHeld.copy();
        phantomStack.stackSize = stackSize;

        slot.putStack(phantomStack);
    }
}
