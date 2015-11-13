package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntitySentryTurret;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockSentryTurret extends BlockPneumaticCraftModeled{

    protected BlockSentryTurret(Material par2Material){
        super(par2Material);
        setBlockBounds(3 / 16F, 0, 3 / 16F, 13 / 16F, 14 / 16F, 13 / 16F);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntitySentryTurret.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.SENTRY_TURRET;
    }

}
