package me.desht.pneumaticcraft.common.thirdparty;

public interface IThirdParty {
    /**
     * Called on both client and server after any registry objects are created, in the mod creation thread.
     */
    default void init() {}

    /**
     * Called on both client and server after any registry objects are created, on a scheduled tick (so in the main
     * execution thread).
     */
    default void postInit() {}

    /**
     * Called client-side after registry objects are created, in the mod creation thread.
     */
    default void clientInit() {}

    default ThirdPartyManager.ModType modType() {
        return null;
    }
}
