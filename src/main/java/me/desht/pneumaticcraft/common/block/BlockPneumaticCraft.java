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
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.misc.IPneumaticCraftProbeable;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.heat.TemperatureCategory;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tileentity.IComparatorSupport;
import me.desht.pneumaticcraft.common.tileentity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.state.properties.BlockStateProperties.FACING;
import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public abstract class BlockPneumaticCraft extends Block implements IPneumaticWrenchable, IUpgradeAcceptor, IPneumaticCraftProbeable {
    static final VoxelShape ALMOST_FULL_SHAPE = Block.box(0.5, 0, 0.5, 15.5, 16, 15.5);

    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty[] CONNECTION_PROPERTIES = new BooleanProperty[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
    public static final EnumProperty<TemperatureCategory> TEMPERATURE = EnumProperty.create("temperature", TemperatureCategory.class);

    protected BlockPneumaticCraft(Properties props) {
        super(props);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return getTileEntityClass() != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        try {
            return getTileEntityClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract Class<? extends TileEntity> getTileEntityClass();

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getDescriptionId();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        ItemStack heldItem = player.getItemInHand(hand);
        TileEntity te = world.getBlockEntity(pos);
        if (player.isShiftKeyDown()
                || !(te instanceof INamedContainerProvider)
                || isRotatable() && (heldItem.getItem() == ModItems.MANOMETER.get() || ModdedWrenchUtils.getInstance().isModdedWrench(heldItem))
                || hand == Hand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(player.getMainHandItem())) {
            return ActionResultType.PASS;
        } else {
            if (!world.isClientSide) {
                if (te instanceof TileEntityBase) {
                    if (FluidUtils.tryFluidInsertion(te, null, player, hand)) {
                        world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        return ActionResultType.SUCCESS;
                    } else if (FluidUtils.tryFluidExtraction(te, null, player, hand)) {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        return ActionResultType.SUCCESS;
                    }
                    // te must be a INamedContainerProvider at this point: see instanceof check above
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
                }
            }

            return ActionResultType.SUCCESS;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state != null) {
            for (Direction facing : DirectionUtil.VALUES) {
                if (state.hasProperty(connectionProperty(facing))) {
                    // handle pneumatic connections to neighbouring air handlers
                    TileEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos().relative(facing));
                    boolean b = te != null && te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, facing.getOpposite()).isPresent();
                    state = state.setValue(connectionProperty(facing), b);
                }
            }
            if (isRotatable()) {
                Direction f = canRotateToTopOrBottom() ? ctx.getNearestLookingDirection() : ctx.getHorizontalDirection();
                state = state.setValue(directionProperty(), reversePlacementRotation() ? f.getOpposite() : f);
            }
        }
        return state;
    }

    /**
     * Does the block face the same way as the player when placed, or opposite?
     * @return whether or not the block should be rotated 180 degrees on placement
     */
    protected boolean reversePlacementRotation() {
        return false;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityBase && stack.hasCustomHoverName()) {
            ((TileEntityBase) te).setCustomName(stack.getHoverName());
        }
        if (te instanceof IHeatExchangingTE) {
            ((IHeatExchangingTE) te).initHeatExchangersOnPlacement(world, pos);
        }
    }

    public static BooleanProperty connectionProperty(Direction dir) {
        return CONNECTION_PROPERTIES[dir.get3DDataValue()];
    }

    DirectionProperty directionProperty() { return canRotateToTopOrBottom() ? FACING : HORIZONTAL_FACING; }

    protected Direction getRotation(IBlockReader world, BlockPos pos) {
        return getRotation(world.getBlockState(pos));
    }

    public Direction getRotation(BlockState state) {
        return state.getValue(directionProperty());
    }

    protected void setRotation(World world, BlockPos pos, Direction rotation) {
        setRotation(world, pos, rotation, world.getBlockState(pos));
    }

    private void setRotation(World world, BlockPos pos, Direction rotation, BlockState state) {
        world.setBlockAndUpdate(pos, state.setValue(directionProperty(), rotation));
    }

    public boolean isRotatable() {
        return false;
    }

    protected boolean canRotateToTopOrBottom() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        if (isRotatable()) {
            builder.add(directionProperty());
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        // this is here to support rotation when creating blocks from templates (jigsaw system)
        // - rotation by the pneumatic wrench is handled by onBlockWrenched() below
        // - rotation by 3rd party wrenches is captured by a client-side event handler, which sends
        //   a PacketModWrenchBlock to the server, also leading to onBlockWrenched()
        if (isRotatable()) {
            state = state.setValue(directionProperty(), rotation.rotate(state.getValue(directionProperty())));
        }

        return state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        // see above
        return isRotatable() ? state.rotate(mirrorIn.getRotation(state.getValue(directionProperty()))) : state;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (player != null && player.isShiftKeyDown()) {
            TileEntity te = world.getBlockEntity(pos);
            boolean preserve = false;
            if (te instanceof TileEntityBase) {
                preserve = true;
                ((TileEntityBase) te).setPreserveStateOnBreak(true);
            }
            if (!player.isCreative() || preserve) {
                Block.dropResources(world.getBlockState(pos), world, pos, te);
            }
            removeBlockSneakWrenched(world, pos);
            return true;
        } else {
            if (isRotatable()) {
                BlockState state = world.getBlockState(pos);
                if (!rotateCustom(world, pos, state, side)) {
                    if (rotateForgeWay()) {
                        if (!canRotateToTopOrBottom()) side = Direction.UP;
                        if (getRotation(world, pos).getAxis() != side.getAxis()) {
                            setRotation(world, pos, DirectionUtil.rotateAround(getRotation(world, pos), side.getAxis()));
                        }
                    } else {
                        Direction f = getRotation(world, pos);
                        do {
                            f = Direction.from3DDataValue(f.get3DDataValue() + 1);
                        } while (!canRotateToTopOrBottom() && f.getAxis() == Axis.Y);
                        setRotation(world, pos, f);
                    }
                    TileEntity te = world.getBlockEntity(pos);
                    if (te instanceof TileEntityBase) ((TileEntityBase) te).onBlockRotated();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    protected boolean rotateForgeWay() {
        return true;
    }

    /**
     * Can be overridden to implement custom rotation behaviour for a block.
     *
     * @param world the world
     * @param pos block position
     * @param state block state
     * @param side the side clicked
     * @return true when the method is overridden, to disable default rotation behaviour
     */
    protected boolean rotateCustom(World world, BlockPos pos, BlockState state, Direction side) {
        return false;
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos tilePos) {
        if (world instanceof World && !((World) world).isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborTileUpdate(tilePos);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborBlockUpdate(fromPos);
            }
        }
    }

    private int getSavedAir(ItemStack stack) {
        CompoundNBT tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(NBTKeys.NBT_AIR_AMOUNT)) {
            return tag.getInt(NBTKeys.NBT_AIR_AMOUNT);
        } else {
            return 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, IBlockReader world, List<ITextComponent> curInfo, ITooltipFlag flag) {
        if (stack.hasTag()) {
            int savedAir = getSavedAir(stack);
            if (savedAir != 0) {
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.air", Integer.toString(savedAir)).withStyle(TextFormatting.GREEN));
            }
            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof IUpgradeAcceptor) {
                UpgradableItemUtils.addUpgradeInformation(stack, curInfo, flag);
            }
            CompoundNBT subTag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (subTag != null && subTag.contains(NBTKeys.NBT_SAVED_TANKS, Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT tag = subTag.getCompound(NBTKeys.NBT_SAVED_TANKS);
                for (String s : tag.getAllKeys()) {
                    CompoundNBT tankTag = tag.getCompound(s);
                    FluidTank tank = new FluidTank(tankTag.getInt("Amount"));
                    tank.readFromNBT(tankTag);
                    FluidStack fluidStack = tank.getFluid();
                    if (!fluidStack.isEmpty()) {
                        curInfo.add(xlate("pneumaticcraft.gui.tooltip.fluid")
                                .append(fluidStack.getAmount() + "mB ")
                                .append(fluidStack.getDisplayName()).withStyle(TextFormatting.GREEN));
                    }
                }
            }
            addExtraInformation(stack, world, curInfo, flag);
        }
        if (ClientUtils.hasShiftDown() && hasTileEntity(defaultBlockState())) {
            TileEntity te = createTileEntity(defaultBlockState(), world);
            if (te instanceof TileEntityPneumaticBase) {
                float pressure = ((TileEntityPneumaticBase) te).getDangerPressure();
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.maxPressure", pressure).withStyle(TextFormatting.YELLOW));
            }
        }
    }

    protected void addExtraInformation(ItemStack stack, IBlockReader world, List<ITextComponent> curInfo, ITooltipFlag flag) {
        // override in subclasses
    }

    /**
     * If this returns true, then comparators facing away from this block will use the value from
     * getComparatorInputOverride instead of the actual redstone signal strength.
     */
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        Class<? extends TileEntity> teClass = getTileEntityClass();
        return teClass != null && IComparatorSupport.class.isAssignableFrom(teClass);
    }

    /**
     * If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
     * strength when this block inputs to a comparator.
     */
    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        return te instanceof IComparatorSupport ? ((IComparatorSupport) te).getComparatorValue() : 0;
    }

    @Override
    public Map<EnumUpgrade, Integer> getApplicableUpgrades() {
        if (hasTileEntity(defaultBlockState())) {
            TileEntity te = createTileEntity(defaultBlockState(), null);
            if (te instanceof IUpgradeAcceptor) return ((IUpgradeAcceptor) te).getApplicableUpgrades();
        }
        return Collections.emptyMap();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityBase) {
                NonNullList<ItemStack> drops = NonNullList.create();
                ((TileEntityBase) te).getContentsToDrop(drops);
                drops.forEach(stack -> PneumaticCraftUtils.dropItemOnGround(stack, world, pos));

                if (!((TileEntityBase) te).shouldPreserveStateOnBreak()) {
                    te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(handler -> {
                        if (handler.getAir() > 0) {
                            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, (int) (5 * handler.getPressure()), 1, 1, 1), world, pos);
                            world.playSound(null, pos, ModSounds.SHORT_HISS.get(), SoundCategory.BLOCKS, 0.3f, 0.8f);
                        }
                    });
                }

            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.hasProperty(connectionProperty(facing))) {
            TileEntity ourTE = worldIn.getBlockEntity(currentPos);
            if (ourTE != null && ourTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, facing).isPresent()) {
                // handle pneumatic connections to neighbouring air handlers
                TileEntity te = worldIn.getBlockEntity(currentPos.relative(facing));
                boolean b = te != null && te.getCapability (PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, facing.getOpposite()).isPresent();
                stateIn = stateIn.setValue(connectionProperty(facing), b);
                return stateIn;
            }
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return getCollisionShape(state, worldIn, pos, ISelectionContext.empty()).isEmpty();
    }

    static void removeBlockSneakWrenched(World world, BlockPos pos) {
        if (!world.isClientSide()) {
            world.removeBlock(pos, false);
            // this only gets called server-side, but the client needs to be informed too, to update neighbour states
            PneumaticRegistry.getInstance().forceClientShapeRecalculation(world, pos);
        }
    }

    @Override
    public void playerDestroy(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (player instanceof ServerPlayerEntity && !(player instanceof FakePlayer)
                && te instanceof TileEntityBase && !(((TileEntityBase) te).shouldPreserveStateOnBreak())) {
            AdvancementTriggers.MACHINE_VANDAL.trigger((ServerPlayerEntity) player);
        }
        super.playerDestroy(worldIn, player, pos, state, te, stack);
    }
}
