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
import me.desht.pneumaticcraft.common.tileentity.TileEntityEtchingTank;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;

public class RenderEtchingTank extends AbstractFluidTER<TileEntityEtchingTank> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(2.1/16f, 1.1/16f, 2.1/16f, 13.9/16f, 13.9/16f, 13.9/16f);

    public RenderEtchingTank(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityEtchingTank te) {
        return ImmutableList.of(new TankRenderInfo(te.getAcidTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
