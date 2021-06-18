package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ThermoPlantRecipeImpl extends ThermoPlantRecipe {
    private final FluidIngredient inputFluid;
    private final FluidStack outputFluid;
    private final Ingredient inputItem;
    private final float requiredPressure;
    private final float recipeSpeed;
    private final boolean exothermic;
    private final TemperatureRange operatingTemperature;
    private final ItemStack outputItem;

    // TODO 1.17 make inputItem a StackedIngredient to support item counts
    public ThermoPlantRecipeImpl(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nonnull Ingredient inputItem,
            FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
            float recipeSpeed, boolean exothermic)
    {
        super(id);

        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.recipeSpeed = recipeSpeed;
        this.exothermic = exothermic;
    }

    @Override
    public boolean matches(FluidStack fluidStack, @Nonnull ItemStack itemStack) {
        return (inputFluid.hasNoMatchingItems() && fluidStack.isEmpty() || inputFluid.testFluid(fluidStack.getFluid()))
                && (inputItem.hasNoMatchingItems() && itemStack.isEmpty() || inputItem.test(itemStack));
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
    public double getRecipeSpeed() {
        return recipeSpeed;
    }

    @Override
    public void write(PacketBuffer buffer) {
        operatingTemperature.write(buffer);
        buffer.writeFloat(requiredPressure);
        inputItem.write(buffer);
        inputFluid.write(buffer);
        outputFluid.writeToPacket(buffer);
        buffer.writeItemStack(outputItem);
        buffer.writeFloat(recipeSpeed);
        buffer.writeBoolean(exothermic);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.THERMO_PLANT.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return PneumaticCraftRecipeType.THERMO_PLANT;
    }

    @Override
    public String getGroup() {
        return ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get().getRegistryName().getPath();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get());
    }

    public static class Serializer<T extends ThermoPlantRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json) {
            if (!json.has("item_input") && !json.has("fluid_input")) {
                throw new JsonSyntaxException("Must have at least one of item_input and/or fluid_input!");
            }
            if (!json.has("item_output") && !json.has("fluid_output")) {
                throw new JsonSyntaxException("Must have at least one of item_output and/or fluid_output!");
            }

            Ingredient itemInput = json.has("item_input") ?
                    Ingredient.deserialize(json.get("item_input")) :
                    Ingredient.EMPTY;
            Ingredient fluidInput = json.has("fluid_input") ?
                    FluidIngredient.deserialize(json.get("fluid_input")) :
                    FluidIngredient.EMPTY;

            FluidStack fluidOutput = json.has("fluid_output") ?
                    ModCraftingHelper.fluidStackFromJson(json.getAsJsonObject("fluid_output")):
                    FluidStack.EMPTY;
            ItemStack itemOutput = json.has("item_output") ?
                    ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "item_output")) :
                    ItemStack.EMPTY;

            TemperatureRange range = json.has("temperature") ?
                    TemperatureRange.fromJson(json.getAsJsonObject("temperature")) :
                    TemperatureRange.any();

            float pressure = JSONUtils.getFloat(json, "pressure", 0f);

            boolean exothermic = JSONUtils.getBoolean(json, "exothermic", false);

            float recipeSpeed = JSONUtils.getFloat(json, "speed", 1.0f);

            return factory.create(recipeId, (FluidIngredient) fluidInput, itemInput, fluidOutput, itemOutput, range, pressure, recipeSpeed, exothermic);
        }

        @Nullable
        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
            TemperatureRange range = TemperatureRange.read(buffer);
            float pressure = buffer.readFloat();
            Ingredient input = Ingredient.read(buffer);
            FluidIngredient fluidIn = (FluidIngredient) Ingredient.read(buffer);
            FluidStack fluidOut = FluidStack.readFromPacket(buffer);
            ItemStack itemOutput = buffer.readItemStack();
            float recipeSpeed = buffer.readFloat();
            boolean exothermic = buffer.readBoolean();
            return factory.create(recipeId, fluidIn, input, fluidOut, itemOutput, range, pressure, recipeSpeed, exothermic);
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory <T extends ThermoPlantRecipe> {
            T create(ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem,
                     FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                     float recipeSpeed, boolean exothermic);
        }
    }
}
