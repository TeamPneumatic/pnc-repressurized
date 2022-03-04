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
        this.recipe = PneumaticCraftRecipeType.assemblyDrillLaser.getRecipe(Minecraft.getInstance().level, recipeId);
        if (recipe == null) {
            this.recipe = PneumaticCraftRecipeType.assemblyDrill.getRecipe(Minecraft.getInstance().level, recipeId);
            if (recipe == null) {
                this.recipe = PneumaticCraftRecipeType.assemblyLaser.getRecipe(Minecraft.getInstance().level, recipeId);
            }
        }
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null) return null;

        ItemStack programStack = new ItemStack(AssemblyProgramItem.fromProgramType(recipe.getProgramType()));
        return switch (key) {
            case "input" -> Patchouli.Util.getStacks(recipe.getInput());
            case "output" -> IVariable.from(recipe.getOutput());
            case "program" -> IVariable.from(programStack);
            case "name" -> IVariable.wrap(recipe.getOutput().getHoverName().getString());
            case "desc" -> IVariable.wrap(xlate("pneumaticcraft.patchouli.processor.assembly.desc",
                    recipe.getOutput().getHoverName(),
                    programStack.getHoverName()
            ).getString());
            default -> null;
        };

    }

}
