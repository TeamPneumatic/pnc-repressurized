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

package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RenderLiquidHopper extends AbstractFluidTER<TileEntityLiquidHopper> {
    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[6];
    static {
        BOUNDS[Direction.DOWN.get3DDataValue()] = new AxisAlignedBB(0.51 / 16f, 0.51 / 16f, 0.51 / 16f, 15.49 / 16f, 4.99 / 16f, 15.49 / 16f);
        BOUNDS[Direction.UP.get3DDataValue()] = new AxisAlignedBB(0.51 / 16f, 11.01 / 16f, 0.51 / 16f, 15.49 / 16f, 15.49 / 16f, 15.49 / 16f);
        BOUNDS[Direction.NORTH.get3DDataValue()] = new AxisAlignedBB(0.51 / 16f, 0.51 / 16f, 0.51 / 16f, 15.49 / 16f, 15.49 / 16f, 4.99 / 16f);
        BOUNDS[Direction.SOUTH.get3DDataValue()] = new AxisAlignedBB(0.51 / 16f, 0.51 / 16f, 11.49 / 16f, 15.49 / 16f, 15.49 / 16f, 15.49 / 16f);
        BOUNDS[Direction.WEST.get3DDataValue()] = new AxisAlignedBB(0.51 / 16f, 0.51 / 16f, 0.51 / 16f, 4.99 / 16f, 15.49 / 16f, 15.49 / 16f);
        BOUNDS[Direction.EAST.get3DDataValue()] = new AxisAlignedBB(11.01 / 16f, 0.51 / 16f, 0.51 / 16f, 15.49 / 16f, 15.49 / 16f, 15.49 / 16f);
    }

    public RenderLiquidHopper(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityLiquidHopper te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), BOUNDS[te.getInputDirection().get3DDataValue()]));
    }

    public static class ItemRenderInfoProvider implements IFluidItemRenderInfoProvider {
        private static final AxisAlignedBB BOUNDS_UP = BOUNDS[Direction.UP.get3DDataValue()];  // item model is always oriented with input UP

        @Override
        public List<TankRenderInfo> getTanksToRender(ItemStack stack) {
            return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                    .map(h -> Collections.singletonList(new TankRenderInfo(h.getFluidInTank(0), h.getTankCapacity(0), BOUNDS_UP).without(Direction.DOWN)))
                    .orElse(Collections.emptyList());
        }
    }
}
