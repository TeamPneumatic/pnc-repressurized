package pneumaticCraft.common.thirdparty.buildcraft;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;
import pneumaticCraft.proxy.CommonProxy;

public class BlockKineticCompressor extends BlockPneumaticCraftModeled{

    public BlockKineticCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityKineticCompressor.class;
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_KINETIC_COMPRESSOR;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    /*@Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis){
        return PneumaticCraftUtils.rotateBuildcraftBlock(world, x, y, z, true);
    }*/
}
