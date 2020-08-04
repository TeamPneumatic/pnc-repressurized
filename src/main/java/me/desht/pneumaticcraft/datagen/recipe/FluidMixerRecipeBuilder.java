package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class FluidMixerRecipeBuilder extends PneumaticCraftRecipeBuilder<FluidMixerRecipeBuilder> {
    private final FluidIngredient input1;
    private final FluidIngredient input2;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final float pressure;
    private final int processingTime;

    public FluidMixerRecipeBuilder(FluidIngredient input1, FluidIngredient input2, FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime) {
        super(RL(PneumaticCraftRecipeTypes.FLUID_MIXER));

        this.input1 = input1;
        this.input2 = input2;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.pressure = pressure;
        this.processingTime = processingTime;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new FluidMixerRecipeResult(id);
    }

    public class FluidMixerRecipeResult extends RecipeResult {
        public FluidMixerRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(JsonObject json) {
            json.add("input1", input1.serialize());
            json.add("input2", input2.serialize());
            if (!outputFluid.isEmpty()) json.add("fluid_output", ModCraftingHelper.fluidStackToJson(outputFluid));
            if (!outputItem.isEmpty()) json.add("item_output", SerializerHelper.serializeOneItemStack(outputItem));
            json.addProperty("pressure", pressure);
            if (processingTime != 200) json.addProperty("time", processingTime);
        }
    }
}
