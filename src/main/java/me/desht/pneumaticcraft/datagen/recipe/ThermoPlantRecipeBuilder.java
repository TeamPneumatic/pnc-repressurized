package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ThermoPlantRecipeBuilder extends PneumaticCraftRecipeBuilder<ThermoPlantRecipeBuilder> {
    private final FluidIngredient inputFluid;
    @Nullable
    private final Ingredient inputItem;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final TemperatureRange operatingTemperature;
    private final float requiredPressure;
    private final float recipeSpeed;
    private final boolean exothermic;

    public ThermoPlantRecipeBuilder(FluidIngredient inputFluid, @Nullable Ingredient inputItem,
                                    FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                                    float recipeSpeed, boolean exothermic) {
        super(RL(PneumaticCraftRecipeTypes.THERMO_PLANT));

        this.inputFluid = inputFluid;
        this.inputItem = inputItem;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.recipeSpeed = recipeSpeed;
        this.exothermic = exothermic;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new ThermoPlantRecipeResult(id);
    }

    public class ThermoPlantRecipeResult extends RecipeResult {
        ThermoPlantRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(JsonObject json) {
            if (inputItem != Ingredient.EMPTY) json.add("item_input", inputItem.serialize());
            if (inputFluid != FluidIngredient.EMPTY) json.add("fluid_input", inputFluid.serialize());
            if (!outputItem.isEmpty()) json.add("item_output", SerializerHelper.serializeOneItemStack(outputItem));
            if (!outputFluid.isEmpty()) json.add("fluid_output", ModCraftingHelper.fluidStackToJson(outputFluid));
            if (!operatingTemperature.isAny()) json.add("temperature", operatingTemperature.toJson());
            if (requiredPressure != 0f) json.addProperty("pressure", requiredPressure);
            if (recipeSpeed != 1.0f) json.addProperty("speed", recipeSpeed);
            json.addProperty("exothermic", exothermic);
        }
    }
}
