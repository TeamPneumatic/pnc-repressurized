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

package me.desht.pneumaticcraft.common.amadron;

import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class AmadronUtil {
    public static ItemStack[] buildStacks(ItemStack stack, int units) {
        int amount = stack.getCount() * units;
        List<ItemStack> stacks = new ArrayList<>();
        while (amount > 0 && stacks.size() < AmadronMenu.HARD_MAX_STACKS) {
            ItemStack toAdd = stack.copyWithCount(Math.min(amount, stack.getMaxStackSize()));
            stacks.add(toAdd);
            amount -= toAdd.getCount();
        }
        return stacks.toArray(new ItemStack[0]);
    }

    public static FluidStack buildFluidStack(FluidStack fluidStack, int units) {
        FluidStack res = fluidStack.copy();
        res.setAmount(Math.min(AmadronMenu.HARD_MAX_MB, res.getAmount() * units));
        return res;
    }
}
