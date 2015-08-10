package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberInterface;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPressureChamberInterface extends BlockPressureChamberWall{

    public BlockPressureChamberInterface(Material par2Material){
        super(par2Material);
    }

    @Override
    public int getRenderType(){
        return PneumaticCraft.proxy.SPECIAL_RENDER_TYPE_VALUE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPressureChamberInterface.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.PRESSURE_CHAMBER_INTERFACE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        par3List.add(new ItemStack(this, 1, 0));
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        if(player.isSneaking() || getGuiID() == null) return false;
        else {
            if(!world.isRemote) {
                TileEntity te = world.getTileEntity(x, y, z);

                if(te != null) {
                    player.openGui(PneumaticCraft.instance, getGuiID().ordinal(), world, x, y, z);
                }
            }

            return true;
        }
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

    @Override
    protected int getInventoryDropEndSlot(IInventory inventory){
        return 5;
    }

    /**
     * Determines the damage on the item the block drops. Used in cloth and
     * wood.
     */
    @Override
    public int damageDropped(int par1){
        return 0;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection side){
        if(player.isSneaking()) {
            return super.rotateBlock(world, player, x, y, z, side);
        } else {
            int newMeta = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z)).getRotation(side).ordinal();
            world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
            return true;
        }
    }

}
