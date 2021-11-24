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

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public abstract class BlockAssemblyIOUnit extends BlockPneumaticCraft {
    private static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape SHAPE = VoxelShapes.or(BASE_SHAPE, Block.box(5, 1, 5, 11, 7, 11));

    public BlockAssemblyIOUnit(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyIOUnit.class;
    }

    public static class Import extends BlockAssemblyIOUnit {
        public Import(Properties props) {
            super(props);
        }
    }

    public static class Export extends BlockAssemblyIOUnit {
        public Export(Properties props) {
            super(props);
        }
    }
}
