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

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.state.properties.BlockStateProperties.*;

public class BlockVacuumTrap extends BlockPneumaticCraft implements IWaterLoggable {
    private static final VoxelShape FLAP1 = box(0, 11, 0, 16, 12, 5);
    private static final VoxelShape FLAP2 = box(0, 11, 11, 16, 12, 16);
    private static final VoxelShape SHAPE_EW_CLOSED = box(0, 0, 3, 16, 12, 13);
    private static final VoxelShape SHAPE_EW_OPEN_BASE = box(0, 0, 3, 16, 11, 13);
    private static final VoxelShape SHAPE_EW_OPEN = VoxelShapeUtils.combine(IBooleanFunction.OR, SHAPE_EW_OPEN_BASE, FLAP1, FLAP2);
    private static final VoxelShape SHAPE_NS_CLOSED = VoxelShapeUtils.rotateY(SHAPE_EW_CLOSED, 90);
    private static final VoxelShape SHAPE_NS_OPEN = VoxelShapeUtils.rotateY(SHAPE_EW_OPEN, 90);

    public BlockVacuumTrap() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
                .setValue(OPEN, false)
                .setValue(POWERED, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(DOWN, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(OPEN, POWERED, NORTH, SOUTH, EAST, WEST, DOWN, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
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
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction.Axis axis = getRotation(state).getAxis();
        if (axis == Direction.Axis.Z) {
            return state.getValue(OPEN) ? SHAPE_NS_OPEN : SHAPE_NS_CLOSED;
        } else if (axis == Direction.Axis.X) {
            return state.getValue(OPEN) ? SHAPE_EW_OPEN : SHAPE_EW_CLOSED;
        } else {
            return super.getShape(state, worldIn, pos, context);
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (player.isShiftKeyDown()) {
            boolean open = state.getValue(OPEN);
            world.setBlockAndUpdate(pos, state.setValue(OPEN, !open));
            world.playSound(player, pos, open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1f, 0.5f);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return super.use(state, world, pos, player, hand, brtr);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = world.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            if (state.getValue(OPEN) != powered) {
                state = state.setValue(OPEN, powered);
                world.playSound(null, pos, powered ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1f, 0.5f);
            }
            world.setBlock(pos, state.setValue(POWERED, powered), Constants.BlockFlags.BLOCK_UPDATE);
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
        public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.appendHoverText(stack, worldIn, tooltip, flagIn);

            CompoundNBT tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (tag != null && tag.contains("Items")) {
                ItemStackHandler handler = new ItemStackHandler(1);
                handler.deserializeNBT(tag.getCompound("Items"));
                if (handler.getStackInSlot(0).getItem() instanceof ItemSpawnerCore) {
                    tooltip.add(xlate("pneumaticcraft.message.vacuum_trap.coreInstalled").withStyle(TextFormatting.YELLOW));
                }
            }
        }
    }
}
