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
import me.desht.pneumaticcraft.common.registry.ModHarvestHandlers.TreePart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public class HarvestHandlerLeaves extends HarvestHandler {

    @Override
    public boolean canHarvest(Level world, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        return state.getBlock() instanceof LeavesBlock;
    }

    @Override
    public List<ItemStack> addFilterItems(Level world, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        Block sapling = TreePart.LEAVES.convert(state.getBlock(), TreePart.SAPLING);
        return Collections.singletonList(new ItemStack(sapling));
    }
}
