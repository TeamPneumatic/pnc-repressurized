package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.item.ItemDrone;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class DroneUpgradeCrafting extends ShapelessRecipe {
    private NonNullList<Ingredient> ingredients = null;

    public DroneUpgradeCrafting(ResourceLocation idIn) {
        super(idIn, "", new ItemStack(ModItems.DRONE.get()), NonNullList.create());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        if (ingredients == null) {
            ingredients = NonNullList.from(Ingredient.EMPTY,
                    Ingredient.fromItems(ModItems.PRINTED_CIRCUIT_BOARD.get()),
                    Ingredient.fromTag(PneumaticCraftTags.Items.BASIC_DRONES)
            );
        }
        return ingredients;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack basicDrone = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (isBasicDrone(stack)) {
                basicDrone = stack.copy();
                break;
            }
        }
        if (basicDrone.isEmpty()) return ItemStack.EMPTY;
        ItemStack drone = new ItemStack(ModItems.DRONE.get());
        CompoundNBT droneTag = basicDrone.getOrCreateTag();
        drone.setTag(droneTag);
        return drone;
    }

    private boolean isBasicDrone(ItemStack stack) {
        return stack.getItem() instanceof ItemDrone && !((ItemDrone) stack.getItem()).canProgram(stack);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.DRONE_UPGRADE_CRAFTING.get();
    }
}
