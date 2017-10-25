package me.desht.pneumaticcraft.common.thirdparty;

public interface IThirdParty {

    default void preInit() {}

    default void init() {}

    default void postInit() {}

    /**
     * Gets called from the ClientProxy in the preInit.
     */
    default void clientSide() {}

    /**
     * Gets called from the ClientProxy in the Init.
     */
    default void clientInit() {}
}
