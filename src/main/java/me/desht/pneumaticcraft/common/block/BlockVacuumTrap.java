package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.state.properties.BlockStateProperties.*;

public class BlockVacuumTrap extends BlockPneumaticCraft implements IWaterLoggable {
    private static final VoxelShape FLAP1 = Block.makeCuboidShape(2, 11, 11, 14, 12, 16);
    private static final VoxelShape FLAP2 = Block.makeCuboidShape(2, 11, 0, 14, 12, 5);
    private static final VoxelShape SHAPE_EW_CLOSED = Stream.of(
            Block.makeCuboidShape(0, 1, 3, 16, 11, 13),
            Block.makeCuboidShape(1, 0, 4, 15, 1, 12),
            Block.makeCuboidShape(0, 12, 7, 2, 14, 9),
            Block.makeCuboidShape(0, 11, 6, 2, 12, 10),
            Block.makeCuboidShape(14, 11, 6, 16, 12, 10),
            Block.makeCuboidShape(0, 14, 7, 8, 16, 9),
            Block.makeCuboidShape(2, 11, 8, 14, 12, 13),
            Block.makeCuboidShape(2, 11, 3, 14, 12, 8)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
    private static final VoxelShape SHAPE_EW_OPEN_BASE = Stream.of(
            Block.makeCuboidShape(0, 1, 3, 16, 11, 13),
            Block.makeCuboidShape(1, 0, 4, 15, 1, 12),
            Block.makeCuboidShape(0, 12, 7, 2, 14, 9),
            Block.makeCuboidShape(0, 11, 6, 2, 12, 10),
            Block.makeCuboidShape(14, 11, 6, 16, 12, 10),
            Block.makeCuboidShape(0, 14, 7, 8, 16, 9)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
    private static final VoxelShape SHAPE_EW_OPEN = VoxelShapeUtils.combine(IBooleanFunction.OR, SHAPE_EW_OPEN_BASE, FLAP1, FLAP2);
    private static final VoxelShape SHAPE_NS_CLOSED = VoxelShapeUtils.rotateY(SHAPE_EW_CLOSED, 90);
    private static final VoxelShape SHAPE_NS_OPEN = VoxelShapeUtils.rotateY(SHAPE_EW_OPEN, 90);

    public BlockVacuumTrap() {
        super(ModBlocks.defaultProps());

        setDefaultState(getStateContainer().getBaseState()
                .with(OPEN, false)
                .with(POWERED, false)
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(DOWN, false)
                .with(WATERLOGGED, false)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(OPEN, POWERED, NORTH, SOUTH, EAST, WEST, DOWN, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getPos());
        return super.getStateForPlacement(ctx).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction.Axis axis = getRotation(state).getAxis();
        if (axis == Direction.Axis.Z) {
            return state.get(OPEN) ? SHAPE_NS_OPEN : SHAPE_NS_CLOSED;
        } else if (axis == Direction.Axis.X) {
            return state.get(OPEN) ? SHAPE_EW_OPEN : SHAPE_EW_CLOSED;
        } else {
            return super.getShape(state, worldIn, pos, context);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (player.isSneaking()) {
            boolean open = state.get(OPEN);
            world.setBlockState(pos, state.with(OPEN, !open));
            world.playSound(player, pos, open ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1f, 0.5f);
            return ActionResultType.func_233537_a_(world.isRemote);
        }
        return super.onBlockActivated(state, world, pos, player, hand, brtr);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = world.isBlockPowered(pos);
        if (powered != state.get(POWERED)) {
            if (state.get(OPEN) != powered) {
                state = state.with(OPEN, powered);
                world.playSound(null, pos, powered ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1f, 0.5f);
            }
            world.setBlockState(pos, state.with(POWERED, powered), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVacuumTrap.class;
    }

    public static class ItemBlockVacuumTrap extends BlockItem {
        public ItemBlockVacuumTrap(BlockVacuumTrap blockVacuumTrap) {
            super(blockVacuumTrap, ModItems.defaultProps());
        }

        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);

            CompoundNBT tag = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
            if (tag != null && tag.contains("Items")) {
                ItemStackHandler handler = new ItemStackHandler(1);
                handler.deserializeNBT(tag.getCompound("Items"));
                if (handler.getStackInSlot(0).getItem() instanceof ItemSpawnerCore) {
                    tooltip.add(xlate("pneumaticcraft.message.vacuum_trap.coreInstalled").mergeStyle(TextFormatting.YELLOW));
                }
            }
        }
    }
}
