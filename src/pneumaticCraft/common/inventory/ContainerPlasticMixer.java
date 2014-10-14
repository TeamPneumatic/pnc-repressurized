package pneumaticCraft.common.inventory;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerPlasticMixer extends Container{
    TileEntityPlasticMixer te;
    private final int[] lastTemperatures = {-1, -1, -1};

    public ContainerPlasticMixer(InventoryPlayer inventoryPlayer, TileEntityPlasticMixer te){
        this.te = te;
        te.openGUI();

        // add the upgrade slots
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                addSlotToContainer(new SlotUpgrade(te, i * 2 + j, 11 + j * 18, 29 + i * 18));
            }
        }

        addSlotToContainer(new SlotItemSpecific(te, Itemss.plastic, 4, 98, 58));
        addSlotToContainer(new SlotItemSpecific(te, Items.dye, 5, 98, 38));//TODO ore dict

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
    public boolean canInteractWith(EntityPlayer player){

        return te.isGuiUseableByPlayer(player);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();

        for(int i = 0; i < 3; i++) {
            if(lastTemperatures[i] != te.getTemperature(i)) {
                lastTemperatures[i] = te.getTemperature(i);
                for(ICrafting crafter : (List<ICrafting>)crafters) {
                    crafter.sendProgressBarUpdate(this, i, te.getTemperature(i));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value){
        super.updateProgressBar(id, value);
        te.setTemperature(value, id);
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

            if(par2 < 6) {
                if(!mergeItemStack(var5, 6, 42, false)) return null;

                var4.onSlotChange(var5, var3);
            } else {
                if(var5.getItem() == Itemss.machineUpgrade) {
                    if(!mergeItemStack(var5, 0, 4, false)) return null;
                } else if(var5.getItem() == Items.dye && !mergeItemStack(var5, 5, 6, false)) return null;
                else if(var5.getItem() == Itemss.plastic && !mergeItemStack(var5, 4, 5, false)) return null;
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

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer){
        super.onContainerClosed(par1EntityPlayer);
        te.closeGUI();
    }
}
