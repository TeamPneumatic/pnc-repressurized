package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.Item;

import java.util.Set;

/**
 * Could be implemented by anything and registered through {@link IItemRegistry#registerUpgradeAcceptor(IUpgradeAcceptor)}
 */
public interface IUpgradeAcceptor {
    /**
     * This method is called right when an instance of this interfaced is registered, be aware.
     * It should return an set of all upgrades that are applicable for this machine/item/...
     *
     * @return
     */
    Set<Item> getApplicableUpgrades();

    String getName();
}
