/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.pneumatic_armor;

import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/**
 * Provides access to the current state of the Pneumatic Armor worn by a player. This object is passed as a parameter
 * to some methods of {@link IArmorUpgradeHandler} and {@link me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler},
 * and can also be directly retrieved via {@link ICommonArmorRegistry#getCommonArmorHandler(Player)}.
 * <p>
 * You should avoid hanging on to instances of this across ticks, since it will become invalid if a player logs off or
 * changes dimension.
 */
public interface ICommonArmorHandler {
    /**
     * Get the player wearing this armor
     *
     * @return the player
     */
    Player getPlayer();

    /**
     * Get the number of the given upgrade installed in the given armor slot
     *
     * @param slot the equipment slot (must be one of HEAD, CHEST, LEGS or FEET)
     * @param upgrade the upgrade to query
     * @return the number of upgrades installed
     */
    int getUpgradeCount(EquipmentSlot slot, PNCUpgrade upgrade);

    /**
     * Convenience method to get the speed boost for the given armor piece, which is 1 + {number_of_speed_upgrades}
     * @param slot the equipment slot
     * @return the speed boost
     */
    int getSpeedFromUpgrades(EquipmentSlot slot);

    /**
     * Get the pressure for the armor piece in the given equipment slot.
     *
     * @param slot the equipment slot
     * @return the pressure of the armor piece, in bar
     */
    float getArmorPressure(EquipmentSlot slot);

    /**
     * Get the amount of air for the armor piece in the given equipment slot.
     *
     * @param slot the equipment slot
     * @return air, in mL
     */
    int getAir(EquipmentSlot slot);

    /**
     * Check that the armor in the given is above the minimum pressure limit to operate
     * @param slot the slot
     * @return true if the armor piece can function, false if not
     */
    boolean hasMinPressure(EquipmentSlot slot);

    /**
     * Add (or remove) air from the armor piece in the given slot.
     *
     * @param slot the slot
     * @param air amount to add (negative amounts remove air)
     * @return the previous pressure for the armor piece
     */
    float addAir(EquipmentSlot slot, int air);

    /**
     * Check if the armor master switch is enabled (i.e. core components are active)
     * @return true if enabled
     */
    boolean isArmorEnabled();

    /**
     * Check if the given upgrade handler is actually usable right now. Validates that the upgrade is installed,
     * and that the corresponding armor piece is initialized and has sufficient pressure.
     *
     * @param handler the upgrade handler to check
     * @param mustBeActive if true, the handler must be currently active
     * @return true if the upgrade is usable right now, false otherwise
     */
    boolean upgradeUsable(IArmorUpgradeHandler<?> handler, boolean mustBeActive);

    /**
     * Get the per-player extension data for the given upgrade, if any.
     * See {@link IArmorUpgradeHandler#extensionData()}.
     *
     * @param handler the armor upgrade handler
     * @param <T> handler type
     * @return the extension, or null if there is none for this type of upgrade
     */
    <T extends IArmorExtensionData> T getExtensionData(IArmorUpgradeHandler<T> handler);

    /**
     * Check if the given upgrade is currently enabled, i.e. has been switched on by the player.
     *
     * @param upgrade the upgrade to check
     * @return true if the upgrade is currently enabled, false otherwise
     */
    boolean isUpgradeEnabled(IArmorUpgradeHandler<?> upgrade);

    /**
     * Set the given upgrade enablement status. This can be called on either client or server, and will handle
     * sync'ing the enablement appropriately. This is a no-op if the status is already the same as the requested
     * new status, or if the upgrade isn't usable (i.e. not installed, armor disabled, out of air...)
     *
     * @param upgrade the upgrade to enable/disable
     * @param enabled the new enablement status
     */
    void setUpgradeEnabled(IArmorUpgradeHandler<?> upgrade, boolean enabled);

    /**
     * Convenience method to check if the armor item in the given slot is on (vanilla) cooldown. Note that the
     * method does not check if the item is actually a Pneumatic Armor piece; this method is typically called
     * from {@link IArmorUpgradeHandler#tick(ICommonArmorHandler, boolean)} where the armor is already known to
     * be Pneumatic Armor.
     *
     * @param slot the slot to check
     * @return true if the equipped item is on cooldown, false otherwise
     */
    boolean isOnCooldown(EquipmentSlot slot);
}
