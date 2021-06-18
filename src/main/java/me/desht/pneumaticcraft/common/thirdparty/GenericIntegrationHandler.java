package me.desht.pneumaticcraft.common.thirdparty;

/**
 * Generic integration tasks which don't depend on specific mods' APIs.
 */
public class GenericIntegrationHandler implements IThirdParty {
    @Override
    public void postInit() {
        ModdedWrenchUtils.getInstance().registerThirdPartyWrenches();
        ModNameCache.init();
    }
}
