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

import me.desht.pneumaticcraft.common.block.entity.compressor.CreativeCompressorBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class CreativeCompressorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {

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
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public CreativeCompressorBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return ALMOST_FULL_SHAPE;
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CreativeCompressorBlockEntity(pPos, pState);
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
