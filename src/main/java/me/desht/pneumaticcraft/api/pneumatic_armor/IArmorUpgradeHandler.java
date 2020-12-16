package me.desht.pneumaticcraft.api.pneumatic_armor;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

/**
 * Represents the common part of a Pneumatic Armor upgrade handler.  Singleton implementations of this are present
 * on both client and server.  See {@link IArmorUpgradeClientHandler} for the client-side handler (which has a
 * one-to-one mapping to this).
 */
public interface IArmorUpgradeHandler {
    /**
     * Used for translation keys and keybind naming
     */
    String UPGRADE_PREFIX = "pneumaticcraft.armor.upgrade.";

    /**
     * Get a unique ID for this upgrade handler.
     *
     * @return a unique resource location
     */
    ResourceLocation getID();

    /**
     * Return the upgrades that are required to be in the armor piece to enable this module.
     *
     * @return an array of required upgrades
     */
    EnumUpgrade[] getRequiredUpgrades();

    /**
     * Get the maximum number of the given upgrade which may be installed.
     *
     * @param upgrade an upgrade
     * @return the maximum installable amount of this upgrade
     */
    default int getMaxInstallableUpgrades(EnumUpgrade upgrade) {
        return 1;
    }

    /**
     * Returns the usage in mL/tick when this upgrade handler is enabled.  Note this is constant usage just from the
     * upgrade being switched on, and does not take into account air used when some action is taken, e.g. flying with
     * the jet boots upgrade is not included here.
     *
     * @param armorHandler the armor handler object (can be used to get upgrades, etc.)
     * @return usage in mL/tick
     */
    float getIdleAirUsage(ICommonArmorHandler armorHandler);

    /**
     * Get the minimum armor pressure for this renderer to operate; the armor piece pressure must be <i>greater</i>
     * than this.  Most components require any pressure >0.0 bar.  Return any negative value for the component to
     * always render regardless of pressure.
     *
     * @return the minimum required pressure
     */
    default float getMinimumPressure() {
        return 0.0f;
    }

    /**
     * Get the armor slot that this upgrade handler is attached to.
     *
     * @return the armor slot
     */
    EquipmentSlotType getEquipmentSlot();

    default String getTranslationKey() {
        return getStringKey(getID());
    }

    /**
     * Source of truth for all translation keys and keybind names. Standard "pneumaticcraft.armor.upgrade" prefix,
     * followed by a resource location ID, where the ID is converted to a string.  ID's from the "pneumaticcraft"
     * namespace use just the resource location's path, while ID's from other namespaces include the namespace. E.g.:
     * <ul>
     * <li>"pneumaticcraft:block_tracker" -> "pneumaticcraft.armor.upgrade.block_tracker"</li>
     * <li>"pneumaticcraft:block_tracker.module.energy" -> "pneumaticcraft.armor.upgrade.block_tracker.module.energy"</li>
     * <li>"mod2:other_upgrade" -> "pneumaticcraft.armor.upgrade.mod2.other_upgrade"</li>
     * </ul>
     * @param id the upgrade ID, as returned by {@link #getID()}
     * @return a converted string
     */
    static String getStringKey(ResourceLocation id) {
        return UPGRADE_PREFIX +
                (id.getNamespace().equals(PneumaticRegistry.MOD_ID) ? id.getPath() : id.toString().replace(':', '.'));
    }
}
