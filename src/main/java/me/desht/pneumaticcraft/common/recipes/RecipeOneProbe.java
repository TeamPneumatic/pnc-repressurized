package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RecipeOneProbe extends AbstractRecipe {
    public static final String ONE_PROBE_TAG = "theoneprobe";

    RecipeOneProbe() {
        super("oneprobe_helmet");
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean probeFound = false, helmetFound = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            Item item = inv.getStackInSlot(i).getItem();
            if (item == Itemss.PNEUMATIC_HELMET) {
                if (helmetFound) return false;
                helmetFound = true;
            } else if (item == CraftingRegistrator.ONE_PROBE) {
                if (probeFound) return false;
                probeFound = true;
            } else if (item != Items.AIR) {
                return false;
            }
        }
        return probeFound && helmetFound;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack output = getRecipeOutput();
        ItemStack helmet = findHelmet(inv);
        NBTTagCompound tag = helmet.isEmpty() ? new NBTTagCompound() : helmet.hasTagCompound() ? helmet.getTagCompound().copy() : new NBTTagCompound();
        tag.setInteger(ONE_PROBE_TAG, 1);
        output.setTagCompound(tag);
        return output;
    }

    private ItemStack findHelmet(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (inv.getStackInSlot(i).getItem() == Itemss.PNEUMATIC_HELMET) {
                return inv.getStackInSlot(i);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 || height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Itemss.PNEUMATIC_HELMET);
    }
}
