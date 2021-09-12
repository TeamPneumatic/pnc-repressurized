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
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(4.1 / 16f, 1 / 16f, 0.1 / 16f, 11.9 / 16f, 13.9 / 16f, 3 / 16f);
//    private static final AxisAlignedBB BOUNDS_EW = new AxisAlignedBB(13.9 / 16f, 1 / 16f, 4.1 / 16f, 15.9 / 16f, 13.9 / 16f, 11.9 / 16f);

    public RenderRefineryOutput(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[4];
    static {
        BOUNDS[0] = TANK_BOUNDS;
        BOUNDS[1] = AbstractFluidTER.rotateY(BOUNDS[0], 90);
        BOUNDS[2] = AbstractFluidTER.rotateY(BOUNDS[1], 90);
        BOUNDS[3] = AbstractFluidTER.rotateY(BOUNDS[2], 90);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityRefineryOutput te) {
        return Collections.singletonList(new TankRenderInfo(te.getOutputTank(), BOUNDS[te.getRotation().get2DDataValue()]).without(Direction.DOWN));
    }


}
