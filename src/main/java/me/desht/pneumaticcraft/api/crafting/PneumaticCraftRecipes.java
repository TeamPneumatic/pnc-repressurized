package me.desht.pneumaticcraft.api.crafting;

import me.desht.pneumaticcraft.api.crafting.recipe.*;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * Public list of all current PneumaticCraft machine recipes.
 * <p>
 * <strong>Do not modify these directly!</strong>  Machine recipes are loaded from datapacks, or alternatively use
 * {@link RegisterMachineRecipesEvent} to register new machine recipes in code.
 */
public class PneumaticCraftRecipes {
    public static Map<ResourceLocation, IPressureChamberRecipe> pressureChamberRecipes;
    public static Map<ResourceLocation, IThermopneumaticProcessingPlantRecipe> thermopneumaticProcessingPlantRecipes;
    public static Map<ResourceLocation, IHeatFrameCoolingRecipe> heatFrameCoolingRecipes;
    public static Map<ResourceLocation, IExplosionCraftingRecipe> explosionCraftingRecipes;
    public static Map<ResourceLocation, IRefineryRecipe> refineryRecipes;
    public static Map<ResourceLocation, IAssemblyRecipe> assemblyLaserRecipes;
    public static Map<ResourceLocation, IAssemblyRecipe> assemblyDrillRecipes;
    public static Map<ResourceLocation, IAssemblyRecipe> assemblyLaserDrillRecipes;
}
