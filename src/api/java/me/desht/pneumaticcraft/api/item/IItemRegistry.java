package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.Item;

import java.util.List;

public interface IItemRegistry {
    enum EnumUpgrade {
        VOLUME("volume"),
        DISPENSER("dispenser"),
        ITEM_LIFE("itemLife"),
        ENTITY_TRACKER("entityTracker"),
        BLOCK_TRACKER("blockTracker"),
        SPEED("speed"),
        SEARCH("search"),
        COORDINATE_TRACKER("coordinateTracker"),
        RANGE("range"),
        SECURITY("security"),
        MAGNET("magnet"),
        THAUMCRAFT("thaumcraft") /*Only around when Thaumcraft is */;

        private final String name;

        EnumUpgrade(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
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
