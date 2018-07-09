package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateSearchStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class InventorySearch implements IInventory {

    private final ItemStack helmetStack;

    public InventorySearch(EntityPlayer player) {
        helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getStackInSlot(0).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return ItemPneumaticArmor.getSearchedStack(helmetStack);
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int i, int j) {
        return null;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int i) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, @Nonnull ItemStack itemstack) {
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, "SearchStack");
        tag.setInteger("itemID", itemstack.isEmpty() ? -1 : Item.getIdFromItem(itemstack.getItem()));
        tag.setInteger("itemDamage", itemstack.isEmpty() ? -1 : itemstack.getItemDamage());
        NetworkHandler.sendToServer(new PacketUpdateSearchStack(itemstack));
    }

    @Override
    public String getName() {
        return "Inventory Search";
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

}
