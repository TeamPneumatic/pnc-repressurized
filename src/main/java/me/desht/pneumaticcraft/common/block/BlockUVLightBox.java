package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;

import javax.annotation.Nullable;

public class BlockUVLightBox extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
    public static final BooleanProperty LOADED = BooleanProperty.create("loaded");
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    private static final VoxelShape SHAPE_EW = Block.makeCuboidShape(1, 0, 4.5, 15, 7, 11.5);
    private static final VoxelShape SHAPE_NS = Block.makeCuboidShape(4.5, 0, 1, 11.5, 7, 15);

    public BlockUVLightBox() {
        super(ModBlocks.defaultProps());
        setDefaultState(getStateContainer().getBaseState().with(LOADED, false).with(LIT, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(LOADED, LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return getRotation(state).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityUVLightBox.class;
    }

    @Override
    public int getLightValue(BlockState state) {
        return state.get(LIT) ? 15 : 0;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable ILightReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            return state.has(BlockUVLightBox.LIT) && state.get(BlockUVLightBox.LIT) ? 0xFF4000FF : 0xFFAFAFE4;
        }
        return 0xFFAFAFE4;
    }
}
