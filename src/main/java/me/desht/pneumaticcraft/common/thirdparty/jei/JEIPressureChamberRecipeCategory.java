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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe.RecipeSlot;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe.SlotCycle;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIPressureChamberRecipeCategory extends AbstractPNCCategory<PressureChamberRecipe> {
    private final ITickTimer tickTimer;

    JEIPressureChamberRecipeCategory() {
        super(RecipeTypes.PRESSURE_CHAMBER,
                xlate("pneumaticcraft.gui.pressureChamber"),
                guiHelper().createDrawable(Textures.GUI_JEI_PRESSURE_CHAMBER, 5, 11, 166, 116),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()))
        );
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    /**
     * Looks for the first slot that has more than one stack and has a match for the given item ignoring NBT.
     *
     * @return empty iff no match was found in a slot with more than one stack, or the matching slot cycle otherwise.
     */
    protected static Optional<SlotCycle> getMatchingCycle(PressureChamberRecipe recipe, IFocus<ItemStack> focus) {
        if (focus == null) return Optional.empty();
        List<List<ItemStack>> slots;
        RecipeIngredientRole role = focus.getRole();
        if (role == RecipeIngredientRole.INPUT) {
            slots = recipe.getInputsForDisplay(ClientUtils.getClientLevel().registryAccess()); //.stream().map(ingr -> Arrays.asList(ingr.getItems())).toList();
        } else if (role == RecipeIngredientRole.OUTPUT) {
            slots = new ArrayList<>(recipe.getResultsForDisplay(ClientUtils.getClientLevel().registryAccess()));
        } else {
            return Optional.empty();
        }
        ItemStack needle = focus.getTypedValue().getIngredient();
        // For each slot
        for (int slot = 0; slot < slots.size(); slot++) {
            List<ItemStack> stacks = slots.get(slot);
            // that has more than one item within
            if (stacks.size() > 1) {
                IntArrayList l = new IntArrayList();
                // find matching stacks
                for (int i = 0; i < stacks.size(); i++) {
                    if (ItemStack.isSameItem(needle, stacks.get(i))) {
                        l.add(i);
                    }
                }
                IntList matches = IntLists.unmodifiable(l);
                if (!matches.isEmpty()) {
                    // Return the first slot that has matches
                    return Optional.of(new SlotCycle(new RecipeSlot(role == RecipeIngredientRole.INPUT, slot), matches));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return slot cycles with the overrides applied if applicable.
     */
    protected static List<List<ItemStack>> applyOverrides(boolean isInput, List<List<ItemStack>> slotCycles, Map<RecipeSlot, IntList> slotCycleOverrides) {
        ImmutableList.Builder<List<ItemStack>> builder = ImmutableList.builder();
        for (int i = 0; i < slotCycles.size(); i++) {
            List<ItemStack> stacks = slotCycles.get(i);
            // Apply cycle overrides if present
            IntList cycleOverrides = slotCycleOverrides.get(new RecipeSlot(isInput, i));
            if (cycleOverrides != null) {
                builder.add(cycleOverrides.intStream()
                        .mapToObj(stacks::get)
                        .filter(Objects::nonNull)
                        .collect(ImmutableList.toImmutableList()));
            } else {
                builder.add(stacks);
            }
        }
        return builder.build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PressureChamberRecipe recipe, IFocusGroup focuses) {
        IFocus<ItemStack> focus = getItemStackFocus(focuses);
        Map<RecipeSlot, IntList> overrides = getMatchingCycle(recipe, focus)
                .map(recipe::getSyncForDisplay)
                .orElseGet(ImmutableMap::of);
        List<List<ItemStack>> l = recipe.getInputsForDisplay(ClientUtils.getClientLevel().registryAccess());//.stream().map(i -> Arrays.asList(i.getItems())).toList();
        List<List<ItemStack>> inputs = applyOverrides(true, l, overrides);
        for (int i = 0; i < inputs.size(); i++) {
            int posX = 19 + i % 3 * 17;
            int posY = 79 - i / 3 * 17;
            builder.addSlot(RecipeIngredientRole.INPUT, posX, posY)
                    .setSlotName("in" + i)
                    .addIngredients(VanillaTypes.ITEM_STACK, inputs.get(i))
                    .addRichTooltipCallback(new Tooltip(recipe));
        }

        List<List<ItemStack>> outputs = applyOverrides(false, recipe.getResultsForDisplay(ClientUtils.getClientLevel().registryAccess()), overrides);
        for (int i = 0; i < outputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 101 + i % 3 * 18, 59 + i / 3 * 18)
                    .setSlotName("out" + i)
                    .addItemStacks(outputs.get(i))
                    .addRichTooltipCallback(new Tooltip(recipe));
        }
    }

    private record Tooltip(PressureChamberRecipe recipe) implements IRecipeSlotRichTooltipCallback {
        @Override
        public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
            String tooltipKey = recipe.getTooltipKey(
                    recipeSlotView.getRole() == RecipeIngredientRole.INPUT,
                    recipeSlotView.getSlotName().orElse("")
            );
            if (!tooltipKey.isEmpty()) {
                tooltip.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get(tooltipKey)));
            }
        }
    }

    private IFocus<ItemStack> getItemStackFocus(IFocusGroup focuses) {
        //noinspection unchecked
        return (IFocus<ItemStack>) focuses.getFocuses(RecipeIngredientRole.INPUT)
                .filter(f -> f.getTypedValue().getIngredient() instanceof ItemStack)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void draw(PressureChamberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        float pressure = recipe.getCraftingPressureForDisplay() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
        PressureGaugeRenderer2D.drawPressureGauge(graphics, Minecraft.getInstance().font, -1, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, recipe.getCraftingPressureForDisplay(), pressure, 130, 27);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, PressureChamberRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(positionalTooltip(mouseX, mouseY, (x, y) -> x >= 100 && y >= 7 && x <= 140 && y <= 47,
                "pneumaticcraft.gui.tooltip.pressure", recipe.getCraftingPressureForDisplay()));
    }
}
