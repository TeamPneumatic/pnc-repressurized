package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDisplayTable;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BlockDisplayTable extends BlockPneumaticCraft {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];

    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    private static final VoxelShape TOP = Block.makeCuboidShape(0, 13, 0, 16, 16, 16);
//    private static final VoxelShape LEG1 = makeCuboidShape(1, 0, 1, 3, 14, 3);
//    private static final VoxelShape LEG2 = makeCuboidShape(1, 0, 13, 3, 14, 15);
//    private static final VoxelShape LEG3 = makeCuboidShape(13, 0, 1, 15, 14, 3);
//    private static final VoxelShape LEG4 = makeCuboidShape(13, 0, 13, 15, 14, 15);

    public BlockDisplayTable() {
        super(ModBlocks.defaultProps());
        setDefaultState(getStateContainer().getBaseState()
                .with(NE, false)
                .with(NW, false)
                .with(SE, false)
                .with(NW, false)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(NE, SW, SE, NW);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        boolean[] connected = getConnections(ctx.getWorld(), ctx.getPos(), state);
        return state.with(NE, connected[0]).with(SE, connected[1]).with(SW, connected[2]).with(NW, connected[3]);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        boolean[] connected = getConnections(worldIn, currentPos, stateIn);
        return stateIn.with(NE, connected[0]).with(SE, connected[1]).with(SW, connected[2]).with(NW, connected[3]);
    }

    private VoxelShape getCachedShape(BlockState state) {
        int shapeIdx = (state.get(NE) ? 1 : 0) | (state.get(SE) ? 2 : 0) | (state.get(SW) ? 4 : 0) | (state.get(NW) ? 8 : 0);
        if (SHAPE_CACHE[shapeIdx] == null) {
            VoxelShape shape = TOP;
            for (Leg corner : Leg.values()) {
                if (!state.get(corner.prop)) {
                    shape = VoxelShapes.or(shape, corner.shape);
                }
            }
            SHAPE_CACHE[shapeIdx] = shape.simplify();
        }
        return SHAPE_CACHE[shapeIdx];
    }

    private boolean[] getConnections(IWorld world, BlockPos pos, BlockState state) {
        boolean[] res = new boolean[4];

        boolean connE = isMatch(world, pos, state, Direction.EAST);
        boolean connW = isMatch(world, pos, state, Direction.WEST);
        boolean connS = isMatch(world, pos, state, Direction.SOUTH);
        boolean connN = isMatch(world, pos, state, Direction.NORTH);

        res[Leg.SE.ordinal()] = connE || connS;
        res[Leg.NE.ordinal()] = connE || connN;
        res[Leg.SW.ordinal()] = connW || connS;
        res[Leg.NW.ordinal()] = connW || connN;

        return res;
    }

    private boolean isMatch(IWorld world, BlockPos pos, BlockState state, Direction dir) {
        BlockState state2 = world.getBlockState(pos.offset(dir));
        return state2.getBlock() == this && getRotation(state) == getRotation(state2);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityDisplayTable.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getCachedShape(state);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntity te = world.getTileEntity(pos);
        ItemStack heldStack = player.getHeldItem(hand);
        if (player.isSneaking() || te instanceof INamedContainerProvider || ModdedWrenchUtils.getInstance().isWrench(heldStack)) {
            return super.onBlockActivated(state, world, pos, player, hand, brtr);
        } else if (te instanceof TileEntityDisplayTable) {
            if (!world.isRemote) {
                TileEntityDisplayTable teDT = (TileEntityDisplayTable) te;
                if (teDT.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                    // try to put the player's held item onto the table
                    ItemStack excess = teDT.getPrimaryInventory().insertItem(0, player.getHeldItem(hand), false);
                    if (!player.isCreative()) player.setHeldItem(hand, excess);
                } else {
                    // try to remove whatever is on the table
                    ItemStack stack = teDT.getPrimaryInventory().extractItem(0, 64, false);
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, world, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    private enum Leg {
        NE( 1,-1, BlockDisplayTable.NE, Stream.of(
                Block.makeCuboidShape(14, 11.5, 0, 15, 12.5, 3),
                Block.makeCuboidShape(13, 0, 0, 16, 1, 3),
                Block.makeCuboidShape(13.5, 1, 0.5, 15.5, 8, 2.5),
                Block.makeCuboidShape(13.25, 7, 0.25, 15.75, 13, 2.75),
                Block.makeCuboidShape(13, 11.5, 1, 16, 12.5, 2),
                Block.makeCuboidShape(14, 7.5, 0, 15, 8.5, 3),
                Block.makeCuboidShape(13, 7.5, 1, 16, 8.5, 2)
        ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()),
        SE( 1, 1, BlockDisplayTable.SE, Stream.of(
                Block.makeCuboidShape(13, 11.5, 14, 16, 12.5, 15),
                Block.makeCuboidShape(13, 0, 13, 16, 1, 16),
                Block.makeCuboidShape(13.5, 1, 13.5, 15.5, 8, 15.5),
                Block.makeCuboidShape(13.25, 7, 13.25, 15.75, 13, 15.75),
                Block.makeCuboidShape(14, 11.5, 13, 15, 12.5, 16),
                Block.makeCuboidShape(13, 7.5, 14, 16, 8.5, 15),
                Block.makeCuboidShape(14, 7.5, 13, 15, 8.5, 16)
        ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()),
        SW(-1, 1, BlockDisplayTable.SW, Stream.of(
                Block.makeCuboidShape(1, 11.5, 13, 2, 12.5, 16),
                Block.makeCuboidShape(0, 0, 13, 3, 1, 16),
                Block.makeCuboidShape(0.5, 1, 13.5, 2.5, 8, 15.5),
                Block.makeCuboidShape(0.25, 7, 13.25, 2.75, 13, 15.75),
                Block.makeCuboidShape(0, 11.5, 14, 3, 12.5, 15),
                Block.makeCuboidShape(1, 7.5, 13, 2, 8.5, 16),
                Block.makeCuboidShape(0, 7.5, 14, 3, 8.5, 15)
        ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()),
        NW(-1,-1, BlockDisplayTable.NW, Stream.of(
                Block.makeCuboidShape(0, 11.5, 1, 3, 12.5, 2),
                Block.makeCuboidShape(0, 0, 0, 3, 1, 3),
                Block.makeCuboidShape(0.5, 1, 0.5, 2.5, 8, 2.5),
                Block.makeCuboidShape(0.25, 7, 0.25, 2.75, 13, 2.75),
                Block.makeCuboidShape(1, 11.5, 0, 2, 12.5, 3),
                Block.makeCuboidShape(0, 7.5, 1, 3, 8.5, 2),
                Block.makeCuboidShape(1, 7.5, 0, 2, 8.5, 3)
        ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());

        final int x;
        final int z;
        final BooleanProperty prop;
        final VoxelShape shape;

        Leg(int x, int z, BooleanProperty prop, VoxelShape shape) {
            this.x = x; this.z = z;
            this.prop = prop;
            this.shape = shape;
        }
    }
}
