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
import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/ExplosionCrafting")
@ZenCodeType.Name("mods.pneumaticcraft.explosioncrafting")
@ZenRegister
public class ExplosionCrafting implements IRecipeManager<ExplosionCraftingRecipe> {
    public static final String name = "PneumaticCraft Explosion Crafting";

    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, IItemStack[] outputs, int lossRate) {
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this,
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath("crafttweaker", fixRecipeName(name)),
                new ExplosionCraftingRecipeImpl(
                        CTUtils.toSizedIngredient(input),
                        lossRate,
                        CTUtils.toItemStacks(outputs))
                )));
    }

    @Override
    public void remove(IIngredient output) {
        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(this, iRecipe -> {
            if (iRecipe != null) {
                return iRecipe.value().getOutputs().stream()
                        .anyMatch(stack -> output.matches(new MCItemStack(stack)));
            }
            return false;
        }));
    }

    @Override
    public RecipeType<ExplosionCraftingRecipe> getRecipeType() {
        return ModRecipeTypes.EXPLOSION_CRAFTING.get();
    }
}
