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
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.item.PNCUpgrade;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public abstract class AbstractPneumaticCraftBlock extends Block
        implements IPneumaticWrenchable, IUpgradeAcceptor, IPneumaticCraftProbeable {
    static final VoxelShape ALMOST_FULL_SHAPE = Block.box(0.5, 0, 0.5, 15.5, 16, 15.5);

    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty[] CONNECTION_PROPERTIES = new BooleanProperty[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
    public static final EnumProperty<TemperatureCategory> TEMPERATURE = EnumProperty.create("temperature", TemperatureCategory.class);

    protected AbstractPneumaticCraftBlock(Properties props) {
        super(props);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getDescriptionId();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity te = world.getBlockEntity(pos);
        if (player.isShiftKeyDown()
                || !(te instanceof MenuProvider)
                || isRotatable() && (heldItem.getItem() == ModItems.MANOMETER.get() || ModdedWrenchUtils.getInstance().isModdedWrench(heldItem))
                || hand == InteractionHand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(player.getMainHandItem())) {
            return InteractionResult.PASS;
        } else {
            if (!world.isClientSide) {
                if (te instanceof TileEntityBase) {
                    if (FluidUtils.tryFluidInsertion(te, null, player, hand)) {
                        world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                        return InteractionResult.SUCCESS;
                    } else if (FluidUtils.tryFluidExtraction(te, null, player, hand)) {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                        return InteractionResult.SUCCESS;
                    }
                    // te must be a MenuProvider at this point: see instanceof check above
                    doOpenGui((ServerPlayer) player, te);
                }
            }

            return InteractionResult.SUCCESS;
        }
    }

    /**
     * Default open gui method just sends the TE's blockpos.  Override if more server->client data needs to be
     * serialised, and handle deserialisation in the corresponding container constructor.
     *
     * @param player the server player
     * @param te the tile entity, which is known to be an INamedContainerProvider
     */
    protected void doOpenGui(ServerPlayer player, BlockEntity te) {
        NetworkHooks.openGui(player, (MenuProvider) te, te.getBlockPos());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state != null) {
            for (Direction facing : DirectionUtil.VALUES) {
                if (state.hasProperty(connectionProperty(facing))) {
                    // handle pneumatic connections to neighbouring air handlers
                    BlockEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos().relative(facing));
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
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        BlockEntity te = world.getBlockEntity(pos);
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

    protected Direction getRotation(BlockGetter world, BlockPos pos) {
        return getRotation(world.getBlockState(pos));
    }

    public Direction getRotation(BlockState state) {
        return state.getValue(directionProperty());
    }

    protected void setRotation(Level world, BlockPos pos, Direction rotation) {
        setRotation(world, pos, rotation, world.getBlockState(pos));
    }

    private void setRotation(Level world, BlockPos pos, Direction rotation, BlockState state) {
        world.setBlockAndUpdate(pos, state.setValue(directionProperty(), rotation));
    }

    public boolean isRotatable() {
        return false;
    }

    protected boolean canRotateToTopOrBottom() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (player != null && player.isShiftKeyDown()) {
            BlockEntity te = world.getBlockEntity(pos);
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
                    BlockEntity te = world.getBlockEntity(pos);
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
    protected boolean rotateCustom(Level world, BlockPos pos, BlockState state, Direction side) {
        return false;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos tilePos) {
        if (world instanceof Level && !((Level) world).isClientSide) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborTileUpdate(tilePos);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborBlockUpdate(fromPos);
            }
        }
    }

    private int getSavedAir(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(NBTKeys.NBT_AIR_AMOUNT)) {
            return tag.getInt(NBTKeys.NBT_AIR_AMOUNT);
        } else {
            return 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, BlockGetter world, List<Component> curInfo, TooltipFlag flag) {
        if (stack.hasTag()) {
            int savedAir = getSavedAir(stack);
            if (savedAir != 0) {
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.air", Integer.toString(savedAir)).withStyle(ChatFormatting.GREEN));
            }
            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof IUpgradeAcceptor) {
                UpgradableItemUtils.addUpgradeInformation(stack, curInfo, flag);
            }
            CompoundTag subTag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (subTag != null && subTag.contains(NBTKeys.NBT_SAVED_TANKS, Tag.TAG_COMPOUND)) {
                CompoundTag tag = subTag.getCompound(NBTKeys.NBT_SAVED_TANKS);
                for (String s : tag.getAllKeys()) {
                    CompoundTag tankTag = tag.getCompound(s);
                    FluidTank tank = new FluidTank(tankTag.getInt("Amount"));
                    tank.readFromNBT(tankTag);
                    FluidStack fluidStack = tank.getFluid();
                    if (!fluidStack.isEmpty()) {
                        curInfo.add(xlate("pneumaticcraft.gui.tooltip.fluid")
                                .append(fluidStack.getAmount() + "mB ")
                                .append(fluidStack.getDisplayName()).withStyle(ChatFormatting.GREEN));
                    }
                }
            }
            addExtraInformation(stack, world, curInfo, flag);
        }
        if (ClientUtils.hasShiftDown() && this instanceof EntityBlock eb) {
            BlockEntity te = eb.newBlockEntity(BlockPos.ZERO, defaultBlockState());
            if (te instanceof TileEntityPneumaticBase pneumatic) {
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.maxPressure", pneumatic.getDangerPressure())
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    protected void addExtraInformation(ItemStack stack, BlockGetter world, List<Component> curInfo, TooltipFlag flag) {
        // override in subclasses
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return this instanceof IBlockComparatorSupport;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) instanceof IComparatorSupport comp ? comp.getComparatorValue() : 0;
    }

    @Override
    public Map<PNCUpgrade, Integer> getApplicableUpgrades() {
        if (this instanceof EntityBlock eb) {
            BlockEntity te = eb.newBlockEntity(BlockPos.ZERO, defaultBlockState());
            if (te instanceof IUpgradeAcceptor ua) return ua.getApplicableUpgrades();
        }
        return Collections.emptyMap();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityBase teBase) {
                NonNullList<ItemStack> drops = NonNullList.create();
                teBase.getContentsToDrop(drops);
                drops.forEach(stack -> PneumaticCraftUtils.dropItemOnGround(stack, world, pos));

                if (!teBase.shouldPreserveStateOnBreak()) {
                    te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(handler -> {
                        if (handler.getAir() > 0) {
                            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, (int) (5 * handler.getPressure()), 1, 1, 1), world, pos);
                            world.playSound(null, pos, ModSounds.SHORT_HISS.get(), SoundSource.BLOCKS, 0.3f, 0.8f);
                        }
                    });
                }

            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.hasProperty(connectionProperty(facing))) {
            BlockEntity ourTE = worldIn.getBlockEntity(currentPos);
            if (ourTE != null && ourTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, facing).isPresent()) {
                // handle pneumatic connections to neighbouring air handlers
                BlockEntity te = worldIn.getBlockEntity(currentPos.relative(facing));
                boolean b = te != null && te.getCapability (PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, facing.getOpposite()).isPresent();
                stateIn = stateIn.setValue(connectionProperty(facing), b);
                return stateIn;
            }
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return getCollisionShape(state, worldIn, pos, CollisionContext.empty()).isEmpty();
    }

    static void removeBlockSneakWrenched(Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            world.removeBlock(pos, false);
            // this only gets called server-side, but the client needs to be informed too, to update neighbour states
            PneumaticRegistry.getInstance().forceClientShapeRecalculation(world, pos);
        }
    }

    @Override
    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        if (player instanceof ServerPlayer sp && !(player instanceof FakePlayer)
                && te instanceof TileEntityBase base && !base.shouldPreserveStateOnBreak()) {
            AdvancementTriggers.MACHINE_VANDAL.trigger(sp);
        }
        super.playerDestroy(worldIn, player, pos, state, te, stack);
    }
}
