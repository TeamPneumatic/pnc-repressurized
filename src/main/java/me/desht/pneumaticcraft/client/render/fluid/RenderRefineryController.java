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

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;
import java.util.Collections;

public class RenderRefineryController extends AbstractFluidTER<TileEntityRefineryController> {
    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[4];
    static {
        BOUNDS[0] = new AxisAlignedBB(2.1 / 16f, 1 / 16f, 3.1 / 16f, 13.9 / 16f, 15 / 16f, 14.9 / 16f);
        BOUNDS[1] = AbstractFluidTER.rotateY(BOUNDS[0], 90);
        BOUNDS[2] = AbstractFluidTER.rotateY(BOUNDS[0], 180);
        BOUNDS[3] = AbstractFluidTER.rotateY(BOUNDS[0], 270);
    }

    public RenderRefineryController(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityRefineryController te) {
        int rot = te.getRotation().get2DDataValue();
        if (rot >= 0 && rot < 4) {
            return Collections.singletonList(new TankRenderInfo(te.getInputTank(), BOUNDS[te.getRotation().get2DDataValue()], te.getRotation().getOpposite(), Direction.UP));
        } else {
            return Collections.emptyList();
        }
    }
}
