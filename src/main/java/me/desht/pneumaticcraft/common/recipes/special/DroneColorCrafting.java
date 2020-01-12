package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.item.ItemDrone;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class DroneColorCrafting extends SpecialRecipe {
    public DroneColorCrafting(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        boolean hasDrone = false, hasDye = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof ItemDrone) {
                if (!hasDrone) hasDrone = true;
                else return false;
            } else if (stack.getItem() instanceof DyeItem) {
                if (!hasDye) hasDye = true;
                else return false;
            } else if (!stack.isEmpty()) {
                return false;
            }
        }
        return hasDrone && hasDye;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack drone = ItemStack.EMPTY;
        DyeColor dyeColor = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemDrone) {
                    drone = stack.copy();
                } else if (dyeColor == null) {
                    dyeColor = ((DyeItem) stack.getItem()).getDyeColor();
                }
            }
        }
        CompoundNBT droneTag = drone.getTag();
        if (droneTag == null) {
            droneTag = new CompoundNBT();
            drone.setTag(droneTag);
        }
        droneTag.putInt("color", dyeColor.getId());
        return drone;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.DRONE_COLOR_CRAFTING.get();
    }
}
