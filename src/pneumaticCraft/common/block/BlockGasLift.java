package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityGasLift;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockGasLift extends BlockPneumaticCraftModeled{

    protected BlockGasLift(Material par2Material){
        super(par2Material);
        setBlockBounds(0, 0, 0, 1, 10 / 16F, 1);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityGasLift.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.GAS_LIFT;
    }

}
