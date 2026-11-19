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

package me.desht.pneumaticcraft.common.inventory.slot;

import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nonnull;

public class FluidContainerSlot extends ItemHandlerSlot {
    private final int minFluid;

    public FluidContainerSlot(ResourceHandler<ItemResource> handler, int index, int x, int y, int minFluid) {
        super(handler, index, x, y);

        this.minFluid = minFluid;
    }

    public FluidContainerSlot(ResourceHandler<ItemResource> handler, int index, int x, int y) {
        super(handler, index, x, y);

        this.minFluid = 0;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (IOHelper.getFluidHandlerForItem(stack).isPresent()) {
            FluidStack fluidStack = FluidUtils.getFirstFluidContained(stack);
            return fluidStack.isEmpty() ? minFluid == 0 : fluidStack.getAmount() >= minFluid;
        } else {
            return false;
        }
    }
}
