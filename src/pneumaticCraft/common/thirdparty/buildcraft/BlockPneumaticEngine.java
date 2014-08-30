package pneumaticCraft.common.thirdparty.buildcraft;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;
import pneumaticCraft.proxy.CommonProxy;

public class BlockPneumaticEngine extends BlockPneumaticCraftModeled{

    public BlockPneumaticEngine(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPneumaticEngine.class;
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_PNEUMATIC_ENGINE;
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack){
        rotateBlock(par1World, par2, par3, par4, ForgeDirection.UNKNOWN);
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
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis){
        return PneumaticCraftUtils.rotateBuildcraftBlock(world, x, y, z, false);
    }

}
