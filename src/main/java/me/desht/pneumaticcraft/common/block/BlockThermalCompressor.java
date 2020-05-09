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
    private static final VoxelShape BOUNDS1 = Block.makeCuboidShape(2, 0, 2, 14, 15, 14);
    private static final VoxelShape BOUNDS2 = Block.makeCuboidShape(3, 15, 3, 13, 16, 13);
    private static final VoxelShape BOUNDS3 = Block.makeCuboidShape(0, 4, 4, 16, 12, 12);
    private static final VoxelShape BOUNDS4 = Block.makeCuboidShape(4, 4, 0, 12, 12, 16);
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
