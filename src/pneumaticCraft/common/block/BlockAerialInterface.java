package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityAerialInterface;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class BlockAerialInterface extends BlockPneumaticCraft{

    private IIcon topTexture;
    private IIcon bottomTexture;

    public BlockAerialInterface(Material par2Material){
        super(par2Material);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister){
        blockIcon = par1IconRegister.registerIcon(Textures.BLOCK_AERIAL_INTERFACE_SIDE);
        topTexture = par1IconRegister.registerIcon(Textures.BLOCK_AERIAL_INTERFACE_TOP);
        bottomTexture = par1IconRegister.registerIcon(Textures.BLOCK_AERIAL_INTERFACE_BOTTOM);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta){
        switch(ForgeDirection.getOrientation(side)){
            case UP:
                return topTexture;
            case DOWN:
                return bottomTexture;
            default:
                return blockIcon;
        }
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAerialInterface.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.AERIAL_INTERFACE;
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase entity, ItemStack par6ItemStack){
        TileEntity te = par1World.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityAerialInterface && entity instanceof EntityPlayer) {
            ((TileEntityAerialInterface)te).setPlayer(((EntityPlayer)entity).getGameProfile());
        }
    }

    /**
     * Returns true if the block is emitting direct/strong redstone power on the
     * specified side. Args: World, X, Y, Z, side. Note that the side is
     * reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    @Override
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
        return 0;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){

        TileEntity te = par1IBlockAccess.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityAerialInterface) {
            TileEntityAerialInterface teAi = (TileEntityAerialInterface)te;
            return teAi.shouldEmitRedstone() ? 15 : 0;
        }

        return 0;
    }

    /**
     * Called to determine whether to allow the a block to handle its own indirect power rather than using the default rules.
     * @param world The world
     * @param x The x position of this block instance
     * @param y The y position of this block instance
     * @param z The z position of this block instance
     * @param side The INPUT side of the block to be powered - ie the opposite of this block's output side
     * @return Whether Block#isProvidingWeakPower should be called when determining indirect power
     */
    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side){
        return true;
    }

    @Override
    public boolean canProvidePower(){
        return true;
    }

    @Override
    protected int getInventoryDropEndSlot(IInventory inventory){
        return 4;
    }

    /**
     * Produce an peripheral implementation from a block location.
     * @see dan200.computercraft.api.ComputerCraftAPI#registerPeripheralProvider(IPeripheralProvider)
     * @return a peripheral, or null if there is not a peripheral here you'd like to handle.
     */
    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public IPeripheral getPeripheral(World world, int x, int y, int z, int side){
        return side == 0 || side == 1 ? super.getPeripheral(world, x, y, z, side) : null;
    }
}
