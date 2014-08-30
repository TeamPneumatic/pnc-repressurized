package pneumaticCraft.common.thirdparty.ae2;

import net.minecraft.creativetab.CreativeTabs;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.thirdparty.IThirdParty;

public class AE2 implements IThirdParty{

    @Override
    public void preInit(CreativeTabs pneumaticCraftTab){}

    @Override
    public void init(){
        PneumaticRegistry.getInstance().registerInventoryItem(new AE2DiskInventoryItemHandler());
    }

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){}

}
