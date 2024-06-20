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

import me.desht.pneumaticcraft.client.render.fluid.IFluidItemRenderInfoProvider;
import me.desht.pneumaticcraft.client.render.fluid.RenderFluidTank;
import me.desht.pneumaticcraft.common.block.entity.AbstractFluidTankBlockEntity;
import me.desht.pneumaticcraft.common.item.IFluidCapProvider;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class FluidTankBlock extends AbstractPneumaticCraftBlock
        implements PneumaticCraftEntityBlock, IBlockComparatorSupport
{
    // TODO: Fix VoxelShapes to show the top/bottom when available is possible. Otherwise update this to be a full block
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(2, 0, 2, 14, 16, 14),
            Block.box(2, 1, 0, 14, 2, 1),
            Block.box(2, 14, 0, 14, 15, 1),
            Block.box(2, 1, 15, 14, 2, 16),
            Block.box(2, 14, 15, 14, 15, 16),
            Block.box(0, 1, 2, 1, 2, 14),
            Block.box(0, 14, 2, 1, 15, 14),
            Block.box(15, 1, 2, 16, 2, 14),
            Block.box(15, 14, 2, 16, 15, 14),
            Block.box(2, 0, 1, 3, 16, 2),
            Block.box(14, 0, 13, 15, 16, 14),
            Block.box(14, 0, 2, 15, 16, 3),
            Block.box(1, 0, 13, 2, 16, 14),
            Block.box(1, 0, 2, 2, 16, 3),
            Block.box(0, 0, 14, 2, 16, 16),
            Block.box(0, 0, 0, 2, 16, 2),
            Block.box(14, 0, 14, 16, 16, 16),
            Block.box(14, 0, 0, 16, 16, 2),
            Block.box(13, 0, 1, 14, 16, 2),
            Block.box(13, 0, 14, 14, 16, 15),
            Block.box(2, 0, 14, 3, 16, 15)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    private final Size size;

    public FluidTankBlock(Size size) {
        super(ModBlocks.defaultProps());
        this.size = size;

        registerDefaultState(defaultBlockState().setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(UP, DOWN);
    }

    public Size getSize() {
        return size;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(UP, false).setValue(DOWN, false);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        BlockState up = worldIn.getBlockState(currentPos.above());
        if (stateIn.getValue(UP) && !(up.getBlock() instanceof FluidTankBlock)) {
            stateIn = stateIn.setValue(UP, false);
        }
        BlockState down = worldIn.getBlockState(currentPos.below());
        if (stateIn.getValue(DOWN) && !(down.getBlock() instanceof FluidTankBlock)) {
            stateIn = stateIn.setValue(DOWN, false);
        }
        return stateIn;
    }

    @Override
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            HitResult rtr = RayTraceUtils.getMouseOverServer(player, PneumaticCraftUtils.getPlayerReachDistance(player));
            if (rtr.getType() == HitResult.Type.BLOCK) {
                BlockHitResult brtr = (BlockHitResult) rtr;
                if (brtr.getBlockPos().equals(pos)) {
                    AbstractFluidTankBlockEntity te = getTankAt(world, pos);
                    if (te != null) {
                        double y = brtr.getLocation().y - (int) brtr.getLocation().y;
                        return tryToggleConnection(te, y >= 0.5 ? Direction.UP : Direction.DOWN);
                    }
                }
            }
            return false;
        } else {
            return super.onWrenched(world, player, pos, side, hand);
        }
    }

    private boolean tryToggleConnection(AbstractFluidTankBlockEntity thisTank, Direction dir) {
        BlockState state = thisTank.getBlockState();
        Level level = thisTank.nonNullLevel();
        AbstractFluidTankBlockEntity neighbourTank = getTankAt(level, thisTank.getBlockPos().relative(dir));
        if (neighbourTank == null) return false;
        BlockState stateOther = neighbourTank.getBlockState();
        boolean isConnected = state.getValue(connectionProperty(dir));
        if (isConnected) {
            level.setBlockAndUpdate(thisTank.getBlockPos(), state.setValue(connectionProperty(dir), false));
            level.setBlockAndUpdate(neighbourTank.getBlockPos(), stateOther.setValue(connectionProperty(dir.getOpposite()), false));
            return true;
        } else {
            FluidStack stack = thisTank.getTank().getFluid();
            if (neighbourTank.isFluidCompatible(stack, neighbourTank.getTank()) && neighbourTank.isNeighbourCompatible(stack, dir)) {
                level.setBlockAndUpdate(thisTank.getBlockPos(), state.setValue(connectionProperty(dir), true));
                level.setBlockAndUpdate(neighbourTank.getBlockPos(), stateOther.setValue(connectionProperty(dir.getOpposite()), true));
                return true;
            }
        }
        return false;
    }

    private AbstractFluidTankBlockEntity getTankAt(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        return te instanceof AbstractFluidTankBlockEntity ? (AbstractFluidTankBlockEntity) te : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return size.beFactory.apply(pPos, pState);
    }

    public static class ItemBlockFluidTank extends BlockItem implements IFluidRendered, IFluidCapProvider {
        private final int capacity;

        private RenderFluidTank.ItemRenderInfoProvider renderInfoProvider;

        public ItemBlockFluidTank(Block block) {
            super(block, ModItems.defaultProps());
            this.capacity = ((FluidTankBlock) block).size.capacity;
        }

        @Override
        public boolean hasCraftingRemainingItem(ItemStack stack) {
            // the tank is a container item if it's being used in fluid crafting
            // but an empty tank used in crafting is not a container item
            return FluidUtil.getFluidContained(stack).map(f -> !f.isEmpty()).orElse(false);
        }

        @Override
        public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
            boolean creative = UpgradableItemUtils.hasCreativeUpgrade(itemStack);
            return FluidUtil.getFluidHandler(itemStack.copy()).map(handler -> {
                // TODO can (or indeed should) we support recipes which drain amounts other than 1000mB?
                handler.drain(1000, creative ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                return handler.getContainer();
            }).orElseThrow(RuntimeException::new);
        }

        @Override
        public IFluidItemRenderInfoProvider getFluidItemRenderer() {
            if (renderInfoProvider == null) renderInfoProvider = new RenderFluidTank.ItemRenderInfoProvider();
            return renderInfoProvider;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            // empty tanks may stack, but not filled tanks (even if filled to the same level)
            // note: can't use hasContainerItem() here: it can lead to infinite recursion on init
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/666
            return stack.has(ModDataComponents.MAIN_TANK.get()) ?  1 : 64;
        }

        @Override
        public IFluidHandlerItem provideFluidCapability(ItemStack stack) {
            return new FluidHandlerItemStack(ModDataComponents.MAIN_TANK, stack, capacity);
        }
    }

    public enum Size {
        SMALL(32000, AbstractFluidTankBlockEntity.Small::new),
        MEDIUM(64000, AbstractFluidTankBlockEntity.Medium::new),
        LARGE(128000, AbstractFluidTankBlockEntity.Large::new),
        HUGE(512000, AbstractFluidTankBlockEntity.Huge::new);

        private final int capacity;
        private final BiFunction<BlockPos,BlockState,BlockEntity> beFactory;

        Size(int capacity, BiFunction<BlockPos, BlockState, BlockEntity> beFactory) {
            this.capacity = capacity;
            this.beFactory = beFactory;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
