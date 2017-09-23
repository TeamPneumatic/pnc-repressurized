package me.desht.pneumaticcraft.common.block;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.common.tileentity.IComparatorSupport;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = ModIds.COMPUTERCRAFT)
public abstract class BlockPneumaticCraft extends Block implements IPneumaticWrenchable, IUpgradeAcceptor, IPeripheralProvider {

    public static final PropertyEnum<EnumFacing> ROTATION = PropertyEnum.create("facing", EnumFacing.class);
    private AxisAlignedBB bounds = FULL_BLOCK_AABB;

    protected BlockPneumaticCraft(Material material, String registryName) {
        super(material);
        setUnlocalizedName(registryName);
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
        if (player.isSneaking() || getGuiID() == null || isRotatable() && (player.getHeldItem(hand).getItem() == Itemss.MANOMETER || ModInteractionUtils.getInstance().isModdedWrench(player.getHeldItemMainhand().getItem())))
            return false;
        else {
            if (!world.isRemote) {
                TileEntity te = world.getTileEntity(pos);

                NonNullList<ItemStack> returnedItems = NonNullList.create();
                if (te != null && !FluidUtils.tryInsertingLiquid(te, facing, player, hand, returnedItems)) {
                    player.openGui(PneumaticCraftRepressurized.instance, getGuiID().ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
                }
            }

            return true;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        if (isRotatable()) {
            EnumFacing rotation = PneumaticCraftUtils.getDirectionFacing(entity, canRotateToTopOrBottom());
            setRotation(world, pos, rotation, state);
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
            return super.getStateFromMeta(meta).withProperty(ROTATION, EnumFacing.getFront(meta));
        } else {
            return super.getStateFromMeta(meta);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        dropInventory(world, pos);
        super.breakBlock(world, pos, state);
    }

    protected void dropInventory(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityBase) {
            ((TileEntityBase) te).dropAllInventoryItems();
        }
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side) {
        if (player.isSneaking()) {
            if (!player.capabilities.isCreativeMode) dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            world.setBlockToAir(pos);
            return true;
        } else {
            if (isRotatable()) {
                IBlockState state = world.getBlockState(pos);
                if (!rotateCustom(world, pos, state, side)) {
                    TileEntityBase te = (TileEntityBase) world.getTileEntity(pos);
                    if (rotateForgeWay()) {
                        if (!canRotateToTopOrBottom()) side = EnumFacing.UP;
                        if (getRotation(world, pos).getAxis() != side.getAxis())
                            setRotation(world, pos, getRotation(world, pos).rotateAround(side.getAxis()));
                    } else {
                        do {
                            setRotation(world, pos, EnumFacing.getFront(getRotation(world, pos).ordinal() + 1));
                        } while (canRotateToTopOrBottom() || getRotation(world, pos).getAxis() != Axis.Y);
                    }
                    te.onBlockRotated();
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

    /**
     * Produce an peripheral implementation from a block location.
     *
     * @return a peripheral, or null if there is not a peripheral here you'd like to handle.
     * @see dan200.computercraft.api.ComputerCraftAPI#registerPeripheralProvider(IPeripheralProvider)
     */
    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof IPeripheral ? (IPeripheral) te : null;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> curInfo, boolean extraInfo) {
        if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
            TileEntity te = createTileEntity(player.world, getDefaultState());
            if (te instanceof TileEntityPneumaticBase) {
                float pressure = ((TileEntityPneumaticBase) te).dangerPressure;
                curInfo.add(TextFormatting.YELLOW + I18n.format("gui.tooltip.maxPressure", pressure));
            }
        }

        String info = "gui.tab.info." + stack.getUnlocalizedName();
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
        return getUnlocalizedName() + ".name";
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
}
