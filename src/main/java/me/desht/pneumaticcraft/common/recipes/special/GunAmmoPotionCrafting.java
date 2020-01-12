package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.item.ItemGunAmmoStandard;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class GunAmmoPotionCrafting extends SpecialRecipe {
    public GunAmmoPotionCrafting(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        int itemCount = 0;
        boolean foundPotion = false;
        boolean foundAmmo = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (++itemCount > 2) return false;
                itemCount++;
                if (stack.getItem() instanceof PotionItem) foundPotion = true;
                if (stack.getItem() == ModItems.GUN_AMMO.get()) foundAmmo = true;
            }
        }
        return foundPotion && foundAmmo;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack ammo = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof PotionItem) {
                    potion = stack;
                } else {
                    ammo = stack;
                }
            }
        }
        ammo = ammo.copy();
        ItemGunAmmoStandard.setPotion(ammo, potion);
        return ammo;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.GUN_AMMO_POTION_CRAFTING.get();
    }
}
