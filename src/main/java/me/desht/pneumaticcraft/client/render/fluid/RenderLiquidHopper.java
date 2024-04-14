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
import me.desht.pneumaticcraft.common.block.entity.hopper.LiquidHopperBlockEntity;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.Collection;
import java.util.List;

public class RenderLiquidHopper extends AbstractFluidTER<LiquidHopperBlockEntity> {
    private static final AABB[] BOUNDS = new AABB[6];
    static {
        BOUNDS[Direction.DOWN.get3DDataValue()] = new AABB(0.51 / 16f, 0.51 / 16f, 0.51 / 16f, 15.49 / 16f, 4.99 / 16f, 15.49 / 16f);
        BOUNDS[Direction.UP.get3DDataValue()] = new AABB(0.51 / 16f, 11.01 / 16f, 0.51 / 16f, 15.49 / 16f, 15.49 / 16f, 15.49 / 16f);
        BOUNDS[Direction.NORTH.get3DDataValue()] = new AABB(0.51 / 16f, 0.51 / 16f, 0.51 / 16f, 15.49 / 16f, 15.49 / 16f, 4.99 / 16f);
        BOUNDS[Direction.SOUTH.get3DDataValue()] = new AABB(0.51 / 16f, 0.51 / 16f, 11.49 / 16f, 15.49 / 16f, 15.49 / 16f, 15.49 / 16f);
        BOUNDS[Direction.WEST.get3DDataValue()] = new AABB(0.51 / 16f, 0.51 / 16f, 0.51 / 16f, 4.99 / 16f, 15.49 / 16f, 15.49 / 16f);
        BOUNDS[Direction.EAST.get3DDataValue()] = new AABB(11.01 / 16f, 0.51 / 16f, 0.51 / 16f, 15.49 / 16f, 15.49 / 16f, 15.49 / 16f);
    }

    public RenderLiquidHopper(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(LiquidHopperBlockEntity te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), BOUNDS[te.getInputDirection().get3DDataValue()]));
    }

    public static class ItemRenderInfoProvider implements IFluidItemRenderInfoProvider {
        private static final AABB BOUNDS_UP = BOUNDS[Direction.UP.get3DDataValue()];  // item model is always oriented with input UP

        @Override
        public List<TankRenderInfo> getTanksToRender(ItemStack stack) {
            IFluidHandler h = IOHelper.getFluidHandlerForItem(stack).orElseThrow(RuntimeException::new);
            return List.of(new TankRenderInfo(h.getFluidInTank(0), h.getTankCapacity(0), BOUNDS_UP).without(Direction.DOWN));
        }
    }
}
