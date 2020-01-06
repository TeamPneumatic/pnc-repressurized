package me.desht.pneumaticcraft.api.item;

import java.util.Map;

/**
 * Could be implemented by anything and registered through {@link IItemRegistry#registerUpgradeAcceptor(IUpgradeAcceptor)}
 */
public interface IUpgradeAcceptor {
    /**
     * This method is called right when an instance of this interface is registered, be aware.
     * It should return an set of all upgrades that are applicable for this machine/item/...
     *
     * @return a set of the items which will be accepted as upgrades
     */
    Map<EnumUpgrade, Integer> getApplicableUpgrades();

    /**
     * Get a translation key for this upgrade acceptor. This is used to display the acceptor in relevant upgrades'
     * tooltip texts.
     *
     * @return a translation key
     */
    String getUpgradeAcceptorTranslationKey();
}
