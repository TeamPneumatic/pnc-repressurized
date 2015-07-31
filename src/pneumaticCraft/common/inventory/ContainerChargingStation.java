package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;

public class ContainerChargingStation extends ContainerPneumaticBase<TileEntityChargingStation>{

    public ContainerChargingStation(InventoryPlayer inventoryPlayer, TileEntityChargingStation te){
        super(te);

        // add the cannoned slot.
        addSlotToContainer(new Slot(te, 0, 91, 39){
            @Override
            public int getSlotStackLimit(){
                return 1;
            }
        });

        // add the upgrade slots
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                addSlotToContainer(new SlotUpgrade(te, i * 2 + j + 1, 42 + j * 18, 29 + i * 18));
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

        // Add the player's armor slots to the container.
        for(int i = 0; i < 4; i++) {
            addSlotToContainer(new SlotPneumaticArmor(inventoryPlayer.player, inventoryPlayer, inventoryPlayer.getSizeInventory() - 1 - i, 9, 8 + i * 18, i));
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

            if(par2 < 5) {
                if(!mergeItemStack(var5, 5, 41, false)) return null;

                var4.onSlotChange(var5, var3);
            } else {
                if(var5.getItem() == Itemss.machineUpgrade) {
                    if(!mergeItemStack(var5, 1, 5, false)) return null;
                }
                /*
                 * else if(!TileEntityFurnace.isItemFuel(var5)){
                 * 
                 * if(par2 < 28){ if (!this.mergeItemStack(var5, 33, 41, false))
                 * return null; }else{ if (!this.mergeItemStack(var5, 5, 33,
                 * false)) return null; } }
                 */
                else if(!mergeItemStack(var5, 0, 1, false)) return null;
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
