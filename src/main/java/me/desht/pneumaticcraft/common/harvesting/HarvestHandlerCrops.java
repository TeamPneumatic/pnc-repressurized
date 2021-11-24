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

import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Harvest handler targeted at handling any subclass of BlockCrops.
 * @author MineMaarten
 *
 */
public class HarvestHandlerCrops extends HarvestHandlerAbstractCrop {
    public HarvestHandlerCrops(){
        super(state -> state.getBlock() instanceof CropsBlock);
    }
    
    @Override
    public boolean isSeed(World world, BlockPos pos, BlockState state, ItemStack stack){
        ItemStack seed = ((CropsBlock)state.getBlock()).getCloneItemStack(world, pos, withMinAge(state));
        return seed.sameItem(stack);
    }

    @Override
    protected boolean isMaxAge(BlockState state){
        return ((CropsBlock)state.getBlock()).isMaxAge(state);
    }
    
    @Override
    protected BlockState withMinAge(BlockState state){
        return ((CropsBlock)state.getBlock()).getStateForAge(0);
    }
}
