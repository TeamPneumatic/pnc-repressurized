package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RecipeLogisticToDrone extends AbstractRecipe {

    RecipeLogisticToDrone() {
        super("logistic_to_drone");
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        boolean hasDrone = false, hasPCB = false;
        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (stack.getItem() == Itemss.LOGISTICS_DRONE) {
                if (!hasDrone) hasDrone = true;
                else return false;
            } else if (stack.getItem() == Itemss.PRINTED_CIRCUIT_BOARD) {
                if (!hasPCB) hasPCB = true;
                else return false;
            }
        }
        return hasDrone && hasPCB;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack logisticDrone = ItemStack.EMPTY;
        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (stack.getItem() == Itemss.LOGISTICS_DRONE) {
                logisticDrone = stack.copy();
                break;
            }
        }
        ItemStack drone = new ItemStack(Itemss.DRONE);
        NBTTagCompound droneTag = logisticDrone.getTagCompound();
        if (droneTag == null) {
            droneTag = new NBTTagCompound();
            logisticDrone.setTagCompound(droneTag);
        }
        drone.setTagCompound(droneTag);
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
