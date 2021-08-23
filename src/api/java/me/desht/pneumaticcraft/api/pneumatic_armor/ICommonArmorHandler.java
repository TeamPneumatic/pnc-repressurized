package me.desht.pneumaticcraft.api.pneumatic_armor;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;

/**
 * Provides some methods for upgrade handlers to easily retrieve information about the player's equipped armor
 */
public interface ICommonArmorHandler {
    /**
     * Get the player wearing this armor
     *
     * @return the player
     */
    PlayerEntity getPlayer();

    /**
     * Get the number of the given upgrade installed in the given armor slot
     *
     * @param slot the equipment slot (must be one of HEAD, CHEST, LEGS or FEET)
     * @param upgrade the upgrade to query
     * @return the number of upgrades installed
     */
    int getUpgradeCount(EquipmentSlotType slot, EnumUpgrade upgrade);

    /**
     * Convenience method to get the speed boost for the given armor piece, which is 1 + {number_of_speed_upgrades}
     * @param slot the equipment slot
     * @return the speed boost
     */
    int getSpeedFromUpgrades(EquipmentSlotType slot);

    /**
     * Get the pressure for the armor piece in the given equipment slot.
     *
     * @param slot the equipment slot
     * @return the pressure of the armor piece, in bar
     */
    float getArmorPressure(EquipmentSlotType slot);

    /**
     * Check that the armor in the given is above the minimum pressure limit to operate
     * @param slot the slot
     * @return true if the armor piece can function, false if not
     */
    boolean hasMinPressure(EquipmentSlotType slot);

    /**
     * Add (or remove) air from the armor piece in the given slot.
     *
     * @param slot the slot
     * @param air amount to add (negative amounts remove air)
     * @return the previous pressure for the armor piece
     */
    float addAir(EquipmentSlotType slot, int air);

    /**
     * Check if the armor master switch is enabled (i.e. core components are active)
     * @return true if enabled
     */
    boolean isArmorEnabled();

    /**
     * CHeck if the given upgrade handler is actually usable right now. Validates that the upgrade is installed,
     * and that the corresponding armor piece is initialized and has sufficient pressure.
     *
     * @param handler the upgrade handler to check
     * @param mustBeActive if true, the handler must be currently active
     * @return true if the upgrade is usable right now, false otherwise
     */
    boolean upgradeUsable(IArmorUpgradeHandler<?> handler, boolean mustBeActive);

    /**
     * Get the per-player extension data for the given upgrade, if any
     * @param handler the armor upgrade handler
     * @param <T> handler type
     * @return the extension, or null if there is none for this type of upgrade
     */
    <T extends IArmorExtensionData> T getExtensionData(IArmorUpgradeHandler<T> handler);
}
