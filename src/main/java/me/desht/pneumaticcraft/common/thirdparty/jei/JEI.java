package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

@JEIPlugin
public class JEI implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        registry.addRecipes(BasicThermopneumaticProcessingPlantRecipe.recipes, ModCategoryUid.THERMO_PNEUMATIC);
        registry.addRecipes(PressureChamberRecipe.recipes, ModCategoryUid.PRESSURE_CHAMBER);
        registry.addRecipes(new JEIPlasticMixerCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.PLASTIC_MIXER);
        registry.addRecipes(new JEIRefineryCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.REFINERY);
        registry.addRecipes(new JEIEtchingAcidCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.ETCHING_ACID);
        registry.addRecipes(new JEIUVLightBoxCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.UV_LIGHT_BOX);
        registry.addRecipes(AssemblyRecipe.drillRecipes, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipes(AssemblyRecipe.laserRecipes, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipes(AssemblyRecipe.drillLaserRecipes, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipes(new JEIAmadronTradeCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.AMADRON_TRADE);
        registry.addRecipes(new JEIHeatFrameCoolingCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.HEAT_FRAME_COOLING);
        if (ConfigHandler.general.explosionCrafting) {
            registry.addRecipes(new JEIExplosionCraftingCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.EXPLOSION_CRAFTING);
        }

        registry.handleRecipes(IPressureChamberRecipe.class,
                JEIPressureChamberRecipeCategory.ChamberRecipeWrapper::new, ModCategoryUid.PRESSURE_CHAMBER);
        registry.handleRecipes(BasicThermopneumaticProcessingPlantRecipe.class,
                JEIThermopneumaticProcessingPlantCategory.ThermopneumaticRecipeWrapper::new, ModCategoryUid.THERMO_PNEUMATIC);
        registry.handleRecipes(AssemblyRecipe.class,
                JEIAssemblyControllerCategory.AssemblyRecipeWrapper::new, ModCategoryUid.ASSEMBLY_CONTROLLER);

        registry.addRecipeClickArea(GuiAssemblyController.class, 110, 10, 50, 50, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipeClickArea(GuiPressureChamber.class, 100, 7, 40, 40, ModCategoryUid.PRESSURE_CHAMBER);
        registry.addRecipeClickArea(GuiRefinery.class, 47, 21, 27, 47, ModCategoryUid.REFINERY);
        registry.addRecipeClickArea(GuiThermopneumaticProcessingPlant.class, 30, 31, 48, 20, ModCategoryUid.THERMO_PNEUMATIC);
        registry.addRecipeClickArea(GuiPlasticMixer.class, 97, 44, 28, 12, ModCategoryUid.PLASTIC_MIXER);

        registry.addRecipeCatalyst(new ItemStack(Itemss.AMADRON_TABLET), ModCategoryUid.AMADRON_TRADE);
        registry.addRecipeCatalyst(new ItemStack(Blockss.ASSEMBLY_CONTROLLER), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipeCatalyst(FluidUtil.getFilledBucket(new FluidStack(Fluids.ETCHING_ACID, 1000)), ModCategoryUid.ETCHING_ACID);
        registry.addRecipeCatalyst(new ItemStack(Blockss.PLASTIC_MIXER), ModCategoryUid.PLASTIC_MIXER);
        registry.addRecipeCatalyst(new ItemStack(Blockss.PRESSURE_CHAMBER_WALL), ModCategoryUid.PRESSURE_CHAMBER);
        registry.addRecipeCatalyst(new ItemStack(Blockss.REFINERY), ModCategoryUid.REFINERY);
        registry.addRecipeCatalyst(new ItemStack(Blockss.THERMOPNEUMATIC_PROCESSING_PLANT), ModCategoryUid.THERMO_PNEUMATIC);
        registry.addRecipeCatalyst(new ItemStack(Blockss.UV_LIGHT_BOX), ModCategoryUid.UV_LIGHT_BOX);
        registry.addRecipeCatalyst(new ItemStack(Itemss.HEAT_FRAME), ModCategoryUid.HEAT_FRAME_COOLING);

        registry.addAdvancedGuiHandlers(new GuiTabHandler());

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(Blockss.FAKE_ICE));

    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IJeiHelpers helpers = registry.getJeiHelpers();
        registry.addRecipeCategories(
                new JEIPressureChamberRecipeCategory(helpers),
                new JEIAssemblyControllerCategory(helpers),
                new JEIThermopneumaticProcessingPlantCategory(helpers),
                new JEIRefineryCategory(helpers),
                new JEIEtchingAcidCategory(helpers),
                new JEIUVLightBoxCategory(helpers),
                new JEIAmadronTradeCategory(helpers),
                new JEIPlasticMixerCategory(helpers),
                new JEIHeatFrameCoolingCategory(helpers)
        );
        if (ConfigHandler.general.explosionCrafting) {
            registry.addRecipeCategories(new JEIExplosionCraftingCategory(helpers));
        }
    }
}
