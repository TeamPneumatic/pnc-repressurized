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
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class HarvestHandlerCropLike extends HarvestHandlerAbstractCrop {

    private final IntegerProperty ageProperty;
    private final int minAge, maxAge;
    private final Predicate<ItemStack> isSeed;
    
    public HarvestHandlerCropLike(Predicate<BlockState> blockChecker, IntegerProperty ageProperty, Predicate<ItemStack> isSeed){
        super(blockChecker);
        this.ageProperty = ageProperty;
        this.isSeed = isSeed;
        minAge = ageProperty.getPossibleValues().stream().mapToInt(Integer::intValue).min().orElse(0);
        maxAge = ageProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(7);
    }
    
    @Override
    protected boolean isSeed(World world, BlockPos pos, BlockState state, ItemStack stack){
        return isSeed.test(stack);
    }
    
    @Override
    protected boolean isMaxAge(BlockState state){
        return state.getValue(ageProperty) == maxAge;
    }
    
    @Override
    protected BlockState withMinAge(BlockState state){
        return state.setValue(ageProperty, minAge);
    }
}
