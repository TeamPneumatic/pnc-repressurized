package me.desht.pneumaticcraft.common.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.common.thirdparty.theoneprobe.ITOPInfoProvider;
import me.desht.pneumaticcraft.common.thirdparty.theoneprobe.TOPCallback;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class BlockPneumaticCraft extends Block implements IPneumaticWrenchable, IUpgradeAcceptor, ITOPInfoProvider {
    public static final PropertyEnum<EnumFacing> ROTATION = PropertyEnum.create("facing", EnumFacing.class);
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool[] CONNECTION_PROPERTIES = new PropertyBool[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};

    private static final String NBT_UPGRADE_INVENTORY = "UpgradeInventory";
    private static final String NBT_SIDECONFIG = "SideConfiguration";
    private static final String NBT_AIR_AMOUNT = "AirAmount";

    private AxisAlignedBB bounds = FULL_BLOCK_AABB;

    protected BlockPneumaticCraft(Material material, String registryName) {
        super(material);
        setTranslationKey(registryName);
        setRegistryName(registryName);
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
        setHardness(3.0F);
        setResistance(10.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return getTileEntityClass() != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        try {
            TileEntity te = getTileEntityClass().newInstance();
            te.setWorld(world);
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

    public EnumGuiId getGuiID() {
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (player.isSneaking()
                || getGuiID() == null
                || isRotatable() && (heldItem.getItem() == Itemss.MANOMETER || ModInteractionUtils.getInstance().isModdedWrench(heldItem))
                || hand == EnumHand.OFF_HAND && ModInteractionUtils.getInstance().isModdedWrench(player.getHeldItemMainhand())) {
            return false;
        } else {
            if (!world.isRemote) {
                TileEntity te = world.getTileEntity(pos);
                if (te != null) {
                    if (FluidUtils.tryFluidInsertion(te, null, player, hand)) {
                        return true;
                    } else if (FluidUtils.tryFluidExtraction(te, null, player, hand)) {
                        return true;
                    }
                    player.openGui(PneumaticCraftRepressurized.instance, getGuiID().ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
                }
            }

            return true;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        if (isRotatable()) {
            EnumFacing f = PneumaticCraftUtils.getDirectionFacing(placer, canRotateToTopOrBottom());
            return state.withProperty(ROTATION, reversePlacementRotation() ? f.getOpposite() : f);
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
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ISerializableTanks && stack.hasTagCompound() && stack.getTagCompound().hasKey(ISerializableTanks.SAVED_TANKS, Constants.NBT.TAG_COMPOUND)) {
            ((ISerializableTanks) te).deserializeTanks(stack.getTagCompound().getCompoundTag(ISerializableTanks.SAVED_TANKS));
        }
        if (te instanceof TileEntityBase && stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_UPGRADE_INVENTORY)) {
            ((TileEntityBase) te).getUpgradesInventory().deserializeNBT(stack.getTagCompound().getCompoundTag(NBT_UPGRADE_INVENTORY));
        }
        if (te instanceof ISideConfigurable && stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_SIDECONFIG)) {
            NBTTagCompound tag = stack.getTagCompound().getCompoundTag(NBT_SIDECONFIG);
            SideConfigurator.readFromNBT(tag, ((ISideConfigurable) te));
        }
        if (te instanceof IPneumaticMachine && stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_AIR_AMOUNT)) {
            int air = stack.getTagCompound().getInteger(NBT_AIR_AMOUNT);
            ((IPneumaticMachine) te).getAirHandler(null).addAir(air);
        }
    }

    protected void setRotation(World world, BlockPos pos, EnumFacing rotation) {
        setRotation(world, pos, rotation, world.getBlockState(pos));
    }

    protected EnumFacing getRotation(IBlockAccess world, BlockPos pos) {
        return getRotation(world.getBlockState(pos));
    }

    protected EnumFacing getRotation(IBlockState state) {
        return state.getValue(ROTATION);
    }

    private void setRotation(World world, BlockPos pos, EnumFacing rotation, IBlockState state) {
        world.setBlockState(pos, state.withProperty(ROTATION, rotation));
    }

    public boolean isRotatable() {
        return false;
    }

    protected boolean canRotateToTopOrBottom() {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        if (isRotatable()) {
            return new BlockStateContainer(this, ROTATION);
        } else {
            return super.createBlockState();
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (isRotatable()) {
            return state.getValue(ROTATION).ordinal();
        } else {
            return super.getMetaFromState(state);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (isRotatable()) {
            return super.getStateFromMeta(meta).withProperty(ROTATION, EnumFacing.byIndex(meta));
        } else {
            return super.getStateFromMeta(meta);
        }
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        // we simply disallow any external block rotation here:
        // - rotation by the pneumatic wrench is handled by our own rotateBlock() below
        // - rotation by 3rd party wrenches is capture by an event handler, which sends
        //   a custom packet, also leading to our own rotateBlock()
        return false;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side) {
        if (player != null && player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).preserveUpgradesOnBreak = true;
            }
            if (!player.capabilities.isCreativeMode) dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            world.setBlockToAir(pos);
            return true;
        } else {
            if (isRotatable()) {
                IBlockState state = world.getBlockState(pos);
                if (!rotateCustom(world, pos, state, side)) {
                    if (rotateForgeWay()) {
                        if (!canRotateToTopOrBottom()) side = EnumFacing.UP;
                        if (getRotation(world, pos).getAxis() != side.getAxis()) {
                            setRotation(world, pos, getRotation(world, pos).rotateAround(side.getAxis()));
                        }
                    } else {
                        EnumFacing f = getRotation(world, pos);
                        do {
                            f = EnumFacing.byIndex(f.ordinal() + 1);
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
    protected boolean rotateCustom(World world, BlockPos pos, IBlockState state, EnumFacing side) {
        return false;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos tilePos) {
        if (world instanceof World && !((World) world).isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborTileUpdate();
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityBase) {
                ((TileEntityBase) te).onNeighborBlockUpdate();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> curInfo, ITooltipFlag flag) {
        if (stack.hasTagCompound()) {
            if (stack.getTagCompound().hasKey(ISerializableTanks.SAVED_TANKS, Constants.NBT.TAG_COMPOUND)) {
                NBTTagCompound tag = stack.getTagCompound().getCompoundTag(ISerializableTanks.SAVED_TANKS);
                for (String s : tag.getKeySet()) {
                    NBTTagCompound tankTag = tag.getCompoundTag(s);
                    FluidTank tank = new FluidTank(tankTag.getInteger("Amount"));
                    tank.readFromNBT(tankTag);
                    FluidStack fluidStack = tank.getFluid();
                    if (fluidStack != null && fluidStack.amount > 0) {
                        curInfo.add(fluidStack.getFluid().getLocalizedName(fluidStack) + ": " + fluidStack.amount + "mB");
                    }
                }
            }
            if (stack.getTagCompound().hasKey(NBT_AIR_AMOUNT, Constants.NBT.TAG_INT)) {
                int air = stack.getTagCompound().getInteger(NBT_AIR_AMOUNT);
                TileEntity te = createTileEntity(world, getDefaultState());
                if (te instanceof IPneumaticMachine) {
                    curInfo.add("Stored air: " + (air + ((IPneumaticMachine) te).getAirHandler(null).getVolume()) + "mL");
                }
            }
        }
        if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
            TileEntity te = createTileEntity(world, getDefaultState());
            if (te instanceof TileEntityPneumaticBase) {
                float pressure = ((TileEntityPneumaticBase) te).dangerPressure;
                curInfo.add(TextFormatting.YELLOW + I18n.format("gui.tooltip.maxPressure", pressure));
            }
        }

        String info = "gui.tab.info." + stack.getTranslationKey();
        String translatedInfo = I18n.format(info);
        if (!translatedInfo.equals(info)) {
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                translatedInfo = TextFormatting.AQUA + translatedInfo.substring(2);
                if (!Loader.isModLoaded(ModIds.IGWMOD))
                    translatedInfo += " \\n \\n" + I18n.format("gui.tab.info.assistIGW");
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 40));
            } else {
                curInfo.add(TextFormatting.AQUA + I18n.format("gui.tooltip.sneakForInfo"));
            }
        }
    }

    /**
     * If this returns true, then comparators facing away from this block will use the value from
     * getComparatorInputOverride instead of the actual redstone signal strength.
     */
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return IComparatorSupport.class.isAssignableFrom(getTileEntityClass());
    }

    /**
     * If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
     * strength when this block inputs to a comparator.
     */
    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return ((IComparatorSupport) world.getTileEntity(pos)).getComparatorValue();
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        TileEntity te = createTileEntity(null, getDefaultState());
        return te instanceof IUpgradeAcceptor ? ((IUpgradeAcceptor) te).getApplicableUpgrades() : Collections.emptySet();
    }

    @Override
    public String getName() {
        return getTranslationKey() + ".name";
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return bounds;
    }

    /**
     * Compatibility with 1.8 code...
     * @param bounds new bounding box
     */
    protected void setBlockBounds(AxisAlignedBB bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
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
            return super.removedByPlayer(state, world, pos, player, false);
        } else {
            // This delays harvesting until after getDrops() is called, giving getDrops() a chance to serialize any TE
            // data onto the itemstack.  harvestBlock() must also be overridden to remove the block (see below)
            return willHarvest || super.removedByPlayer(state, world, pos, player, false);
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);  // see removedByPlayer() above
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityBase) {
            NonNullList<ItemStack> drops = NonNullList.create();
            ((TileEntityBase) te).getContentsToDrop(drops);
            drops.forEach(stack -> PneumaticCraftUtils.dropItemOnGround(stack, worldIn, pos));
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        TileEntity te = world.getTileEntity(pos);
        if (te != null && drops.size() > 0) {
            ItemStack teStack = drops.get(0);
            if (teStack == null || teStack.isEmpty()) {
                // should never happen, but paranoid coding to maybe deal with
                // https://github.com/TeamPneumatic/pnc-repressurized/issues/292
                // some other mod could be messing with the dropped itemstack?
                Log.warning("unexpected value for itemstack dropped from " + this + ": " + teStack);
                teStack = new ItemStack(Item.getItemFromBlock(this));
            }
            teStack.setTagCompound(new NBTTagCompound());
            if (te instanceof ISerializableTanks) {
                ((ISerializableTanks) te).serializeTanks(teStack);
            }
            if (te instanceof TileEntityBase && ((TileEntityBase) te).preserveUpgradesOnBreak) {
                TileEntityBase.UpgradeHandler handler = ((TileEntityBase) te).getUpgradesInventory();
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (!handler.getStackInSlot(i).isEmpty()) {
                        teStack.getTagCompound().setTag(NBT_UPGRADE_INVENTORY, ((TileEntityBase) te).getUpgradesInventory().serializeNBT());
                        break;
                    }
                }
                if (te instanceof IPneumaticMachine) {
                    IAirHandler airHandler = ((IPneumaticMachine) te).getAirHandler(null);
                    if (airHandler != null) {
                        teStack.getTagCompound().setInteger(NBT_AIR_AMOUNT, airHandler.getAir());
                    }
                }
            }
            if (te instanceof ISideConfigurable) {
                NBTTagCompound tag = SideConfigurator.writeToNBT((ISideConfigurable) te);
                if (!tag.isEmpty()) {
                    teStack.getTagCompound().setTag(NBT_SIDECONFIG, tag);
                }
            }
            if (teStack.hasTagCompound() && teStack.getTagCompound().isEmpty()) {
                teStack.setTagCompound(null);
            }
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if(te instanceof IInfoForwarder){
            te = ((IInfoForwarder)te).getInfoTileEntity();
        }
        
        if (te instanceof IPneumaticMachine) {
            TOPCallback.handlePneumatic(mode, probeInfo, (IPneumaticMachine)te);
        }
        if (te instanceof IHeatExchanger) {
            TOPCallback.handleHeat(mode, probeInfo, (IHeatExchanger) te);
        }
        if (ConfigHandler.client.topShowsFluids && te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, data.getSideHit())) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, data.getSideHit());
            TOPCallback.handleFluidTanks(mode, probeInfo, handler);
        }
        if (te instanceof TileEntityBase) {
            TOPCallback.handleRedstoneMode(mode, probeInfo, (TileEntityBase) te);
        }
        if (te instanceof TileEntityPressureTube) {
            TOPCallback.handlePressureTube(mode, probeInfo, (TileEntityPressureTube) te, data.getSideHit(), player);
        }
    }
}
