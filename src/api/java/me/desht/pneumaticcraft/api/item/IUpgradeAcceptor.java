package me.desht.pneumaticcraft.api.item;

import java.util.Map;

/**
 * Indicates that the implementor can accept PneumaticCraft upgrades.
 *
 * Could be implemented by anything; implementations should be registered through
 * {@link IItemRegistry#registerUpgradeAcceptor(IUpgradeAcceptor)}
 */
public interface IUpgradeAcceptor {
    /**
     * This method is called as soon as an instance of this interface is registered, be aware.
     * It should return a map which maps the collection of accepted upgrades for this machine/item to the
     * maximum number of each upgrade which can be inserted.
     *
     * @return a map of the accepted upgrades and their maximum count
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
