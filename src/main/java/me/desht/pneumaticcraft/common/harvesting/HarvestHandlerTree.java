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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class HarvestHandlerTree extends HarvestHandler {
    private static final int SAPLING_PICK_RANGE = 8;

    @Override
    public boolean canHarvest(Level world, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        return state.is(BlockTags.LOGS);
    }

    @Override
    public boolean harvestAndReplant(Level world, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        harvest(world, chunkCache, pos, state, drone);

        // this will work for all vanilla trees, and any modded trees where the mod is consistent about log/sapling naming
        Block saplingBlock = TreePart.LOG.convert(state.getBlock(), TreePart.SAPLING);

        if (saplingBlock != null && saplingBlock != Blocks.AIR) {
            BlockState saplingState = saplingBlock.defaultBlockState();
            if (saplingState.canSurvive(world, pos)) {
                List<ItemEntity> saplingItems = world.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(SAPLING_PICK_RANGE), entityItem -> entityItem.getItem().getItem() == saplingBlock.asItem());
                if (!saplingItems.isEmpty()) {
                    saplingItems.getFirst().getItem().shrink(1); // Use a sapling
                    world.setBlockAndUpdate(pos, saplingState);  // And plant it.
                    return true;
                }
            }
        }

        return false;
    }
}
