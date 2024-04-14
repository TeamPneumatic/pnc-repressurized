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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;

public interface IAutoFluidEjecting {
    default void autoExportFluid(AbstractPneumaticCraftBlockEntity te) {
        IOHelper.getFluidHandlerForBlock(te).ifPresent(handler -> {
            FluidStack toDrain = handler.drain(BUCKET_VOLUME, FluidAction.SIMULATE);
            if (!toDrain.isEmpty()) {
                Direction ejectDir = te.getUpgradeCache().getEjectDirection();
                if (ejectDir != null) {
                    tryEjectLiquid(te, handler, ejectDir, toDrain.getAmount());
                } else {
                    for (Direction d : DirectionUtil.VALUES) {
                        toDrain.setAmount(toDrain.getAmount() - tryEjectLiquid(te, handler, d, toDrain.getAmount()));
                        if (toDrain.getAmount() <= 0) break;
                    }
                }
            }
        });
    }

    default int tryEjectLiquid(AbstractPneumaticCraftBlockEntity te, IFluidHandler handler, Direction dir, int amount) {
        BlockEntity teNeighbour = te.getCachedNeighbor(dir);
        if (teNeighbour != null) {
            return IOHelper.getFluidHandlerForBlock(teNeighbour, dir.getOpposite()).map(destHandler -> {
                FluidStack fluidStack = FluidUtil.tryFluidTransfer(destHandler, handler, amount, true);
                return fluidStack.getAmount();
            }).orElse(0);
        }
        return 0;
    }
}
