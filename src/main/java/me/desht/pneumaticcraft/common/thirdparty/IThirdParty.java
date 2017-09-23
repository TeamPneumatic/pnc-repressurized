package me.desht.pneumaticcraft.common.thirdparty;

public interface IThirdParty {

    void preInit();

    void init();

    void postInit();

    /**
     * Gets called from the ClientProxy in the preInit.
     */
    void clientSide();

    /**
     * Gets called from the ClientProxy in the Init.
     */
    void clientInit();
}
