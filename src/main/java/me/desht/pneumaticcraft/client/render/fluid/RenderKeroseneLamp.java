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

import me.desht.pneumaticcraft.common.block.entity.KeroseneLampBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.List;

public class RenderKeroseneLamp extends AbstractFluidTER<KeroseneLampBlockEntity> {
    private static final AABB TANK_BOUNDS = new AABB(5.1/16f, 1/16f, 5.1/16f, 10.9/16f, 9/16f, 10.9/16f);

    public RenderKeroseneLamp(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(KeroseneLampBlockEntity te) {
        return List.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
