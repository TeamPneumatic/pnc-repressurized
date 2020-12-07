package me.desht.pneumaticcraft.common.recipes.other;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.FuelQualityRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class FuelQualityRecipeImpl extends FuelQualityRecipe {
    private final FluidIngredient fuel;
    private final int airPerBucket;
    private final float burnRate;

    public FuelQualityRecipeImpl(ResourceLocation id, FluidIngredient fuel, int airPerBucket, float burnRate) {
        super(id);

        this.fuel = fuel;
        this.airPerBucket = airPerBucket;
        this.burnRate = burnRate;
    }

    @Override
    public boolean matchesFluid(Fluid inputFluid) {
        return fuel.testFluid(inputFluid);
    }

    @Override
    public FluidIngredient getFuel() {
        return fuel;
    }

    @Override
    public int getAirPerBucket() {
        return airPerBucket;
    }

    @Override
    public float getBurnRate() {
        return burnRate;
    }

    @Override
    public void write(PacketBuffer buffer) {
        fuel.write(buffer);
        buffer.writeInt(airPerBucket);
        buffer.writeFloat(burnRate);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.FUEL_QUALITY.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return PneumaticCraftRecipeType.FUEL_QUALITY;
    }

    public static class Serializer<T extends FuelQualityRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json) {
            Ingredient fluidInput = FluidIngredient.deserialize(json.get("fluid"));
            int airPerBucket = JSONUtils.getInt(json, "air_per_bucket");
            float burnRate = JSONUtils.getFloat(json, "burn_rate", 1f);

            return factory.create(recipeId, (FluidIngredient) fluidInput, airPerBucket, burnRate);
        }

        @Nullable
        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
            FluidIngredient fluidIn = (FluidIngredient) Ingredient.read(buffer);
            int airPerBucket = buffer.readInt();
            float burnRate = buffer.readFloat();

            return factory.create(recipeId, fluidIn, airPerBucket, burnRate);
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory <T extends FuelQualityRecipe> {
            T create(ResourceLocation id, FluidIngredient fluid, int airPerBucket, float burnRate);
        }
    }
}
