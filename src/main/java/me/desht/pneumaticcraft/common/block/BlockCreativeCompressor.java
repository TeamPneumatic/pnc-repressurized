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

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.stream.Stream;

public class BlockCreativeCompressor extends BlockPneumaticCraft {

    private static final VoxelShape SHAPE = Stream.of(
            Block.box(15, 0, 0, 16, 1, 16),
            Block.box(0, 0, 0, 1, 1, 16),
            Block.box(1, 0, 0, 15, 1, 1),
            Block.box(1, 0, 15, 15, 1, 16),
            Block.box(0, 15, 0, 1, 16, 16),
            Block.box(15, 15, 0, 16, 16, 16),
            Block.box(1, 15, 0, 15, 16, 1),
            Block.box(1, 15, 15, 15, 16, 16),
            Block.box(0, 1, 0, 1, 15, 1),
            Block.box(0, 1, 15, 1, 15, 16),
            Block.box(15, 1, 15, 16, 15, 16),
            Block.box(15, 1, 0, 16, 15, 1),
            Block.box(4, 14, 4, 12, 16, 12),
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(2, 2, 2, 14, 14, 14),
            Block.box(0, 4, 4, 2, 12, 12),
            Block.box(14, 4, 4, 16, 12, 12),
            Block.box(4, 4, 14, 12, 12, 16),
            Block.box(4, 4, 0, 12, 12, 2)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

    public BlockCreativeCompressor() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCreativeCompressor.class;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return ALMOST_FULL_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    public static class ItemBlockCreativeCompressor extends BlockItem {
        public ItemBlockCreativeCompressor(Block block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public Rarity getRarity(ItemStack stack) {
            return Rarity.EPIC;
        }
    }
}
