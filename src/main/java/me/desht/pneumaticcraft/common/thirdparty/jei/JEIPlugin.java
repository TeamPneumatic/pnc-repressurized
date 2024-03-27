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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.recipe.PneumaticCraftRecipe;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetItemFilterScreen;
import me.desht.pneumaticcraft.client.gui.semiblock.AbstractLogisticsScreen;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.PressurizableItem;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.AmadronAddTradeGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.LogisticsFilterGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.ghost.ProgWidgetItemFilterGhost;
import me.desht.pneumaticcraft.common.thirdparty.jei.transfer.ProgrammerTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    static IJeiHelpers jeiHelpers;
    static IRecipeManager recipeManager;
    static IRecipesGui recipesGui;

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (var item: ModItems.ITEMS.getEntries()) {
            if (item.get() instanceof PressurizableItem) {
                registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item.get(),
                        (stack, ctx) -> PNCCapabilities.getAirHandler(stack)
                                .map(airHandler -> String.valueOf(airHandler.getPressure()))
                                .orElse(IIngredientSubtypeInterpreter.NONE)
                );
            }
        }
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.EMPTY_PCB.get(),
                (s, ctx) -> String.valueOf(UVLightBoxBlockEntity.getExposureProgress(s)));
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
                new JEIMemoryEssenceCategory(),
                new JEIExplosionCraftingCategory()
        );
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
        addRecipeType(registration, ModRecipeTypes.PRESSURE_CHAMBER.get(), RecipeTypes.PRESSURE_CHAMBER);
        addRecipeType(registration, ModRecipeTypes.HEAT_FRAME_COOLING.get(), RecipeTypes.HEAT_FRAME_COOLING);
        addRecipeType(registration, ModRecipeTypes.REFINERY.get(), RecipeTypes.REFINERY);
        addRecipeType(registration, ModRecipeTypes.THERMO_PLANT.get(), RecipeTypes.THERMO_PLANT);
        addRecipeType(registration, ModRecipeTypes.ASSEMBLY_LASER.get(), RecipeTypes.ASSEMBLY);
        addRecipeType(registration, ModRecipeTypes.ASSEMBLY_DRILL.get(), RecipeTypes.ASSEMBLY);
        addRecipeType(registration, ModRecipeTypes.ASSEMBLY_DRILL_LASER.get(), RecipeTypes.ASSEMBLY);
        addRecipeType(registration, ModRecipeTypes.AMADRON.get(), RecipeTypes.AMADRON_TRADE);
        addRecipeType(registration, ModRecipeTypes.FLUID_MIXER.get(), RecipeTypes.FLUID_MIXER);
        addRecipeType(registration, ModRecipeTypes.EXPLOSION_CRAFTING.get(), RecipeTypes.EXPLOSION_CRAFTING);

        // these have their own pseudo-recipes
        registration.addRecipes(RecipeTypes.UV_LIGHT_BOX, JEIUVLightBoxCategory.getAllRecipes());
        registration.addRecipes(RecipeTypes.ETCHING_TANK, JEIEtchingTankCategory.getAllRecipes());
        registration.addRecipes(RecipeTypes.SPAWNER_EXTRACTION, JEISpawnerExtractionCategory.getAllRecipes());
        registration.addRecipes(RecipeTypes.MEMORY_ESSENCE, JEIMemoryEssenceCategory.getAllRecipes());
        if (ConfigHelper.common().recipes.inWorldPlasticSolidification.get()) {
            registration.addRecipes(RecipeTypes.PLASTIC_SOLIDIFYING, JEIPlasticSolidifyingCategory.getAllRecipes());
        }
        if (ConfigHelper.common().recipes.inWorldYeastCrafting.get()) {
            registration.addRecipes(RecipeTypes.YEAST_CRAFTING, JEIYeastCraftingCategory.getAllRecipes());
        }

        // even though heat properties are in the vanilla recipe system, we use a custom registration here
        // so we can pull extra entries from the BlockHeatProperties manager (auto-registered fluids etc.)
        registration.addRecipes(RecipeTypes.HEAT_PROPERTIES, JEIBlockHeatPropertiesCategory.getAllRecipes());

        for (var item: ModItems.ITEMS.getEntries()) {
            addStackInfo(registration, new ItemStack(item.get()));
        }
    }

    private <T extends PneumaticCraftRecipe> void addRecipeType(IRecipeRegistration registration, PneumaticCraftRecipeType<T> type, RecipeType<T> recipeType) {
        registration.addRecipes(recipeType, ImmutableList.copyOf(type.allRecipes(Minecraft.getInstance().level)));
    }

    private void addStackInfo(IRecipeRegistration registry, ItemStack stack) {
        String k = ICustomTooltipName.getTranslationKey(stack, false);
        if (I18n.exists(k)) {
            for (String s : StringUtils.splitByWholeSeparator(I18n.get(k), GuiUtils.TRANSLATION_LINE_BREAK)) {
                registry.addIngredientInfo(stack, VanillaTypes.ITEM_STACK, Component.literal(s));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.AMADRON_TABLET.get()), RecipeTypes.AMADRON_TRADE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER.get()), RecipeTypes.ASSEMBLY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()), RecipeTypes.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_VALVE.get()), RecipeTypes.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get()), RecipeTypes.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PRESSURE_CHAMBER_GLASS.get()), RecipeTypes.PRESSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REFINERY.get()), RecipeTypes.REFINERY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REFINERY_OUTPUT.get()), RecipeTypes.REFINERY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()), RecipeTypes.THERMO_PLANT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.UV_LIGHT_BOX.get()), RecipeTypes.UV_LIGHT_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.HEAT_FRAME.get()), RecipeTypes.HEAT_FRAME_COOLING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ETCHING_TANK.get()), RecipeTypes.ETCHING_TANK);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLUID_MIXER.get()), RecipeTypes.FLUID_MIXER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SPAWNER_EXTRACTOR.get()), RecipeTypes.SPAWNER_EXTRACTION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.HEAT_PIPE.get()), RecipeTypes.HEAT_PROPERTIES);
        registration.addRecipeCatalyst(new ItemStack(ModItems.MEMORY_ESSENCE_BUCKET.get()), RecipeTypes.MEMORY_ESSENCE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.TNT), RecipeTypes.EXPLOSION_CRAFTING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AssemblyControllerScreen.class, 110, 10, 50, 50, RecipeTypes.ASSEMBLY);
        registration.addRecipeClickArea(PressureChamberScreen.class, 100, 7, 60, 60, RecipeTypes.PRESSURE_CHAMBER);
        CustomRecipeClickArea.add(registration, RefineryControllerScreen.class, 47, 33, 27, 47, RecipeTypes.REFINERY);
        CustomRecipeClickArea.add(registration, ThermoPlantScreen.class, 30, 36, 48, 30, RecipeTypes.THERMO_PLANT);
        CustomRecipeClickArea.add(registration, FluidMixerScreen.class, 50, 40, 47, 24, RecipeTypes.FLUID_MIXER);

        registration.addGenericGuiContainerHandler(AbstractPneumaticCraftContainerScreen.class, new GuiTabHandler());

        registration.addGuiScreenHandler(ProgWidgetItemFilterScreen.class, Helpers::getGuiProperties);

        registration.addGhostIngredientHandler(AmadronAddTradeScreen.class, new AmadronAddTradeGhost());
        registration.addGhostIngredientHandler(AbstractLogisticsScreen.class, new LogisticsFilterGhost<>());
        registration.addGhostIngredientHandler(ProgWidgetItemFilterScreen.class, new ProgWidgetItemFilterGhost());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        recipeManager = jeiRuntime.getRecipeManager();
        recipesGui = jeiRuntime.getRecipesGui();

        NeoForge.EVENT_BUS.register(ProgrammerTransferHandler.Listener.class);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addUniversalRecipeTransferHandler(new ProgrammerTransferHandler(registration.getTransferHelper()));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return RL("default");
    }

    public static class GuiTabHandler implements IGuiContainerHandler<AbstractPneumaticCraftContainerScreen<?,?>> {
        @Override
        public List<Rect2i> getGuiExtraAreas(AbstractPneumaticCraftContainerScreen<?,?> containerScreen) {
            return containerScreen.getTabRectangles();
        }
    }
}
