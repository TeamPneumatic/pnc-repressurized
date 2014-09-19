package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberValve;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberWall;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPressureChamberWall extends BlockPneumaticCraft{

    private IIcon[] textures;
    public IIcon[] connectedIcons = new IIcon[iconRefByID.length];
    public static int[] iconRefByID = {0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11};

    public BlockPressureChamberWall(Material par2Material){
        super(par2Material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register){
        textures = new IIcon[8];
        String s = Textures.ICON_LOCATION + "pressureChamber/";
        textures[0] = register.registerIcon(s + "pressureChamberWall");
        textures[1] = register.registerIcon(s + "bottomLeft");
        textures[2] = register.registerIcon(s + "topLeft");
        textures[3] = register.registerIcon(s + "topRight");
        textures[4] = register.registerIcon(s + "bottomRight");
        textures[5] = register.registerIcon(s + "middleHorizontal");
        textures[6] = register.registerIcon(s + "middleVertical");
        textures[7] = register.registerIcon(s + "center");

        for(int i = 0; i < 47; i++)
            connectedIcons[i] = register.registerIcon(s + "windows/window_" + (i + 1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side){
        if(world.getBlockMetadata(x, y, z) > 5) return getConnectedIcon(world, x, y, z, side);
        TileEntityPressureChamberWall te = (TileEntityPressureChamberWall)world.getTileEntity(x, y, z);
        TileEntityPressureChamberValve core = te.getCore();
        if(core == null) {
            return textures[0];
        } else {
            boolean xMid = x != core.multiBlockX && x != core.multiBlockX + core.multiBlockSize - 1;
            boolean yMid = y != core.multiBlockY && y != core.multiBlockY + core.multiBlockSize - 1;
            boolean zMid = z != core.multiBlockZ && z != core.multiBlockZ + core.multiBlockSize - 1;
            if(yMid && !xMid && !zMid) return textures[6];
            if(!yMid && xMid ^ zMid) {
                if(side < 2 && zMid) {
                    return textures[6];
                } else {
                    return textures[5];
                }
            }
            if(!xMid && !yMid && !zMid) {
                boolean minX = x == core.multiBlockX;
                boolean minY = y == core.multiBlockY;
                boolean minZ = z == core.multiBlockZ;
                switch(side){
                    case 0:
                    case 1:
                        if(minX && minZ) return textures[2];
                        if(!minX && minZ) return textures[3];
                        if(minX && !minZ) return textures[1];
                        break;
                    case 2:
                        if(!minX && minY) return textures[1];
                        if(minX && !minY) return textures[3];
                        if(!minX && !minY) return textures[2];
                        break;
                    case 3:
                        if(minX && minY) return textures[1];
                        if(!minX && !minY) return textures[3];
                        if(minX && !minY) return textures[2];
                        break;
                    case 4:
                        if(minZ && minY) return textures[1];
                        if(!minZ && !minY) return textures[3];
                        if(minZ && !minY) return textures[2];
                        break;
                    case 5:
                        if(!minZ && minY) return textures[1];
                        if(minZ && !minY) return textures[3];
                        if(!minZ && !minY) return textures[2];
                        break;
                }
                return textures[4];
            }
            return textures[7];
        }
    }

    /**
     * @author amadornes , used in Blue Power, https://github.com/Qmunity/BluePower
     * modified by MineMaarten
     */
    public IIcon getConnectedIcon(IBlockAccess world, int x, int y, int z, int side){

        boolean[] bitMatrix = new boolean[8];

        if(side == 0 || side == 1) {
            bitMatrix[0] = isGlass(world, x - 1, y, z - 1);
            bitMatrix[1] = isGlass(world, x, y, z - 1);
            bitMatrix[2] = isGlass(world, x + 1, y, z - 1);
            bitMatrix[3] = isGlass(world, x - 1, y, z);
            bitMatrix[4] = isGlass(world, x + 1, y, z);
            bitMatrix[5] = isGlass(world, x - 1, y, z + 1);
            bitMatrix[6] = isGlass(world, x, y, z + 1);
            bitMatrix[7] = isGlass(world, x + 1, y, z + 1);
        }
        if(side == 2 || side == 3) {
            bitMatrix[0] = isGlass(world, x + (side == 2 ? 1 : -1), y + 1, z);
            bitMatrix[1] = isGlass(world, x, y + 1, z);
            bitMatrix[2] = isGlass(world, x + (side == 3 ? 1 : -1), y + 1, z);
            bitMatrix[3] = isGlass(world, x + (side == 2 ? 1 : -1), y, z);
            bitMatrix[4] = isGlass(world, x + (side == 3 ? 1 : -1), y, z);
            bitMatrix[5] = isGlass(world, x + (side == 2 ? 1 : -1), y - 1, z);
            bitMatrix[6] = isGlass(world, x, y - 1, z);
            bitMatrix[7] = isGlass(world, x + (side == 3 ? 1 : -1), y - 1, z);
        }
        if(side == 4 || side == 5) {
            bitMatrix[0] = isGlass(world, x, y + 1, z + (side == 5 ? 1 : -1));
            bitMatrix[1] = isGlass(world, x, y + 1, z);
            bitMatrix[2] = isGlass(world, x, y + 1, z + (side == 4 ? 1 : -1));
            bitMatrix[3] = isGlass(world, x, y, z + (side == 5 ? 1 : -1));
            bitMatrix[4] = isGlass(world, x, y, z + (side == 4 ? 1 : -1));
            bitMatrix[5] = isGlass(world, x, y - 1, z + (side == 5 ? 1 : -1));
            bitMatrix[6] = isGlass(world, x, y - 1, z);
            bitMatrix[7] = isGlass(world, x, y - 1, z + (side == 4 ? 1 : -1));
        }

        int idBuilder = 0;

        for(int i = 0; i <= 7; i++)
            idBuilder = idBuilder + (bitMatrix[i] ? i == 0 ? 1 : i == 1 ? 2 : i == 2 ? 4 : i == 3 ? 8 : i == 4 ? 16 : i == 5 ? 32 : i == 6 ? 64 : 128 : 0);

        return idBuilder > 255 || idBuilder < 0 ? connectedIcons[0] : connectedIcons[iconRefByID[idBuilder]];

    }

    private boolean isGlass(IBlockAccess world, int x, int y, int z){
        return world.getBlock(x, y, z) == this && world.getBlockMetadata(x, y, z) > 5;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side){
        ForgeDirection d = ForgeDirection.getOrientation(side).getOpposite();
        return !isGlass(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ) || !isGlass(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta){
        return meta > 5 ? connectedIcons[0] : textures[0];
    }

    @Override
    public boolean isOpaqueCube(){
        return false;
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
        super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLiving, iStack);
        TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, par2, par3, par4);
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

}
