package pneumaticCraft.common.thirdparty.igwmod;

import pneumaticCraft.common.thirdparty.IThirdParty;
import cpw.mods.fml.common.event.FMLInterModComms;

public class IGWMod implements IThirdParty{

    @Override
    public void preInit(){}

    @Override
    public void init(){}

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){
        FMLInterModComms.sendMessage("IGWMod", "pneumaticCraft.common.thirdparty.igwmod.IGWHandler", "init");
    }

    @Override
    public void clientInit(){}

}
