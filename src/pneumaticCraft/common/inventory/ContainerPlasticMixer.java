package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;

public class ContainerPlasticMixer extends ContainerPneumaticBase<TileEntityPlasticMixer>{

    public ContainerPlasticMixer(InventoryPlayer inventoryPlayer, TileEntityPlasticMixer te){
        super(te);

        // add the upgrade slots
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                addSlotToContainer(new SlotUpgrade(te, i * 2 + j, 11 + j * 18, 29 + i * 18));
            }
        }

        addSlotToContainer(new SlotInventoryLimiting(te, TileEntityPlasticMixer.INV_INPUT, 98, 26));
        addSlotToContainer(new SlotInventoryLimiting(te, TileEntityPlasticMixer.INV_OUTPUT, 98, 58));
        for(int i = 0; i < 3; i++) {
            addSlotToContainer(new SlotInventoryLimiting(te, TileEntityPlasticMixer.INV_DYE_RED + i, 127, 22 + i * 18));
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

            if(par2 < 9) {
                if(!mergeItemStack(var5, 9, 45, false)) return null;

                var4.onSlotChange(var5, var3);
            } else {
                for(int i = 0; i < 9; i++) {
                    Slot slot = (Slot)inventorySlots.get(i);
                    if(slot.isItemValid(var5)) {
                        if(!mergeItemStack(var5, i, i + 1, false)) return null;
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

}
