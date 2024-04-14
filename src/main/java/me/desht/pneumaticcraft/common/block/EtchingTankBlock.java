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

import me.desht.pneumaticcraft.common.block.entity.processing.EtchingTankBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
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

public class EtchingTankBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(0, 1, 0, 16, 9, 2),
            Block.box(2, 1, 2, 14, 14, 14),
            Block.box(0, 1, 14, 16, 9, 16),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 9, 0, 2, 14, 2),
            Block.box(14, 9, 0, 16, 14, 2),
            Block.box(14, 9, 14, 16, 14, 16),
            Block.box(0, 9, 14, 2, 14, 16),
            Block.box(0, 14, 14, 16, 16, 16),
            Block.box(0, 14, 0, 16, 16, 2),
            Block.box(14, 14, 2, 16, 16, 14),
            Block.box(0, 14, 2, 2, 16, 14),
            Block.box(5, 13, 5, 11, 16, 11),
            Block.box(14, 1, 2, 16, 9, 14),
            Block.box(0, 1, 2, 2, 9, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public EtchingTankBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return AbstractPneumaticCraftBlock.ALMOST_FULL_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EtchingTankBlockEntity(pPos, pState);
    }
}
