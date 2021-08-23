package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ExplosionCraftingRecipeBuilder extends PneumaticCraftRecipeBuilder<ExplosionCraftingRecipeBuilder> {
    private final Ingredient input;
    private final int lossRate;
    private final ItemStack[] outputs;

    public ExplosionCraftingRecipeBuilder(Ingredient input, int lossRate, ItemStack... outputs) {
        super(RL(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING));

        this.input = input;
        this.lossRate = lossRate;
        this.outputs = outputs;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new ExplosionCraftingRecipeResult(id);
    }

    public class ExplosionCraftingRecipeResult extends RecipeResult {
        ExplosionCraftingRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            json.add("results", SerializerHelper.serializeItemStacks(outputs));
            json.addProperty("loss_rate", lossRate);
        }
    }
}
