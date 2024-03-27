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

import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class DrillPipeBlock extends AbstractPneumaticCraftBlock {
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(6, 0, 6, 7, 16, 10),
            Block.box(7, 0, 6, 9, 16, 7),
            Block.box(7, 0, 9, 9, 16, 10),
            Block.box(10, 14, 6, 10.25, 18, 10),
            Block.box(5.75, 14, 10, 10.25, 18, 10.25),
            Block.box(5.75, 2, 6, 6, 3, 10),
            Block.box(5.75, 2, 5.75, 10.25, 3, 6),
            Block.box(9, 0, 6, 10, 16, 10),
            Block.box(5.75, 14, 5.75, 10.25, 18, 6),
            Block.box(5.75, 14, 6, 6, 18, 10),
            Block.box(10, 2, 6, 10.25, 3, 10),
            Block.box(5.75, 2, 10, 10.25, 3, 10.25)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public DrillPipeBlock() {
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
}
