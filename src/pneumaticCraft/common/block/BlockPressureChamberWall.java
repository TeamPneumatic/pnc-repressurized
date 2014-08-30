package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberValve;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberWall;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPressureChamberWall extends BlockPneumaticCraftModeled{

    public BlockPressureChamberWall(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPressureChamberWall.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        for(int var4 = 0; var4 < 2; ++var4) {
            par3List.add(new ItemStack(this, 1, var4 * 6));
        }
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack iStack){
        int meta = PneumaticCraftUtils.getDirectionFacing(par5EntityLiving, true).ordinal() + (iStack.getItemDamage() == 6 ? 6 : 0);
        par1World.setBlockMetadataWithNotify(par2, par3, par4, meta, 3);
        // System.out.println("meta: " + meta);
        TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, par2, par3, par4);
    }

    @Override
    protected boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        if(world.isRemote) return true;
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityPressureChamberWall) {
            TileEntityPressureChamberValve valve = ((TileEntityPressureChamberWall)te).getCore();
            if(valve != null) {
                return valve.getBlockType().onBlockActivated(world, valve.xCoord, valve.yCoord, valve.zCoord, player, par6, par7, par8, par9);

            }
        }
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityPressureChamberWall && !world.isRemote) {
            ((TileEntityPressureChamberWall)te).onBlockBreak();
        }
        super.breakBlock(world, x, y, z, block, meta);

    }

    /**
     * Determines the damage on the item the block drops. Used in cloth and
     * wood.
     */
    @Override
    public int damageDropped(int par1){
        return par1 < 6 ? 0 : 6;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection face){
        int newMeta = (world.getBlockMetadata(x, y, z) / 2 + 1) * 2;
        if(newMeta == 6 || newMeta == 12) newMeta -= 6;
        world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
        return true;
    }

}
