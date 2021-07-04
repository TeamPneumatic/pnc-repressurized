package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.stream.Stream;

public class BlockThermalCompressor extends BlockPneumaticCraft implements ColorHandlers.IHeatTintable {
    private static final VoxelShape BOUNDS = Stream.of(
            Block.makeCuboidShape(3, 3, 0, 13, 13, 1),
            Block.makeCuboidShape(0, 15, 0, 16, 16, 1),
            Block.makeCuboidShape(15, 1, 0, 16, 15, 1),
            Block.makeCuboidShape(0, 1, 0, 1, 15, 1),
            Block.makeCuboidShape(0, 0, 0, 16, 1, 1),
            Block.makeCuboidShape(15, 0, 1, 16, 1, 15),
            Block.makeCuboidShape(0, 0, 15, 16, 1, 16),
            Block.makeCuboidShape(15, 1, 15, 16, 15, 16),
            Block.makeCuboidShape(0, 15, 15, 16, 16, 16),
            Block.makeCuboidShape(0, 1, 15, 1, 15, 16),
            Block.makeCuboidShape(15, 15, 1, 16, 16, 15),
            Block.makeCuboidShape(0, 0, 1, 1, 1, 15),
            Block.makeCuboidShape(0, 15, 1, 1, 16, 15),
            Block.makeCuboidShape(4, 15, 4, 12, 16, 12),
            Block.makeCuboidShape(1, 1, 1, 15, 15, 15),
            Block.makeCuboidShape(3, 3, 15, 13, 13, 16),
            Block.makeCuboidShape(15, 3, 3, 16, 13, 13),
            Block.makeCuboidShape(0, 3, 3, 1, 13, 13),
            Block.makeCuboidShape(4, 0, 4, 12, 1, 12)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();

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
