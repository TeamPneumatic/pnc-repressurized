package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCompressor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockAirCompressor extends BlockPneumaticCraft {
    private static final VoxelShape S1 = Block.box(0, 0, 2, 16, 16, 14);
    private static final VoxelShape S2 = Block.box(2, 0, 0, 14, 16, 16);
    private static final VoxelShape CROSS_SHAPE = VoxelShapes.join(S1, S2, IBooleanFunction.OR);

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public BlockAirCompressor() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any().setValue(ON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ON);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAirCompressor.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return CROSS_SHAPE;
    }
}
