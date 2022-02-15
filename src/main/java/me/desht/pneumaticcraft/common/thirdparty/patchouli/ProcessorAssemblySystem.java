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

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.item.AssemblyProgramItem;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

@SuppressWarnings("unused")
public class ProcessorAssemblySystem implements IComponentProcessor {
    private AssemblyRecipe recipe = null;

    @Override
    public void setup(IVariableProvider iVariableProvider) {
        ResourceLocation recipeId = new ResourceLocation(iVariableProvider.get("recipe").asString());
        this.recipe = PneumaticCraftRecipeType.ASSEMBLY_DRILL_LASER.getRecipe(Minecraft.getInstance().level, recipeId);
        if (recipe == null) {
            this.recipe = PneumaticCraftRecipeType.ASSEMBLY_DRILL.getRecipe(Minecraft.getInstance().level, recipeId);
            if (recipe == null) {
                this.recipe = PneumaticCraftRecipeType.ASSEMBLY_LASER.getRecipe(Minecraft.getInstance().level, recipeId);
            }
        }
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null) return null;

        ItemStack programStack = new ItemStack(AssemblyProgramItem.fromProgramType(recipe.getProgramType()));
        switch (key) {
            case "input":
                return Patchouli.Util.getStacks(recipe.getInput());
            case "output":
                return IVariable.from(recipe.getOutput());
            case "program":
                return IVariable.from(programStack);
            case "name":
                return IVariable.wrap(recipe.getOutput().getHoverName().getString());
            case "desc":
                return IVariable.wrap(xlate("pneumaticcraft.patchouli.processor.assembly.desc",
                        recipe.getOutput().getHoverName(),
                        programStack.getHoverName()
                ).getString());
        }

        return null;
    }

}
