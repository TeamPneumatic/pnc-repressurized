package pneumaticCraft.common.thirdparty;

import net.minecraft.creativetab.CreativeTabs;

public interface IThirdParty{

    public void preInit(CreativeTabs pneumaticCraftTab);

    public void init();

    public void postInit();

    /**
     * Gets called from the ClientProxy in the preInit.
     */
    public void clientSide();
}
