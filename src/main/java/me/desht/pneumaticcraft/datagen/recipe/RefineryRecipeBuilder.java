package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class RefineryRecipeBuilder extends PneumaticCraftRecipeBuilder<RefineryRecipeBuilder> {
    private final FluidIngredient input;
    private final TemperatureRange operatingTemp;
    private final FluidStack[] outputs;

    public RefineryRecipeBuilder(FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs) {
        super(RL(PneumaticCraftRecipeTypes.REFINERY));
        this.input = input;
        this.operatingTemp = operatingTemp;
        this.outputs = outputs;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new RefineryRecipeResult(id);
    }

    public class RefineryRecipeResult extends RecipeResult {
        RefineryRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            json.add("temperature", operatingTemp.toJson());
            JsonArray o = new JsonArray();
            for (FluidStack f : outputs) {
                o.add(ModCraftingHelper.fluidStackToJson(f));
            }
            json.add("results", o);
        }
    }
}
