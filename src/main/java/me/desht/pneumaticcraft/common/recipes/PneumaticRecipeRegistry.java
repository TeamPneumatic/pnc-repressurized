package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.IModRecipeSerializer;
import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.api.crafting.recipe.IAssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.common.recipes.machine.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public enum PneumaticRecipeRegistry implements IPneumaticRecipeRegistry {
    INSTANCE;

    public static PneumaticRecipeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerSerializer(ResourceLocation recipeType, Supplier<IModRecipeSerializer<? extends IModRecipe>> serializer) {
        ModCraftingHelper.register(recipeType, serializer);
    }

    @Override
    public IAssemblyRecipe basicLaserRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output) {
        return new AssemblyRecipe(id, input, output, AssemblyProgramType.LASER);
    }

    @Override
    public IAssemblyRecipe basicDrillRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output) {
        return new AssemblyRecipe(id, input, output, AssemblyProgramType.DRILL);
    }

    @Override
    public IExplosionCraftingRecipe basicRecipe(ResourceLocation id, Ingredient input, int lossRate, ItemStack... outputs) {
        return new ExplosionCraftingRecipe(id, input, lossRate, outputs);
    }

    @Override
    public IHeatFrameCoolingRecipe basicRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output) {
        return new HeatFrameCoolingRecipe(id, input, temperature, output);
    }

    @Override
    public IPressureChamberRecipe basicRecipe(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs) {
        return new BasicPressureChamberRecipe(id, inputs, pressureRequired, outputs);
    }

    @Override
    public IRefineryRecipe basicRecipe(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs) {
        return new RefineryRecipe(id, input, operatingTemp, outputs);
    }

    @Override
    public IThermopneumaticProcessingPlantRecipe basicRecipe(ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem, FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure) {
        return new BasicThermopneumaticProcessingPlantRecipe(id, inputFluid, inputItem, outputFluid, operatingTemperature, requiredPressure, false);
    }

    @Override
    public IThermopneumaticProcessingPlantRecipe basicExothermicRecipe(ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem, FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure) {
        return new BasicThermopneumaticProcessingPlantRecipe(id, inputFluid, inputItem, outputFluid, operatingTemperature, requiredPressure, true);
    }
}
