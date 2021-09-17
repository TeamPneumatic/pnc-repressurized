package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockUVLightBox extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
    public static final BooleanProperty LOADED = BooleanProperty.create("loaded");
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    private static final VoxelShape SHAPE_N = VoxelShapes.join(Block.box(1, 0, 2, 15, 14, 14), Block.box(15, 5, 5, 16, 11, 11), IBooleanFunction.OR);
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);

    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_E, SHAPE_S, SHAPE_W, SHAPE_N };

    public BlockUVLightBox() {
        super(ModBlocks.defaultProps().lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        registerDefaultState(getStateDefinition().any().setValue(LOADED, false).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOADED, LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityUVLightBox.class;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

//    @Override
//    public int getLightValue(BlockState state) {
//        return state.get(LIT) ? 15 : 0;
//    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            return state.hasProperty(BlockUVLightBox.LIT) && state.getValue(BlockUVLightBox.LIT) ? 0xFF4000FF : 0xFFAFAFE4;
        }
        return 0xFFAFAFE4;
    }
}
