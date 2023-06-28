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

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Harvest handler targeted at handling any subclass of BlockCrops.
 *
 * @author MineMaarten
 */
public class HarvestHandlerCrops extends HarvestHandlerAbstractCrop {
    public HarvestHandlerCrops() {
        super(state -> state.getBlock() instanceof CropBlock);
    }

    @Override
    public boolean isSeed(Level world, BlockPos pos, BlockState state, ItemStack stack) {
        ItemStack seed = ((CropBlock) state.getBlock()).getCloneItemStack(world, pos, withMinAge(state));
        return ItemStack.isSameItem(seed, stack);
    }

    @Override
    protected boolean isMaxAge(BlockState state) {
        return ((CropBlock) state.getBlock()).isMaxAge(state);
    }

    @Override
    protected BlockState withMinAge(BlockState state) {
        return ((CropBlock) state.getBlock()).getStateForAge(0);
    }
}
