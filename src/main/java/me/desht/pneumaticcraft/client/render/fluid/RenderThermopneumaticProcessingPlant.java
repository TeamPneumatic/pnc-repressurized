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
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;

public class RenderThermopneumaticProcessingPlant extends AbstractFluidTER<TileEntityThermopneumaticProcessingPlant> {
    private static final AxisAlignedBB TANK_BOUNDS_1 = new AxisAlignedBB(10 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 11 / 16f, 6 / 16f);
    private static final AxisAlignedBB TANK_BOUNDS_2 = new AxisAlignedBB( 1 / 16f, 1 / 16f, 1 / 16f,  6 / 16f, 11 / 16f, 6 / 16f);

    private static final AxisAlignedBB[] BOUNDS_IN = new AxisAlignedBB[4];
    static {
        BOUNDS_IN[0] = TANK_BOUNDS_1;
        BOUNDS_IN[1] = AbstractFluidTER.rotateY(BOUNDS_IN[0], 90);
        BOUNDS_IN[2] = AbstractFluidTER.rotateY(BOUNDS_IN[1], 90);
        BOUNDS_IN[3] = AbstractFluidTER.rotateY(BOUNDS_IN[2], 90);
    }
    private static final AxisAlignedBB[] BOUNDS_OUT = new AxisAlignedBB[4];
    static {
        BOUNDS_OUT[0] = TANK_BOUNDS_2;
        BOUNDS_OUT[1] = AbstractFluidTER.rotateY(BOUNDS_OUT[0], 90);
        BOUNDS_OUT[2] = AbstractFluidTER.rotateY(BOUNDS_OUT[1], 90);
        BOUNDS_OUT[3] = AbstractFluidTER.rotateY(BOUNDS_OUT[2], 90);
    }

    public RenderThermopneumaticProcessingPlant(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityThermopneumaticProcessingPlant te) {
        return ImmutableList.of(
                new TankRenderInfo(te.getInputTank(), BOUNDS_IN[te.getRotation().get2DDataValue()]).without(Direction.DOWN),
                new TankRenderInfo(te.getOutputTank(), BOUNDS_OUT[te.getRotation().get2DDataValue()]).without(Direction.DOWN)
        );
    }
}
