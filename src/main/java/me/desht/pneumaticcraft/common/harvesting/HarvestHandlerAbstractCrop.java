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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

public abstract class HarvestHandlerAbstractCrop extends HarvestHandler {

    private final Predicate<BlockState> blockChecker;

    public HarvestHandlerAbstractCrop(Predicate<BlockState> blockChecker) {
        this.blockChecker = blockChecker;
    }

    @Override
    public boolean canHarvest(Level world, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        return blockChecker.test(state) && isMaxAge(state);
    }

    @Override
    public boolean harvestAndReplant(Level world, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        harvest(world, chunkCache, pos, state, drone);
        List<ItemEntity> seedItems = world.getEntitiesOfClass(ItemEntity.class, new AABB(pos), entityItem -> isSeed(world, pos, state, entityItem.getItem()));
        if (!seedItems.isEmpty()) {
            seedItems.getFirst().getItem().shrink(1);//Use a seed
            world.setBlockAndUpdate(pos, withMinAge(state)); //And plant it.
            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean isSeed(Level world, BlockPos pos, BlockState state, ItemStack stack);

    protected abstract boolean isMaxAge(BlockState state);

    protected abstract BlockState withMinAge(BlockState state);
}
