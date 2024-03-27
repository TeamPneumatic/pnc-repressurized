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

import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractFluidTankBlockEntity;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RenderFluidTank extends AbstractFluidTER<AbstractFluidTankBlockEntity> {
    private static final AABB BOUNDS_NONE = new AABB(2.01 / 16f, 1.01 / 16f, 2.01 / 16f, 13.99 / 16f, 14.99 / 16f, 13.99 / 16f);
    private static final AABB BOUNDS_UP = new AABB(2.01 / 16f, 1.01 / 16f, 2.01 / 16f, 13.99 / 16f, 16 / 16f, 13.99 / 16f);
    private static final AABB BOUNDS_DOWN = new AABB(2.01 / 16f, 0 / 16f, 2.01 / 16f, 13.99 / 16f, 14.99 / 16f, 13.99 / 16f);
    private static final AABB BOUNDS_BOTH = new AABB(2.01 / 16f, 0 / 16f, 2.01 / 16f, 13.99 / 16f, 16 / 16f, 13.99 / 16f);

    public RenderFluidTank(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(AbstractFluidTankBlockEntity te) {
        boolean up = te.getBlockState().getValue(AbstractPneumaticCraftBlock.UP);
        boolean down = te.getBlockState().getValue(AbstractPneumaticCraftBlock.DOWN);
        AABB bounds;
        if (up && down)
            bounds = BOUNDS_BOTH;
        else if (up)
            bounds = BOUNDS_UP;
        else if (down)
            bounds = BOUNDS_DOWN;
        else
            bounds = BOUNDS_NONE;
        return Collections.singletonList(new FluidTankRenderInfo(te.getTank(), up, down, bounds));
    }

    public static class ItemRenderInfoProvider implements IFluidItemRenderInfoProvider {
        @Override
        public List<TankRenderInfo> getTanksToRender(ItemStack stack) {
            IFluidHandler h = IOHelper.getFluidHandlerForItem(stack).orElseThrow(RuntimeException::new);
            return List.of(new TankRenderInfo(h.getFluidInTank(0), h.getTankCapacity(0), BOUNDS_NONE));
        }
    }

    private static class FluidTankRenderInfo extends TankRenderInfo {
        private final boolean up;
        private final boolean down;

        FluidTankRenderInfo(IFluidTank tank, boolean up, boolean down, AABB bounds) {
            super(tank, bounds);
            this.up = up;
            this.down = down;
        }

        @Override
        public boolean shouldRender(Direction face) {
            return switch (face) {
                case UP -> up
                        || getTank().getFluid().getAmount() < getTank().getCapacity()
                        && !getTank().getFluid().getFluid().getFluidType().isLighterThanAir();
                case DOWN -> down
                        || getTank().getFluid().getAmount() < getTank().getCapacity()
                        && getTank().getFluid().getFluid().getFluidType().isLighterThanAir();
                default -> true;
            };
        }
    }
}
