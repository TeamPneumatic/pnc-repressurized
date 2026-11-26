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

package me.desht.pneumaticcraft.common.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class CountedFluidStacks extends Object2IntOpenCustomHashMap<FluidStack> {
    private static class FluidStackHashingStrategy implements Strategy<FluidStack> {
        @Override
        public int hashCode(FluidStack object) {
            return FluidStack.hashFluidAndComponents(object);
        }

        @Override
        public boolean equals(FluidStack o1, FluidStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && FluidStack.isSameFluidSameComponents(o1, o2);
        }
    }

    public CountedFluidStacks() {
        super(new FluidStackHashingStrategy());
    }

    public CountedFluidStacks(ResourceHandler<FluidResource> handler) {
        super(handler.size(), new FluidStackHashingStrategy());

        for (int i = 0; i < handler.size(); i++) {
            FluidStack stack = FluidUtils.getFluid(handler, i);
            if (!stack.isEmpty()) {
                int seenAlready = getInt(stack);
                put(stack, seenAlready + stack.getAmount());
            }
        }
    }

    public int adjust(FluidStack stack, int amount) {
        int adjusted = Math.max(0, getOrDefault(stack, 0) + amount);
        put(stack, adjusted);
        return adjusted;
    }
}
