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
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.render.fluid.IFluidItemRenderInfoProvider;
import me.desht.pneumaticcraft.client.render.fluid.RenderFluidTank;
import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidTank;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BlockFluidTank extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
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
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

    private final Size size;

    public BlockFluidTank(Size size) {
        super(ModBlocks.defaultProps());
        this.size = size;

        registerDefaultState(getStateDefinition().any().setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(UP, DOWN);
    }

    public Size getSize() {
        return size;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return size.cls;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(UP, false).setValue(DOWN, false);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        BlockState up = worldIn.getBlockState(currentPos.above());
        if (stateIn.getValue(UP) && !(up.getBlock() instanceof BlockFluidTank)) {
            stateIn = stateIn.setValue(UP, false);
        }
        BlockState down = worldIn.getBlockState(currentPos.below());
        if (stateIn.getValue(DOWN) && !(down.getBlock() instanceof BlockFluidTank)) {
            stateIn = stateIn.setValue(DOWN, false);
        }
        return stateIn;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (!player.isShiftKeyDown()) {
            RayTraceResult rtr = RayTraceUtils.getMouseOverServer(player, PneumaticCraftUtils.getPlayerReachDistance(player));
            if (rtr.getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
                if (brtr.getBlockPos().equals(pos)) {
                    TileEntityFluidTank te = getTankAt(world, pos);
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

    private boolean tryToggleConnection(TileEntityFluidTank thisTank, Direction dir) {
        BlockState state = thisTank.getBlockState();
        TileEntityFluidTank neighbourTank = getTankAt(thisTank.getLevel(), thisTank.getBlockPos().relative(dir));
        if (neighbourTank == null) return false;
        BlockState stateOther = neighbourTank.getBlockState();
        boolean isConnected = state.getValue(connectionProperty(dir));
        if (isConnected) {
            thisTank.getLevel().setBlockAndUpdate(thisTank.getBlockPos(), state.setValue(connectionProperty(dir), false));
            thisTank.getLevel().setBlockAndUpdate(neighbourTank.getBlockPos(), stateOther.setValue(connectionProperty(dir.getOpposite()), false));
            return true;
        } else {
            FluidStack stack = thisTank.getTank().getFluid();
            if (neighbourTank.isFluidCompatible(stack, neighbourTank.getTank()) && neighbourTank.isNeighbourCompatible(stack, dir)) {
                thisTank.getLevel().setBlockAndUpdate(thisTank.getBlockPos(), state.setValue(connectionProperty(dir), true));
                thisTank.getLevel().setBlockAndUpdate(neighbourTank.getBlockPos(), stateOther.setValue(connectionProperty(dir.getOpposite()), true));
                return true;
            }
        }
        return false;
    }

    private TileEntityFluidTank getTankAt(World world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        return te instanceof TileEntityFluidTank ? (TileEntityFluidTank) te : null;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 1) {
            return size.tintColor;
        }
        return 0xFFFFFFFF;
    }

    public static class ItemBlockFluidTank extends BlockItem implements ColorHandlers.ITintableItem, IFluidRendered {
        public static final String TANK_NAME = "Tank";
        private final int capacity;

        private RenderFluidTank.ItemRenderInfoProvider renderInfoProvider;

        public ItemBlockFluidTank(Block block) {
            super(block, ModItems.defaultProps());
            this.capacity = ((BlockFluidTank) block).size.capacity;
        }

        @Override
        public boolean hasContainerItem(ItemStack stack) {
            // the tank is a container item if it's being used in fluid crafting
            // but an empty tank used in crafting is not a container item
            return FluidUtil.getFluidContained(stack).map(f -> !f.isEmpty()).orElse(false);
        }

        @Override
        public ItemStack getContainerItem(ItemStack itemStack) {
            boolean creative = UpgradableItemUtils.hasCreativeUpgrade(itemStack);
            return FluidUtil.getFluidHandler(itemStack.copy()).map(handler -> {
                // TODO can (or indeed should) we support recipes which drain amounts other than 1000mB?
                handler.drain(1000, creative ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                return handler.getContainer();
            }).orElseThrow(RuntimeException::new);
        }

        // TODO: Remove tint as it is now Texture Based
        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            if (tintIndex == 1 && stack.getItem() instanceof ItemBlockFluidTank) {
                Block b = ((ItemBlockFluidTank) stack.getItem()).getBlock();
                return ((BlockFluidTank) b).size.tintColor;
            }
            return 0xFFFFFFFF;
        }

        @Override
        public IFluidItemRenderInfoProvider getFluidItemRenderer() {
            if (renderInfoProvider == null) renderInfoProvider = new RenderFluidTank.ItemRenderInfoProvider();
            return renderInfoProvider;
        }

        @Override
        public int getItemStackLimit(ItemStack stack) {
            // empty tanks may stack, but not filled tanks (even if filled to the same level)
            // note: can't use hasContainerItem() here: it can lead to infinite recursion on init
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/666
            return stack.hasTag() && stack.getTag().contains(NBTKeys.BLOCK_ENTITY_TAG) ? 1 : 64;
        }

        @Nullable
        @Override
        public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
            if (stack.getItem() instanceof ItemBlockFluidTank) {
                return new FluidItemWrapper(stack, TANK_NAME, capacity);
            } else {
                return super.initCapabilities(stack, nbt);
            }
        }
    }

    public enum Size {
        SMALL(32000, 0xFF909090, TileEntityFluidTank.Small.class),
        MEDIUM(64000, 0xFFFFFF40, TileEntityFluidTank.Medium.class),
        LARGE(128000, 0xFF91E8E4, TileEntityFluidTank.Large.class),
        HUGE(512000, 0xFF5A3950, TileEntityFluidTank.Huge.class);

        private final int capacity;
        private final int tintColor;
        private final Class<? extends TileEntityFluidTank> cls;

        Size(int capacity, int tintColor, Class<? extends TileEntityFluidTank> cls) {
            this.capacity = capacity;
            this.tintColor = tintColor;
            this.cls = cls;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
