package pneumaticCraft.common.thirdparty.ic2;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockPneumaticGenerator extends BlockPneumaticCraftModeled{

    public BlockPneumaticGenerator(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPneumaticGenerator.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.PNEUMATIC_GENERATOR;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

}
