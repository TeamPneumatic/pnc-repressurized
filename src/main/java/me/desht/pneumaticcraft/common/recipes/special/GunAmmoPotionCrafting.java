package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.item.ItemGunAmmoStandard;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class GunAmmoPotionCrafting extends ShapelessRecipe {
    public GunAmmoPotionCrafting(ResourceLocation idIn) {
        super(idIn, "", new ItemStack(ModItems.GUN_AMMO.get()),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromItems(ModItems.GUN_AMMO.get()), new PotionIngredient()));
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack ammo = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof PotionItem) {
                potion = stack;
            } else if (stack.getItem() == ModItems.GUN_AMMO.get()) {
                ammo = stack;
            } else if (!stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        if (ammo.isEmpty() || potion.isEmpty()) return ItemStack.EMPTY;

        ammo = ammo.copy();
        ItemGunAmmoStandard.setPotion(ammo, potion);
        return ammo;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.GUN_AMMO_POTION_CRAFTING.get();
    }

    private static class PotionIngredient extends Ingredient {
        PotionIngredient() {
            super(Stream.empty());
        }

        @Override
        public ItemStack[] getMatchingStacks() {
            NonNullList<ItemStack> potions = NonNullList.create();
            for (Potion p : ForgeRegistries.POTION_TYPES.getValues()) {
                if (p != Potions.EMPTY) potions.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), p));
            }

            return potions.toArray(new ItemStack[0]);
        }

        @Override
        public boolean test(@Nullable ItemStack stack) {
            return !PotionUtils.getEffectsFromStack(stack).isEmpty();
        }
    }
}
