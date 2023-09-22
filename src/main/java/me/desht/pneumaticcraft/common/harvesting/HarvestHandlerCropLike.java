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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Predicate;

public class HarvestHandlerCropLike extends HarvestHandlerAbstractCrop {

    private final IntegerProperty ageProperty;
    private final int minAge, maxAge;
    private final Predicate<ItemStack> isSeed;

    public HarvestHandlerCropLike(Predicate<BlockState> blockChecker, IntegerProperty ageProperty, Predicate<ItemStack> isSeed) {
        super(blockChecker);
        this.ageProperty = ageProperty;
        this.isSeed = isSeed;
        minAge = ageProperty.getPossibleValues().stream().mapToInt(Integer::intValue).min().orElse(0);
        maxAge = ageProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(7);
    }

    @Override
    protected boolean isSeed(Level world, BlockPos pos, BlockState state, ItemStack stack) {
        return isSeed.test(stack);
    }

    @Override
    protected boolean isMaxAge(BlockState state) {
        return state.getValue(ageProperty) == maxAge;
    }

    @Override
    protected BlockState withMinAge(BlockState state) {
        return state.setValue(ageProperty, minAge);
    }

    public static class Cocoa extends HarvestHandlerCropLike {
        public Cocoa() {
            super(state -> state.getBlock() == Blocks.COCOA, CocoaBlock.AGE, stack -> stack.getItem() == Items.COCOA_BEANS);
        }
    }

    public static class NetherWart extends HarvestHandlerCropLike {
        public NetherWart() {
            super(state -> state.getBlock() == Blocks.NETHER_WART, NetherWartBlock.AGE, stack -> stack.getItem() == Items.NETHER_WART);
        }
    }

    public static class SweetBerry extends HarvestHandlerCropLike {
        public SweetBerry() {
            super(state -> state.getBlock() == Blocks.SWEET_BERRY_BUSH, SweetBerryBushBlock.AGE, stack -> stack.getItem() == Items.SWEET_BERRIES);
        }

        @Override
        protected BlockState withMinAge(BlockState state) {
            return state.setValue(SweetBerryBushBlock.AGE, 1);
        }
    }
}
