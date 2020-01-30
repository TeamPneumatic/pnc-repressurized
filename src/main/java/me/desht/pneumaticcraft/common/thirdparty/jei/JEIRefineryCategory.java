package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class JEIRefineryCategory implements IRecipeCategory<IRefineryRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer tickTimer;
    private final Map<ResourceLocation, WidgetTemperature> tempWidgets = new HashMap<>();

    JEIRefineryCategory() {
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.REFINERY.get()));
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_REFINERY, 6, 3, 166, 79);
        localizedName = I18n.format(ModBlocks.REFINERY.get().getTranslationKey());
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.REFINERY;
    }

    @Override
    public Class<? extends IRefineryRecipe> getRecipeClass() {
        return IRefineryRecipe.class;
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
    public void setIngredients(IRefineryRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInput().getFluidStacks()));
        ingredients.setOutputs(VanillaTypes.FLUID, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRefineryRecipe recipe, IIngredients ingredients) {
        FluidStack in = ingredients.getInputs(VanillaTypes.FLUID).get(0).get(0);

        recipeLayout.getFluidStacks().init(0, true, 2, 10, 16, 64, in.getAmount(), true, Helpers.makeTankOverlay(64));
        recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));

        int n = 1;
        for (List<FluidStack> out : ingredients.getOutputs(VanillaTypes.FLUID)) {
            int h = out.get(0).getAmount() * 64 / in.getAmount();
            int yOff = 64 - h;
            recipeLayout.getFluidStacks().init(n, false, 69 + n * 20, 18 - n * 4 + yOff, 16, h, out.get(0).getAmount(), true, Helpers.makeTankOverlay(h));
            recipeLayout.getFluidStacks().set(n, out);
            n++;
        }
    }

    @Override
    public void draw(IRefineryRecipe recipe, double mouseX, double mouseY) {
        WidgetTemperature w = tempWidgets.computeIfAbsent(recipe.getId(),
                id -> Helpers.makeTemperatureWidget(26, 18, recipe.getOperatingTemp().getMin()));
        w.setTemperature(tickTimer.getValue() * (w.getScales()[0] - 273.0) / tickTimer.getMaxValue() + 273.0);
        w.renderButton((int)mouseX, (int)mouseY, 0f);
    }

    @Override
    public List<String> getTooltipStrings(IRefineryRecipe recipe, double mouseX, double mouseY) {
        WidgetTemperature w = tempWidgets.get(recipe.getId());
        if (w != null && w.isMouseOver(mouseX, mouseY)) {
            return ImmutableList.of(HeatUtil.formatHeatString(recipe.getOperatingTemp().getMin()).getFormattedText());
        }
        return Collections.emptyList();
    }

    static Collection<IRefineryRecipe> getAllRecipes() {
        return PneumaticCraftRecipes.refineryRecipes.values();
    }
}
