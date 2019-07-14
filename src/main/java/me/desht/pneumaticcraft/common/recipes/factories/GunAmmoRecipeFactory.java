package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGunAmmoStandard;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class GunAmmoRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        return new GunAmmoRecipe(RL("gun_ammo"), recipe.getRecipeOutput(), recipe.getIngredients().toArray());
    }

    private class GunAmmoRecipe extends ShapelessOreRecipe {
        GunAmmoRecipe(ResourceLocation group, ItemStack result, Object... recipe) {
            super(group, result, recipe);
        }

        @Override
        public boolean matches(CraftingInventory invCrafting, World world) {
            int itemCount = 0;
            boolean foundPotion = false;
            boolean foundAmmo = false;
            for (int i = 0; i < invCrafting.getSizeInventory(); i++) {
                ItemStack stack = invCrafting.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    itemCount++;
                    if (stack.getItem() instanceof PotionItem) foundPotion = true;
                    if (stack.getItem() == ModItems.GUN_AMMO) foundAmmo = true;
                }
            }
            return foundPotion && foundAmmo && itemCount == 2;
        }

        @Override
        public ItemStack getCraftingResult(CraftingInventory invCrafting) {
            ItemStack potion = ItemStack.EMPTY;
            ItemStack ammo = ItemStack.EMPTY;
            for (int i = 0; i < invCrafting.getSizeInventory(); i++) {
                ItemStack stack = invCrafting.getStackInSlot(i);
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
    }
}
