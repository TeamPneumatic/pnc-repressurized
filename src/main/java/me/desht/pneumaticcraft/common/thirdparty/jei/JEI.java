package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
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

        registry.addRecipeCatalyst(new ItemStack(ModItems.AMADRON_TABLET), ModCategoryUid.AMADRON_TRADE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registry.addRecipeCatalyst(FluidUtil.getFilledBucket(new FluidStack(Fluids.ETCHING_ACID, 1000)), ModCategoryUid.ETCHING_ACID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.PLASTIC_MIXER), ModCategoryUid.PLASTIC_MIXER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL), ModCategoryUid.PRESSURE_CHAMBER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.REFINERY), ModCategoryUid.REFINERY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT), ModCategoryUid.THERMO_PNEUMATIC);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.UV_LIGHT_BOX), ModCategoryUid.UV_LIGHT_BOX);
        registry.addRecipeCatalyst(new ItemStack(ModItems.HEAT_FRAME), ModCategoryUid.HEAT_FRAME_COOLING);

        registry.addAdvancedGuiHandlers(new GuiTabHandler());

        addIngredientInfoTabs(registry);

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.FAKE_ICE));
    }

    private void addIngredientInfoTabs(IModRegistry registry) {
        for (Item item : ModItems.items) {
            NonNullList<ItemStack> stacks = NonNullList.create();
            if (item.getHasSubtypes()) {
                item.getSubItems(PneumaticCraftRepressurized.tabPneumaticCraft, stacks);
            } else {
                stacks.add(new ItemStack(item, 1, 0));
            }
            stacks.forEach(s -> addStackInfo(registry, s));
        }

        for (Block block : ModBlocks.blocks) {
            ItemStack stack = new ItemStack(block, 1, 0);
            addStackInfo(registry, stack);
        }

        for (Fluid fluid : Fluids.FLUIDS) {
            String k = "gui.tooltip.item." + fluid.getName() + "_bucket";
            if (I18n.hasKey(k)) {
                String raw = TextFormatting.getTextWithoutFormattingCodes(I18n.format(k));
                registry.addIngredientInfo(new FluidStack(fluid, 1000), VanillaTypes.FLUID, raw.split(" \\\\n"));
            }
        }
    }

    private void addStackInfo(IModRegistry registry, ItemStack stack) {
        String k = (stack.getItem() instanceof BlockItem ? "gui.tab.info." : "gui.tooltip.") + stack.getTranslationKey();
        if (I18n.hasKey(k)) {
            String raw = TextFormatting.getTextWithoutFormattingCodes(I18n.format(k));
            registry.addIngredientInfo(stack, VanillaTypes.ITEM, raw.split(" \\\\n"));
        }
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
