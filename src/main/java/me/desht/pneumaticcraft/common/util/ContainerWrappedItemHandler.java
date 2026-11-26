package me.desht.pneumaticcraft.common.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Borrowed from 1.20.4 & earlier, since RecipeWrapper no longer implements Container...
 */
public class ContainerWrappedItemHandler implements Container {
    protected final ResourceHandler<ItemResource> inv;

    public ContainerWrappedItemHandler(ResourceHandler<ItemResource> inv) {
        this.inv = inv;
    }

    @Override
    public int getContainerSize() {
        return inv.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        return IOHelper.getStack(inv, slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = IOHelper.getStack(inv, slot).copy();
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = stack.split(count);
        setItem(slot, stack);
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        IOHelper.setStack(inv, slot, stack);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack s = getItem(index);
        if (s.isEmpty()) return ItemStack.EMPTY;
        setItem(index, ItemStack.EMPTY);
        return s;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inv.size(); i++) {
            if (!IOHelper.getStack(inv, i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return inv.isValid(slot, ItemResource.of(stack));
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inv.size(); i++) {
            IOHelper.setStack(inv, i, ItemStack.EMPTY);
        }
    }

    //The following methods are never used by vanilla in crafting.  They are defunct as mods need not override them.
    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
