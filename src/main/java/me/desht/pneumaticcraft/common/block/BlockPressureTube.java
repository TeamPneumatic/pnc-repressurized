/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.block.tubes.INetworkedModule;
import me.desht.pneumaticcraft.common.block.tubes.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedPressureTube;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.block.BlockPressureTube.ConnectionType.CONNECTED;
import static me.desht.pneumaticcraft.common.util.DirectionUtil.HORIZONTALS;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class BlockPressureTube extends BlockPneumaticCraftCamo implements IWaterLoggable {

    private static final int TUBE_WIDTH = 2;
    public static final int CORE_MIN = 8 - TUBE_WIDTH;
    public static final int CORE_MAX = 8 + TUBE_WIDTH;
    private static final double PLUG_SIZE = 2.5;
    private static final VoxelShape CORE = Block.box(
            8 - TUBE_WIDTH, 8 - TUBE_WIDTH, 8 - TUBE_WIDTH, 8 + TUBE_WIDTH, 8 + TUBE_WIDTH, 8 + TUBE_WIDTH
    );
    private static final VoxelShape[] ARM_CONNECTED = {  // DUNSWE order
            Block.box(CORE_MIN, 0, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX),
            Block.box(CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX, 16, CORE_MAX),
            Block.box(CORE_MIN, CORE_MIN, 0, CORE_MAX, CORE_MAX, CORE_MIN),
            Block.box(CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX, 16),
            Block.box(0, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX),
            Block.box(CORE_MAX, CORE_MIN, CORE_MIN, 16, CORE_MAX, CORE_MAX)
    };
    private static final VoxelShape[] ARM_CLOSED = {  // DUNSWE order
            Block.box(CORE_MIN, CORE_MIN - PLUG_SIZE, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX),
            Block.box(CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX, CORE_MAX + PLUG_SIZE, CORE_MAX),
            Block.box(CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MIN - PLUG_SIZE),
            Block.box(CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX, CORE_MAX + PLUG_SIZE),
            Block.box(CORE_MIN - PLUG_SIZE, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX),
            Block.box(CORE_MAX, CORE_MIN, CORE_MIN, CORE_MAX + PLUG_SIZE, CORE_MAX, CORE_MAX)
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
        super(ModBlocks.defaultProps().noOcclusion());  // notSolid() because of camo requirements
        this.tier = tier;

        BlockState state = getStateDefinition().any();
        for (EnumProperty<ConnectionType> p : CONNECTION_PROPERTIES_3) {
            state = state.setValue(p, ConnectionType.UNCONNECTED);
        }
        registerDefaultState(state.setValue(WATERLOGGED, false));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return tier.teClass;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(CONNECTION_PROPERTIES_3);
        builder.add(WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = defaultBlockState();
        List<Direction> l = new ArrayList<>();
        for (Direction dir : DirectionUtil.VALUES) {
            TileEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos().relative(dir));
            if (te != null && te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()).isPresent()) {
                state = setSide(state, dir, CONNECTED);
                l.add(dir);
            }
        }
        if (l.size() == 1) state = setSide(state, l.get(0).getOpposite(), CONNECTED);
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        BlockState newState = recalculateState(worldIn, currentPos, stateIn);
        return newState == null ?
                super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos) :
                newState;
    }

    public static BlockState recalculateState(IWorld worldIn, BlockPos currentPos, BlockState stateIn) {
        TileEntityPressureTube tePT = getPressureTube(worldIn, currentPos);
        if (tePT != null) {
            // can't clear cached shape immediately since it appears getShape() can get called
            // soon enough to re-cache the old shape...
            tePT.clearCachedShape();
            BlockState state = stateIn;
            for (Direction dir : DirectionUtil.VALUES) {
                ConnectionType type = ConnectionType.UNCONNECTED;
                if (tePT.isSideClosed(dir)) {
                    type = ConnectionType.CLOSED;
                } else if (tePT.canConnectPneumatic(dir)) {
                    TileEntity neighbourTE = worldIn.getBlockEntity(currentPos.relative(dir));
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
        for (Direction dir : DirectionUtil.VALUES) {
            if (te.getModule(dir) != null) {
                return state;
            }
            switch (state.getValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()])) {
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
        return te != null ? te.getCachedTubeShape(res) : res;
    }

    private VoxelShape getCachedShape(BlockState state) {
        int idx = 0;
        int mul = 1;
        for (Direction d : Direction.values()) {
            idx += state.getValue(CONNECTION_PROPERTIES_3[d.get3DDataValue()]).getIndex() * mul;
            mul *= 3;
        }
        if (SHAPE_CACHE[idx] == null) {
            VoxelShape res = CORE;
            for (Direction d : Direction.values()) {
                switch (state.getValue(CONNECTION_PROPERTIES_3[d.get3DDataValue()])) {
                    case CONNECTED: res = VoxelShapes.join(res, ARM_CONNECTED[d.get3DDataValue()], IBooleanFunction.OR); break;
                    case CLOSED: res = VoxelShapes.join(res, ARM_CLOSED[d.get3DDataValue()], IBooleanFunction.OR); break;
                }
            }
            SHAPE_CACHE[idx] = res;
        }

        return SHAPE_CACHE[idx];
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (tryPlaceModule(player, world, pos, brtr.getDirection(), hand, false)) {
            return ActionResultType.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            TubeModule module = getFocusedModule(world, pos, player);
            if (module != null) {
                return module.onActivated(player, hand) ? ActionResultType.SUCCESS : ActionResultType.PASS;
            }
        }
        return super.use(state, world, pos, player, hand, brtr);
    }

    public int getTier() {
        return tier.tier;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        ModuleNetworkManager.getInstance(world).invalidateCache();
        // force TE to calculate its connections immediately so network manager rescanning works
        TileEntityPressureTube te = getPressureTube(world, pos);
        if (te != null) {
            te.onNeighborTileUpdate(null);
        }
    }

    public boolean tryPlaceModule(PlayerEntity player, World world, BlockPos pos, Direction side, Hand hand, boolean simulate) {
        TileEntityPressureTube tePT = getPressureTube(world, pos);
        if (tePT == null) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.getItem() instanceof ItemTubeModule) {
            TubeModule module = ((ItemTubeModule) heldStack.getItem()).createModule();
            if (tePT.mayPlaceModule(module, side)) {
                if (simulate) module.markFake();
                tePT.setModule(side, module);
                if (!simulate && !world.isClientSide) {
                    neighborChanged(world.getBlockState(pos), world, pos, this, pos.relative(side), false);
                    world.updateNeighborsAt(pos, this);
                    if (!player.isCreative()) heldStack.shrink(1);
                    world.playSound(null, pos, SoundType.GLASS.getStepSound(), SoundCategory.BLOCKS, SoundType.GLASS.getVolume() * 5.0f, SoundType.GLASS.getPitch() * 0.9f);
                    if (module instanceof INetworkedModule) {
                        ModuleNetworkManager.getInstance(world).invalidateCache();
                    }
                }
                if (!simulate) module.onPlaced();
                return true;
            }
        } else if (heldStack.getItem() == ModItems.ADVANCED_PCB.get() && !simulate) {
            TubeModule module = BlockPressureTube.getFocusedModule(world, pos, player);
            if (module != null && !module.isUpgraded() && module.canUpgrade()) {
                if (!world.isClientSide) {
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
        Pair<Vector3d, Vector3d> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockState state = world.getBlockState(pos);
        BlockRayTraceResult rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            TileEntityPressureTube tube = getPressureTube(world, pos);
            return tube == null ? null : tube.getModule(tubeHitInfo.dir);
        }
        return null;
    }

    private static BlockState setSide(BlockState state, Direction side, ConnectionType type) {
        return state.setValue(CONNECTION_PROPERTIES_3[side.get3DDataValue()], type);
    }

    private static TileEntityPressureTube getPressureTube(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
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
        Pair<Vector3d, Vector3d> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockState state = world.getBlockState(pos);
        BlockRayTraceResult rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            // return either the tube arm (if connected), or the side of the centre face (if not)
            return tubeHitInfo.dir == null ? Pair.of(true, Objects.requireNonNull(rayTraceResult).getDirection()) : Pair.of(false, tubeHitInfo.dir);
        }
        return null;
    }

    private static BlockRayTraceResult doTrace(BlockState state, IBlockReader world, BlockPos pos, Vector3d origin, Vector3d direction) {
        BlockRayTraceResult bestRTR = null;

        // first try & trace the tube core (center cube)
        BlockRayTraceResult brtr = AxisAlignedBB.clip(Collections.singletonList(CORE.bounds()), origin, direction, pos);
        if (brtr != null) {
            brtr.hitInfo = TubeHitInfo.CENTER;
            bestRTR = brtr;
        }

        // now check each arm of the tube
        TileEntityPressureTube tube = getPressureTube(world, pos);
        if (tube == null) return null;
        for (int i = 0; i < 6; i++) {
            AxisAlignedBB arm;
            switch (state.getValue(CONNECTION_PROPERTIES_3[i])) {
                case CLOSED:
                    arm = ARM_CLOSED[i].bounds(); break;
                case CONNECTED:
                    arm = ARM_CONNECTED[i].bounds(); break;
                default:
                    arm = null; break;
            }
            if (arm != null) {
                brtr = AxisAlignedBB.clip(Collections.singletonList(arm), origin, direction, pos);
                if (brtr != null) {
                    if (isCloserIntersection(origin, bestRTR, brtr)) {
                        brtr.hitInfo = new TubeHitInfo(Direction.from3DDataValue(i), TubeHitInfo.PartType.TUBE);
                        bestRTR = brtr;
                    }
                }
            }
        }

        // now check attached tube modules
        for (Direction dir : DirectionUtil.VALUES) {
            TubeModule tm = tube.getModule(dir);
            if (tm != null) {
                AxisAlignedBB tubeAABB = tm.getShape().bounds();
                brtr = AxisAlignedBB.clip(Collections.singletonList(tubeAABB), origin, direction, pos);
                if (isCloserIntersection(origin, bestRTR, brtr)) {
                    brtr.hitInfo = new TubeHitInfo(dir, TubeHitInfo.PartType.MODULE);  // tube module
                    bestRTR = brtr;
                }
            }
        }

        return bestRTR;
    }

    private static boolean isCloserIntersection(Vector3d origin, RayTraceResult oldRTR, RayTraceResult newRTR) {
        return newRTR != null &&
                (oldRTR == null || origin.distanceToSqr(newRTR.getLocation()) <= origin.distanceToSqr(oldRTR.getLocation()));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        Pair<Vector3d, Vector3d> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockRayTraceResult rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            return super.getPickBlock(state, target, world, pos, player);
        } else if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            TileEntityPressureTube tube = getPressureTube(world, pos);
            if (tube != null) {
                TubeModule tm = tube.getModule(tubeHitInfo.dir);
                if (tm != null) {
                    return new ItemStack(tm.getItem());
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
        if (player.isShiftKeyDown()) {
            if (module != null) {
                // detach and drop the module as an item
                if (!player.isCreative()) {
                    for (ItemStack drop : module.getDrops()) {
                        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        entity.setItem(drop);
                        world.addFreshEntity(entity);
                        entity.playerTouch(player);
                    }
                }
                tube.setModule(module.getDirection(), null);
                neighborChanged(world.getBlockState(pos), world, pos, this, pos.relative(side), false);
                world.updateNeighborsAt(pos, this);
            } else {
                // drop the pressure tube as an item
                if (!player.isCreative()) dropResources(world.getBlockState(pos), world, pos, tube);
                removeBlockSneakWrenched(world, pos);
            }
        } else {
            if (module != null) {
                module.onActivated(player, hand);
            } else {
                // toggle closed/open for this side of the pipe
                Pair<Boolean, Direction> lookData = getLookedTube(world, pos, player);
                if (lookData != null) {
                    Direction sideHit = lookData.getRight();
                    tube.setSideClosed(sideHit, !tube.isSideClosed(sideHit));
                    tube.onNeighborBlockUpdate(pos.relative(sideHit));
                    world.setBlockAndUpdate(pos, recalculateState(world, pos, world.getBlockState(pos)));
                    PneumaticRegistry.getInstance().forceClientShapeRecalculation(world, pos);
                }
            }
        }
        ModuleNetworkManager.getInstance(world).invalidateCache();

        return true;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != state.getBlock()) {
            getModuleDrops(getPressureTube(world, pos))
                    .forEach(drop -> world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop)));
            ModuleNetworkManager.getInstance(world).invalidateCache();
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private static NonNullList<ItemStack> getModuleDrops(TileEntityPressureTube tube) {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (tube != null) {
            tube.tubeModules().map(TubeModule::getDrops).forEach(drops::addAll);
        }
        return drops;
    }

    // leaving disabled for now... to do this reliably would need a lot more network traffic, I think,
    // and it's not that important

//    @Override
//    public void animateTick(BlockState state, World par1World, BlockPos pos, Random rand) {
//        if (!ConfigHelper.client().tubeModuleRedstoneParticles.get()) return;
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
    public int getSignal(BlockState state, IBlockReader par1IBlockAccess, BlockPos pos, Direction side) {
        TileEntityPressureTube tePt = getPressureTube(par1IBlockAccess, pos);
        if (tePt != null) {
            int redstoneLevel = 0;
            for (Direction face : DirectionUtil.VALUES) {
                TubeModule tm = tePt.getModule(face);
                if (tm != null) {
                    if (side.getOpposite() == face || face != side && tm.isInline()) {
                        // if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                        redstoneLevel = Math.max(redstoneLevel, tm.getRedstoneLevel());
                    }
                }
            }
            return redstoneLevel;
        }
        return 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
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
            conns[rotation.rotate(dir).get2DDataValue()] = state.getValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()]);
        }
        for (Direction dir : HORIZONTALS) {
            state = state.setValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()], conns[dir.get2DDataValue()]);
        }
        return super.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        ConnectionType[] conns = new ConnectionType[HORIZONTALS.length];
        for (Direction dir : HORIZONTALS) {
            Rotation r = mirrorIn.getRotation(dir);
            conns[r.rotate(dir).get2DDataValue()] = state.getValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()]);
        }
        for (Direction dir : HORIZONTALS) {
            state = state.setValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()], conns[dir.get2DDataValue()]);
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
        UNCONNECTED(0, "open"),
        CONNECTED(1, "connected"),
        CLOSED(2, "closed");

        private final int index;
        private final String name;

        ConnectionType(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public int getIndex() {
            return index;
        }
    }
}
