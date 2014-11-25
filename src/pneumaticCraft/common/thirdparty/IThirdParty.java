package pneumaticCraft.common.thirdparty;

public interface IThirdParty{

    public void preInit();

    public void init();

    public void postInit();

    /**
     * Gets called from the ClientProxy in the preInit.
     */
    public void clientSide();
}
