package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * Do not modify these directly!  Use {@link RegisterMachineRecipesEvent} to register new machine recipes.
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
