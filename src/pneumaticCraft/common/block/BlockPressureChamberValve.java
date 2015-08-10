package pneumaticCraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberValve;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.proxy.CommonProxy;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPressureChamberValve extends BlockPneumaticCraft{

    private IIcon[] textures;

    public BlockPressureChamberValve(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPressureChamberValve.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register){
        textures = new IIcon[4];
        String s = Textures.ICON_LOCATION + "pressureChamber/";
        textures[0] = register.registerIcon(s + "valve");
        textures[1] = register.registerIcon(s + "valve_formed");
        textures[2] = register.registerIcon(s + "valve_side");
        textures[3] = register.registerIcon(s + "center");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side){
        TileEntityPressureChamberValve valve = (TileEntityPressureChamberValve)world.getTileEntity(x, y, z);
        boolean formed = valve.accessoryValves != null && valve.accessoryValves.size() > 0;
        if((world.getBlockMetadata(x, y, z) | 1) == (side | 1)) {
            return textures[formed ? 1 : 0];
        } else {
            return textures[formed ? 3 : 2];
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta){
        return side == 2 || side == 3 ? textures[0] : textures[2];
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack iStack){
        super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLiving, iStack);
        TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, par2, par3, par4);
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        if(player.isSneaking()) return false;
        TileEntity te = world.getTileEntity(x, y, z);
        if(!world.isRemote && te instanceof TileEntityPressureChamberValve) {
            if(((TileEntityPressureChamberValve)te).multiBlockSize > 0) {
                player.openGui(PneumaticCraft.instance, EnumGuiId.PRESSURE_CHAMBER.ordinal(), world, x, y, z);
            } else if(((TileEntityPressureChamberValve)te).accessoryValves.size() > 0) { // when
                                                                                         // this
                                                                                         // isn't
                                                                                         // the
                                                                                         // core
                                                                                         // Valve,
                                                                                         // track
                                                                                         // down
                                                                                         // the
                                                                                         // core
                                                                                         // Valve.
                //  System.out.println("size: " + ((TileEntityPressureChamberValve)te).accessoryValves.size());
                for(TileEntityPressureChamberValve valve : ((TileEntityPressureChamberValve)te).accessoryValves) {
                    if(valve.multiBlockSize > 0) {
                        player.openGui(PneumaticCraft.instance, EnumGuiId.PRESSURE_CHAMBER.ordinal(), world, valve.xCoord, valve.yCoord, valve.zCoord);
                        break;
                    }
                }
            } else {
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        invalidateMultiBlock(world, x, y, z);
        super.breakBlock(world, x, y, z, block, meta);

    }

    private void invalidateMultiBlock(World world, int x, int y, int z){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityPressureChamberValve && !world.isRemote) {
            if(((TileEntityPressureChamberValve)te).multiBlockSize > 0) {
                ((TileEntityPressureChamberValve)te).onMultiBlockBreak();
            } else if(((TileEntityPressureChamberValve)te).accessoryValves.size() > 0) {
                for(TileEntityPressureChamberValve valve : ((TileEntityPressureChamberValve)te).accessoryValves) {
                    if(valve.multiBlockSize > 0) {
                        valve.onMultiBlockBreak();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection face){
        if(player.isSneaking()) {
            return super.rotateBlock(world, player, x, y, z, face);
        } else {
            int newMeta = (world.getBlockMetadata(x, y, z) / 2 + 1) * 2;
            if(newMeta == 6) newMeta = 0;
            world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
            invalidateMultiBlock(world, x, y, z);
            TileEntityPressureChamberValve.checkIfProperlyFormed(world, x, y, z);
            return true;
        }
    }

}
