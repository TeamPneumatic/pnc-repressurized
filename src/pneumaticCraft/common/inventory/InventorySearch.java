package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdateSearchStack;

public class InventorySearch implements IInventory{

    private final ItemStack helmetStack;

    public InventorySearch(EntityPlayer player){
        helmetStack = player.getCurrentArmor(3);
    }

    @Override
    public int getSizeInventory(){
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i){
        return ItemPneumaticArmor.getSearchedStack(helmetStack);
    }

    @Override
    public ItemStack decrStackSize(int i, int j){
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i){
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack){
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, "SearchStack");
        tag.setInteger("itemID", itemstack != null ? Item.getIdFromItem(itemstack.getItem()) : -1);
        tag.setInteger("itemDamage", itemstack != null ? itemstack.getItemDamage() : -1);
        NetworkHandler.sendToServer(new PacketUpdateSearchStack(itemstack));
    }

    @Override
    public String getInventoryName(){
        return "Inventory Search";
    }

    @Override
    public int getInventoryStackLimit(){
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer){
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return false;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return true;
    }

    @Override
    public void markDirty(){}

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

}
