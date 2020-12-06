package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.block.tubes.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedPressureTube;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.block.BlockPressureTube.ConnectionType.CONNECTED;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.HORIZONTALS;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class BlockPressureTube extends BlockPneumaticCraftCamo implements IWaterLoggable {

    private static final int TUBE_WIDTH = 2;
    public static final int CORE_MIN = 8 - TUBE_WIDTH;
    public static final int CORE_MAX = 8 + TUBE_WIDTH;
    private static final double PLUG_SIZE = 2.5;
    private static final VoxelShape CORE = Block.makeCuboidShape(
            8 - TUBE_WIDTH, 8 - TUBE_WIDTH, 8 - TUBE_WIDTH, 8 + TUBE_WIDTH, 8 + TUBE_WIDTH, 8 + TUBE_WIDTH
    );
    private static final VoxelShape[] ARM_CONNECTED = {  // DUNSWE order
            Block.makeCuboidShape(CORE_MIN, 0, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX),
            Block.makeCuboidShape(CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX, 16, CORE_MAX),
            Block.makeCuboidShape(CORE_MIN, CORE_MIN, 0, CORE_MAX, CORE_MAX, CORE_MIN),
            Block.makeCuboidShape(CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX, 16),
            Block.makeCuboidShape(0, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX),
            Block.makeCuboidShape(CORE_MAX, CORE_MIN, CORE_MIN, 16, CORE_MAX, CORE_MAX)
    };
    private static final VoxelShape[] ARM_CLOSED = {  // DUNSWE order
            Block.makeCuboidShape(CORE_MIN, CORE_MIN - PLUG_SIZE, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX),
            Block.makeCuboidShape(CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX, CORE_MAX + PLUG_SIZE, CORE_MAX),
            Block.makeCuboidShape(CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MIN - PLUG_SIZE),
            Block.makeCuboidShape(CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX, CORE_MAX + PLUG_SIZE),
            Block.makeCuboidShape(CORE_MIN - PLUG_SIZE, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX),
            Block.makeCuboidShape(CORE_MAX, CORE_MIN, CORE_MIN, CORE_MAX + PLUG_SIZE, CORE_MAX, CORE_MAX)
    };
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[729];  // 3 ^ 6

    private static final EnumProperty<ConnectionType> UP_3 = EnumProperty.create("up", ConnectionType.class);
    private static final EnumProperty<ConnectionType> DOWN_3 = EnumProperty.create("down", ConnectionType.class);
    private static final EnumProperty<ConnectionType> NORTH_3 = EnumProperty.create("north", ConnectionType.class);
    private static final EnumProperty<ConnectionType> EAST_3 = EnumProperty.create("east", ConnectionType.class);
    private static final EnumProperty<ConnectionType> SOUTH_3 = EnumProperty.create("south", ConnectionType.class);
    private static final EnumProperty<ConnectionType> WEST_3 = EnumProperty.create("west", ConnectionType.class);
    @SuppressWarnings("unchecked")
    private static final EnumProperty<ConnectionType>[] CONNECTION_PROPERTIES_3 = new EnumProperty[]{
            DOWN_3, UP_3, NORTH_3, SOUTH_3, WEST_3, EAST_3
    };

    private final Tier tier;

    public BlockPressureTube(Tier tier) {
        super(ModBlocks.defaultProps().notSolid());  // notSolid() because of camo requirements
        this.tier = tier;

        BlockState state = getStateContainer().getBaseState();
        for (EnumProperty<ConnectionType> p : CONNECTION_PROPERTIES_3) {
            state = state.with(p, ConnectionType.UNCONNECTED);
        }
        setDefaultState(state.with(WATERLOGGED, false));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return tier.teClass;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(CONNECTION_PROPERTIES_3);
        builder.add(WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = getDefaultState();
        List<Direction> l = new ArrayList<>();
        for (Direction dir : Direction.VALUES) {
            TileEntity te = ctx.getWorld().getTileEntity(ctx.getPos().offset(dir));
            if (te != null && te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()).isPresent()) {
                state = setSide(state, dir, CONNECTED);
                l.add(dir);
            }
        }
        if (l.size() == 1) state = setSide(state, l.get(0).getOpposite(), CONNECTED);
        IFluidState fluidState = ctx.getWorld().getFluidState(ctx.getPos());
        return state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        BlockState newState = recalculateState(worldIn, currentPos, stateIn);
        return newState == null ?
                super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos) :
                newState;
    }

    public static BlockState recalculateState(IWorld worldIn, BlockPos currentPos, BlockState stateIn) {
        TileEntityPressureTube tePT = getPressureTube(worldIn, currentPos);
        if (tePT != null) {
            BlockState state = stateIn;
            for (Direction dir : Direction.VALUES) {
                ConnectionType type = ConnectionType.UNCONNECTED;
                if (tePT.sidesClosed[dir.getIndex()]) {
                    type = ConnectionType.CLOSED;
                } else if (tePT.canConnectPneumatic(dir)) {
                    TileEntity neighbourTE = worldIn.getTileEntity(currentPos.offset(dir));
                    if (neighbourTE != null && neighbourTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()).isPresent()) {
                        type = CONNECTED;
                    }
                }
                state = setSide(state, dir, type);
            }
            return checkForSingleConnection(tePT, state);
        }
        return stateIn;
    }

    private static BlockState checkForSingleConnection(TileEntityPressureTube te, BlockState state) {
        List<Direction> connected = new ArrayList<>();
        int nUnconnected = 0;
        for (Direction dir : Direction.VALUES) {
            if (te.getModule(dir) != null) {
                return state;
            }
            switch (state.get(CONNECTION_PROPERTIES_3[dir.getIndex()])) {
                case CONNECTED: connected.add(dir); break;
                case UNCONNECTED: nUnconnected++; break;
                case CLOSED: return state;
            }
            if (connected.size() > 1) break;
        }
        // tube has no modules and only a single connected side; make the opposite side "connected" too so it appears open
        if (nUnconnected == 5 && connected.size() == 1) {
            state = setSide(state, connected.get(0).getOpposite(), CONNECTED);
        }
        return state;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        VoxelShape res = getCachedShape(state);
        TileEntityPressureTube te = getPressureTube(reader, pos);
        return te != null ? VoxelShapes.or(res, te.getCachedModuleShape()) : res;
    }

    private VoxelShape getCachedShape(BlockState state) {
        int idx = 0;
        int mul = 1;
        for (Direction d : Direction.values()) {
            idx += state.get(CONNECTION_PROPERTIES_3[d.getIndex()]).ordinal() * mul;
            mul *= 3;
        }
        if (SHAPE_CACHE[idx] == null) {
            VoxelShape res = CORE;
            for (Direction d : Direction.values()) {
                switch (state.get(CONNECTION_PROPERTIES_3[d.getIndex()])) {
                    case CONNECTED: res = VoxelShapes.combineAndSimplify(res, ARM_CONNECTED[d.getIndex()], IBooleanFunction.OR); break;
                    case CLOSED: res = VoxelShapes.combineAndSimplify(res, ARM_CLOSED[d.getIndex()], IBooleanFunction.OR); break;
                }
            }
            SHAPE_CACHE[idx] = res;
        }

        return SHAPE_CACHE[idx];
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (tryPlaceModule(player, world, pos, brtr.getFace(), hand, false)) {
            return ActionResultType.SUCCESS;
        }
        if (!player.isSneaking()) {
            TubeModule module = getFocusedModule(world, pos, player);
            if (module != null) {
                return module.onActivated(player, hand) ? ActionResultType.SUCCESS : ActionResultType.PASS;
            }
        }
        return super.onBlockActivated(state, world, pos, player, hand, brtr);
    }

    public int getTier() {
        return tier.tier;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        ModuleNetworkManager.getInstance(world).invalidateCache();
        // force TE to calculate its connections immediately so network manager rescanning works
        TileEntityPressureTube te = getPressureTube(world, pos);
        if (te != null) {
            te.onNeighborTileUpdate();
        }
    }

    public boolean tryPlaceModule(PlayerEntity player, World world, BlockPos pos, Direction side, Hand hand, boolean simulate) {
        TileEntityPressureTube tePT = getPressureTube(world, pos);
        if (tePT == null) return false;

        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.getItem() instanceof ItemTubeModule) {
            TubeModule module = ((ItemTubeModule) heldStack.getItem()).createModule();
            if (tePT.mayPlaceModule(side)) {
                if (simulate) module.markFake();
                tePT.setModule(module, side);
                if (!simulate && !world.isRemote) {
                    neighborChanged(world.getBlockState(pos), world, pos, this, pos.offset(side), false);
                    world.notifyNeighborsOfStateChange(pos, this);
                    if (!player.isCreative()) heldStack.shrink(1);
                    NetworkHandler.sendToAllAround(
                            new PacketPlaySound(SoundType.GLASS.getStepSound(), SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(),
                                    SoundType.GLASS.getVolume() * 5.0f, SoundType.GLASS.getPitch() * 0.9f, false),
                            world);
                    ModuleNetworkManager.getInstance(world).invalidateCache();
                }
                if (!simulate) module.onPlaced();
                return true;
            }
        } else if (heldStack.getItem() == ModItems.ADVANCED_PCB.get() && !simulate) {
            TubeModule module = BlockPressureTube.getFocusedModule(world, pos, player);
            if (module != null && !module.isUpgraded() && module.canUpgrade()) {
                if (!world.isRemote) {
                    module.upgrade();
                    tePT.sendDescriptionPacket();
                    if (!player.isCreative()) heldStack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Get the tube module being looked at by the player, if any.
     *
     * @param world the world
     * @param pos the blockpos
     * @param player the player
     * @return a tube module, or null if no module is focused
     */
    public static TubeModule getFocusedModule(World world, BlockPos pos, PlayerEntity player) {
        Pair<Vec3d, Vec3d> vecs = PneumaticCraftUtils.getStartAndEndLookVec(player);
        BlockState state = world.getBlockState(pos);
        BlockRayTraceResult rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            TileEntityPressureTube tube = getPressureTube(world, pos);
            return tube == null ? null : tube.modules[tubeHitInfo.dir.ordinal()];
        }
        return null;
    }

    private static BlockState setSide(BlockState state, Direction side, ConnectionType type) {
        return state.with(CONNECTION_PROPERTIES_3[side.getIndex()], type);
    }

    private static TileEntityPressureTube getPressureTube(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityPressureTube ? (TileEntityPressureTube) te : null;
    }

    /**
     * Get the part of the tube being looked at.
     *
     * @param world the world
     * @param pos the blockpos
     * @param player the player
     * @return (true, side) if it's the side of the tube core, or (false, side) if it's a tube arm
     */
    private static Pair<Boolean, Direction> getLookedTube(IBlockReader world, BlockPos pos, PlayerEntity player) {
        Pair<Vec3d, Vec3d> vecs = PneumaticCraftUtils.getStartAndEndLookVec(player);
        BlockState state = world.getBlockState(pos);
        BlockRayTraceResult rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            // return either the tube arm (if connected), or the side of the centre face (if not)
            return tubeHitInfo.dir == null ? Pair.of(true, rayTraceResult.getFace()) : Pair.of(false, tubeHitInfo.dir);
        }
        return null;
    }

    private static BlockRayTraceResult doTrace(BlockState state, IBlockReader world, BlockPos pos, Vec3d origin, Vec3d direction) {
        BlockRayTraceResult bestRTR = null;

        // first try & trace the tube core (center cube)
        BlockRayTraceResult brtr = AxisAlignedBB.rayTrace(Collections.singletonList(CORE.getBoundingBox()), origin, direction, pos);
        if (brtr != null) {
            brtr.hitInfo = TubeHitInfo.CENTER;
            bestRTR = brtr;
        }

        // now check each arm of the tube
        TileEntityPressureTube tube = getPressureTube(world, pos);
        if (tube == null) return null;
        for (int i = 0; i < 6; i++) {
            AxisAlignedBB arm;
            switch (state.get(CONNECTION_PROPERTIES_3[i])) {
                case CLOSED:
                    arm = ARM_CLOSED[i].getBoundingBox(); break;
                case CONNECTED:
                    arm = ARM_CONNECTED[i].getBoundingBox(); break;
                default:
                    arm = null; break;
            }
            if (arm != null) {
                brtr = AxisAlignedBB.rayTrace(Collections.singletonList(arm), origin, direction, pos);
                if (brtr != null) {
                    if (isCloserIntersection(origin, bestRTR, brtr)) {
                        brtr.hitInfo = new TubeHitInfo(Direction.byIndex(i), TubeHitInfo.PartType.TUBE);
                        bestRTR = brtr;
                    }
                }
            }
        }

        // now check attached tube modules
        TubeModule[] modules = tube.modules;
        for (Direction dir : Direction.VALUES) {
            if (modules[dir.getIndex()] != null) {
                AxisAlignedBB tubeAABB = modules[dir.getIndex()].getShape().getBoundingBox();
                brtr = AxisAlignedBB.rayTrace(Collections.singletonList(tubeAABB), origin, direction, pos);
                if (isCloserIntersection(origin, bestRTR, brtr)) {
                    brtr.hitInfo = new TubeHitInfo(dir, TubeHitInfo.PartType.MODULE);  // tube module
                    bestRTR = brtr;
                }
            }
        }

        return bestRTR;
    }

    private static boolean isCloserIntersection(Vec3d origin, RayTraceResult oldRTR, RayTraceResult newRTR) {
        return newRTR != null &&
                (oldRTR == null || origin.squareDistanceTo(newRTR.getHitVec()) <= origin.squareDistanceTo(oldRTR.getHitVec()));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        Pair<Vec3d, Vec3d> vecs = PneumaticCraftUtils.getStartAndEndLookVec(player);
        BlockRayTraceResult rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            return super.getPickBlock(state, target, world, pos, player);
        } else if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            TileEntityPressureTube tube = getPressureTube(world, pos);
            if (tube != null) {
                TubeModule module = tube.modules[tubeHitInfo.dir.ordinal()];
                if (module != null) {
                    return new ItemStack(module.getItem());
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (player == null) return false;
        TileEntityPressureTube tube = getPressureTube(world, pos);
        if (tube == null) return false;
        TubeModule module = getFocusedModule(world, pos, player);
        if (player.isSneaking()) {
            if (module != null) {
                // detach and drop the module as an item
                if (!player.isCreative()) {
                    for (ItemStack drop : module.getDrops()) {
                        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        entity.setItem(drop);
                        world.addEntity(entity);
                        entity.onCollideWithPlayer(player);
                    }
                }
                tube.setModule(null, module.getDirection());
                neighborChanged(world.getBlockState(pos), world, pos, this, pos.offset(side), false);
                world.notifyNeighborsOfStateChange(pos, this);
            } else {
                // drop the pressure tube as an item
                if (!player.isCreative()) spawnDrops(world.getBlockState(pos), world, pos, tube);
                world.removeBlock(pos, false);
            }
        } else {
            if (module != null) {
                module.onActivated(player, hand);
            } else {
                // close (or reopen) this side of the pipe
                Pair<Boolean, Direction> lookData = getLookedTube(world, pos, player);
                if (lookData != null) {
                    Direction sideHit = lookData.getRight();
                    tube.sidesClosed[sideHit.ordinal()] = !tube.sidesClosed[sideHit.ordinal()];
                    tube.onNeighborBlockUpdate();
                    world.setBlockState(pos, recalculateState(world, pos, world.getBlockState(pos)));
                }
            }
        }
        ModuleNetworkManager.getInstance(world).invalidateCache();
        return true;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != state.getBlock()) {
            getModuleDrops(getPressureTube(world, pos))
                    .forEach(drop -> world.addEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop)));
            ModuleNetworkManager.getInstance(world).invalidateCache();
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    private static NonNullList<ItemStack> getModuleDrops(TileEntityPressureTube tube) {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (tube != null) {
            for (TubeModule module : tube.modules) {
                if (module != null) {
                    drops.addAll(module.getDrops());
                }
            }
        }
        return drops;
    }

    // leaving disabled for now... to do this reliably would need a lot more network traffic, I think,
    // and it's not that important

//    @Override
//    public void animateTick(BlockState state, World par1World, BlockPos pos, Random rand) {
//        if (!PNCConfig.Client.tubeModuleRedstoneParticles) return;
//
//        TileEntityPressureTube tePt = TileEntityPressureTube.getTube(par1World.getTileEntity(pos));
//        if (tePt != null) {
//            int l = 0;
//            Direction side = null;
//            for (TubeModule module : tePt.modules) {
//                if (module != null && module.getRedstoneLevel() > l) {
//                    l = module.getRedstoneLevel();
//                    side = module.getDirection();
//                }
//            }
//            if (l > 0) {
//                double x = pos.getX() + 0.5D + side.getXOffset() * 0.5D + (rand.nextFloat() - 0.5D) * 0.5D;
//                double y = pos.getY() + 0.5D + side.getYOffset() * 0.5D + (rand.nextFloat() - 0.5D) * 0.5D;
//                double z = pos.getZ() + 0.5D + side.getZOffset() * 0.5D + (rand.nextFloat() - 0.5D) * 0.5D;
//                float f = l / 15.0F;
//                float dx = f * 0.6F + 0.4F;
//                float dy = Math.max(0f, f * f * 0.7F - 0.5F);
//                float dz = Math.max(0f, f * f * 0.6F - 0.7F);
//                par1World.addParticle(RedstoneParticleData.REDSTONE_DUST, x, y, z, dx, dy, dz);
//            }
//        }
//    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader par1IBlockAccess, BlockPos pos, Direction side) {
        TileEntityPressureTube tePt = getPressureTube(par1IBlockAccess, pos);
        if (tePt != null) {
            int redstoneLevel = 0;
            for (Direction face : Direction.VALUES) {
                if (tePt.modules[face.ordinal()] != null) {
                    if (side.getOpposite() == face || face != side && tePt.modules[face.ordinal()].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                        redstoneLevel = Math.max(redstoneLevel, tePt.modules[face.ordinal()].getRedstoneLevel());
                    }
                }
            }
            return redstoneLevel;
        }
        return 0;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Nonnull
    private static TubeHitInfo getHitInfo(RayTraceResult result) {
        return result != null && result.hitInfo instanceof TubeHitInfo ? (TubeHitInfo) result.hitInfo : TubeHitInfo.NO_HIT;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        ConnectionType[] conns = new ConnectionType[HORIZONTALS.length];
        for (Direction dir : HORIZONTALS) {
            conns[rotation.rotate(dir).getHorizontalIndex()] = state.get(CONNECTION_PROPERTIES_3[dir.getIndex()]);
        }
        for (Direction dir : HORIZONTALS) {
            state = state.with(CONNECTION_PROPERTIES_3[dir.getIndex()], conns[dir.getHorizontalIndex()]);
        }
        return super.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        ConnectionType[] conns = new ConnectionType[HORIZONTALS.length];
        for (Direction dir : HORIZONTALS) {
            Rotation r = mirrorIn.toRotation(dir);
            conns[r.rotate(dir).getHorizontalIndex()] = state.get(CONNECTION_PROPERTIES_3[dir.getIndex()]);
        }
        for (Direction dir : HORIZONTALS) {
            state = state.with(CONNECTION_PROPERTIES_3[dir.getIndex()], conns[dir.getHorizontalIndex()]);
        }
        return super.mirror(state, mirrorIn);
    }

    public enum Tier {
        ONE(1, PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE, TileEntityPressureTube.class),
        TWO(2, PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.VOLUME_ADVANCED_PRESSURE_TUBE, TileEntityAdvancedPressureTube.class);

        private final int tier;
        final float dangerPressure;
        final float criticalPressure;
        final int volume;
        private final Class<? extends TileEntityPressureTube> teClass;

        Tier(int tier, float dangerPressure, float criticalPressure, int volume, Class<? extends TileEntityPressureTube> teClass) {
            this.tier = tier;
            this.dangerPressure = dangerPressure;
            this.criticalPressure = criticalPressure;
            this.volume = volume;
            this.teClass = teClass;
        }
    }

    /**
     * Stores information about the subpart of a pressure tube that is being looked at or interacted with.
     */
    private static class TubeHitInfo {
        static final TubeHitInfo NO_HIT = new TubeHitInfo(null, null);
        static final TubeHitInfo CENTER = new TubeHitInfo(null, PartType.TUBE);

        enum PartType { TUBE, MODULE }
        final Direction dir;
        final PartType type;

        TubeHitInfo(Direction dir, PartType type) {
            this.dir = dir;
            this.type = type;
        }
    }

    /**
     * Tri-state representing the 3 possible states for a tube connection.
     */
    public enum ConnectionType implements IStringSerializable {
        UNCONNECTED("open"),
        CONNECTED("connected"),
        CLOSED("closed");

        private final String name;
        ConnectionType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
