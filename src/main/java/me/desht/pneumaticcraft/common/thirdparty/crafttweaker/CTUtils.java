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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CTUtils {
    public static SizedIngredient toSizedIngredient(IIngredientWithAmount ingredient) {
        return new SizedIngredient(ingredient.ingredient().asVanillaIngredient(), ingredient.amount());
    }

    public static List<SizedIngredient> toSizedIngredientList(IIngredientWithAmount[] ingredients) {
        return Arrays.stream(ingredients).map(CTUtils::toSizedIngredient).toList();
    }

    public static List<ItemStack> toItemStacks(IItemStack[] stacks) {
        return Arrays.stream(stacks).map(IItemStack::getInternal).toList();
    }

    public static List<FluidStack> toFluidStacks(MCFluidStack[] stacks) {
        return Arrays.stream(stacks).map(MCFluidStack::getImmutableInternal).toList();
    }

    public static FluidIngredient toFluidIngredient(CTFluidIngredient ingredient) {
        return ingredient.mapTo(
                fStack -> FluidIngredient.of(new FluidStack(fStack.getFluid(), (int) fStack.getAmount())),
                (tag, amount) -> FluidIngredient.tag(tag),
                CTUtils::flattenIngredients
        );
    }

    public static SizedFluidIngredient toSizedFluidIngredient(CTFluidIngredient ingredient) {
        return ingredient.mapTo(
                fluidStack -> SizedFluidIngredient.of(fluidStack.getFluid(), (int) fluidStack.getAmount()),
                SizedFluidIngredient::of,
                stream -> stream.findFirst().map(ingr -> new SizedFluidIngredient(ingr.ingredient(), ingr.amount())).orElseThrow()
        );
    }

    private static FluidIngredient flattenIngredients(Stream<FluidIngredient> stream) {
        return FluidIngredient.of(stream.flatMap(ingredient -> Arrays.stream(ingredient.getStacks()))
                .map(FluidStack::getFluid)
                .distinct()
                .toArray(Fluid[]::new)
        );
    }
}
