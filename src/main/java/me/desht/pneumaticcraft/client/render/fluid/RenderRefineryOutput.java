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

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryOutput;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;
import java.util.Collections;

public class RenderRefineryOutput extends AbstractFluidTER<TileEntityRefineryOutput> {
    private static final AxisAlignedBB BOUNDS_NS = new AxisAlignedBB(5 / 16f, 1 / 16f, 1 / 16f, 11 / 16f, 15 / 16f, 15 / 16f);
    private static final AxisAlignedBB BOUNDS_EW = new AxisAlignedBB(1 / 16f, 1 / 16f, 5 / 16f, 15 / 16f, 15 / 16f, 11 / 16f);

    public RenderRefineryOutput(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityRefineryOutput te) {
        return Collections.singletonList(new TankRenderInfo(te.getOutputTank(), te.getRotation().getAxis() == Direction.Axis.Z ? BOUNDS_NS : BOUNDS_EW).without(Direction.DOWN));
    }
}
