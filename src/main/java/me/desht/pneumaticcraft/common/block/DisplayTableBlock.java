package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.DisplayTableBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class DisplayTableBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock, IBlockComparatorSupport {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];

    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    private static final VoxelShape TOP = Block.box(0, 13, 0, 16, 16, 16);

    public DisplayTableBlock() {
        super(ModBlocks.defaultProps());
        registerDefaultState(defaultBlockState()
                .setValue(NE, false)
                .setValue(NW, false)
                .setValue(SE, false)
                .setValue(NW, false)
        );
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(NE, SW, SE, NW);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null) return null;
        boolean[] connected = getConnections(ctx.getLevel(), ctx.getClickedPos(), state);
        return state.setValue(NE, connected[0]).setValue(SE, connected[1]).setValue(SW, connected[2]).setValue(NW, connected[3]);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        boolean[] connected = getConnections(worldIn, currentPos, stateIn);
        return stateIn.setValue(NE, connected[0]).setValue(SE, connected[1]).setValue(SW, connected[2]).setValue(NW, connected[3]);
    }

    private VoxelShape getCachedShape(BlockState state) {
        int shapeIdx = (state.getValue(NE) ? 1 : 0) | (state.getValue(SE) ? 2 : 0) | (state.getValue(SW) ? 4 : 0) | (state.getValue(NW) ? 8 : 0);
        VoxelShape[] shapeCache = getShapeCache();
        if (shapeCache[shapeIdx] == null) {
            VoxelShape shape = adjustShapeForHeight(TOP);
            for (Leg leg : Leg.values()) {
                if (!state.getValue(leg.prop)) {
                    shape = Shapes.or(shape, adjustShapeForHeight(leg.shape));
                }
            }
            shapeCache[shapeIdx] = shape.optimize();
        }
        return shapeCache[shapeIdx];
    }

    private VoxelShape adjustShapeForHeight(VoxelShape shape) {
        AABB aabb = shape.bounds();
        return Shapes.box(
                aabb.minX, Math.max(0, aabb.minY - (1 - getTableHeight())), aabb.minZ,
                aabb.maxX, aabb.maxY - (1 - getTableHeight()), aabb.maxZ
        );
    }

    public double getTableHeight() {
        // for shape calculation and item rendering Y positioning

        return 1d;  // 1 block
    }

    protected VoxelShape[] getShapeCache() {
        return SHAPE_CACHE;
    }

    protected boolean shelfLegs() {
        return false;
    }

    private boolean[] getConnections(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockPos below = pos.below();
        if (shelfLegs() && !world.getBlockState(below).isFaceSturdy(world, below, Direction.UP)) {
            // no ground below; hide all legs
            return new boolean[] { true, true, true, true };
        }

        boolean connE = isMatch(world, pos, state, Direction.EAST);
        boolean connW = isMatch(world, pos, state, Direction.WEST);
        boolean connS = isMatch(world, pos, state, Direction.SOUTH);
        boolean connN = isMatch(world, pos, state, Direction.NORTH);

        boolean[] res = new boolean[4];
        res[Leg.SE.ordinal()] = connE || connS;
        res[Leg.NE.ordinal()] = connE || connN;
        res[Leg.SW.ordinal()] = connW || connS;
        res[Leg.NW.ordinal()] = connW || connN;
        return res;
    }

    private boolean isMatch(LevelAccessor world, BlockPos pos, BlockState state, Direction dir) {
        BlockState state2 = world.getBlockState(pos.relative(dir));
        return state2.getBlock() == this && getRotation(state) == getRotation(state2);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        BlockEntity te = world.getBlockEntity(pos);
        ItemStack heldStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || te instanceof MenuProvider || ModdedWrenchUtils.getInstance().isWrench(heldStack)) {
            return super.use(state, world, pos, player, hand, brtr);
        } else if (te instanceof DisplayTableBlockEntity) {
            if (!world.isClientSide) {
                DisplayTableBlockEntity teDT = (DisplayTableBlockEntity) te;
                if (teDT.getItemHandler().getStackInSlot(0).isEmpty()) {
                    // try to put the player's held item onto the table
                    ItemStack excess = teDT.getItemHandler().insertItem(0, player.getItemInHand(hand), false);
                    if (!player.isCreative()) player.setItemInHand(hand, excess);
                } else {
                    // try to remove whatever is on the table
                    ItemStack stack = teDT.getItemHandler().extractItem(0, 64, false);
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, world, pos.getX() + 0.5, pos.getY() + getTableHeight() + 0.1, pos.getZ() + 0.5);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DisplayTableBlockEntity(pPos, pState);
    }

    private enum Leg {
        NE( 1,-1, DisplayTableBlock.NE, Stream.of(
                Block.box(14, 11.5, 0, 15, 12.5, 3),
                Block.box(13, 0, 0, 16, 1, 3),
                Block.box(13.5, 1, 0.5, 15.5, 8, 2.5),
                Block.box(13.25, 7, 0.25, 15.75, 13, 2.75),
                Block.box(13, 11.5, 1, 16, 12.5, 2),
                Block.box(14, 7.5, 0, 15, 8.5, 3),
                Block.box(13, 7.5, 1, 16, 8.5, 2)
        ).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get()),
        SE( 1, 1, DisplayTableBlock.SE, Stream.of(
                Block.box(13, 11.5, 14, 16, 12.5, 15),
                Block.box(13, 0, 13, 16, 1, 16),
                Block.box(13.5, 1, 13.5, 15.5, 8, 15.5),
                Block.box(13.25, 7, 13.25, 15.75, 13, 15.75),
                Block.box(14, 11.5, 13, 15, 12.5, 16),
                Block.box(13, 7.5, 14, 16, 8.5, 15),
                Block.box(14, 7.5, 13, 15, 8.5, 16)
        ).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get()),
        SW(-1, 1, DisplayTableBlock.SW, Stream.of(
                Block.box(1, 11.5, 13, 2, 12.5, 16),
                Block.box(0, 0, 13, 3, 1, 16),
                Block.box(0.5, 1, 13.5, 2.5, 8, 15.5),
                Block.box(0.25, 7, 13.25, 2.75, 13, 15.75),
                Block.box(0, 11.5, 14, 3, 12.5, 15),
                Block.box(1, 7.5, 13, 2, 8.5, 16),
                Block.box(0, 7.5, 14, 3, 8.5, 15)
        ).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get()),
        NW(-1,-1, DisplayTableBlock.NW, Stream.of(
                Block.box(0, 11.5, 1, 3, 12.5, 2),
                Block.box(0, 0, 0, 3, 1, 3),
                Block.box(0.5, 1, 0.5, 2.5, 8, 2.5),
                Block.box(0.25, 7, 0.25, 2.75, 13, 2.75),
                Block.box(1, 11.5, 0, 2, 12.5, 3),
                Block.box(0, 7.5, 1, 3, 8.5, 2),
                Block.box(1, 7.5, 0, 2, 8.5, 3)
        ).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get());

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

    public static class Shelf extends DisplayTableBlock {
        private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];

        @Override
        public double getTableHeight() {
            return 0.5d;
        }

        @Override
        protected VoxelShape[] getShapeCache() {
            return SHAPE_CACHE;
        }

        @Override
        protected boolean shelfLegs() {
            return true;
        }
    }
}
