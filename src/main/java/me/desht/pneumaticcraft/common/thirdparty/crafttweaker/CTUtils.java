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

package me.desht.pneumaticcraft.common.thirdparty.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CTUtils {
    public static Ingredient toStackedIngredient(IIngredientWithAmount ingredient) {
        return StackedIngredient.fromIngredient(ingredient.getAmount(), ingredient.getIngredient().asVanillaIngredient());
    }

    public static List<Ingredient> toStackedIngredientList(IIngredientWithAmount[] ingredients) {
        return Arrays.stream(ingredients).map(CTUtils::toStackedIngredient).collect(Collectors.toList());
    }

    public static ItemStack[] toItemStacks(IItemStack[] stacks) {
        return Arrays.stream(stacks).map(IItemStack::getInternal).toArray(ItemStack[]::new);
    }

    public static FluidStack[] toFluidStacks(IFluidStack[] stacks) {
        return Arrays.stream(stacks).map(IFluidStack::getImmutableInternal).toArray(FluidStack[]::new);
    }

    public static FluidIngredient toFluidIngredient(CTFluidIngredient ingredient) {
        // TODO is this OK?
        return ingredient.mapTo(fStack -> FluidIngredient.of((FluidStack) fStack.getImmutableInternal()), (tag, amount) -> FluidIngredient.of(amount, tag), FluidIngredient::ofFluidStream);
    }
}
