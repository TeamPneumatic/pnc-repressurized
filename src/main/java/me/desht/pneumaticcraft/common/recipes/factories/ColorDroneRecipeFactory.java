package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.item.ItemDrone;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.DyeUtils;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ColorDroneRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        return new ColorDroneRecipe(RL("color_drone"), recipe.getRecipeOutput(), recipe.getIngredients().toArray());
    }

    private class ColorDroneRecipe extends ShapelessOreRecipe {
        ColorDroneRecipe(ResourceLocation group, ItemStack result, Object... recipe) {
            super(group, result, recipe);
        }

        @Override
        public boolean matches(CraftingInventory inv, World world) {
            boolean hasDrone = false, hasDye = false;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() instanceof ItemDrone) {
                    if (!hasDrone) hasDrone = true;
                    else return false;
                } else if (!stack.isEmpty() && DyeUtils.rawDyeDamageFromStack(stack) >= 0) {
                    if (!hasDye) hasDye = true;
                    else return false;
                }
            }
            return hasDrone && hasDye;
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(CraftingInventory inv) {
            ItemStack drone = ItemStack.EMPTY;
            int dyeIndex = -1;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof ItemDrone) {
                        drone = stack.copy();
                    } else if (dyeIndex == -1) {
                        dyeIndex = DyeUtils.rawDyeDamageFromStack(stack);
                    }
                }
            }
            CompoundNBT droneTag = drone.getTagCompound();
            if (droneTag == null) {
                droneTag = new CompoundNBT();
                drone.setTagCompound(droneTag);
            }
            droneTag.setInteger("color", DyeItem.DYE_COLORS[dyeIndex]);
            return drone;
        }
    }
}
