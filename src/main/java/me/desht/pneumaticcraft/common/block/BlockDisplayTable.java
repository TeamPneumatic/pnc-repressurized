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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDisplayTable extends BlockPneumaticCraft {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];

    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    private static final VoxelShape TOP = box(0, 14, 0, 16, 16, 16);

    public BlockDisplayTable() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any()
                .setValue(NE, false)
                .setValue(NW, false)
                .setValue(SE, false)
                .setValue(NW, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(NE, SW, SE, NW);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null) return null;
        boolean[] connected = getConnections(ctx.getLevel(), ctx.getClickedPos(), state);
        return state.setValue(NE, connected[0]).setValue(SE, connected[1]).setValue(SW, connected[2]).setValue(NW, connected[3]);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
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
                    shape = VoxelShapes.or(shape, adjustShapeForHeight(leg.shape));
                }
            }
            shapeCache[shapeIdx] = shape.optimize();
        }
        return shapeCache[shapeIdx];
    }

    private VoxelShape adjustShapeForHeight(VoxelShape shape) {
        AxisAlignedBB aabb = shape.bounds();
        return VoxelShapes.box(
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

    private boolean[] getConnections(IWorld world, BlockPos pos, BlockState state) {
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

    private boolean isMatch(IWorld world, BlockPos pos, BlockState state, Direction dir) {
        BlockState state2 = world.getBlockState(pos.relative(dir));
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
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntity te = world.getBlockEntity(pos);
        ItemStack heldStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || te instanceof INamedContainerProvider || ModdedWrenchUtils.getInstance().isWrench(heldStack)) {
            return super.use(state, world, pos, player, hand, brtr);
        } else if (te instanceof TileEntityDisplayTable) {
            if (!world.isClientSide) {
                TileEntityDisplayTable teDT = (TileEntityDisplayTable) te;
                if (teDT.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                    // try to put the player's held item onto the table
                    ItemStack excess = teDT.getPrimaryInventory().insertItem(0, player.getItemInHand(hand), false);
                    if (!player.isCreative()) player.setItemInHand(hand, excess);
                } else {
                    // try to remove whatever is on the table
                    ItemStack stack = teDT.getPrimaryInventory().extractItem(0, 64, false);
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, world, pos.getX() + 0.5, pos.getY() + getTableHeight() + 0.1, pos.getZ() + 0.5);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    private enum Leg {
        NE( 1,-1, BlockDisplayTable.NE, Block.box(13f, 0,  1f, 15f, 14,  3f)),
        SE( 1, 1, BlockDisplayTable.SE, Block.box(13f, 0, 13f, 15f, 14, 15f)),
        SW(-1, 1, BlockDisplayTable.SW, Block.box( 1f, 0, 13f,  3f, 14, 15f)),
        NW(-1,-1, BlockDisplayTable.NW, Block.box( 1f, 0,  1f,  3f, 14,  3f));

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
