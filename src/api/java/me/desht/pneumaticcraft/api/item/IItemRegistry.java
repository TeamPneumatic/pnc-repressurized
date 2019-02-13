package me.desht.pneumaticcraft.api.item;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.util.List;

/**
 * Get an instance of this with {@link PneumaticRegistry.IPneumaticCraftInterface#getItemRegistry()}
 */
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
     * Register a third-party class that can contain items.  This is intended for classes from other mods - if it's
     * your class, just make it implement {@link IInventoryItem} directly.
     *
     * @param handler instance of any class that implements {@link IInventoryItem}
     */
    void registerInventoryItem(IInventoryItem handler);

    /**
     * Returns the upgrade item that is mapped to the given type.  Note that if the upgrade in question depends upon
     * another mod which is not loaded, this will return null.  See {@link EnumUpgrade#isDepLoaded()}
     *
     * @param type the upgrade type
     */
    Item getUpgrade(EnumUpgrade type);

    /**
     * Register an item or block as being able to accept PneumaticCraft upgrades.
     *
     * @param upgradeAcceptor the upgrade acceptor
     */
    void registerUpgradeAcceptor(IUpgradeAcceptor upgradeAcceptor);

    /**
     * Can be used for custom upgrade items to handle tooltips. This will work for implementors registered via
     * {@link IItemRegistry#registerUpgradeAcceptor(IUpgradeAcceptor)}. You would generally call this from your
     * {@link Item#addInformation(ItemStack, World, List, ITooltipFlag)} method to display
     * which machines and/or items accept it.
     *
     * @param upgrade the upgrade item
     * @param tooltip the tooltip string list to append to
     */
    void addTooltip(Item upgrade, List<String> tooltip);

    /**
     * Register a magnet suppressor; an object which can prevent the Magnet Upgrade from pulling in (usually item)
     * entities.
     *
     * @param suppressor a suppressor object
     */
    void registerMagnetSuppressor(IMagnetSuppressor suppressor);
}
