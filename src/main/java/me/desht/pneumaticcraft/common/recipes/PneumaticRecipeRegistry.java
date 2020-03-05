package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class PneumaticRecipeRegistry implements IPneumaticRecipeRegistry {

    private static final PneumaticRecipeRegistry INSTANCE = new PneumaticRecipeRegistry();

    public static PneumaticRecipeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerThermopneumaticProcessingPlantRecipe(IThermopneumaticProcessingPlantRecipe recipe) {
        Validate.notNull(recipe);
        BasicThermopneumaticProcessingPlantRecipe.recipes.add(recipe);
    }

    @Override
    public void registerThermopneumaticProcessingPlantRecipe(FluidStack requiredFluid, @Nonnull ItemStack requiredItem, FluidStack output, double requiredTemperature, float requiredPressure) {
        Validate.notNull(output);
        registerThermopneumaticProcessingPlantRecipe(new BasicThermopneumaticProcessingPlantRecipe(requiredFluid, requiredItem, output, requiredTemperature, requiredPressure));
    }

    @Override
    public void addAssemblyDrillRecipe(Object input, Object output) {
        Validate.notNull(input);
        Validate.notNull(output);
        AssemblyRecipe.drillRecipes.add(new AssemblyRecipe(getStackFromObject(input), getStackFromObject(output),
                ItemAssemblyProgram.getStackForProgramType(ItemAssemblyProgram.DRILL_DAMAGE, 1))
        );
    }

    @Override
    public void addAssemblyLaserRecipe(Object input, Object output) {
        Validate.notNull(input);
        Validate.notNull(output);
        AssemblyRecipe.laserRecipes.add(new AssemblyRecipe(getStackFromObject(input), getStackFromObject(output),
                ItemAssemblyProgram.getStackForProgramType(ItemAssemblyProgram.LASER_DAMAGE, 1))
        );
    }

    @Override
    public void registerPressureChamberRecipe(ItemIngredient[] input, float pressureRequired, ItemStack[] output) {
        Validate.notNull(input);
        Validate.notNull(output);
        PressureChamberRecipe.recipes.add(new PressureChamberRecipe.SimpleRecipe(input, pressureRequired, output));
    }

    @Override
    public void registerPressureChamberRecipe(IPressureChamberRecipe recipe) {
        Validate.notNull(recipe);
        PressureChamberRecipe.recipes.add(recipe);
    }

    private static ItemStack getStackFromObject(Object object) {
        if (object instanceof Block) {
            return new ItemStack((Block) object);
        } else if (object instanceof Item) {
            return new ItemStack((Item) object);
        } else if (object instanceof ItemStack) {
            return (ItemStack) object;
        } else {
            throw new IllegalArgumentException("object needs to be of type Block, Item or ItemStack");
        }
    }

    @Override
    public void registerExplosionCraftingRecipe(ItemStack input, ItemStack output, int lossRate) {
        ExplosionCraftingRecipe.recipes.add(new ExplosionCraftingRecipe(input, output, lossRate));
    }

    @Override
    public void registerExplosionCraftingRecipe(String oreDictKey, ItemStack output, int lossRate) {
        ExplosionCraftingRecipe.recipes.add(new ExplosionCraftingRecipe(oreDictKey, output, lossRate));
    }

    private static String makeKey(ItemStack stack) {
        return stack.getItem().getRegistryName() + ":" + stack.getMetadata();
    }

    @Override
    public void registerDefaultStaticAmadronOffer(Object input, Object output) {
        AmadronOffer offer = new AmadronOffer(input, output);
        AmadronOfferManager.getInstance().addStaticOffer(offer);
    }

    @Override
    public void registerDefaultPeriodicAmadronOffer(Object input, Object output) {
        AmadronOffer offer = new AmadronOffer(input, output);
        AmadronOfferManager.getInstance().addPeriodicOffer(offer);
    }

    @Override
    public void registerHeatFrameCoolRecipe(ItemIngredient input, ItemStack output) {
    	HeatFrameCoolingRecipe.recipes.add(new HeatFrameCoolingRecipe(input, output));
    }

    @Override
	public void registerRefineryRecipe(FluidStack input, FluidStack... outputs) {
        registerRefineryRecipe(373, input, outputs);
	}

    @Override
    public void registerRefineryRecipe(int minimumTemperature, FluidStack input, FluidStack... outputs) {
        RefineryRecipe.recipes.add(new RefineryRecipe(minimumTemperature, input, outputs));
    }

    @Override
    public void registerPlasticMixerRecipe(FluidStack fluidPlastic, ItemStack solidPlastic, int temperature, boolean allowMelting, boolean allowSolidifying) {
        PlasticMixerRegistry.INSTANCE.addPlasticMixerRecipe(fluidPlastic, solidPlastic, temperature, allowMelting, allowSolidifying, true, -1);
    }

    @Override
    public void registerPlasticMixerRecipe(FluidStack fluidPlastic, ItemStack solidPlastic, int temperature, boolean allowMelting, boolean allowSolidifying, boolean useDye, int meta) {
        PlasticMixerRegistry.INSTANCE.addPlasticMixerRecipe(fluidPlastic, solidPlastic, temperature, allowMelting, allowSolidifying, useDye, meta);
    }
}
