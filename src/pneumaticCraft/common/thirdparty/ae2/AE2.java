package pneumaticCraft.common.thirdparty.ae2;

import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.thirdparty.IThirdParty;

public class AE2 implements IThirdParty{

    @Override
    public void preInit(){}

    @Override
    public void init(){
        PneumaticRegistry.getInstance().registerInventoryItem(new AE2DiskInventoryItemHandler());
    }

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){}

    @Override
    public void clientInit(){}

}
