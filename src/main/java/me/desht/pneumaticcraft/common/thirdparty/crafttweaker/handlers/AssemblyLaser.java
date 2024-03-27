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

package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/AssemblyLaser")
@ZenRegister
@ZenCodeType.Name("mods.pneumaticcraft.AssemblyLaser")
public class AssemblyLaser implements IRecipeManager<AssemblyRecipe> {
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, IItemStack output) {
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this,
                new RecipeHolder<>(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                new AssemblyRecipeImpl(
                        CTUtils.toStackedIngredient(input),
                        output.getImmutableInternal(),
                        AssemblyRecipe.AssemblyProgramType.LASER))
        ));
    }

    @Override
    public RecipeType<AssemblyRecipe> getRecipeType() {
        return ModRecipeTypes.ASSEMBLY_LASER.get();
    }
}
