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

package me.desht.pneumaticcraft.common.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class HarvestHandlerCactusLike extends HarvestHandler {

    private final Predicate<BlockState> blockChecker;

    public HarvestHandlerCactusLike(Predicate<BlockState> blockChecker) {
        this.blockChecker = blockChecker;
    }

    @Override
    public boolean canHarvest(Level world, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        if (blockChecker.test(state)) {
            BlockState stateBelow = chunkCache.getBlockState(pos.relative(Direction.DOWN));
            return blockChecker.test(stateBelow);
        }
        return false;
    }

    public static class VanillaCrops extends HarvestHandlerCactusLike {
        public VanillaCrops() {
            super(state -> state.getBlock() == Blocks.CACTUS || state.getBlock() == Blocks.SUGAR_CANE
                    || state.getBlock() == Blocks.KELP_PLANT || state.getBlock() == Blocks.BAMBOO);
        }
    }
}
