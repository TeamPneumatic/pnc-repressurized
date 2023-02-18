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
import me.desht.pneumaticcraft.api.block.ITubeNetworkConnector;
import me.desht.pneumaticcraft.api.block.PNCBlockStateProperties;
import me.desht.pneumaticcraft.api.block.PressureTubeConnection;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.TubeModuleItem;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import me.desht.pneumaticcraft.common.tubemodules.INetworkedModule;
import me.desht.pneumaticcraft.common.tubemodules.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static me.desht.pneumaticcraft.api.block.PressureTubeConnection.CONNECTED;
import static me.desht.pneumaticcraft.common.util.DirectionUtil.HORIZONTALS;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class PressureTubeBlock extends AbstractCamouflageBlock
        implements SimpleWaterloggedBlock, PneumaticCraftEntityBlock, ITubeNetworkConnector {

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
            Block.box(CORE_MIN, CORE_MIN, CORE_MIN - PLUG_SIZE, CORE_MAX, CORE_MAX, CORE_MIN),
            Block.box(CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX, CORE_MAX + PLUG_SIZE),
            Block.box(CORE_MIN - PLUG_SIZE, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX),
            Block.box(CORE_MAX, CORE_MIN, CORE_MIN, CORE_MAX + PLUG_SIZE, CORE_MAX, CORE_MAX)
    };
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[729];  // 3 ^ 6

    @SuppressWarnings("unchecked")
    private static final EnumProperty<PressureTubeConnection>[] CONNECTION_PROPERTIES_3 = new EnumProperty[]{
            PNCBlockStateProperties.PressureTubes.DOWN, PNCBlockStateProperties.PressureTubes.UP, PNCBlockStateProperties.PressureTubes.NORTH,
            PNCBlockStateProperties.PressureTubes.SOUTH, PNCBlockStateProperties.PressureTubes.WEST, PNCBlockStateProperties.PressureTubes.EAST
    };

    private final BiFunction<BlockPos,BlockState,? extends PressureTubeBlockEntity> teFactory;

    public PressureTubeBlock(BiFunction<BlockPos,BlockState,? extends PressureTubeBlockEntity> teFactory) {
        super(ModBlocks.defaultProps().noOcclusion());  // noOcclusion() because of camo requirements
        this.teFactory = teFactory;

        BlockState state = getStateDefinition().any();
        for (EnumProperty<PressureTubeConnection> p : CONNECTION_PROPERTIES_3) {
            state = state.setValue(p, PressureTubeConnection.OPEN);
        }
        registerDefaultState(state.setValue(WATERLOGGED, false));
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return teFactory.apply(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(CONNECTION_PROPERTIES_3);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null) return null;

        List<Direction> l = new ArrayList<>();
        for (Direction dir : DirectionUtil.VALUES) {
            BlockEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos().relative(dir));
            if (te != null && te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()).isPresent()) {
                state = setSide(state, dir, CONNECTED);
                l.add(dir);
                if (l.size() > 1) break;
            }
        }
        if (l.size() == 1) state = setSide(state, l.get(0).getOpposite(), CONNECTED);
        return state;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        BlockState newState = recalculateState(worldIn, currentPos, stateIn);
        return newState == null ?
                super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos) :
                newState;
    }

    public static BlockState recalculateState(LevelAccessor worldIn, BlockPos currentPos, BlockState stateIn) {
        PressureTubeBlockEntity tePT = getPressureTube(worldIn, currentPos);
        if (tePT != null) {
            // can't clear cached shape immediately since it appears getShape() can get called
            // soon enough to re-cache the old shape...
            tePT.clearCachedShape();
            BlockState state = stateIn;
            for (Direction dir : DirectionUtil.VALUES) {
                PressureTubeConnection type = PressureTubeConnection.OPEN;
                if (tePT.isSideClosed(dir)) {
                    type = PressureTubeConnection.CLOSED;
                } else if (tePT.canConnectPneumatic(dir)) {
                    BlockEntity neighbourTE = tePT.getCachedNeighbor(dir); //worldIn.getBlockEntity(currentPos.relative(dir));
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

    private static BlockState checkForSingleConnection(PressureTubeBlockEntity te, BlockState state) {
        List<Direction> connected = new ArrayList<>();
        int nUnconnected = 0;
        for (Direction dir : DirectionUtil.VALUES) {
            if (te.getModule(dir) != null) {
                return state;
            }
            switch (state.getValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()])) {
                case CONNECTED: connected.add(dir); break;
                case OPEN: nUnconnected++; break;
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
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        VoxelShape res = getCachedShape(state);
        PressureTubeBlockEntity te = getPressureTube(reader, pos);
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
                    case CONNECTED -> res = Shapes.join(res, ARM_CONNECTED[d.get3DDataValue()], BooleanOp.OR);
                    case CLOSED -> res = Shapes.join(res, ARM_CLOSED[d.get3DDataValue()], BooleanOp.OR);
                }
            }
            SHAPE_CACHE[idx] = res;
        }

        return SHAPE_CACHE[idx];
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        if (tryPlaceModule(player, world, pos, brtr.getDirection(), hand, false)) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            AbstractTubeModule module = getFocusedModule(world, pos, player);
            if (module != null) {
                return module.onActivated(player, hand) ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
        }
        return super.use(state, world, pos, player, hand, brtr);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        ModuleNetworkManager.getInstance(world).invalidateCache();
        // force BE to calculate its connections immediately so network manager rescanning works
        PressureTubeBlockEntity te = getPressureTube(world, pos);
        if (te != null) {
            te.onNeighborTileUpdate(null);
        }
    }

    public boolean tryPlaceModule(Player player, Level world, BlockPos pos, Direction side, InteractionHand hand, boolean simulate) {
        PressureTubeBlockEntity tePT = getPressureTube(world, pos);
        if (tePT == null) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.getItem() instanceof TubeModuleItem tubeModuleItem) {
            AbstractTubeModule module = tubeModuleItem.createModule(side, tePT);
            if (tePT.mayPlaceModule(module)) {
                if (simulate) module.markFake();
                tePT.setModule(side, module);
                if (!simulate && !world.isClientSide) {
                    neighborChanged(world.getBlockState(pos), world, pos, this, pos.relative(side), false);
                    world.updateNeighborsAt(pos, this);
                    if (!player.isCreative()) heldStack.shrink(1);
                    world.playSound(null, pos, SoundType.GLASS.getStepSound(), SoundSource.BLOCKS, SoundType.GLASS.getVolume() * 5.0f, SoundType.GLASS.getPitch() * 0.9f);
                    if (module instanceof INetworkedModule) {
                        ModuleNetworkManager.getInstance(world).invalidateCache();
                    }
                }
                if (!simulate) module.onPlaced();
                return true;
            }
        } else if (heldStack.getItem() == ModItems.MODULE_EXPANSION_CARD.get() && !simulate) {
            AbstractTubeModule module = PressureTubeBlock.getFocusedModule(world, pos, player);
            if (module != null && !module.isUpgraded() && module.canUpgrade()) {
                if (!world.isClientSide) {
                    module.upgrade();
                    tePT.setChanged();
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
    public static AbstractTubeModule getFocusedModule(Level world, BlockPos pos, Player player) {
        Pair<Vec3, Vec3> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockState state = world.getBlockState(pos);
        BlockHitInfo rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = rayTraceResult.tubeHitInfo();
        if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            PressureTubeBlockEntity tube = getPressureTube(world, pos);
            return tube == null ? null : tube.getModule(tubeHitInfo.dir);
        }
        return null;
    }

    private static BlockState setSide(BlockState state, Direction side, PressureTubeConnection type) {
        return state.setValue(CONNECTION_PROPERTIES_3[side.get3DDataValue()], type);
    }

    private static PressureTubeBlockEntity getPressureTube(BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        return te instanceof PressureTubeBlockEntity ? (PressureTubeBlockEntity) te : null;
    }

    /**
     * Get the part of the tube being looked at.
     *
     * @param world the world
     * @param pos the blockpos
     * @param player the player
     * @return (true, side) if it's the side of the tube core, or (false, side) if it's a tube arm
     */
    private static Pair<Boolean, Direction> getLookedTube(BlockGetter world, BlockPos pos, Player player) {
        Pair<Vec3, Vec3> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockState state = world.getBlockState(pos);
        BlockHitInfo blockHitInfo = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = blockHitInfo.tubeHitInfo();
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            // return either the tube arm (if connected), or the side of the centre face (if not)
            return tubeHitInfo.dir == null ?
                    Pair.of(true, Objects.requireNonNull(blockHitInfo.res()).getDirection()) :
                    Pair.of(false, tubeHitInfo.dir);
        }
        return null;
    }

    @Nonnull
    private static BlockHitInfo doTrace(BlockState state, BlockGetter world, BlockPos pos, Vec3 origin, Vec3 direction) {
        BlockHitResult bestRTR = null;
        TubeHitInfo hitInfo = TubeHitInfo.NO_HIT;

        // first try & trace the tube core (center cube)
        BlockHitResult brtr = AABB.clip(Collections.singletonList(CORE.bounds()), origin, direction, pos);
        if (brtr != null) {
            hitInfo = TubeHitInfo.CENTER;
            bestRTR = brtr;
        }

        // now check each arm of the tube
        PressureTubeBlockEntity tube = getPressureTube(world, pos);
        if (tube == null) return new BlockHitInfo(BlockHitResult.miss(origin, Direction.UP, pos), TubeHitInfo.NO_HIT);
        for (int i = 0; i < 6; i++) {
            AABB arm = switch (state.getValue(CONNECTION_PROPERTIES_3[i])) {
                case CLOSED -> ARM_CLOSED[i].bounds();
                case CONNECTED -> ARM_CONNECTED[i].bounds();
                default -> null;
            };
            if (arm != null) {
                brtr = AABB.clip(Collections.singletonList(arm), origin, direction, pos);
                if (brtr != null) {
                    if (isCloserIntersection(origin, bestRTR, brtr)) {
                        hitInfo = new TubeHitInfo(Direction.from3DDataValue(i), TubeHitInfo.PartType.TUBE);
                        bestRTR = brtr;
                    }
                }
            }
        }

        // now check attached tube modules
        for (Direction dir : DirectionUtil.VALUES) {
            AbstractTubeModule tm = tube.getModule(dir);
            if (tm != null) {
                AABB tubeAABB = tm.getShape().bounds();
                brtr = AABB.clip(Collections.singletonList(tubeAABB), origin, direction, pos);
                if (isCloserIntersection(origin, bestRTR, brtr) || tm.isInlineAndFocused(hitInfo)) {
                    hitInfo = new TubeHitInfo(dir, TubeHitInfo.PartType.MODULE);  // tube module
                    bestRTR = brtr;
                }
            }
        }

        return new BlockHitInfo(bestRTR, hitInfo);
    }

    private static boolean isCloserIntersection(Vec3 origin, HitResult oldRTR, HitResult newRTR) {
        return newRTR != null &&
                (oldRTR == null || origin.distanceToSqr(newRTR.getLocation()) <= origin.distanceToSqr(oldRTR.getLocation()));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        Pair<Vec3, Vec3> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockHitInfo rayTraceResult = doTrace(state, world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = rayTraceResult.tubeHitInfo();
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            return super.getCloneItemStack(state, target, world, pos, player);
        } else if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            PressureTubeBlockEntity tube = getPressureTube(world, pos);
            if (tube != null) {
                AbstractTubeModule tm = tube.getModule(tubeHitInfo.dir);
                if (tm != null) {
                    return new ItemStack(tm.getItem());
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (player == null) return false;
        PressureTubeBlockEntity tube = getPressureTube(world, pos);
        if (tube == null) return false;
        AbstractTubeModule module = getFocusedModule(world, pos, player);
        if (player.isShiftKeyDown()) {
            if (module != null) {
                // detach and drop the module as an item
                if (!player.isCreative()) {
                    for (ItemStack drop : module.getDrops()) {
                        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
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
                    setTubeSideClosed(tube, sideHit, !tube.isSideClosed(sideHit));
                    if (tube.isSideClosed(sideHit)) {
                        // if there's an adjacent tube which would now leak, close that too
                        PneumaticCraftUtils.getTileEntityAt(world, pos.relative(sideHit), PressureTubeBlockEntity.class).ifPresent(tube2 -> {
                            if (shouldCloseNeighbor(tube2, sideHit)) {
                                setTubeSideClosed(tube2, sideHit.getOpposite(), true);
                            }
                        });
                    }
                }
            }
        }
        ModuleNetworkManager.getInstance(world).invalidateCache();

        return true;
    }

    private boolean shouldCloseNeighbor(PressureTubeBlockEntity tube2, Direction offset) {
        boolean doClose = false;
        for (Direction d : DirectionUtil.VALUES) {
            if (tube2.getConnectedNeighbor(d) != null) {
                if (d.getAxis() == offset.getAxis()) {
                    doClose = true;
                } else {
                    return false;
                }
            }
        }
        return doClose;
    }

    private void setTubeSideClosed(PressureTubeBlockEntity tube, Direction side, boolean closed) {
        tube.setSideClosed(side, closed);
        Level world = tube.nonNullLevel();
        BlockPos pos = tube.getBlockPos();
        world.setBlockAndUpdate(pos, recalculateState(world, pos, world.getBlockState(pos)));
        PneumaticRegistry.getInstance().getMiscHelpers().forceClientShapeRecalculation(world, pos);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != state.getBlock()) {
            getModuleDrops(getPressureTube(world, pos))
                    .forEach(drop -> world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop)));
            ModuleNetworkManager.getInstance(world).invalidateCache();
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private static NonNullList<ItemStack> getModuleDrops(PressureTubeBlockEntity tube) {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (tube != null) {
            tube.tubeModules().map(AbstractTubeModule::getDrops).forEach(drops::addAll);
        }
        return drops;
    }

    // leaving disabled for now... to do this reliably would need a lot more network traffic, I think,
    // and it's not that important

//    @Override
//    public void animateTick(BlockState state, World par1World, BlockPos pos, Random rand) {
//        if (!ConfigHelper.client().tubeModuleRedstoneParticles.get()) return;
//
//        PressureTubeBlockEntity tePt = PressureTubeBlockEntity.getTube(par1World.getTileEntity(pos));
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
    public int getSignal(BlockState state, BlockGetter par1IBlockAccess, BlockPos pos, Direction side) {
        PressureTubeBlockEntity tePt = getPressureTube(par1IBlockAccess, pos);
        if (tePt != null) {
            int redstoneLevel = 0;
            for (Direction face : DirectionUtil.VALUES) {
                AbstractTubeModule tm = tePt.getModule(face);
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

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        PressureTubeConnection[] conns = new PressureTubeConnection[HORIZONTALS.length];
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
        PressureTubeConnection[] conns = new PressureTubeConnection[HORIZONTALS.length];
        for (Direction dir : HORIZONTALS) {
            Rotation r = mirrorIn.getRotation(dir);
            conns[r.rotate(dir).get2DDataValue()] = state.getValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()]);
        }
        for (Direction dir : HORIZONTALS) {
            state = state.setValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()], conns[dir.get2DDataValue()]);
        }
        return super.mirror(state, mirrorIn);
    }

    @Override
    public boolean canConnectToNetwork(Level level, BlockPos pos, Direction dir, BlockState state) {
        return state.hasProperty(CONNECTION_PROPERTIES_3[dir.get3DDataValue()])
                && state.getValue(CONNECTION_PROPERTIES_3[dir.get3DDataValue()]) == CONNECTED;
    }

    /**
     * Stores information about the subpart of a pressure tube that is being looked at or interacted with.
     */
    public record TubeHitInfo(Direction dir, PressureTubeBlock.TubeHitInfo.PartType type) {
        static final TubeHitInfo NO_HIT = new TubeHitInfo(null, null);
        public static final TubeHitInfo CENTER = new TubeHitInfo(null, PartType.TUBE);

        enum PartType { TUBE, MODULE }
    }

    private record BlockHitInfo(BlockHitResult res, @Nonnull TubeHitInfo tubeHitInfo) {
    }

}
