package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import me.desht.pneumaticcraft.common.recipes.MachineRecipeHandler;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

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
    private final ItemStack outputItem;

    public BasicThermopneumaticProcessingPlantRecipe(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem,
            FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
            boolean exothermic)
    {
        this.id = id;
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.exothermic = exothermic;

        Validate.isTrue(!inputFluid.hasNoMatchingItems() || !inputItem.hasNoMatchingItems(),
                "At least on of input fluid or input item must be non-empty!");
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
    public ItemStack getOutputItem() {
        return outputItem;
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
            if (itemInput.hasNoMatchingItems() && fluidInput.hasNoMatchingItems()) {
                throw new JsonSyntaxException("Must have at least one of item_input and/or fluid_input!");
            }

            FluidStack fluidOutput = json.has("fluid_output") ?
                    ModCraftingHelper.fluidStackFromJSON(json.getAsJsonObject("fluid_output")):
                    FluidStack.EMPTY;
            ItemStack itemOutput = json.has("item_output") ?
                    ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "item_output")) :
                    ItemStack.EMPTY;
            if (fluidOutput.isEmpty() && itemOutput.isEmpty()) {
                throw new JsonSyntaxException("Must have at least one of item_output and/or fluid_output!");
            }

            TemperatureRange range;
            if (!json.has("min_temp") && !json.has("max_temp")) {
                range = TemperatureRange.any();
            } else {
                int minTemp = JSONUtils.getInt(json, "min_temp", 373);
                int maxTemp = JSONUtils.getInt(json, "max_temp", Integer.MAX_VALUE);
                range = TemperatureRange.of(minTemp, maxTemp);
            }
            float pressure = JSONUtils.getFloat(json, "pressure", 0f);
            boolean exothermic = JSONUtils.getBoolean(json, "exothermic", false);

            return new BasicThermopneumaticProcessingPlantRecipe(recipeId, (FluidIngredient) fluidInput, itemInput,
                    fluidOutput, itemOutput, range, pressure, exothermic);
        }

        @Nullable
        @Override
        public BasicThermopneumaticProcessingPlantRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            TemperatureRange range = TemperatureRange.of(buffer.readVarInt(), buffer.readVarInt());
            float pressure = buffer.readFloat();
            Ingredient input = Ingredient.read(buffer);
            FluidIngredient fluidIn = FluidIngredient.readFromPacket(buffer);
            FluidStack fluidOut = FluidStack.readFromPacket(buffer);
            ItemStack itemOutput = buffer.readItemStack();
            boolean exothermic = buffer.readBoolean();
            return new BasicThermopneumaticProcessingPlantRecipe(recipeId, fluidIn, input, fluidOut, itemOutput, range, pressure, exothermic);
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
            buffer.writeItemStack(recipe.outputItem);
            buffer.writeBoolean(recipe.exothermic);
        }
    }
}
