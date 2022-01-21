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
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe.RecipeSlot;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe.SlotCycle;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIPressureChamberRecipeCategory extends AbstractPNCCategory<PressureChamberRecipe> {
    private final ITickTimer tickTimer;

    JEIPressureChamberRecipeCategory() {
        super(ModCategoryUid.PRESSURE_CHAMBER, PressureChamberRecipe.class,
                xlate("pneumaticcraft.gui.pressureChamber"),
                guiHelper().createDrawable(Textures.GUI_JEI_PRESSURE_CHAMBER, 5, 11, 166, 116),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()))
        );
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    /**
     * Looks for the first slot that has more than one stack and has a match for the given item ignoring NBT.
     *
     * @return empty iff no match was found in a slot with more than one stack, or the matching slot cycle otherwise.
     */
    protected static Optional<SlotCycle> getMatchingCycle(IIngredients ingredients, IFocus<ItemStack> focus) {
        if (focus == null) return Optional.empty();
        List<List<ItemStack>> slots;
        IFocus.Mode mode = focus.getMode();
        if (mode == IFocus.Mode.INPUT) {
            slots = ingredients.getInputs(VanillaTypes.ITEM);
        } else if (mode == IFocus.Mode.OUTPUT) {
            slots = ingredients.getOutputs(VanillaTypes.ITEM);
        } else {
            return Optional.empty();
        }
        ItemStack needle = focus.getValue();
        // For each slot
        for (int slot = 0; slot < slots.size(); slot++) {
            List<ItemStack> stacks = slots.get(slot);
            // that has more than one item within
            if (stacks.size() > 1) {
                ImmutableList.Builder<Integer> builder = ImmutableList.builder();
                // find matching stacks
                for (int i = 0; i < stacks.size(); i++) {
                    if (needle.sameItem(stacks.get(i))) {
                        builder.add(i);
                    }
                }
                ImmutableList<Integer> matches = builder.build();
                if (matches.size() > 0) {
                    // Return the first slot that has matches
                    return Optional.of(new SlotCycle(new RecipeSlot(mode == IFocus.Mode.INPUT, slot), matches));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return slot cycles with the overrides applied if applicable.
     */
    protected static List<List<ItemStack>> applyOverrides(boolean isInput, List<List<ItemStack>> slotCycles, Map<RecipeSlot, List<Integer>> slotCycleOverrides) {
        ImmutableList.Builder<List<ItemStack>> builder = ImmutableList.builder();
        for (int i = 0; i < slotCycles.size(); i++) {
            List<ItemStack> stacks = slotCycles.get(i);
            // Apply cycle overrides if present
            List<Integer> cycleOverrides = slotCycleOverrides.get(new RecipeSlot(isInput, i));
            if (cycleOverrides != null) {
                builder.add(cycleOverrides.stream()
                        .map(stacks::get)
                        .filter(Objects::nonNull)
                        .collect(ImmutableList.toImmutableList()));
            } else {
                builder.add(stacks);
            }
        }
        return builder.build();
    }

    @Override
    public void setIngredients(PressureChamberRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(recipe.getInputsForDisplay());
        ingredients.setOutputLists(VanillaTypes.ITEM, recipe.getResultsForDisplay());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PressureChamberRecipe recipe, IIngredients ingredients) {
        Map<RecipeSlot, List<Integer>> overrides = getMatchingCycle(ingredients, recipeLayout.getFocus(VanillaTypes.ITEM))
                .map(recipe::getSyncForDisplay)
                .orElseGet(ImmutableMap::of);
        List<List<ItemStack>> inputs = applyOverrides(true, ingredients.getInputs(VanillaTypes.ITEM), overrides);
        for (int i = 0; i < inputs.size(); i++) {
            int posX = 18 + i % 3 * 17;
            int posY = 78 - i / 3 * 17;
            recipeLayout.getItemStacks().init(i, true, posX, posY);
            recipeLayout.getItemStacks().set(i, inputs.get(i));
        }
        List<List<ItemStack>> outputs = applyOverrides(false, ingredients.getOutputs(VanillaTypes.ITEM), overrides);
        for (int i = 0; i < outputs.size(); i++) {
            recipeLayout.getItemStacks().init(inputs.size() + i, false, 100 + i % 3 * 18, 58 + i / 3 * 18);
            recipeLayout.getItemStacks().set(inputs.size() + i, outputs.get(i));
        }
        recipeLayout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            String tooltipKey = recipe.getTooltipKey(input, slotIndex);
            if (!tooltipKey.isEmpty()) {
                tooltip.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get(tooltipKey)));
            }
        });
    }

    @Override
    public void draw(PressureChamberRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        float pressure = recipe.getCraftingPressureForDisplay() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
        PressureGaugeRenderer2D.drawPressureGauge(matrixStack, Minecraft.getInstance().font, -1, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, recipe.getCraftingPressureForDisplay(), pressure, 130, 27);
    }

    @Override
    public List<Component> getTooltipStrings(PressureChamberRecipe recipe, double mouseX, double mouseY) {
        if (mouseX >= 100 && mouseY >= 7 && mouseX <= 140 && mouseY <= 47) {
            return ImmutableList.of(xlate("pneumaticcraft.gui.tooltip.pressure", recipe.getCraftingPressureForDisplay()));
        }
        return Collections.emptyList();
    }
}
