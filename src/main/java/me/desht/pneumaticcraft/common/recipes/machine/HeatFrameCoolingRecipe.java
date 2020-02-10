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
    private final float bonusMultiplier;
    private final float bonusLimit;

    public HeatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output) {
        this(id, input, temperature, output, 0f, 1f);
    }

    public HeatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit) {
        this.id = id;
        this.input = input;
        this.temperature = temperature;
        this.output = output;
        this.bonusMultiplier = bonusMultiplier;
        this.bonusLimit = bonusLimit;
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
    public int getThresholdTemperature() {
        return temperature;
    }

    @Override
    public float getBonusMultiplier() {
        return bonusMultiplier;
    }

    @Override
    public float getBonusLimit() {
        return bonusLimit;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack);
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
            float bonusMultiplier = 0f;
            float bonusLimit = 1f;
            if (json.has("bonus_output")) {
                JsonObject bonus = json.getAsJsonObject("bonus_output");
                bonusMultiplier = JSONUtils.getFloat(bonus, "multiplier");
                bonusLimit = JSONUtils.getFloat(bonus, "limit");
            }
            return new HeatFrameCoolingRecipe(recipeId, input, maxTemp, result, bonusMultiplier, bonusLimit);
        }

        @Nullable
        @Override
        public HeatFrameCoolingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient input = Ingredient.read(buffer);
            int temperature = buffer.readInt();
            ItemStack out = buffer.readItemStack();
            float bonusMultiplier = buffer.readFloat();
            float bonusLimit = buffer.readFloat();
            return new HeatFrameCoolingRecipe(recipeId, input, temperature, out, bonusMultiplier, bonusLimit);
        }

        @Override
        public void write(PacketBuffer buffer, HeatFrameCoolingRecipe recipe) {
            super.write(buffer, recipe);

            recipe.input.write(buffer);
            buffer.writeInt(recipe.temperature);
            buffer.writeItemStack(recipe.output);
            buffer.writeFloat(recipe.bonusMultiplier);
            buffer.writeFloat(recipe.bonusLimit);
        }
    }
}
