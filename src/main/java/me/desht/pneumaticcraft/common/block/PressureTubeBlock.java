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

import me.desht.pneumaticcraft.api.block.ITubeNetworkConnector;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.item.TubeModuleItem;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.tubemodules.AbstractNetworkedRedstoneModule;
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
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class PressureTubeBlock extends AbstractCamouflageBlock
        implements SimpleWaterloggedBlock, PneumaticCraftEntityBlock, ITubeNetworkConnector {

    private static final int TUBE_WIDTH = 2;
    public static final int CORE_MIN = 8 - TUBE_WIDTH;
    public static final int CORE_MAX = 8 + TUBE_WIDTH;
    private static final double PLUG_SIZE = 2.5;
    private static final VoxelShape CORE_SHAPE = Block.box(
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
    private static final Map<Integer,VoxelShape> SHAPE_CACHE = new ConcurrentHashMap<>();

    private final BiFunction<BlockPos,BlockState,? extends PressureTubeBlockEntity> blockEntityFactory;

    public PressureTubeBlock(BiFunction<BlockPos,BlockState,? extends PressureTubeBlockEntity> blockEntityFactory) {
        super(ModBlocks.defaultProps().noOcclusion());  // noOcclusion() because of camo requirements

        this.blockEntityFactory = blockEntityFactory;
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return blockEntityFactory.apply(pPos, pState);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (worldIn instanceof Level level) {
            ModuleNetworkManager.getInstance(level).invalidateCache();
            AbstractNetworkedRedstoneModule.onNetworkReform(level, currentPos);
        }
        return stateIn;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        PressureTubeBlockEntity te = getPressureTube(reader, pos);

        return te != null ? getCachedShape(te) : CORE_SHAPE;
    }

    private VoxelShape getCachedShape(PressureTubeBlockEntity bePT) {
        int data = bePT.getShapeCacheKey();
        VoxelShape cachedShape = SHAPE_CACHE.get(data);
        if (cachedShape == null) {
            cachedShape = CORE_SHAPE;
            for (Direction dir : Direction.values()) {
                if (bePT.isSideClosed(dir)) {
                    cachedShape = Shapes.joinUnoptimized(cachedShape, ARM_CLOSED[dir.get3DDataValue()], BooleanOp.OR);
                } else if (bePT.isSideConnected(dir)) {
                    cachedShape = Shapes.joinUnoptimized(cachedShape, ARM_CONNECTED[dir.get3DDataValue()], BooleanOp.OR);
                }
                AbstractTubeModule module = bePT.getModule(dir);
                if (module != null) {
                    cachedShape = Shapes.joinUnoptimized(cachedShape, module.getShape(), BooleanOp.OR);
                }
            }
            cachedShape = cachedShape.optimize();
            SHAPE_CACHE.put(data, cachedShape);
        }
        return cachedShape;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        if (tryPlaceModule(player, world, pos, brtr.getDirection(), hand, false)) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            AbstractTubeModule module = getFocusedModule(world, pos, player);
            if (module != null) {
                return module.onActivated(player, hand) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        }
        return super.useItemOn(stack, state, world, pos, player, hand, brtr);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        // force BE to calculate its connections immediately so network manager rescanning works
        PressureTubeBlockEntity tube = getPressureTube(world, pos);
        if (!world.isClientSide()) {
            if (tube != null) {
                tube.onNeighborTileUpdate(null);
            }
        }
    }

    public boolean tryPlaceModule(Player player, Level world, BlockPos pos, Direction side, InteractionHand hand, boolean simulate) {
        PressureTubeBlockEntity tube = getPressureTube(world, pos);
        if (tube == null) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.getItem() instanceof TubeModuleItem tubeModuleItem) {
            AbstractTubeModule module = tubeModuleItem.createModule(side, tube);
            if (tube.mayPlaceModule(module)) {
                if (simulate) module.markFake();
                tube.setModule(side, module);
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
                    tube.setChanged();
                    tube.sendDescriptionPacket();
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
        BlockHitInfo rayTraceResult = doTrace(world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = rayTraceResult.tubeHitInfo();
        if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            PressureTubeBlockEntity tube = getPressureTube(world, pos);
            return tube == null ? null : tube.getModule(tubeHitInfo.dir);
        }
        return null;
    }

    /**
     * Get the face of the tube being looked at, for wrenching purposes.
     *
     * @param level the level
     * @param pos the blockpos
     * @param player the player
     * @return side of the tube being looked at
     */
    @Nullable
    private static Direction getFocusedTubeSide(BlockGetter level, BlockPos pos, Player player) {
        Pair<Vec3, Vec3> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockHitInfo blockHitInfo = doTrace(level, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = blockHitInfo.tubeHitInfo();
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            // return either the face for the tube arm (if connected), or the side of the centre face (if not)
            return tubeHitInfo.dir == null ?
                    Objects.requireNonNull(blockHitInfo.res()).getDirection() :
                    tubeHitInfo.dir;
        }
        return null;
    }

    @Nullable
    private static PressureTubeBlockEntity getPressureTube(BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        return te instanceof PressureTubeBlockEntity ? (PressureTubeBlockEntity) te : null;
    }

    @Nonnull
    private static BlockHitInfo doTrace(BlockGetter world, BlockPos pos, Vec3 origin, Vec3 direction) {
        BlockHitResult bestRTR = null;
        TubeHitInfo hitInfo = TubeHitInfo.NO_HIT;

        // first try & trace the tube core (center cube)
        BlockHitResult brtr = AABB.clip(List.of(CORE_SHAPE.bounds()), origin, direction, pos);
        if (brtr != null) {
            hitInfo = TubeHitInfo.CENTER;
            bestRTR = brtr;
        }

        // now check each arm of the tube
        PressureTubeBlockEntity tube = getPressureTube(world, pos);
        if (tube == null) return new BlockHitInfo(BlockHitResult.miss(origin, Direction.UP, pos), TubeHitInfo.NO_HIT);
        for (Direction dir: DirectionUtil.VALUES) {
            AABB arm = null;
            if (tube.isSideClosed(dir)) {
                arm = ARM_CLOSED[dir.get3DDataValue()].bounds();
            } else if (tube.isSideConnected(dir)) {
                arm = ARM_CONNECTED[dir.get3DDataValue()].bounds();
            }
            if (arm != null) {
                brtr = AABB.clip(List.of(arm), origin, direction, pos);
                if (brtr != null) {
                    if (isCloserIntersection(origin, bestRTR, brtr)) {
                        hitInfo = new TubeHitInfo(dir, TubeHitInfo.PartType.TUBE);
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
                brtr = AABB.clip(List.of(tubeAABB), origin, direction, pos);
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
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
        Pair<Vec3, Vec3> vecs = RayTraceUtils.getStartAndEndLookVec(player, PneumaticCraftUtils.getPlayerReachDistance(player));
        BlockHitInfo rayTraceResult = doTrace(world, pos, vecs.getLeft(), vecs.getRight());
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
                // toggle closed/open for this side of the tube
                Direction sideHit = getFocusedTubeSide(world, pos, player);
                if (sideHit != null) {
                    tube.setSideClosed(sideHit, !tube.isSideClosed(sideHit));
                    if (tube.isSideClosed(sideHit)) {
                        // if there's an adjacent tube which would now leak, close that too
                        PneumaticCraftUtils.getBlockEntityAt(world, pos.relative(sideHit), PressureTubeBlockEntity.class).ifPresent(tube2 -> {
                            if (shouldCloseNeighbor(tube2, sideHit)) {
                                tube2.setSideClosed(sideHit.getOpposite(), true);
                            }
                        });
                    }
                }
            }
        }

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

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != state.getBlock()) {
            getModuleDrops(getPressureTube(world, pos))
                    .forEach(drop -> world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop)));
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
    public boolean canConnectToNetwork(Level level, BlockPos pos, Direction dir, BlockState state) {
        PressureTubeBlockEntity tube = getPressureTube(level, pos);
        return tube != null && tube.isSideConnected(dir);
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
