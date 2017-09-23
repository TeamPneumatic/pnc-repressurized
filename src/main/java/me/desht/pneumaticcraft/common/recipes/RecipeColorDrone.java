package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RecipeColorDrone extends AbstractRecipe {

    RecipeColorDrone() {
        super("color_drone");
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        boolean hasDrone = false, hasDye = false;
        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (stack.getItem() == Itemss.DRONE) {
                if (!hasDrone) hasDrone = true;
                else return false;
            } else if (!stack.isEmpty() && TileEntityPlasticMixer.getDyeIndex(stack) >= 0) {
                if (!hasDye) hasDye = true;
                else return false;
            }
        }
        return hasDrone && hasDye;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack drone = ItemStack.EMPTY;
        int dyeIndex = -1;
        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == Itemss.DRONE) {
                    drone = stack.copy();
                } else if (dyeIndex == -1) {
                    dyeIndex = TileEntityPlasticMixer.getDyeIndex(stack);
                }
            }
        }
        NBTTagCompound droneTag = drone.getTagCompound();
        if (droneTag == null) {
            droneTag = new NBTTagCompound();
            drone.setTagCompound(droneTag);
        }
        droneTag.setInteger("color", ItemDye.DYE_COLORS[dyeIndex]);
        return drone;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Itemss.DRONE);
    }

}
