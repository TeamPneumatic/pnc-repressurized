package pneumaticCraft.common.thirdparty.fmp;

import pneumaticCraft.common.thirdparty.IThirdParty;

public class FMPLoader implements IThirdParty{
    //Apparently this helper class is needed :(
    public FMP fmp = new FMP();

    @Override
    public void preInit(){
        fmp.preInit();
    }

    @Override
    public void init(){
        fmp.init();
    }

    @Override
    public void postInit(){
        fmp.postInit();
    }

    @Override
    public void clientSide(){
        fmp.clientSide();
    }

    @Override
    public void clientInit(){
        fmp.clientInit();
    }

}
