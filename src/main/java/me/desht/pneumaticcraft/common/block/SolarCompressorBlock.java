package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.block.entity.SolarCompressorBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SolarCompressorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    public static final BooleanProperty BOUNDING = BooleanProperty.create("bounding");

    public SolarCompressorBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
                .setValue(BOUNDING, false));
    }

    private static final VoxelShape SHAPE = Stream.of(
            Block.box(0, 1, 4, 16, 16, 12),
            Block.box(5, 16, 5, 11, 17, 11),
            Block.box(2, 15, 3, 3, 17, 13),
            Block.box(13, 15, 3, 14, 17, 13),
            Block.box(3, 3, 0, 13, 13, 16),
            Block.box(1, 1, 1, 15, 15, 15),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(7, 17, 7, 9, 31, 9)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if(state.getValue(BOUNDING)) {
            return Block.box(0,0,0,0,0,0);
        }
        else {
            return SHAPE;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if(state.getValue(BOUNDING)) {
            return Block.box(0,0,0,0,0,0);
        }
        else {
            return SHAPE;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOUNDING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SolarCompressorBlockEntity(pPos, pState);
    }

    public BlockPos getMainPos(BlockState state, LevelReader world, BlockPos pos) {
        // Gets position of main from bounding block based on offset
        if(state.getValue(BOUNDING)) {
            SolarCompressorBlockEntity bounding = (SolarCompressorBlockEntity) world.getBlockEntity(pos);

            return pos.subtract(bounding.offsetFromMain);
        }

        // Returns current position as block is main
        else {
            return pos;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(BOUNDING)) {
            return world.getBlockState(getMainPos(state, world, pos)).getBlock() == this;
        } else {
            return world.isEmptyBlock(pos.offset(0,1,0)) &&
                    world.isEmptyBlock(pos.offset(0,1,1)) &&
                    world.isEmptyBlock(pos.offset(0,1,-1));
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack par6ItemStack) {
        super.setPlacedBy(world, pos, state, par5EntityLiving, par6ItemStack);

        // Declares bounding block offsets
        Vec3i boundingVec1 = new Vec3i(0, 1, 0);
        Vec3i boundingVec2 = new Vec3i(0, 1, 1);
        Vec3i boundingVec3 = new Vec3i(0, 1, -1);

        // Creates bounding blocks and sets their offsets
        world.setBlock(pos.offset(boundingVec1), world.getBlockState(pos).setValue(BOUNDING, true), Block.UPDATE_ALL);
        ((SolarCompressorBlockEntity)world.getBlockEntity(pos.offset(boundingVec1))).offsetFromMain = boundingVec1;

        world.setBlock(pos.offset(boundingVec2), world.getBlockState(pos).setValue(BOUNDING, true), Block.UPDATE_ALL);
        ((SolarCompressorBlockEntity)world.getBlockEntity(pos.offset(boundingVec2))).offsetFromMain = boundingVec2;

        world.setBlock(pos.offset(boundingVec3), world.getBlockState(pos).setValue(BOUNDING, true), Block.UPDATE_ALL);
        ((SolarCompressorBlockEntity)world.getBlockEntity(pos.offset(boundingVec3))).offsetFromMain = boundingVec3;

    }

    /**
     * Removes the connected bounding blocks of the main block
     * @param state main block state
     * @param level main block level
     * @param pos main block position
     * @param player player to spawn break particles around, null if no particles should spawn
     */
    public void removeBoundingBlocks (BlockState state, Level level, BlockPos pos,
                                      @Nullable Player player) {
        BlockPos boundingPos1 = pos.offset(0, 1, 0);
        BlockPos boundingPos2 = pos.offset(0, 1, 1);
        BlockPos boundingPos3 = pos.offset(0, 1, -1);

        if (level.getBlockState(boundingPos1).getBlock() == this) {
            if(player != null) {
                spawnDestroyParticles(level, player, boundingPos1, state);
            }

            level.removeBlock(boundingPos1, false);
        }

        if (level.getBlockState(boundingPos2).getBlock() == this) {
            if(player != null) {
                spawnDestroyParticles(level, player, boundingPos2, state);
            }

            level.removeBlock(boundingPos2, false);
        }

        if (level.getBlockState(boundingPos3).getBlock() == this) {
            if(player != null) {
                spawnDestroyParticles(level, player, boundingPos3, state);
            }

            level.removeBlock(boundingPos3, false);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockPos mainPos = getMainPos(state, level, pos);
        BlockState mainBlockState = level.getBlockState(mainPos);

        // Redirects destroy to main block for bounding blocks
        if (state.getValue(BOUNDING)) {
            onDestroyedByPlayer(mainBlockState, level, mainPos, player, willHarvest,
                    mainBlockState.getFluidState());

            return false;
        }

        // Destroys all present bounding blocks
        else {
            removeBoundingBlocks(state, level, pos, player);
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
    }

    @Override
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction face, InteractionHand hand) {
        BlockState state = world.getBlockState(pos);
        BlockPos mainPos = getMainPos(state, world, pos);

        // Redirects wrenching to main block for bounding blocks
        if (state.getValue(BOUNDING)) {
            return onWrenched(world, player, mainPos, face, hand);
        }

        // Destroy main and then all present bounding blocks on wrench pickup
        if (player != null && player.isShiftKeyDown()) {
            super.onWrenched(world, player, pos, face, hand);

            removeBoundingBlocks(state, world, pos, null);
        }

        return true;
    }

    // Ensures that all blocks are broken when one block breaks
    // Common breaks should be handled by their own methods, this is more of a catch-all for
    // things like /fill and explosions
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockPos mainPos = getMainPos(state, world, pos);
        BlockPos boundingPos1 = mainPos.offset(0, 1, 0);
        BlockPos boundingPos2 = mainPos.offset(0, 1, 1);
        BlockPos boundingPos3 = mainPos.offset(0, 1, -1);

        // Removes bounding blocks
        if (world.getBlockState(boundingPos1).getBlock() == this) {
            world.removeBlock(boundingPos1, false);
        }

        if (world.getBlockState(boundingPos2).getBlock() == this) {
            world.removeBlock(boundingPos2, false);
        }

        if (world.getBlockState(boundingPos3).getBlock() == this) {
            world.removeBlock(boundingPos3, false);
        }

        // Removes main block
        if (world.getBlockState(mainPos).getBlock() == this) {
            world.removeBlock(mainPos, false);
        }

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        // Only can repair main block of compressor
        if (!state.getValue(BOUNDING)) {
            BlockEntity be = world.getBlockEntity(pos);

            // Only can repair if proper block entity type
            if (be instanceof SolarCompressorBlockEntity) {

                // Only can repair if broken
                if (((SolarCompressorBlockEntity)be).isBroken()) {
                    // Uses wafer in main hand to repair compressor
                    if (player.getMainHandItem().getItem() == ModItems.SOLAR_WAFER.get()) {
                        ItemStack wafers = player.getMainHandItem();

                        // Only consumes wafers outside of creative mode
                        if (!player.isCreative()) {
                            wafers.setCount(wafers.getCount() - 1);
                        }

                        ((SolarCompressorBlockEntity) be).fixBroken();

                        return InteractionResult.CONSUME;
                    }

                    // Uses wafer in offhand to repair compressor
                    else if (player.getOffhandItem().getItem() == ModItems.SOLAR_WAFER.get()) {
                        ItemStack wafers = player.getOffhandItem();

                        // Only consumes wafers outside of creative mode
                        if (!player.isCreative()) {
                            wafers.setCount(wafers.getCount() - 1);
                        }

                        ((SolarCompressorBlockEntity) be).fixBroken();

                        return InteractionResult.CONSUME;
                    }
                }
            }
        }

        return super.use(state, world, pos, player, hand, brtr);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter world, List<Component> curInfo, TooltipFlag flag) {
        super.appendHoverText(stack, world, curInfo, flag);

        if (stack.hasTag()) {
            CompoundTag subTag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (subTag != null && subTag.contains(NBTKeys.NBT_BROKEN)) {
                if (subTag.getBoolean(NBTKeys.NBT_BROKEN)) {
                    curInfo.add(xlate("pneumaticcraft.gui.tooltip.broken"));
                }
            }
        }
    }
}
