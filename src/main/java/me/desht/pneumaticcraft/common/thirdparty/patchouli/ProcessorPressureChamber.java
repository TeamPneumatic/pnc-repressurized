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

package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.List;

@SuppressWarnings("unused")
public class ProcessorPressureChamber implements IComponentProcessor {
    private PressureChamberRecipe recipe = null;
    private String header = null;

    @Override
    public void setup(Level level, IVariableProvider iVariableProvider) {
        ResourceLocation recipeId = new ResourceLocation(iVariableProvider.get("recipe").asString());
        ModRecipeTypes.PRESSURE_CHAMBER.get().getRecipe(Minecraft.getInstance().level, recipeId)
                .ifPresentOrElse(h -> recipe = h.value(),
                        () -> Log.warning("Missing pressure chamber recipe: " + recipeId));
        this.header = iVariableProvider.has("header") ? iVariableProvider.get("header").asString() : "";
    }

    @Override
    public IVariable process(Level level, String s) {
        if (recipe == null) return null;

        if (s.equals("header")) {
            return IVariable.wrap(header.isEmpty() ? defaultHeader() : header);
        } else if (s.startsWith("input")) {
            int index = Integer.parseInt(s.substring(5)) - 1;
            if (index >= 0 && index < recipe.getInputsForDisplay().size()) {
                return PatchouliAccess.getStacks(recipe.getInputsForDisplay().get(index));
            }
        } else if (s.startsWith("output")) {
            int index = Integer.parseInt(s.substring(6)) - 1;
            List<? extends List<ItemStack>> results = recipe.getResultsForDisplay();
            if (index >= 0 && index < results.size()) {
                return IVariable.wrapList(results.get(index).stream().map(IVariable::from).collect(ImmutableList.toImmutableList()));
            }
        } else if (s.equals("pressure")) {
            String pr = PneumaticCraftUtils.roundNumberTo(recipe.getCraftingPressureForDisplay(), 1);
            return IVariable.wrap(I18n.get("pneumaticcraft.patchouli.processor.pressureChamber.desc", pr));
        }

        return null;
    }

    private String defaultHeader() {
        // note: only returns first item. use a custom "header" if needed
        List<? extends List<ItemStack>> results = recipe.getResultsForDisplay();
        if (!results.isEmpty()) {
            List<ItemStack> stacks = results.get(0);
            if (!stacks.isEmpty()) {
                return stacks.get(0).getHoverName().getString();
            }
        }
        return "";
    }
}
