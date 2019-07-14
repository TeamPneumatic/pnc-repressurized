package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.IComparatorSupport;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class BlockPneumaticCraft extends Block implements IPneumaticWrenchable, IUpgradeAcceptor {
    public static final EnumProperty<Direction> ROTATION = EnumProperty.create("facing", Direction.class);
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty[] CONNECTION_PROPERTIES = new BooleanProperty[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
    public static final Block.Properties DEFAULT_PROPS = Block.Properties.create(Material.IRON)
            .hardnessAndResistance(3f, 10f).sound(SoundType.METAL);

    protected BlockPneumaticCraft(Material material, String registryName) {
        super(DEFAULT_PROPS);
        setRegistryName(registryName);
    }

    protected BlockPneumaticCraft(Block.Properties props, String registryName) {
        super(props);
        setRegistryName(registryName);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return getTileEntityClass() != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        try {
            TileEntity te = getTileEntityClass().newInstance();
            if (world instanceof World) {
                // should be true in all cases except worldgen (where we don't care anyway)
                te.setWorld((World) world);
            }
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onTileEntityCreated();
            }
            return te;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract Class<? extends TileEntity> getTileEntityClass();

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        ItemStack heldItem = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        if (player.isSneaking()
                || !(te instanceof INamedContainerProvider)
                || isRotatable() && (heldItem.getItem() == ModItems.MANOMETER || ModdedWrenchUtils.getInstance().isModdedWrench(heldItem))
                || hand == Hand.OFF_HAND && ModdedWrenchUtils.getInstance().isModdedWrench(player.getHeldItemMainhand())) {
            return false;
        } else {
            if (!world.isRemote) {
                if (te instanceof TileEntityBase) {
                    if (FluidUtils.tryFluidInsertion(te, null, player, hand)) {
                        return true;
                    } else if (FluidUtils.tryFluidExtraction(te, null, player, hand)) {
                        return true;
                    }
                    if (te instanceof INamedContainerProvider) {
                        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
                    }
                }
            }

            return true;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (isRotatable()) {
            Direction f = PneumaticCraftUtils.getDirectionFacing(ctx.getPlayer(), canRotateToTopOrBottom());
            return state.with(ROTATION, reversePlacementRotation() ? f.getOpposite() : f);
        } else {
            return state;
        }
    }

    /**
     * Bit of a kludge for historical reasons; some blocks face the wrong way by default.
     * @return whether or not the block should be rotated 180 degrees on placement
     */
    protected boolean reversePlacementRotation() {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IHeatExchanger) {
            double ambient = HeatExchangerLogicAmbient.atPosition(world, pos).getAmbientTemperature();
            ((IHeatExchanger) te).getHeatExchangerLogic(null).setTemperature(ambient);
        }
    }

    protected void setRotation(World world, BlockPos pos, Direction rotation) {
        setRotation(world, pos, rotation, world.getBlockState(pos));
    }

    protected Direction getRotation(IBlockReader world, BlockPos pos) {
        return getRotation(world.getBlockState(pos));
    }

    protected Direction getRotation(BlockState state) {
        return state.get(ROTATION);
    }

    private void setRotation(World world, BlockPos pos, Direction rotation, BlockState state) {
        world.setBlockState(pos, state.with(ROTATION, rotation));
    }

    public boolean isRotatable() {
        return false;
    }

    protected boolean canRotateToTopOrBottom() {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        if (isRotatable()) {
            builder.add(ROTATION);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        // we simply disallow any external block rotation here:
        // - rotation by the pneumatic wrench is handled by onBlockWrenched() below
        // - rotation by 3rd party wrenches is captured by a client-side event handler, which sends
        //   a PacketModWrenchBlock to the server, also leading to onBlockWrenched()
        return state;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (player != null && player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).setPreserveStateOnBreak(true);
            }
            if (!player.isCreative()) {
                spawnDrops(world.getBlockState(pos), world, pos);
            }
            world.removeBlock(pos, false);
            return true;
        } else {
            if (isRotatable()) {
                BlockState state = world.getBlockState(pos);
                if (!rotateCustom(world, pos, state, side)) {
                    if (rotateForgeWay()) {
                        if (!canRotateToTopOrBottom()) side = Direction.UP;
                        if (getRotation(world, pos).getAxis() != side.getAxis()) {
                            setRotation(world, pos, getRotation(world, pos).rotateAround(side.getAxis()));
                        }
                    } else {
                        Direction f = getRotation(world, pos);
                        do {
                            f = Direction.byIndex(f.ordinal() + 1);
                        } while (!canRotateToTopOrBottom() && f.getAxis() == Axis.Y);
                        setRotation(world, pos, f);
                    }
                    TileEntity te = world.getTileEntity(pos);
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
        if (world instanceof World && !((World) world).isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborTileUpdate();
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean b) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborBlockUpdate();
            }
        }
    }

    private int getSavedAir(ItemStack stack) {
        CompoundNBT tag = stack.getChildTag("BlockEntityTag");
        if (tag != null) {
            CompoundNBT airTag = tag.getCompound(NBTKeys.NBT_AIR_HANDLER);
            if (airTag != null && airTag.contains("air")) {
                return airTag.getInt("air");
            }
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, IBlockReader world, List<ITextComponent> curInfo, ITooltipFlag flag) {
        if (stack.hasTag()) {
            int savedAir = getSavedAir(stack);
            if (savedAir != 0) {
                curInfo.add(new StringTextComponent("Stored Air: " + savedAir + "mL").applyTextStyle(TextFormatting.GREEN));
            }
            if (stack.getItem() instanceof IUpgradeAcceptor) {
                UpgradableItemUtils.addUpgradeInformation(stack, world, curInfo, flag);
            }
            if (stack.getTag().contains(NBTKeys.NBT_SAVED_TANKS, Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT tag = stack.getTag().getCompound(NBTKeys.NBT_SAVED_TANKS);
                for (String s : tag.keySet()) {
                    CompoundNBT tankTag = tag.getCompound(s);
                    FluidTank tank = new FluidTank(tankTag.getInt("Amount"));
                    tank.readFromNBT(tankTag);
                    FluidStack fluidStack = tank.getFluid();
                    if (fluidStack != null && fluidStack.amount > 0) {
                        curInfo.add(new StringTextComponent(fluidStack.getFluid().getLocalizedName(fluidStack) + ": " + fluidStack.amount + "mB"));
                    }
                }
            }
            addExtraInformation(stack, world, curInfo, flag);
        }
        if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
            TileEntity te = createTileEntity(getDefaultState(), world);
            if (te instanceof TileEntityPneumaticBase) {
                float pressure = ((TileEntityPneumaticBase) te).dangerPressure;
                curInfo.add(xlate("gui.tooltip.maxPressure", pressure).applyTextStyle(TextFormatting.YELLOW));
            }
        }

        String info = "gui.tab.info." + stack.getTranslationKey();
        if (I18n.hasKey(info)) {
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                String translatedInfo = TextFormatting.AQUA + I18n.format(info).substring(2);  // strip out the leading text formatting
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 50).stream().map(StringTextComponent::new).collect(Collectors.toList()));
                if (!ThirdPartyManager.instance().docsProvider.docsProviderInstalled()) {
                    curInfo.add(xlate("gui.tab.info.assistIGW"));
                }
            } else {
                curInfo.add(xlate("gui.tooltip.sneakForInfo").applyTextStyle(TextFormatting.AQUA));
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
    public boolean hasComparatorInputOverride(BlockState state) {
        return IComparatorSupport.class.isAssignableFrom(getTileEntityClass());
    }

    /**
     * If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
     * strength when this block inputs to a comparator.
     */
    @Override
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        return ((IComparatorSupport) world.getTileEntity(pos)).getComparatorValue();
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        TileEntity te = createTileEntity(getDefaultState(), null);
        return te instanceof IUpgradeAcceptor ? ((IUpgradeAcceptor) te).getApplicableUpgrades() : Collections.emptySet();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

//    @Override
//    public AxisAlignedBB getBoundingBox(BlockState state, IBlockReader source, BlockPos pos) {
//        return bounds;
//    }

//    /**
//     * Compatibility with 1.8 code...
//     * @param bounds new bounding box
//     */
//    protected void setBlockBounds(AxisAlignedBB bounds) {
//        this.bounds = bounds;
//    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        if (player.isCreative()) {
            if (!world.isRemote) {
                // Drop any contained items here (but don't drop the block itself as an item; this is creative mode)
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityBase) {
                    NonNullList<ItemStack> drops = NonNullList.create();
                    ((TileEntityBase) te).getContentsToDrop(drops);
                    drops.forEach(stack -> PneumaticCraftUtils.dropItemOnGround(stack, world, pos));
                }
            }
            return super.removedByPlayer(state, world, pos, player, false, fluid);
        } else {
            // This delays harvesting until after getDrops() is called, giving getDrops() a chance to serialize any TE
            // data onto the itemstack.  harvestBlock() must also be overridden to remove the block (see below)
            return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluid);
        }
    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.removeBlock(pos, false);  // see removedByPlayer() above
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityBase) {
                NonNullList<ItemStack> drops = NonNullList.create();
                ((TileEntityBase) te).getContentsToDrop(drops);
                drops.forEach(stack -> PneumaticCraftUtils.dropItemOnGround(stack, world, pos));
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

}
