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

import me.desht.pneumaticcraft.common.block.entity.processing.FluidMixerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.List;

public class RenderFluidMixer extends AbstractFluidTER<FluidMixerBlockEntity> {
    private static final AABB[] TANK_BOUNDS_BASE = new AABB[]{
            new AABB(0.1 / 16f, 1 / 16f, 11.1 / 16f, 6.9 / 16f, 8.9/ 16f, 15.9 / 16f),  // in1
            new AABB(9.1 / 16f, 1 / 16f, 11.1 / 16f, 15.9 / 16f, 8.9 / 16f, 15.9 / 16f),  // in2
            new AABB(2.1 / 16f,  10.1 / 16f, 11.1 / 16f,  13.9 / 16f, 15.9 / 16f, 15.9 / 16f)    // out
    };
    private static final AABB[][] BOUNDS = new AABB[3][4];
    static {
        for (int i = 0; i < TANK_BOUNDS_BASE.length; i++) {
            BOUNDS[i][0] = TANK_BOUNDS_BASE[i];
            BOUNDS[i][1] = AbstractFluidTER.rotateY(BOUNDS[i][0], 90);
            BOUNDS[i][2] = AbstractFluidTER.rotateY(BOUNDS[i][1], 90);
            BOUNDS[i][3] = AbstractFluidTER.rotateY(BOUNDS[i][2], 90);
        }
    }

    public RenderFluidMixer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(FluidMixerBlockEntity te) {
        return List.of(
                new TankRenderInfo(te.getInputTank1(), BOUNDS[0][te.getRotation().get2DDataValue()]).without(Direction.DOWN),
                new TankRenderInfo(te.getInputTank2(), BOUNDS[1][te.getRotation().get2DDataValue()]).without(Direction.DOWN),
                new TankRenderInfo(te.getOutputTank(), BOUNDS[2][te.getRotation().get2DDataValue()]).without(Direction.DOWN)
        );
    }
}
