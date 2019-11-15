package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.item.ItemBasicDrone;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class DroneUpgradeCrafting extends SpecialRecipe {
    public DroneUpgradeCrafting(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        boolean hasDrone = false, hasPCB = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBasicDrone) {
                if (!hasDrone) hasDrone = true;
                else return false;
            } else if (stack.getItem() == ModItems.PRINTED_CIRCUIT_BOARD) {
                if (!hasPCB) hasPCB = true;
                else return false;
            }
        }
        return hasDrone && hasPCB;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack basicDrone = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBasicDrone) {
                basicDrone = stack.copy();
                break;
            }
        }
        ItemStack drone = new ItemStack(ModItems.DRONE);
        CompoundNBT droneTag = basicDrone.getTag();
        if (droneTag == null) {
            droneTag = new CompoundNBT();
            basicDrone.setTag(droneTag);
        }
        drone.setTag(droneTag);
        return drone;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.DRONE_UPGRADE_CRAFTING;
    }
}
