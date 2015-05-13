package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.proxy.CommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockProgrammableController extends BlockPneumaticCraft{

    private IIcon topTexture;
    private IIcon bottomTexture;

    public BlockProgrammableController(Material par2Material){
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
        return TileEntityProgrammableController.class;
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_PROGRAMMABLE_CONTROLLER;
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

        /*TileEntity te = par1IBlockAccess.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityAerialInterface) {
            TileEntityAerialInterface teAi = (TileEntityAerialInterface)te;
            return teAi.shouldEmitRedstone() ? 15 : 0;
        }*/

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
}
