package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.item.ItemPressurizable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

/**
 * A recipe factory where ingredients may be pressurizable; add the air from any such ingredients to the
 * output item.
 */
public class ShapedPressurizableRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getWidth();
        primer.height = recipe.getHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new ShapedPressurizableRecipe(RL("shaped_pressurizable"), recipe.getRecipeOutput(), primer);
    }

    public static class ShapedPressurizableRecipe extends ShapedOreRecipe {
        ShapedPressurizableRecipe(ResourceLocation group, ItemStack recipeOutput, ShapedPrimer primer) {
            super(group, recipeOutput, primer);
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
            ItemStack newOutput = this.output.copy();
            int totalAir = 0;
            // Relying on the fact that IPressurizable items use item damage to store air
            // - will probably need to move to NBT in 1.13
            for (int i = 0; i < var1.getSizeInventory(); ++i) {
                ItemStack stack = var1.getStackInSlot(i);
                if (stack.getItem() instanceof IPressurizable) {
                    totalAir += stack.getItem().getMaxDamage() - stack.getItem().getDamage(stack);
                }
            }

            if (newOutput.getItem() instanceof ItemPressurizable) {
                newOutput.getItem().setDamage(newOutput, newOutput.getItem().getMaxDamage(newOutput));
            }
            if (newOutput.getItem() instanceof IPressurizable) {
                ((IPressurizable) newOutput.getItem()).addAir(newOutput, totalAir);
            }

            return newOutput;
        }
    }
}
