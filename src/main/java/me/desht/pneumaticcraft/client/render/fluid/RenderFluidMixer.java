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
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;

public class RenderFluidMixer extends AbstractFluidTER<TileEntityFluidMixer> {
    private static final AxisAlignedBB[] TANK_BOUNDS_BASE = new AxisAlignedBB[]{
            new AxisAlignedBB(0.1 / 16f, 1 / 16f, 11.1 / 16f, 6.9 / 16f, 8.9/ 16f, 15.9 / 16f),  // in1
            new AxisAlignedBB(9.1 / 16f, 1 / 16f, 11.1 / 16f, 15.9 / 16f, 8.9 / 16f, 15.9 / 16f),  // in2
            new AxisAlignedBB(2.1 / 16f,  10.1 / 16f, 11.1 / 16f,  13.9 / 16f, 15.9 / 16f, 15.9 / 16f)    // out
    };
    private static final AxisAlignedBB[][] BOUNDS = new AxisAlignedBB[3][4];
    static {
        for (int i = 0; i < TANK_BOUNDS_BASE.length; i++) {
            BOUNDS[i][0] = TANK_BOUNDS_BASE[i];
            BOUNDS[i][1] = AbstractFluidTER.rotateY(BOUNDS[i][0], 90);
            BOUNDS[i][2] = AbstractFluidTER.rotateY(BOUNDS[i][1], 90);
            BOUNDS[i][3] = AbstractFluidTER.rotateY(BOUNDS[i][2], 90);
        }
    }

    public RenderFluidMixer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityFluidMixer te) {
        return ImmutableList.of(
                new TankRenderInfo(te.getInputTank1(), BOUNDS[0][te.getRotation().get2DDataValue()]).without(Direction.DOWN),
                new TankRenderInfo(te.getInputTank2(), BOUNDS[1][te.getRotation().get2DDataValue()]).without(Direction.DOWN),
                new TankRenderInfo(te.getOutputTank(), BOUNDS[2][te.getRotation().get2DDataValue()]).without(Direction.DOWN)
        );
    }
}
