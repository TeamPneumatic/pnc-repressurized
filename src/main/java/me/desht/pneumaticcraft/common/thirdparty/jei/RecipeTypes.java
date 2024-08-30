package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.recipes.machine.UVLightBoxRecipe;
import mezz.jei.api.recipe.RecipeType;

public class RecipeTypes {
    public static final RecipeType<AmadronRecipe> AMADRON_TRADE = register("amadron_trade", AmadronRecipe.class);
    public static final RecipeType<AssemblyRecipe> ASSEMBLY = register("assembly", AssemblyRecipe.class);
    public static final RecipeType<ExplosionCraftingRecipe> EXPLOSION_CRAFTING = register("explosion_crafting", ExplosionCraftingRecipe.class);
    public static final RecipeType<FluidMixerRecipe> FLUID_MIXER = register("fluid_mixer", FluidMixerRecipe.class);
    public static final RecipeType<HeatFrameCoolingRecipe> HEAT_FRAME_COOLING = register("heat_frame_cooling", HeatFrameCoolingRecipe.class);
    public static final RecipeType<HeatPropertiesRecipe> HEAT_PROPERTIES = register("heat_properties", HeatPropertiesRecipe.class);
    public static final RecipeType<PressureChamberRecipe> PRESSURE_CHAMBER = register("pressure_chamber", PressureChamberRecipe.class);
    public static final RecipeType<RefineryRecipe> REFINERY = register("refinery", RefineryRecipe.class);
    public static final RecipeType<ThermoPlantRecipe> THERMO_PLANT = register("thermo_plant", ThermoPlantRecipe.class);

    // pseudo-recipes
    public static final RecipeType<JEIElectroStaticGridCategory.ElectrostaticGridRecipe> ELECTRO_GRID
            = registerPseudo("electro_grid", JEIElectroStaticGridCategory.ElectrostaticGridRecipe.class);
    public static final RecipeType<JEIEtchingTankCategory.EtchingTankRecipe> ETCHING_TANK
            = registerPseudo("etching_tank", JEIEtchingTankCategory.EtchingTankRecipe.class);
    public static final RecipeType<JEIMemoryEssenceCategory.MemoryEssenceRecipe> MEMORY_ESSENCE
            = registerPseudo("memory_essence", JEIMemoryEssenceCategory.MemoryEssenceRecipe.class);
    public static final RecipeType<JEIPlasticSolidifyingCategory.PlasticSolidifyingRecipe> PLASTIC_SOLIDIFYING
            = registerPseudo("plastic_solidifying", JEIPlasticSolidifyingCategory.PlasticSolidifyingRecipe.class);
    public static final RecipeType<JEISpawnerExtractionCategory.SpawnerExtractionRecipe> SPAWNER_EXTRACTION
            = registerPseudo("spawner_extraction", JEISpawnerExtractionCategory.SpawnerExtractionRecipe.class);
    public static final RecipeType<UVLightBoxRecipe> UV_LIGHT_BOX
            = registerPseudo("uv_light_box", UVLightBoxRecipe.class);
    public static final RecipeType<JEIYeastCraftingCategory.YeastCraftingRecipe> YEAST_CRAFTING
            = registerPseudo("yeast_crafting", JEIYeastCraftingCategory.YeastCraftingRecipe.class);

    private static <T extends PneumaticCraftRecipe> RecipeType<T> register(String name, Class<T> recipeClass) {
        return RecipeType.create(Names.MOD_ID, name, recipeClass);
    }

    private static <T> RecipeType<T> registerPseudo(String name, Class<T> recipeClass) {
        return RecipeType.create(Names.MOD_ID, name, recipeClass);
    }
}
