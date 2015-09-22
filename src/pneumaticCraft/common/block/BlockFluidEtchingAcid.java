package pneumaticCraft.common.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.common.DamageSourcePneumaticCraft;
import pneumaticCraft.common.fluid.Fluids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFluidEtchingAcid extends BlockFluidPneumaticCraft{

    public BlockFluidEtchingAcid(){
        super(Fluids.etchingAcid, new MaterialLiquid(MapColor.waterColor){
            @Override
            public int getMaterialMobility(){
                return 1;
            }
        });
        setBlockName("etchingAcid");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta){
        return net.minecraft.init.Blocks.flowing_water.getIcon(side, meta);
    }

    @Override
    public int colorMultiplier(IBlockAccess iblockaccess, int x, int y, int z){
        return 0x501c00;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity){
        if(entity instanceof EntityLivingBase && entity.ticksExisted % 10 == 0) {
            ((EntityLivingBase)entity).attackEntityFrom(DamageSourcePneumaticCraft.etchingAcid, 1);
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister){}
}
