package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipeGunAmmo extends AbstractRecipe {

    RecipeGunAmmo() {
        super("gun_ammo");
    }

    @Override
    public boolean matches(InventoryCrafting invCrafting, World world) {
        int itemCount = 0;
        boolean foundPotion = false;
        boolean foundAmmo = false;
        for (int i = 0; i < invCrafting.getSizeInventory(); i++) {
            ItemStack stack = invCrafting.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == Items.POTIONITEM) foundPotion = true;
                if (stack.getItem() == Itemss.GUN_AMMO) foundAmmo = true;
            }
        }
        return foundPotion && foundAmmo && itemCount == 2;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting invCrafting) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack ammo = ItemStack.EMPTY;
        for (int i = 0; i < invCrafting.getSizeInventory(); i++) {
            ItemStack stack = invCrafting.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == Items.POTIONITEM) {
                    potion = stack;
                } else {
                    ammo = stack;
                }
            }
        }
        ammo = ammo.copy();
        ItemGunAmmo.setPotion(ammo, potion);
        return ammo;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Itemss.GUN_AMMO);
    }

}
