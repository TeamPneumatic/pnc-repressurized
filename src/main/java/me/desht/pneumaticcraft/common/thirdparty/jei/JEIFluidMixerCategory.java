package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIFluidMixerCategory implements IRecipeCategory<FluidMixerRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer tickTimer;
    private final IDrawableAnimated progressBar;

    public JEIFluidMixerCategory() {
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.FLUID_MIXER.get()));
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_FLUID_MIXER, 0, 0, 166, 70);
        localizedName = I18n.format(ModBlocks.FLUID_MIXER.get().getTranslationKey());
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
        IDrawableStatic d = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_FLUID_MIXER, 180, 0, 44, 30);
        progressBar = JEIPlugin.jeiHelpers.getGuiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.FLUID_MIXER;
    }

    @Override
    public Class<? extends FluidMixerRecipe> getRecipeClass() {
        return FluidMixerRecipe.class;
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
    public void setIngredients(FluidMixerRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, ImmutableList.of(recipe.getInput1().getFluidStacks(), recipe.getInput2().getFluidStacks()));

        if (!recipe.getOutputFluid().isEmpty()) {
            ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutputFluid());
        }
        if (!recipe.getOutputItem().isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutputItem());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidMixerRecipe recipe, IIngredients ingredients) {
        FluidStack in1 = ingredients.getInputs(VanillaTypes.FLUID).get(0).get(0);
        FluidStack in2 = ingredients.getInputs(VanillaTypes.FLUID).get(1).get(0);
        FluidStack outF = recipe.getOutputFluid();

        int[] amounts = new int[] { in1.getAmount(), in2.getAmount(), outF.getAmount() };
        int max = Arrays.stream(amounts).max().getAsInt();

        int inH1 = Math.min(64, in1.getAmount() * 64 / max);
        int inH2 = Math.min(64, in2.getAmount() * 64 / max);
        int outH = Math.min(64, outF.getAmount() * 64 / max);

        recipeLayout.getFluidStacks().init(0, true, 5, 3 + (64 - inH1), 16, inH1, in1.getAmount(), false, Helpers.makeTankOverlay(inH1));
        recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));

        recipeLayout.getFluidStacks().init(1, true, 28, 3 + (64 - inH2), 16, inH2, in2.getAmount(), false, Helpers.makeTankOverlay(inH2));
        recipeLayout.getFluidStacks().set(1, ingredients.getInputs(VanillaTypes.FLUID).get(1));

        if (!recipe.getOutputFluid().isEmpty()) {
            recipeLayout.getFluidStacks().init(2, false, 90, 3 + (64 - outH), 16, outH, outF.getAmount(), false, Helpers.makeTankOverlay(outH));
            recipeLayout.getFluidStacks().set(2, ingredients.getOutputs(VanillaTypes.FLUID).get(0));
        }
        if (!recipe.getOutputItem().isEmpty()) {
            recipeLayout.getItemStacks().init(3, false, 63, 50);
            recipeLayout.getItemStacks().set(3, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
        }
    }

    @Override
    public void draw(FluidMixerRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        float pressure = recipe.getRequiredPressure() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
        PressureGaugeRenderer2D.drawPressureGauge(matrixStack, Minecraft.getInstance().fontRenderer, -1, PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.DANGER_PRESSURE_TIER_ONE, recipe.getRequiredPressure(), pressure, 138, 35);

        progressBar.draw(matrixStack, 45, 20);
    }

    @Override
    public List<ITextComponent> getTooltipStrings(FluidMixerRecipe recipe, double mouseX, double mouseY) {
        List<ITextComponent> res = new ArrayList<>();
        if (recipe.getRequiredPressure() > 0 && mouseX >= 117 && mouseY >= 15 && mouseX <= 157 && mouseY <= 55) {
            res.add(xlate("pneumaticcraft.gui.tooltip.pressure", recipe.getRequiredPressure()));
        } else if (mouseX >= 45 && mouseY >= 20 && mouseX <= 89 && mouseY <= 50) {
            res.add(new StringTextComponent((recipe.getProcessingTime()) / 20f + "s"));
            res.add(xlate("pneumaticcraft.gui.jei.tooltip.processingTime").mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
        }
        return res;
    }
}
