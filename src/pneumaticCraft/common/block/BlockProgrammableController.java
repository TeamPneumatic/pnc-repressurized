package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockProgrammableController extends BlockPneumaticCraft{

    public BlockProgrammableController(Material par2Material){
        super(par2Material);
        setBlockTextureName(Textures.BLOCK_PROGRAMMABLE_CONTROLLER);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityProgrammableController.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.PROGRAMMABLE_CONTROLLER;
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
        if(te instanceof TileEntityProgrammableController) {
            return ((TileEntityProgrammableController)te).getEmittingRedstone(par5 ^ 1);
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
        return false;
    }
}
