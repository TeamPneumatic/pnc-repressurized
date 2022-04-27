/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetItemFilter;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsBase;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.ItemPressurizable;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.AmadronAddTradeGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.LogisticsFilterGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.ProgWidgetItemFilterGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.transfer.ProgrammerTransferHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.RegistryObject;
import org.apache.commons.lang3.StringUtils;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    static IJeiHelpers jeiHelpers;
    static IRecipeManager recipeManager;
    static IRecipesGui recipesGui;

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (RegistryObject<Item> item: ModItems.ITEMS.getEntries()) {
            if (item.get() instanceof ItemPressurizable) {
                registration.registerSubtypeInterpreter(item.get(),
                        (s, ctx) -> s.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                                .map(h2 -> String.valueOf(h2.getPressure()))
                                .orElse(IIngredientSubtypeInterpreter.NONE)
                );
            }
        }
        registration.registerSubtypeInterpreter(ModItems.EMPTY_PCB.get(), (s, ctx) -> String.valueOf(TileEntityUVLightBox.getExposureProgress(s)));
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
                new JEIEtchingTankCategory(),
                new JEISpawnerExtractionCategory(),
                new JEIBlockHeatPropertiesCategory(),
                new JEIMemoryEssenceCategory()
        );
        if (ConfigHelper.common().recipes.explosionCrafting.get()) {
            registry.addRecipeCategories(new JEIExplosionCraftingCategory());
        }
        if (ConfigHelper.common().recipes.inWorldPlasticSolidification.get()) {
            registry.addRecipeCategories(new JEIPlasticSolidifyingCategory());
        }
        if (ConfigHelper.common().recipes.inWorldYeastCrafting.get()) {
            registry.addRecipeCategories(new JEIYeastCraftingCategory());
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // these all use recipes from the vanilla RecipeManager
        addRecipeType(registration, PneumaticCraftRecipeType.PRESSURE_CHAMBER, ModCategoryUid.PRESSURE_CHAMBER);
        addRecipeType(registration, PneumaticCraftRecipeType.HEAT_FRAME_COOLING, ModCategoryUid.HEAT_FRAME_COOLING);
        addRecipeType(registration, PneumaticCraftRecipeType.REFINERY, ModCategoryUid.REFINERY);
        addRecipeType(registration, PneumaticCraftRecipeType.THERMO_PLANT, ModCategoryUid.THERMO_PLANT);
        addRecipeType(registration, PneumaticCraftRecipeType.ASSEMBLY_LASER, ModCategoryUid.ASSEMBLY_CONTROLLER);
        addRecipeType(registration, PneumaticCraftRecipeType.ASSEMBLY_DRILL, ModCategoryUid.ASSEMBLY_CONTROLLER);
        addRecipeType(registration, PneumaticCraftRecipeType.ASSEMBLY_DRILL_LASER, ModCategoryUid.ASSEMBLY_CONTROLLER);
        addRecipeType(registration, PneumaticCraftRecipeType.AMADRON_OFFERS, ModCategoryUid.AMADRON_TRADE);
        addRecipeType(registration, PneumaticCraftRecipeType.FLUID_MIXER, ModCategoryUid.FLUID_MIXER);
        if (ConfigHelper.common().recipes.explosionCrafting.get()) {
            addRecipeType(registration, PneumaticCraftRecipeType.EXPLOSION_CRAFTING, ModCategoryUid.EXPLOSION_CRAFTING);
        }

        // these have their own pseudo-recipes
        registration.addRecipes(JEIUVLightBoxCategory.getAllRecipes(), ModCategoryUid.UV_LIGHT_BOX);
        registration.addRecipes(JEIEtchingTankCategory.getAllRecipes(), ModCategoryUid.ETCHING_TANK);
        registration.addRecipes(JEISpawnerExtractionCategory.getAllRecipes(), ModCategoryUid.SPAWNER_EXTRACTION);
        registration.addRecipes(JEIMemoryEssenceCategory.getAllRecipes(), ModCategoryUid.MEMORY_ESSENCE);
        if (ConfigHelper.common().recipes.inWorldPlasticSolidification.get()) {
            registration.addRecipes(JEIPlasticSolidifyingCategory.getAllRecipes(), ModCategoryUid.PLASTIC_SOLIDIFYING);
        }
        if (ConfigHelper.common().recipes.inWorldYeastCrafting.get()) {
            registration.addRecipes(JEIYeastCraftingCategory.getAllRecipes(), ModCategoryUid.YEAST_CRAFTING);
        }

        // even though heat properties are in the vanilla recipe system, we use a custom registration here
        // so we can pull extra entries from the BlockHeatProperties manager (auto-registered fluids etc.)
        registration.addRecipes(JEIBlockHeatPropertiesCategory.getAllRecipes(), ModCategoryUid.HEAT_PROPERTIES);

        for (RegistryObject<Item> item: ModItems.ITEMS.getEntries()) {
            addStackInfo(registration, new ItemStack(item.get()));
        }
    }

    private void addRecipeType(IRecipeRegistration registration, PneumaticCraftRecipeType<?> type, ResourceLocation id) {
        registration.addRecipes(type.getRecipes(Minecraft.getInstance().level).values(), id);
    }

    private void addStackInfo(IRecipeRegistration registry, ItemStack stack) {
        String k = ICustomTooltipName.getTranslationKey(stack, false);
        if (I18n.exists(k)) {
            for (String s : StringUtils.splitByWholeSeparator(I18n.get(k), GuiUtils.TRANSLATION_LINE_BREAK)) {
                registry.addIngredientInfo(stack, VanillaTypes.ITEM, new StringTextComponent(s));
            }
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
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SPAWNER_EXTRACTOR.get()), ModCategoryUid.SPAWNER_EXTRACTION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.HEAT_PIPE.get()), ModCategoryUid.HEAT_PROPERTIES);
        registration.addRecipeCatalyst(new ItemStack(ModItems.MEMORY_ESSENCE_BUCKET.get()), ModCategoryUid.MEMORY_ESSENCE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GuiAssemblyController.class, 110, 10, 50, 50, ModCategoryUid.ASSEMBLY_CONTROLLER);
        registration.addRecipeClickArea(GuiPressureChamber.class, 100, 7, 60, 60, ModCategoryUid.PRESSURE_CHAMBER);
        CustomRecipeClickArea.add(registration, GuiRefineryController.class, 47, 33, 27, 47, ModCategoryUid.REFINERY);
        CustomRecipeClickArea.add(registration, GuiThermopneumaticProcessingPlant.class, 30, 36, 48, 30, ModCategoryUid.THERMO_PLANT);
        CustomRecipeClickArea.add(registration, GuiFluidMixer.class, 50, 40, 47, 24, ModCategoryUid.FLUID_MIXER);

        registration.addGenericGuiContainerHandler(GuiPneumaticContainerBase.class, new GuiTabHandler());

        registration.addGuiScreenHandler(GuiProgWidgetItemFilter.class, Helpers::getGuiProperties);

        registration.addGhostIngredientHandler(GuiAmadronAddTrade.class, new AmadronAddTradeGhost());
        registration.addGhostIngredientHandler(GuiLogisticsBase.class, new LogisticsFilterGhost());
        registration.addGhostIngredientHandler(GuiProgWidgetItemFilter.class, new ProgWidgetItemFilterGhost());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addUniversalRecipeTransferHandler(new ProgrammerTransferHandler(registration.getTransferHelper()));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        recipeManager = jeiRuntime.getRecipeManager();
        recipesGui = jeiRuntime.getRecipesGui();
    }

    @Override
    public ResourceLocation getPluginUid() {
        return RL("default");
    }

}
