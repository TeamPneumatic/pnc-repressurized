package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class BasicThermopneumaticProcessingPlantRecipe implements IThermopneumaticProcessingPlantRecipe {
    public static final ResourceLocation RECIPE_TYPE = RL("thermopneumatic_processing_plant");

    private final ResourceLocation id;
    private final FluidStack inputFluid, outputFluid;
    private final Ingredient inputItem;
    private final float requiredPressure;
    private final boolean exothermic;
    private final TemperatureRange operatingTemperature;

    public BasicThermopneumaticProcessingPlantRecipe(ResourceLocation id, @Nonnull FluidStack inputFluid, @Nullable Ingredient inputItem,
                                                     FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure, boolean exothermic) {
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
        return (inputFluid.isEmpty() || (fluidStack.isFluidEqual(inputFluid) && fluidStack.getAmount() >= inputFluid.getAmount()))
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
    public FluidStack getInputFluid() {
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
        return RECIPE_TYPE;
    }

    public static class Serializer extends AbstractRecipeSerializer<BasicThermopneumaticProcessingPlantRecipe> {
        @Override
        public BasicThermopneumaticProcessingPlantRecipe read(ResourceLocation recipeId, JsonObject json) {
            return null;
        }

        @Nullable
        @Override
        public BasicThermopneumaticProcessingPlantRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            TemperatureRange range = TemperatureRange.of(buffer.readVarInt(), buffer.readVarInt());
            float pressure = buffer.readFloat();
            Ingredient input = Ingredient.read(buffer);
            FluidStack fluidIn = FluidStack.readFromPacket(buffer);
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
