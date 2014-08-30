package pneumaticCraft.common.thirdparty.fmp;

import net.minecraft.creativetab.CreativeTabs;
import pneumaticCraft.common.thirdparty.IThirdParty;

public class FMPLoader implements IThirdParty {
	//Apparently this helper class is needed :(
	public FMP fmp = new FMP();
	
	@Override
	public void preInit(CreativeTabs pneumaticCraftTab) {
		fmp.preInit(pneumaticCraftTab);
	}

	@Override
	public void init() {
		fmp.init();
	}

	@Override
	public void postInit() {
		fmp.postInit();
	}

	@Override
	public void clientSide() {
		fmp.clientSide();
	}

}
