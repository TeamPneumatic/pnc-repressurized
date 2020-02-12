package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class JEIThermopneumaticProcessingPlantCategory implements IRecipeCategory<IThermopneumaticProcessingPlantRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer tickTimer;
    private final Map<ResourceLocation, WidgetTemperature> tempWidgets = new HashMap<>();

    JEIThermopneumaticProcessingPlantCategory() {
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()));
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT, 5, 11, 166, 70);
        localizedName = I18n.format(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get().getTranslationKey());
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.THERMO_PNEUMATIC;
    }

    @Override
    public Class<? extends IThermopneumaticProcessingPlantRecipe> getRecipeClass() {
        return IThermopneumaticProcessingPlantRecipe.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(IThermopneumaticProcessingPlantRecipe recipe, IIngredients ingredients) {
        if (!recipe.getInputFluid().hasNoMatchingItems()) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInputFluid().getFluidStacks()));
        }
        if (!recipe.getInputItem().hasNoMatchingItems()) {
            ingredients.setInputIngredients(Collections.singletonList(recipe.getInputItem()));
        }
        if (!recipe.getOutputFluid().isEmpty()) {
            ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutputFluid());
        }
        if (!recipe.getOutputItem().isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutputItem());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IThermopneumaticProcessingPlantRecipe recipe, IIngredients ingredients) {
        FluidStack in = ingredients.getInputs(VanillaTypes.FLUID).get(0).get(0);

        int inH = 64, outH = 64;
        FluidStack out = FluidStack.EMPTY;
        if (!recipe.getOutputFluid().isEmpty()) {
            out = ingredients.getOutputs(VanillaTypes.FLUID).get(0).get(0);
            if (in.getAmount() > out.getAmount()) {
                outH = Math.min(64, out.getAmount() * 64 / in.getAmount());
            } else {
                inH = Math.min(64, in.getAmount() * 64 / out.getAmount());
            }
        }

        if (!recipe.getInputFluid().hasNoMatchingItems()) {
            recipeLayout.getFluidStacks().init(0, true, 8, 3 + (64 - inH), 16, inH, in.getAmount(), false, Helpers.makeTankOverlay(inH));
            recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        }
        if (!recipe.getInputItem().hasNoMatchingItems()) {
            recipeLayout.getItemStacks().init(0, true, 32, 2);
            recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        }
        if (!recipe.getOutputFluid().isEmpty()) {
            recipeLayout.getFluidStacks().init(1, false, 74, 3 + (64 - outH), 16, outH, out.getAmount(), false, Helpers.makeTankOverlay(outH));
            recipeLayout.getFluidStacks().set(1, recipe.getOutputFluid());
        }
        if (!recipe.getOutputItem().isEmpty()) {
            recipeLayout.getItemStacks().init(1, false, 47, 50);
            recipeLayout.getItemStacks().set(1, recipe.getOutputItem());
        }
    }

    @Override
    public void draw(IThermopneumaticProcessingPlantRecipe recipe, double mouseX, double mouseY) {
        if (recipe.getRequiredPressure() > 0) {
            float pressure = recipe.getRequiredPressure() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
            GuiUtils.drawPressureGauge(Minecraft.getInstance().fontRenderer, -1, PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.DANGER_PRESSURE_TIER_ONE, recipe.getRequiredPressure(), pressure, 136, 42);
        }

        WidgetTemperature w = tempWidgets.computeIfAbsent(recipe.getId(),
                id -> Helpers.makeTemperatureWidget(92, 12, recipe.getOperatingTemperature().getMin()));
        w.setTemperature(tickTimer.getValue() * (w.getScales()[0] - 273.0) / tickTimer.getMaxValue() + 273.0);
        w.render((int)mouseX, (int)mouseY, 0f);
    }

    static Collection<IThermopneumaticProcessingPlantRecipe> getAllRecipes() {
        return PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes.values();
    }

    @Override
    public List<String> getTooltipStrings(IThermopneumaticProcessingPlantRecipe recipe, double mouseX, double mouseY) {
        List<String> res = new ArrayList<>();
        WidgetTemperature w = tempWidgets.get(recipe.getId());
        if (w != null && w.isMouseOver(mouseX, mouseY)) {
            res.add(HeatUtil.formatHeatString(recipe.getOperatingTemperature().getMin()).getFormattedText());
        }
        if (recipe.getRequiredPressure() > 0 && mouseX >= 116 && mouseY >= 22 && mouseX <= 156 && mouseY <= 62) {
            res.add(I18n.format("gui.tooltip.pressure", recipe.getRequiredPressure()));
        }
        return res;
    }
}
