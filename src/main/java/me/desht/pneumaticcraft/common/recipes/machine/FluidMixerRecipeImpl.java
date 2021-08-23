package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
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

import javax.annotation.Nullable;

public class FluidMixerRecipeImpl extends FluidMixerRecipe {
    private final FluidIngredient input1;
    private final FluidIngredient input2;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final float pressure;
    private final int processingTime;

    public FluidMixerRecipeImpl(ResourceLocation id, FluidIngredient input1, FluidIngredient input2, FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime) {
        super(id);
        this.input1 = input1;
        this.input2 = input2;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.pressure = pressure;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(FluidStack fluid1, FluidStack fluid2) {
        return input1.testFluid(fluid1) && input2.testFluid(fluid2)
                || input2.testFluid(fluid1) && input1.testFluid(fluid2);
    }

    @Override
    public FluidIngredient getInput1() {
        return input1;
    }

    @Override
    public FluidIngredient getInput2() {
        return input2;
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
    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public float getRequiredPressure() {
        return pressure;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input1.toNetwork(buffer);
        input2.toNetwork(buffer);
        outputFluid.writeToPacket(buffer);
        buffer.writeItem(outputItem);
        buffer.writeFloat(pressure);
        buffer.writeVarInt(processingTime);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.FLUID_MIXER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return PneumaticCraftRecipeType.FLUID_MIXER;
    }

    public static class Serializer<T extends FluidMixerRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(FluidMixerRecipeImpl.Serializer.IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input1 = FluidIngredient.fromJson(json.get("input1"));
            Ingredient input2 = FluidIngredient.fromJson(json.get("input2"));
            FluidStack outputFluid = json.has("fluid_output") ?
                    ModCraftingHelper.fluidStackFromJson(json.getAsJsonObject("fluid_output")):
                    FluidStack.EMPTY;
            ItemStack outputItem = json.has("item_output") ?
                    ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "item_output")) :
                    ItemStack.EMPTY;
            float pressure = JSONUtils.getAsFloat(json, "pressure");
            int processingTime = JSONUtils.getAsInt(json, "time", 200);

            return factory.create(recipeId, (FluidIngredient) input1, (FluidIngredient) input2, outputFluid, outputItem, pressure, processingTime);
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            FluidIngredient input1 = (FluidIngredient) Ingredient.fromNetwork(buffer);
            FluidIngredient input2 = (FluidIngredient) Ingredient.fromNetwork(buffer);
            FluidStack outputFluid = FluidStack.readFromPacket(buffer);
            ItemStack outputItem = buffer.readItem();
            float pressure = buffer.readFloat();
            int processingTime = buffer.readVarInt();

            return factory.create(recipeId, input1, input2, outputFluid, outputItem, pressure, processingTime);
        }

        @Override
        public void toNetwork(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory <T extends FluidMixerRecipe> {
            T create(ResourceLocation id, FluidIngredient input1, FluidIngredient input2,
                     FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime);
        }
    }
}
