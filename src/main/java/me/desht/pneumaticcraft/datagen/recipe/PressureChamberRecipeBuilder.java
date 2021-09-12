package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PressureChamberRecipeBuilder extends PneumaticCraftRecipeBuilder<PressureChamberRecipeBuilder> {
    private final List<Ingredient> inputs;
    private final float requiredPressure;
    private final ItemStack[] outputs;

    public PressureChamberRecipeBuilder(List<Ingredient> inputs, float requiredPressure, ItemStack... outputs) {
        super(RL(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER));

        this.inputs = inputs;
        this.requiredPressure = requiredPressure;
        this.outputs = outputs;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new PressureChamberRecipeResult(id);
    }

    public class PressureChamberRecipeResult extends RecipeResult {
        PressureChamberRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray in = new JsonArray();
            for (Ingredient ingr : inputs) {
                in.add(ingr.toJson());
            }
            json.add("inputs", in);
            json.addProperty("pressure", requiredPressure);
            json.add("results", SerializerHelper.serializeItemStacks(outputs));
        }
    }
}
