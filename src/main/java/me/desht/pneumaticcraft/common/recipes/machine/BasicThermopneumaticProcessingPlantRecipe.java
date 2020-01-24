package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import me.desht.pneumaticcraft.common.recipes.MachineRecipeHandler;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BasicThermopneumaticProcessingPlantRecipe implements IThermopneumaticProcessingPlantRecipe {
    private final ResourceLocation id;
    private final FluidIngredient inputFluid;
    private final FluidStack outputFluid;
    private final Ingredient inputItem;
    private final float requiredPressure;
    private final boolean exothermic;
    private final TemperatureRange operatingTemperature;

    public BasicThermopneumaticProcessingPlantRecipe(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem,
            FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure,
            boolean exothermic)
    {
        this.id = id;
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.exothermic = exothermic;
    }

    @Override
    public boolean matches(FluidStack fluidStack, @Nonnull ItemStack stack) {
        return (inputFluid == null || inputFluid.testFluid(fluidStack))
                && (inputItem == null || inputItem.test(stack));
    }

    @Override
    public TemperatureRange getOperatingTemperature() {
        return operatingTemperature;
    }

    @Override
    public float getRequiredPressure() {
        return requiredPressure;
    }

    @Override
    public FluidIngredient getInputFluid() {
        return inputFluid;
    }

    @Nonnull
    @Override
    public Ingredient getInputItem() {
        return inputItem;
    }

    @Override
    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    @Override
    public boolean isExothermic() {
        return exothermic;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ResourceLocation getRecipeType() {
        return MachineRecipeHandler.Category.THERMO_PLANT.getId();
    }

    public static class Serializer extends AbstractRecipeSerializer<BasicThermopneumaticProcessingPlantRecipe> {
        @Override
        public BasicThermopneumaticProcessingPlantRecipe read(ResourceLocation recipeId, JsonObject json) {
            Ingredient itemInput = json.has("item_input") ?
                    Ingredient.deserialize(json.get("item_input")) :
                    Ingredient.EMPTY;
            Ingredient fluidInput = json.has("fluid_input") ?
                    FluidIngredient.deserialize(json.get("fluid_input")) :
                    Ingredient.EMPTY;
            FluidStack fluidOutput = ModCraftingHelper.fluidStackFromJSON(json.getAsJsonObject("fluid_output"));
            int minTemp = JSONUtils.getInt(json, "min_temp", 373);
            int maxTemp = JSONUtils.getInt(json, "max_temp", Integer.MAX_VALUE);
            float pressure = JSONUtils.getFloat(json, "pressure", 0f);
            boolean exothermic = JSONUtils.getBoolean(json, "exothermic", false);

            return new BasicThermopneumaticProcessingPlantRecipe(recipeId, (FluidIngredient) fluidInput, itemInput,
                    fluidOutput, TemperatureRange.of(minTemp, maxTemp), pressure, exothermic);
        }

        @Nullable
        @Override
        public BasicThermopneumaticProcessingPlantRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            TemperatureRange range = TemperatureRange.of(buffer.readVarInt(), buffer.readVarInt());
            float pressure = buffer.readFloat();
            Ingredient input = Ingredient.read(buffer);
            FluidIngredient fluidIn = FluidIngredient.readFromPacket(buffer);
            FluidStack fluidOut = FluidStack.readFromPacket(buffer);
            boolean exothermic = buffer.readBoolean();
            return new BasicThermopneumaticProcessingPlantRecipe(recipeId, fluidIn, input, fluidOut, range, pressure, exothermic);
        }

        @Override
        public void write(PacketBuffer buffer, BasicThermopneumaticProcessingPlantRecipe recipe) {
            super.write(buffer, recipe);

            buffer.writeVarInt(recipe.operatingTemperature.getMin());
            buffer.writeVarInt(recipe.operatingTemperature.getMax());
            buffer.writeFloat(recipe.requiredPressure);
            recipe.inputItem.write(buffer);
            recipe.inputFluid.writeToPacket(buffer);
            recipe.outputFluid.writeToPacket(buffer);
            buffer.writeBoolean(recipe.exothermic);
        }
    }
}
