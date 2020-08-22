package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

public class BlockKeroseneLamp extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE_NS = Block.makeCuboidShape(3, 0, 5, 13, 10, 11);
    private static final VoxelShape SHAPE_EW = Block.makeCuboidShape(5, 0, 3, 11, 10, 13);

    public static final EnumProperty<Direction> CONNECTED = EnumProperty.create("connected", Direction.class);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public BlockKeroseneLamp() {
        super(ModBlocks.defaultProps().setLightLevel(state -> state.get(LIT) ? 15 : 0));
        setDefaultState(getStateContainer().getBaseState().with(LIT, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return getRotation(state).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(CONNECTED, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return super.getStateForPlacement(ctx).with(CONNECTED, getConnectedDirection(ctx.getWorld(), ctx.getPos()));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return stateIn.with(CONNECTED, getConnectedDirection(worldIn, currentPos));
    }

    private Direction getConnectedDirection(IWorld world, BlockPos pos) {
        Direction connectedDir = Direction.DOWN;
        for (Direction d : Direction.VALUES) {
            BlockPos neighborPos = pos.offset(d);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (Block.hasSolidSide(neighborState, world, neighborPos, d.getOpposite())) {
                connectedDir = d;
                break;
            }
        }
        return connectedDir;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityKeroseneLamp.class;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.get(LIT) ? 15 : 0;
    }

    public static class ItemBlockKeroseneLamp extends BlockItem implements ICustomTooltipName {
        public ItemBlockKeroseneLamp(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return PNCConfig.Common.Machines.keroseneLampCanUseAnyFuel ? getTranslationKey() : getTranslationKey() + ".kerosene_only";
        }
    }
}
