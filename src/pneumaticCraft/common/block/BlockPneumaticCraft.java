package pneumaticCraft.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.block.IPneumaticWrenchable;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.IComparatorSupport;
import pneumaticCraft.common.tileentity.TileEntityBase;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.util.FluidUtils;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = ModIds.COMPUTERCRAFT)
public abstract class BlockPneumaticCraft extends BlockContainer implements IPneumaticWrenchable, IPeripheralProvider{

    protected BlockPneumaticCraft(Material par2Material){
        super(par2Material);
        setBlockTextureName(Textures.BLOCK_PRESSURE_TUBE); //registering an icon to render for the block breaking animation
        setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        setHardness(3.0F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata){
        try {
            return getTileEntityClass().newInstance();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract Class<? extends TileEntity> getTileEntityClass();

    public EnumGuiId getGuiID(){
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        if(player.isSneaking() || getGuiID() == null || isRotatable() && player.getCurrentEquippedItem() != null && (player.getCurrentEquippedItem().getItem() == Itemss.manometer || ModInteractionUtils.getInstance().isModdedWrench(player.getCurrentEquippedItem().getItem()))) return false;
        else {
            if(!world.isRemote) {
                TileEntity te = world.getTileEntity(x, y, z);

                List<ItemStack> returnedItems = new ArrayList<ItemStack>();
                if(te != null && !FluidUtils.tryInsertingLiquid(te, player.getCurrentEquippedItem(), player.capabilities.isCreativeMode, returnedItems)) {
                    player.openGui(PneumaticCraft.instance, getGuiID().ordinal(), world, x, y, z);
                } else {
                    if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().stackSize <= 0) {
                        player.setCurrentItemOrArmor(0, null);
                    }
                    for(ItemStack returnedItem : returnedItems) {
                        returnedItem = returnedItem.copy();
                        if(player.getCurrentEquippedItem() == null) {
                            player.setCurrentItemOrArmor(0, returnedItem);
                        } else {
                            player.inventory.addItemStackToInventory(returnedItem);
                        }
                    }
                }
            }

            return true;
        }
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack){
        if(isRotatable()) {
            int l = PneumaticCraftUtils.getDirectionFacing(par5EntityLiving, canRotateToTopOrBottom()).ordinal();
            par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 3);
        }
    }

    public boolean isRotatable(){
        return false;
    }

    protected boolean canRotateToTopOrBottom(){
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        dropInventory(world, x, y, z);
        super.breakBlock(world, x, y, z, block, meta);
    }

    protected void dropInventory(World world, int x, int y, int z){

        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if(!(tileEntity instanceof IInventory)) return;

        IInventory inventory = (IInventory)tileEntity;
        Random rand = new Random();
        for(int i = getInventoryDropStartSlot(inventory); i < getInventoryDropEndSlot(inventory); i++) {

            ItemStack itemStack = inventory.getStackInSlot(i);

            if(itemStack != null && itemStack.stackSize > 0) {
                float dX = rand.nextFloat() * 0.8F + 0.1F;
                float dY = rand.nextFloat() * 0.8F + 0.1F;
                float dZ = rand.nextFloat() * 0.8F + 0.1F;

                EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, new ItemStack(itemStack.getItem(), itemStack.stackSize, itemStack.getItemDamage()));

                if(itemStack.hasTagCompound()) {
                    entityItem.getEntityItem().setTagCompound((NBTTagCompound)itemStack.getTagCompound().copy());
                }

                float factor = 0.05F;
                entityItem.motionX = rand.nextGaussian() * factor;
                entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
                entityItem.motionZ = rand.nextGaussian() * factor;
                world.spawnEntityInWorld(entityItem);
                itemStack.stackSize = 0;
            }
        }
    }

    protected int getInventoryDropStartSlot(IInventory inventory){
        return 0;
    }

    protected int getInventoryDropEndSlot(IInventory inventory){
        return inventory.getSizeInventory();
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection side){
        if(player.isSneaking()) {
            if(!player.capabilities.isCreativeMode) dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
            return true;
        } else {
            if(isRotatable()) {
                int meta = world.getBlockMetadata(x, y, z);
                if(!rotateCustom(world, x, y, z, side, meta)) {
                    int newMeta;
                    if(rotateForgeWay()) {
                        if(!canRotateToTopOrBottom()) side = ForgeDirection.UP;

                        newMeta = ForgeDirection.getOrientation(meta).getRotation(side).ordinal();
                        world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
                    } else {
                        newMeta = (meta + 1) % 6;
                        if(!canRotateToTopOrBottom()) {
                            if(newMeta == 0) {
                                newMeta = 2;
                            }
                        }
                    }
                    world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
                }

                TileEntity te = world.getTileEntity(x, y, z);
                if(te instanceof TileEntityBase) {
                    ((TileEntityBase)te).onBlockRotated();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    protected boolean rotateForgeWay(){
        return true;
    }

    protected boolean rotateCustom(World world, int x, int y, int z, ForgeDirection side, int meta){
        return false;
    }

    /**
     * Called when a tile entity on a side of this block changes is created or is destroyed.
     * @param world The world
     * @param x The x position of this block instance
     * @param y The y position of this block instance
     * @param z The z position of this block instance
     * @param tileX The x position of the tile that changed
     * @param tileY The y position of the tile that changed
     * @param tileZ The z position of the tile that changed
     */
    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ){
        if(world instanceof World && !((World)world).isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityBase) {
                ((TileEntityBase)te).onNeighborTileUpdate();
            }
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        if(world instanceof World && !world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityBase) {
                ((TileEntityBase)te).onNeighborBlockUpdate();
            }
        }
    }

    /**
     * Produce an peripheral implementation from a block location.
     * @see dan200.computercraft.api.ComputerCraftAPI#registerPeripheralProvider(IPeripheralProvider)
     * @return a peripheral, or null if there is not a peripheral here you'd like to handle.
     */
    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public IPeripheral getPeripheral(World world, int x, int y, int z, int side){
        TileEntity te = world.getTileEntity(x, y, z);
        return te instanceof IPeripheral ? (IPeripheral)te : null;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List curInfo, boolean extraInfo){
        if(PneumaticCraft.proxy.isSneakingInGui()) {
            TileEntity te = createNewTileEntity(player.worldObj, 0);
            if(te instanceof TileEntityPneumaticBase) {
                float pressure = ((TileEntityPneumaticBase)te).DANGER_PRESSURE;
                curInfo.add(EnumChatFormatting.YELLOW + I18n.format("gui.tooltip.maxPressure", pressure));
            }
        }

        String info = "gui.tab.info." + stack.getUnlocalizedName();
        String translatedInfo = I18n.format(info);
        if(!translatedInfo.equals(info)) {
            if(PneumaticCraft.proxy.isSneakingInGui()) {
                translatedInfo = EnumChatFormatting.AQUA + translatedInfo.substring(2);
                if(!Loader.isModLoaded(ModIds.IGWMOD)) translatedInfo += " \\n \\n" + I18n.format("gui.tab.info.assistIGW");
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 60));
            } else {
                curInfo.add(EnumChatFormatting.AQUA + I18n.format("gui.tooltip.sneakForInfo"));
            }
        }
    }

    /**
     * If this returns true, then comparators facing away from this block will use the value from
     * getComparatorInputOverride instead of the actual redstone signal strength.
     */
    @Override
    public boolean hasComparatorInputOverride(){
        return IComparatorSupport.class.isAssignableFrom(getTileEntityClass());
    }

    /**
     * If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
     * strength when this block inputs to a comparator.
     */
    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side){
        return ((IComparatorSupport)world.getTileEntity(x, y, z)).getComparatorValue(ForgeDirection.getOrientation(side));
    }
}
