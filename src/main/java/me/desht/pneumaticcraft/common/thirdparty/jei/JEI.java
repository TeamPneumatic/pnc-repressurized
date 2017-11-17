package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.client.gui.GuiAssemblyController;
import me.desht.pneumaticcraft.client.gui.GuiPressureChamber;
import me.desht.pneumaticcraft.client.gui.GuiRefinery;
import me.desht.pneumaticcraft.client.gui.GuiThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.oredict.OreDictionary;

@JEIPlugin
public class JEI implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        registry.addRecipes(BasicThermopneumaticProcessingPlantRecipe.recipes, ModCategoryUid.THERMO_PNEUMATIC);
        registry.addRecipes(PressureChamberRecipe.chamberRecipes, ModCategoryUid.PRESSURE_CHAMBER);
        registry.addRecipes(new JEIPlasticMixerCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.PLASTIC_MIXER);
        registry.addRecipes(new JEIRefineryCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.REFINERY);
        registry.addRecipes(new JEIEtchingAcidCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.ETCHING_ACID);
        registry.addRecipes(new JEICompressedIronCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.COMPRESSED_IRON_EXPLOSION);
        registry.addRecipes(new JEIUVLightBoxCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.UV_LIGHT_BOX);
        registry.addRecipes(AssemblyRecipe.drillRecipes, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipes(AssemblyRecipe.laserRecipes, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipes(AssemblyRecipe.drillLaserRecipes, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipes(new JEIAmadronTradeCategory(jeiHelpers).getAllRecipes(), ModCategoryUid.AMADRON_TRADE);

        registry.handleRecipes(PressureChamberRecipe.class,
                JEIPressureChamberRecipeCategory.ChamberRecipeWrapper::new, ModCategoryUid.PRESSURE_CHAMBER);
        registry.handleRecipes(BasicThermopneumaticProcessingPlantRecipe.class,
                JEIThermopneumaticProcessingPlantCategory.ThermopneumaticRecipeWrapper::new, ModCategoryUid.THERMO_PNEUMATIC);
        registry.handleRecipes(AssemblyRecipe.class,
                JEIAssemblyControllerCategory.AssemblyRecipeWrapper::new, ModCategoryUid.ASSEMBLY_CONTROLLER);

        registry.addRecipeClickArea(GuiAssemblyController.class, 68, 75, 24, 17, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipeClickArea(GuiPressureChamber.class, 100, 7, 40, 40, ModCategoryUid.PRESSURE_CHAMBER);
        registry.addRecipeClickArea(GuiRefinery.class, 47, 21, 27, 47, ModCategoryUid.REFINERY);
        registry.addRecipeClickArea(GuiThermopneumaticProcessingPlant.class, 30, 31, 48, 20, ModCategoryUid.THERMO_PNEUMATIC);

        jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(Blockss.DRONE_REDSTONE_EMITTER, 1, OreDictionary.WILDCARD_VALUE));
        jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(Blockss.KEROSENE_LAMP_LIGHT));

        registry.addRecipeCatalyst(new ItemStack(Itemss.AMADRON_TABLET), ModCategoryUid.AMADRON_TRADE);
        registry.addRecipeCatalyst(new ItemStack(Blockss.ASSEMBLY_CONTROLLER), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipeCatalyst(FluidUtil.getFilledBucket(new FluidStack(Fluids.ETCHING_ACID, 1000)), ModCategoryUid.ETCHING_ACID);
        registry.addRecipeCatalyst(new ItemStack(Blockss.PLASTIC_MIXER), ModCategoryUid.PLASTIC_MIXER);
        registry.addRecipeCatalyst(new ItemStack(Blockss.PRESSURE_CHAMBER_WALL), ModCategoryUid.PRESSURE_CHAMBER);
        registry.addRecipeCatalyst(new ItemStack(Blockss.REFINERY), ModCategoryUid.REFINERY);
        registry.addRecipeCatalyst(new ItemStack(Blockss.THERMOPNEUMATIC_PROCESSING_PLANT), ModCategoryUid.THERMO_PNEUMATIC);
        registry.addRecipeCatalyst(new ItemStack(Blockss.UV_LIGHT_BOX), ModCategoryUid.UV_LIGHT_BOX);

        registry.addAdvancedGuiHandlers(new GuiTabHandler());
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
                new JEICompressedIronCategory(helpers),
                new JEIUVLightBoxCategory(helpers),
                new JEIAmadronTradeCategory(helpers),
                new JEIPlasticMixerCategory(helpers)
        );
    }
}
