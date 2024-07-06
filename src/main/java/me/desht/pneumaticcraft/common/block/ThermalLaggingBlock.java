package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;

import javax.annotation.Nullable;

public class ThermalLaggingBlock extends AbstractPneumaticCraftBlock {
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(0, 0, 0, 16,  0.5, 16),
            Block.box(0, 15.5, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16,  0.5),
            Block.box(0, 0, 15.5, 16, 16, 16),
            Block.box(0, 0, 0,  0.5, 16, 16),
            Block.box(15.5, 0, 0, 16, 16, 16),
    };

    public ThermalLaggingBlock() {
        super(ModBlocks.defaultProps().noOcclusion().noCollission());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        if (selectionContext instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getMainHandItem();
            return ModdedWrenchUtils.getInstance().isWrench(stack)
                    || stack.getItem().canPerformAction(stack, ItemAbilities.PICKAXE_DIG)
                    || livingEntity.isCrouching() ?
                    SHAPES[getRotation(state).get3DDataValue()] : Shapes.empty();
        }
        return SHAPES[getRotation(state).get3DDataValue()];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(directionProperty(), ctx.getClickedFace().getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        Direction dir = getRotation(state);
        return !worldIn.getBlockState(pos.relative(dir)).isAir();
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!canSurvive(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() == state.getBlock();
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }
}
