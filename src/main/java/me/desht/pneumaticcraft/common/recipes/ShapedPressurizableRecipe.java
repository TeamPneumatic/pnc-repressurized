package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.capabilities.CapabilityAirHandler;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Just like a regular shaped recipe, but any air in any input ingredients is added up and put into the output item.
 */
public class ShapedPressurizableRecipe extends ShapedRecipe {
    private ShapedPressurizableRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack newOutput = this.getRecipeOutput().copy();
        int totalAir = 0;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            totalAir += stack.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY).map(IAirHandler::getAir).orElse(0);
        }

        final int toAdd = totalAir;
        newOutput.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> h.addAir(toAdd));

        return newOutput;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe r = super.read(recipeId, json);
            return new ShapedPressurizableRecipe(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getRecipeOutput());
        }

        @Nullable
        @Override
        public ShapedRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            ShapedRecipe r = super.read(recipeId, buffer);
            return new ShapedPressurizableRecipe(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getRecipeOutput());
        }

        @Override
        public void write(PacketBuffer buffer, ShapedRecipe recipe) {
            super.write(buffer, recipe);
        }
    }
}
