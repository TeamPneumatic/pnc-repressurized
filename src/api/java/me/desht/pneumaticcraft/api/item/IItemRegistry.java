package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;

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
        THAUMCRAFT("thaumcraft", "thaumcraft"), /*Only around when Thaumcraft is */
        CHARGING("charging"),
        ARMOR("armor"),
        JET_BOOTS("jetboots"),
        NIGHT_VISION("night_vision"),
        SCUBA("scuba");

        private final String name;
        private final String depModId;

        EnumUpgrade(String name) {
            this(name, null);
        }

        EnumUpgrade(String name, String depModId) {
            this.name = name;
            this.depModId = depModId;
        }

        public String getName() {
            return name;
        }

        /**
         * Check if this upgrade's dependent mod (if any) is loaded.  If this returns false, then
         * {@link IItemRegistry#getUpgrade(EnumUpgrade)} will return null.
         *
         * @return true if this upgrade's dependent mod is loaded, false otherwise
         */
        public boolean isDepLoaded() {
            return depModId == null || Loader.isModLoaded(depModId);
        }
    }

    /**
     * See {@link me.desht.pneumaticcraft.api.item.IInventoryItem}
     *
     * @param handler
     */
    void registerInventoryItem(IInventoryItem handler);

    /**
     * Returns the upgrade item that is mapped to the asked type.  Note that if the upgrade in question depends upon
     * another mod which is not loaded, this will return null.  See {@link EnumUpgrade#isDepLoaded()}
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

    /**
     * Register a magnet suppressor; an object which can prevent the Magnet Upgrade from pulling in (usually item) entities.
     *
     * @param suppressor a suppressor object
     */
    void registerMagnetSuppressor(IMagnetSuppressor suppressor);
}
