package me.desht.pneumaticcraft.api.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

/**
 * Represents the common part of a Pneumatic Armor upgrade handler.  Singleton implementations of this are present
 * on both client and server.  See {@link IArmorUpgradeClientHandler} for the client-side handler (which has a
 * one-to-one mapping to this).
 */
public interface IArmorUpgradeHandler {

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
        return ArmorUpgradeRegistry.getStringKey(getID());
    }
}
