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

package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicTicking;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HeatBehaviourHeatFrame extends HeatBehaviour<TileEntity> {
    static final ResourceLocation ID = RL("heat_frame");

    private EntityHeatFrame semiBlock;

    @Override
    public HeatBehaviour<?> initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        super.initialize(connectedHeatLogic, world, pos, direction);
        semiBlock = null;
        return this;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    private EntityHeatFrame getHeatFrame() {
        if (semiBlock == null || !semiBlock.isAlive()) {
            ISemiBlock s = SemiblockTracker.getInstance().getSemiblock(getWorld(), getPos());
            if (s instanceof EntityHeatFrame) {
                semiBlock = (EntityHeatFrame) s;
            }
        }
        return semiBlock;
    }

    @Override
    public boolean isApplicable() {
        return getHeatFrame() != null;
    }

    @Override
    public void tick() {
        HeatExchangerLogicTicking.exchange(getHeatFrame().getHeatExchangerLogic(), getHeatExchanger());
    }
}
