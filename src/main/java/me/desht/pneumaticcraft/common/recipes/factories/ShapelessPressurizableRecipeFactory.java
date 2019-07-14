package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.item.ItemPressurizable;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ShapelessPressurizableRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        return new ShapelessPressurizableRecipe(RL("shapeless_pressurizable"), recipe.getRecipeOutput(), recipe.getIngredients().toArray());
    }

    public static class ShapelessPressurizableRecipe extends ShapelessOreRecipe {
        public ShapelessPressurizableRecipe(ResourceLocation group, ItemStack result, Object... recipe) {
            super(group, result, recipe);
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull CraftingInventory inv) {
            ItemStack newOutput = this.output.copy();
            int totalAir = 0;
            // Relying on the fact that IPressurizable items use item damage to store air
            // - will probably need to move to NBT in 1.13
            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() instanceof IPressurizable) {
                    totalAir += stack.getMaxDamage() - stack.getItemDamage();
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
