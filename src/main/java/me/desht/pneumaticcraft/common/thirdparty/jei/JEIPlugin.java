package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.client.gui.GuiAssemblyController;
import me.desht.pneumaticcraft.client.gui.GuiPressureChamber;
import me.desht.pneumaticcraft.client.gui.GuiRefinery;
import me.desht.pneumaticcraft.client.gui.GuiThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import me.desht.pneumaticcraft.common.thirdparty.jei.extension.HelmetOneProbeExtension;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IJeiHelpers helpers = registry.getJeiHelpers();
        registry.addRecipeCategories(
                new JEIPressureChamberRecipeCategory(helpers),
                new JEIAssemblyControllerCategory(helpers),
                new JEIThermopneumaticProcessingPlantCategory(helpers),
                new JEIRefineryCategory(helpers),
                new JEIUVLightBoxCategory(helpers),
                new JEIAmadronTradeCategory(helpers),
                new JEIHeatFrameCoolingCategory(helpers)
        );
        if (PNCConfig.Common.Recipes.explosionCrafting) {
            registry.addRecipeCategories(new JEIExplosionCraftingCategory(helpers));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PneumaticCraftRecipes.assemblyLaserRecipes.values(), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipes(PneumaticCraftRecipes.assemblyDrillRecipes.values(), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipes(PneumaticCraftRecipes.assemblyLaserDrillRecipes.values(), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipes(PneumaticCraftRecipes.explosionCraftingRecipes.values(), ModCategoryUid.EXPLOSION_CRAFTING);
        registration.addRecipes(PneumaticCraftRecipes.refineryRecipes.values(), ModCategoryUid.REFINERY);
        registration.addRecipes(PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes.values(), ModCategoryUid.THERMO_PNEUMATIC);
        registration.addRecipes(PneumaticCraftRecipes.heatFrameCoolingRecipes.values(), ModCategoryUid.HEAT_FRAME_COOLING);
        registration.addRecipes(PneumaticCraftRecipes.pressureChamberRecipes.values(), ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipes(JEIUVLightBoxCategory.UV_LIGHT_BOX_RECIPES, ModCategoryUid.UV_LIGHT_BOX);
        registration.addRecipes(AmadronOfferManager.getInstance().getAllOffers(), ModCategoryUid.AMADRON_TRADE);

        for (Item item : ModItems.Registration.ALL_ITEMS) {
            addStackInfo(registration, new ItemStack(item));
        }
        for (Block block : ModBlocks.Registration.ALL_BLOCKS) {
            addStackInfo(registration, new ItemStack(block));
        }
    }

    private void addStackInfo(IRecipeRegistration registry, ItemStack stack) {
        String k = (stack.getItem() instanceof BlockItem ? "gui.tab.info.tile." : "gui.tooltip.") + stack.getTranslationKey();
        if (I18n.hasKey(k)) {
            String raw = TextFormatting.getTextWithoutFormattingCodes(I18n.format(k));
            registry.addIngredientInfo(stack, VanillaTypes.ITEM, raw.split(" \\\\n"));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.AMADRON_TABLET), ModCategoryUid.AMADRON_TRADE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL), ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REFINERY), ModCategoryUid.REFINERY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT), ModCategoryUid.THERMO_PNEUMATIC);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.UV_LIGHT_BOX), ModCategoryUid.UV_LIGHT_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.HEAT_FRAME), ModCategoryUid.HEAT_FRAME_COOLING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GuiAssemblyController.class, 110, 10, 50, 50, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipeClickArea(GuiPressureChamber.class, 100, 7, 40, 40, ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeClickArea(GuiRefinery.class, 47, 21, 27, 47, ModCategoryUid.REFINERY);
        registration.addRecipeClickArea(GuiThermopneumaticProcessingPlant.class, 30, 31, 48, 20, ModCategoryUid.THERMO_PNEUMATIC);

        registration.addGlobalGuiHandler(new GuiTabHandler());
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        if (OneProbeCrafting.ONE_PROBE != null) {
            registration.getCraftingCategory().addCategoryExtension(OneProbeCrafting.class, HelmetOneProbeExtension::new);
        }
    }

    @Override
    public ResourceLocation getPluginUid() {
        return RL("default");
    }
}
