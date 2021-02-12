package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIPressureChamberRecipeCategory implements IRecipeCategory<PressureChamberRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer tickTimer;

    JEIPressureChamberRecipeCategory() {
//        super(jeiHelpers);
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_PRESSURE_CHAMBER, 5, 11, 166, 116);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()));
        localizedName = I18n.format("pneumaticcraft.gui.pressureChamber");
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.PRESSURE_CHAMBER;
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

    /**
     * Looks for the slot that has more than one stack and has a match for the given item ignoring NBT.
     *
     * @return -1 iff no match was found in a slot with more than one stack or the index of the stack otherwise.
     */
    protected static int getMatchingCycleIndex(List<List<ItemStack>> slots, ItemStack value) {
        for (List<ItemStack> stacks : slots) {
            if (stacks.size() > 1) {
                for (int i = 0; i < stacks.size(); i++) {
                    if (value.isItemEqual(stacks.get(i))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public void setIngredients(PressureChamberRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(recipe.getInputsForDisplay());
        // Needs a List<List<T>> instead of List<? extends List<T>>
        List<List<ItemStack>> results = recipe.getResultsForDisplay().stream()
                .map(list -> (List<ItemStack>) list)
                .collect(ImmutableList.toImmutableList());
        ingredients.setOutputLists(VanillaTypes.ITEM, results);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PressureChamberRecipe recipe, IIngredients ingredients) {
        List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);
        int cycleIndex = -1;
        IFocus<ItemStack> focus = recipeLayout.getFocus(VanillaTypes.ITEM);
        if (focus != null) {
            if (focus.getMode() == IFocus.Mode.INPUT) {
                cycleIndex = getMatchingCycleIndex(inputs, focus.getValue());
            } else if (focus.getMode() == IFocus.Mode.OUTPUT) {
                cycleIndex = getMatchingCycleIndex(outputs, focus.getValue());
            }
        }
        for (int i = 0; i < inputs.size(); i++) {
            int posX = 18 + i % 3 * 17;
            int posY = 78 - i / 3 * 17;
            recipeLayout.getItemStacks().init(i, true, posX, posY);
            List<ItemStack> stacks = inputs.get(i);
            if (cycleIndex >= 0 && cycleIndex < stacks.size()) {
                stacks = ImmutableList.of(stacks.get(cycleIndex));
            }
            recipeLayout.getItemStacks().set(i, stacks);
        }
        for (int i = 0; i < outputs.size(); i++) {
            List<ItemStack> stacks = outputs.get(i);
            if (cycleIndex >= 0 && cycleIndex < stacks.size()) {
                stacks = ImmutableList.of(stacks.get(cycleIndex));
            }
            recipeLayout.getItemStacks().init(inputs.size() + i, false, 100 + i % 3 * 18, 58 + i / 3 * 18);
            recipeLayout.getItemStacks().set(inputs.size() + i, stacks);
        }
        recipeLayout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            String tooltipKey = recipe.getTooltipKey(input, slotIndex);
            if (!tooltipKey.isEmpty()) {
                tooltip.addAll(PneumaticCraftUtils.splitStringComponent(I18n.format(tooltipKey)));
            }
        });
    }

    @Override
    public void draw(PressureChamberRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        float pressure = recipe.getCraftingPressure() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
        PressureGaugeRenderer2D.drawPressureGauge(matrixStack, Minecraft.getInstance().fontRenderer, -1, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, recipe.getCraftingPressure(), pressure, 130, 27);
    }

    @Override
    public Class<? extends PressureChamberRecipe> getRecipeClass() {
        return PressureChamberRecipe.class;
    }

    @Override
    public List<ITextComponent> getTooltipStrings(PressureChamberRecipe recipe, double mouseX, double mouseY) {
        if (mouseX >= 100 && mouseY >= 7 && mouseX <= 140 && mouseY <= 47) {
            return ImmutableList.of(xlate("pneumaticcraft.gui.tooltip.pressure", recipe.getCraftingPressure()));
        }
        return Collections.emptyList();
    }
}
