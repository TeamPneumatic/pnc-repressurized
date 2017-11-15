package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.Item;

import java.util.List;

public interface IItemRegistry {
    enum EnumUpgrade {
        VOLUME, DISPENSER, ITEM_LIFE, ENTITY_TRACKER, BLOCK_TRACKER, SPEED, SEARCH, COORDINATE_TRACKER, RANGE, SECURITY, THAUMCRAFT /*Only around when Thaumcraft is */
    }

    /**
     * See {@link me.desht.pneumaticcraft.api.item.IInventoryItem}
     *
     * @param handler
     */
    void registerInventoryItem(IInventoryItem handler);

    /**
     * Returns the upgrade item that is mapped to the asked type.
     */
    Item getUpgrade(EnumUpgrade type);

    /**
     * When an instance of this registered, this will only add to the applicable upgrade's tooltip.
     *
     * @param upgradeAcceptor
     */
    void registerUpgradeAcceptor(IUpgradeAcceptor upgradeAcceptor);

    /**
     * Can be used for custom upgrade items to handle tooltips. This will work for implementors registered via {@link IItemRegistry#registerUpgradeAcceptor(IUpgradeAcceptor)}.
     *
     * @param upgrade
     * @param tooltip
     */
    void addTooltip(Item upgrade, List<String> tooltip);
}
