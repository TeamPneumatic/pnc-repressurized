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
}
