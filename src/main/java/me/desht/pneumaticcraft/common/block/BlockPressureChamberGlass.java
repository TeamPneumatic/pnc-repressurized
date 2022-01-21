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

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberGlass;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

public class BlockPressureChamberGlass extends BlockPressureChamberWallBase implements EntityBlockPneumaticCraft {
    public BlockPressureChamberGlass() {
        super(IBlockPressureChamber.pressureChamberBlockProps().noOcclusion());
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (worldIn.isClientSide()) {
            PneumaticCraftUtils.getTileEntityAt(worldIn, currentPos, TileEntityPressureChamberGlass.class).ifPresent(teGlass -> {
                teGlass.requestModelDataUpdate();
                // handle any glass that's diagonally connected
                for (Direction d : DirectionUtil.VALUES) {
                    if (d.getAxis() != facing.getAxis()) {
                        BlockEntity te1 = teGlass.getCachedNeighbor(d);
                        if (te1 instanceof TileEntityPressureChamberGlass) te1.requestModelDataUpdate();
                    }
                }
            });
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState ourState, BlockState theirState, Direction side) {
        return ourState.getBlock() == theirState.getBlock() || super.skipRendering(ourState, theirState, side);
    }

    @Override
    public float getShadeBrightness(BlockState p_220080_1_, BlockGetter p_220080_2_, BlockPos p_220080_3_) {
        return 0.2F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TileEntityPressureChamberGlass(pPos, pState);
    }

//    @Override
//    public boolean causesSuffocation(BlockState p_220060_1_, IBlockReader p_220060_2_, BlockPos p_220060_3_) {
//        return false;
//    }

//    @Override
//    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
//        return false;
//    }

//    public boolean canEntitySpawn(BlockState p_220067_1_, IBlockReader p_220067_2_, BlockPos p_220067_3_, EntityType<?> p_220067_4_) {
//        return false;
//    }

}
