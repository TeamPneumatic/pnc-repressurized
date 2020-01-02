package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.gui.GuiAssemblyController;
import me.desht.pneumaticcraft.client.gui.GuiPressureChamber;
import me.desht.pneumaticcraft.client.gui.GuiRefineryController;
import me.desht.pneumaticcraft.client.gui.GuiThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemPressurizable;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import me.desht.pneumaticcraft.common.recipes.special.PatchouliBookCrafting;
import me.desht.pneumaticcraft.common.thirdparty.jei.extension.HelmetOneProbeExtension;
import me.desht.pneumaticcraft.common.thirdparty.jei.extension.PatchouliBookExtension;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
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
    static IJeiHelpers jeiHelpers;

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (Item item : ModItems.Registration.ALL_ITEMS) {
            if (item instanceof ItemPressurizable) {
                registration.registerSubtypeInterpreter(item, s -> s.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h2 -> String.valueOf(h2.getPressure())).orElse(ISubtypeInterpreter.NONE));
            }
        }
        registration.registerSubtypeInterpreter(ModItems.EMPTY_PCB, s -> String.valueOf(TileEntityUVLightBox.getExposureProgress(s)));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(
                new JEIPressureChamberRecipeCategory(),
                new JEIAssemblyControllerCategory(),
                new JEIThermopneumaticProcessingPlantCategory(),
                new JEIRefineryCategory(),
                new JEIUVLightBoxCategory(),
                new JEIAmadronTradeCategory(),
                new JEIHeatFrameCoolingCategory()
        );
        if (PNCConfig.Common.Recipes.explosionCrafting) {
            registry.addRecipeCategories(new JEIExplosionCraftingCategory());
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JEIAssemblyControllerCategory.getAllRecipes(), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipes(JEIExplosionCraftingCategory.getAllRecipes(), ModCategoryUid.EXPLOSION_CRAFTING);
        registration.addRecipes(JEIHeatFrameCoolingCategory.getAllRecipes(), ModCategoryUid.HEAT_FRAME_COOLING);
        registration.addRecipes(JEIRefineryCategory.getAllRecipes(), ModCategoryUid.REFINERY);
        registration.addRecipes(JEIThermopneumaticProcessingPlantCategory.getAllRecipes(), ModCategoryUid.THERMO_PNEUMATIC);
        registration.addRecipes(JEIPressureChamberRecipeCategory.getAllRecipes(), ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipes(JEIUVLightBoxCategory.getAllRecipes(), ModCategoryUid.UV_LIGHT_BOX);
        registration.addRecipes(JEIAmadronTradeCategory.getAllRecipes(), ModCategoryUid.AMADRON_TRADE);

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
        registration.addRecipeClickArea(GuiPressureChamber.class, 100, 7, 60, 60, ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeClickArea(GuiRefineryController.class, 47, 21, 27, 47, ModCategoryUid.REFINERY);
        registration.addRecipeClickArea(GuiThermopneumaticProcessingPlant.class, 30, 31, 48, 20, ModCategoryUid.THERMO_PNEUMATIC);

        registration.addGlobalGuiHandler(new GuiTabHandler());
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        if (OneProbeCrafting.ONE_PROBE != null) {
            registration.getCraftingCategory().addCategoryExtension(OneProbeCrafting.class, HelmetOneProbeExtension::new);
        }
        registration.getCraftingCategory().addCategoryExtension(PatchouliBookCrafting.class, PatchouliBookExtension::new);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return RL("default");
    }
}
