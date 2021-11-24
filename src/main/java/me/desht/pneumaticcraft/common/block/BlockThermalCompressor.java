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

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockThermalCompressor extends BlockPneumaticCraft implements ColorHandlers.IHeatTintable {
    private static final VoxelShape BOUNDS1 = Block.box(2, 0, 2, 14, 15, 14);
    private static final VoxelShape BOUNDS2 = Block.box(3, 15, 3, 13, 16, 13);
    private static final VoxelShape BOUNDS3 = Block.box(0, 4, 4, 16, 12, 12);
    private static final VoxelShape BOUNDS4 = Block.box(4, 4, 0, 12, 12, 16);
    private static final VoxelShape BOUNDS = VoxelShapes.or(BOUNDS1, BOUNDS2, BOUNDS3, BOUNDS4);

    public BlockThermalCompressor() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return BOUNDS;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityThermalCompressor.class;
    }

}
