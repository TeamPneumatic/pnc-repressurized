package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetItemFilter;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsBase;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.ItemPressurizable;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.AmadronAddTradeGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.LogisticsFilterGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.ProgWidgetItemFilterGhost;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.RegistryObject;

import java.util.regex.Pattern;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    static IJeiHelpers jeiHelpers;

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (RegistryObject<Item> item: ModItems.ITEMS.getEntries()) {
            if (item.get() instanceof ItemPressurizable) {
                registration.registerSubtypeInterpreter(item.get(),
                        s -> s.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                                .map(h2 -> String.valueOf(h2.getPressure()))
                                .orElse(ISubtypeInterpreter.NONE)
                );
            }
        }
        registration.registerSubtypeInterpreter(ModItems.EMPTY_PCB.get(), s -> String.valueOf(TileEntityUVLightBox.getExposureProgress(s)));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(
                new JEIPressureChamberRecipeCategory(),
                new JEIAssemblyControllerCategory(),
                new JEIThermopneumaticProcessingPlantCategory(),
                new JEIRefineryCategory(),
                new JEIFluidMixerCategory(),
                new JEIUVLightBoxCategory(),
                new JEIAmadronTradeCategory(),
                new JEIHeatFrameCoolingCategory(),
                new JEIPlasticSolidifyingCategory(),
                new JEIEtchingTankCategory(),
                new JEIYeastCraftingCategory()
        );
        if (PNCConfig.Common.Recipes.explosionCrafting) {
            registry.addRecipeCategories(new JEIExplosionCraftingCategory());
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // these all use recipes from the vanilla RecipeManager
        addRecipeType(registration, PneumaticCraftRecipeType.PRESSURE_CHAMBER, ModCategoryUid.PRESSURE_CHAMBER);
        addRecipeType(registration, PneumaticCraftRecipeType.EXPLOSION_CRAFTING, ModCategoryUid.EXPLOSION_CRAFTING);
        addRecipeType(registration, PneumaticCraftRecipeType.HEAT_FRAME_COOLING, ModCategoryUid.HEAT_FRAME_COOLING);
        addRecipeType(registration, PneumaticCraftRecipeType.REFINERY, ModCategoryUid.REFINERY);
        addRecipeType(registration, PneumaticCraftRecipeType.THERMO_PLANT, ModCategoryUid.THERMO_PLANT);
        addRecipeType(registration, PneumaticCraftRecipeType.ASSEMBLY_LASER, ModCategoryUid.ASSEMBLY_CONTROLLER);
        addRecipeType(registration, PneumaticCraftRecipeType.ASSEMBLY_DRILL, ModCategoryUid.ASSEMBLY_CONTROLLER);
        addRecipeType(registration, PneumaticCraftRecipeType.ASSEMBLY_DRILL_LASER, ModCategoryUid.ASSEMBLY_CONTROLLER);
        addRecipeType(registration, PneumaticCraftRecipeType.AMADRON_OFFERS, ModCategoryUid.AMADRON_TRADE);
        addRecipeType(registration, PneumaticCraftRecipeType.FLUID_MIXER, ModCategoryUid.FLUID_MIXER);

        // these have their own pseudo-recipes
        registration.addRecipes(JEIUVLightBoxCategory.getAllRecipes(), ModCategoryUid.UV_LIGHT_BOX);
        registration.addRecipes(JEIPlasticSolidifyingCategory.getAllRecipes(), ModCategoryUid.PLASTIC_SOLIDIFYING);
        registration.addRecipes(JEIEtchingTankCategory.getAllRecipes(), ModCategoryUid.ETCHING_TANK);
        registration.addRecipes(JEIYeastCraftingCategory.getAllRecipes(), ModCategoryUid.YEAST_CRAFTING);

        for (RegistryObject<Item> item: ModItems.ITEMS.getEntries()) {
            addStackInfo(registration, new ItemStack(item.get()));
        }
    }

    private void addRecipeType(IRecipeRegistration registration, PneumaticCraftRecipeType<?> type, ResourceLocation id) {
        registration.addRecipes(type.getRecipes(Minecraft.getInstance().world).values(), id);
    }

    private void addStackInfo(IRecipeRegistration registry, ItemStack stack) {
        String k = ICustomTooltipName.getTranslationKey(stack, false);
        if (I18n.hasKey(k)) {
            String raw = TextFormatting.getTextWithoutFormattingCodes(I18n.format(k));
            registry.addIngredientInfo(stack, VanillaTypes.ITEM, raw.split(Pattern.quote("${br}")));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.AMADRON_TABLET.get()), ModCategoryUid.AMADRON_TRADE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER.get()), ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()), ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_VALVE.get()), ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get()), ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_GLASS.get()), ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REFINERY.get()), ModCategoryUid.REFINERY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REFINERY_OUTPUT.get()), ModCategoryUid.REFINERY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()), ModCategoryUid.THERMO_PLANT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.UV_LIGHT_BOX.get()), ModCategoryUid.UV_LIGHT_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.HEAT_FRAME.get()), ModCategoryUid.HEAT_FRAME_COOLING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ETCHING_TANK.get()), ModCategoryUid.ETCHING_TANK);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLUID_MIXER.get()), ModCategoryUid.FLUID_MIXER);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GuiAssemblyController.class, 110, 10, 50, 50, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipeClickArea(GuiPressureChamber.class, 100, 7, 60, 60, ModCategoryUid.PRESSURE_CHAMBER);
        registration.addRecipeClickArea(GuiRefineryController.class, 47, 33, 27, 47, ModCategoryUid.REFINERY);
        registration.addRecipeClickArea(GuiThermopneumaticProcessingPlant.class, 30, 36, 48, 30, ModCategoryUid.THERMO_PLANT);
        registration.addRecipeClickArea(GuiFluidMixer.class, 50, 40, 47, 24, ModCategoryUid.FLUID_MIXER);

        registration.addGlobalGuiHandler(new GuiTabHandler());

        registration.addGuiScreenHandler(GuiProgWidgetItemFilter.class, Helpers::getGuiProperties);

        registration.addGhostIngredientHandler(GuiAmadronAddTrade.class, new AmadronAddTradeGhost());
        registration.addGhostIngredientHandler(GuiLogisticsBase.class, (IGhostIngredientHandler) new LogisticsFilterGhost());
        registration.addGhostIngredientHandler(GuiProgWidgetItemFilter.class, new ProgWidgetItemFilterGhost());
    }

    @Override
    public ResourceLocation getPluginUid() {
        return RL("default");
    }
}
