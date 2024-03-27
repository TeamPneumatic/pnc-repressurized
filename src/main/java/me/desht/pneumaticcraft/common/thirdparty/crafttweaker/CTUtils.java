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
import com.blamejared.crafttweaker.api.fluid.MCFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;

public class CTUtils {
    public static StackedIngredient toStackedIngredient(IIngredientWithAmount ingredient) {
        return StackedIngredient.fromIngredient(ingredient.getAmount(), ingredient.getIngredient().asVanillaIngredient());
    }

    public static List<StackedIngredient> toStackedIngredientList(IIngredientWithAmount[] ingredients) {
        return Arrays.stream(ingredients).map(CTUtils::toStackedIngredient).toList();
    }

    public static List<ItemStack> toItemStacks(IItemStack[] stacks) {
        return Arrays.stream(stacks).map(IItemStack::getInternal).toList();
    }

    public static List<FluidStack> toFluidStacks(MCFluidStack[] stacks) {
        return Arrays.stream(stacks).map(MCFluidStack::getImmutableInternal).toList();
    }

    public static FluidIngredient toFluidIngredient(CTFluidIngredient ingredient) {
        return ingredient.mapTo(
                fStack -> FluidIngredient.of((int) fStack.getAmount(), fStack.getFluid()),
                (tag, amount) -> FluidIngredient.of(amount, tag),
                FluidIngredient::ofFluidStream
        );
    }
}
