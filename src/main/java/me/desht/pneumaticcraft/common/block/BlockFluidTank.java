package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.render.fluid.IFluidItemRenderInfoProvider;
import me.desht.pneumaticcraft.client.render.fluid.RenderFluidTank;
import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidTank;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
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

public class BlockFluidTank extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
    private static final VoxelShape S1 = makeCuboidShape(0, 0, 1, 16, 16, 15);
    private static final VoxelShape S2 = makeCuboidShape(1, 0, 0, 15, 16, 16);
    private static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(S1, S2, IBooleanFunction.OR);

    private final Size size;

    public BlockFluidTank(Size size) {
        super(ModBlocks.defaultProps());
        this.size = size;

        setDefaultState(getStateContainer().getBaseState().with(UP, false).with(DOWN, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

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

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        BlockState up = worldIn.getBlockState(currentPos.up());
        if (stateIn.get(UP) && !(up.getBlock() instanceof BlockFluidTank)) {
            stateIn = stateIn.with(UP, false);
        }
        BlockState down = worldIn.getBlockState(currentPos.down());
        if (stateIn.get(DOWN) && !(down.getBlock() instanceof BlockFluidTank)) {
            stateIn = stateIn.with(DOWN, false);
        }
        return stateIn;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (!player.isSneaking()) {
            RayTraceResult rtr = PneumaticCraftUtils.getMouseOverServer(player, 5);
            if (rtr.getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
                if (brtr.getPos().equals(pos)) {
                    TileEntityFluidTank te = getTankAt(world, pos);
                    if (te != null) {
                        double y = brtr.getHitVec().y - (int) brtr.getHitVec().y;
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
        TileEntityFluidTank neighbourTank = getTankAt(thisTank.getWorld(), thisTank.getPos().offset(dir));
        if (neighbourTank == null) return false;
        BlockState stateOther = neighbourTank.getBlockState();
        boolean isConnected = state.get(connectionProperty(dir));
        if (isConnected) {
            thisTank.getWorld().setBlockState(thisTank.getPos(), state.with(connectionProperty(dir), false));
            thisTank.getWorld().setBlockState(neighbourTank.getPos(), stateOther.with(connectionProperty(dir.getOpposite()), false));
            return true;
        } else {
            FluidStack stack = thisTank.getTank().getFluid();
            if (neighbourTank.isFluidCompatible(stack, neighbourTank.getTank()) && neighbourTank.isNeighbourCompatible(stack, dir)) {
                thisTank.getWorld().setBlockState(thisTank.getPos(), state.with(connectionProperty(dir), true));
                thisTank.getWorld().setBlockState(neighbourTank.getPos(), stateOther.with(connectionProperty(dir.getOpposite()), true));
                return true;
            }
        }
        return false;
    }

    private TileEntityFluidTank getTankAt(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
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
        LARGE(128000, 0xFF91E8E4, TileEntityFluidTank.Large.class);

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
