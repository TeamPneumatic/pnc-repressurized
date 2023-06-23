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

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

@SuppressWarnings("unused")
public class ProcessorThermoPlant implements IComponentProcessor {
    private ThermoPlantRecipe recipe = null;
    private String header = null;

    @Override
    public void setup(Level level, IVariableProvider iVariableProvider) {
        ResourceLocation recipeId = new ResourceLocation(iVariableProvider.get("recipe").asString());
        this.recipe = ModRecipeTypes.THERMO_PLANT.get().getRecipe(Minecraft.getInstance().level, recipeId);
        this.header = iVariableProvider.has("header") ? iVariableProvider.get("header").asString() : "";
    }

    @Override
    public IVariable process(Level level, String s) {
        if (recipe == null) return null;

        switch (s) {
            case "header":
                return IVariable.wrap(header.isEmpty() ? defaultHeader() : header);
            case "item_input":
                return PatchouliAccess.getStacks(recipe.getInputItem());
            case "fluid_input":
                return PatchouliAccess.getFluidStacks(recipe.getInputFluid());
            case "item_output":
                return IVariable.from(recipe.getOutputItem());
            case "fluid_output":
                return IVariable.from(recipe.getOutputFluid());
            case "text":
                String pr = PneumaticCraftUtils.roundNumberTo(recipe.getRequiredPressure(), 1);
                String temp = recipe.getOperatingTemperature().asString(TemperatureRange.TemperatureScale.CELSIUS);
                return IVariable.wrap(I18n.get("pneumaticcraft.patchouli.processor.thermoPlant.desc", pr, temp));
            case "scale":
                return IVariable.wrap(getScale(recipe));
        }
        return null;
    }

    private int getScale(ThermoPlantRecipe recipe) {
        int in = recipe.getInputFluid().getAmount();
        int out = recipe.getOutputFluid().getAmount();
        if (in >= 4000 || out >= 4000) {
            return 16000;
        } else {
            return 2 * Math.max(in, out);
        }
    }

    private String defaultHeader() {
        if (!recipe.getOutputFluid().isEmpty()) {
            return recipe.getOutputFluid().getDisplayName().getString();
        } else if (!recipe.getOutputItem().isEmpty()) {
            return recipe.getOutputItem().getHoverName().getString();
        } else {
            return "";
        }
    }
}
