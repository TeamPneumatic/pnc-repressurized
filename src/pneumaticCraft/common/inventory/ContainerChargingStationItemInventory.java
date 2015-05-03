package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;

public class ContainerChargingStationItemInventory extends ContainerPneumaticBase<TileEntityChargingStation>{

    public InventoryPneumaticInventoryItem armor;

    public ContainerChargingStationItemInventory(InventoryPlayer inventoryPlayer, TileEntityChargingStation te){
        super(te);
        if(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX) == null) throw new IllegalArgumentException("instanciating ContainerPneumaticArmor with a charge item being null!");
        armor = te.getChargeableInventory();

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                addSlotToContainer(new SlotUpgrade(armor, i * 3 + j, 31 + j * 18, 24 + i * 18));
            }
        }

        // addSlotToContainer(new Slot(teChargingStation, 0, 91, 39));

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

            if(par2 < 9 || par2 >= 44) {
                if(!mergeItemStack(var5, 9, 44, false)) return null;
                var4.onSlotChange(var5, var3);
            } else {
                if(var3.getItem() instanceof ItemArmor && !((Slot)inventorySlots.get(45 + ((ItemArmor)var3.getItem()).armorType)).getHasStack()) {
                    int j = 45 + ((ItemArmor)var3.getItem()).armorType;

                    if(!mergeItemStack(var5, j, j + 1, false)) {
                        return null;
                    }
                } else if(var3.getItem() == Itemss.machineUpgrade) {
                    if(!mergeItemStack(var5, 0, 9, false)) return null;
                }

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
