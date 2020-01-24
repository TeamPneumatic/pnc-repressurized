package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.IHeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import me.desht.pneumaticcraft.common.recipes.MachineRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class HeatFrameCoolingRecipe implements IHeatFrameCoolingRecipe {
    private final ResourceLocation id;
    public final Ingredient input;
    private final int temperature;
    public final ItemStack output;

    public HeatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output) {
        this.id = id;
        this.input = input;
        this.temperature = temperature;
        this.output = output;
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    public int getInputAmount() {
        return input.getMatchingStacks().length > 0 ? input.getMatchingStacks()[0].getCount() : 0;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ResourceLocation getRecipeType() {
        return MachineRecipeHandler.Category.HEAT_FRAME_COOLING.getId();
    }

    public static class Serializer extends AbstractRecipeSerializer<HeatFrameCoolingRecipe> {
        @Override
        public HeatFrameCoolingRecipe read(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.deserialize(json.get("input"));
            ItemStack result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            int maxTemp = JSONUtils.getInt(json,"max_temp", 273);
            return new HeatFrameCoolingRecipe(recipeId, input, maxTemp, result);
        }

        @Nullable
        @Override
        public HeatFrameCoolingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient input = Ingredient.read(buffer);
            int temperature = buffer.readInt();
            ItemStack out = buffer.readItemStack();
            return new HeatFrameCoolingRecipe(recipeId, input, temperature, out);
        }

        @Override
        public void write(PacketBuffer buffer, HeatFrameCoolingRecipe recipe) {
            super.write(buffer, recipe);

            recipe.input.write(buffer);
            buffer.writeInt(recipe.temperature);
            buffer.writeItemStack(recipe.output);
        }
    }
}
