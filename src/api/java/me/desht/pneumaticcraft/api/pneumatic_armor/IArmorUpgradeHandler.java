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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Represents the common part of a Pneumatic Armor upgrade handler.  Singleton implementations of this are present
 * on both client and server.  See {@link IArmorUpgradeClientHandler} for the client-side handler (which has a
 * one-to-one mapping to this).
 * It is recommended to extend {@link BaseArmorUpgradeHandler} rather than implement this interface directly.
 */
public interface IArmorUpgradeHandler<T extends IArmorExtensionData> {
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
     * Used internally for quick lookup. Don't use this directly; use {@link #getID()} for a unique identifier.
     *
     * @return the internal numeric index for this upgrade
     */
    @ApiStatus.Internal
    int getIndex();

    /**
     * Used internally for quick lookup. Set once when this upgrade is registered; do not attempt to change this!
     *
     * @param index the internal numeric index for this upgrade
     */
    @ApiStatus.Internal
    void setIndex(int index);

    /**
     * Return the upgrades that are required to be in the armor piece to enable this module.
     *
     * @return an array of required upgrades
     */
    PNCUpgrade[] getRequiredUpgrades();

    /**
     * Get the maximum number of the given upgrade which may be installed.
     *
     * @param upgrade an upgrade
     * @return the maximum installable amount of this upgrade
     */
    default int getMaxInstallableUpgrades(PNCUpgrade upgrade) {
        return 1;
    }

    /**
     * Returns the usage in mL/tick when this upgrade handler is enabled.  Note this is constant usage just from the
     * upgrade being switched on, and does not take into account air used when some action is taken, e.g. flying with
     * the Jet Boots upgrade is not included here, but air used by the Entity Tracker upgrade is included.
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
    EquipmentSlot getEquipmentSlot();

    /**
     * Get a translation key for this upgrade, for text display purposes.
     *
     * @return a translation key
     */
    default String getTranslationKey() {
        return getStringKey(getID());
    }

    /**
     * Called every tick for a player when this upgrade is installed in their armor
     * @param commonArmorHandler the armor handler object
     * @param enabled true if the upgrade is currently enabled, false otherwise
     */
    default void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {}

    /**
     * Called when the armor is initialising and this upgrade is installed.
     *
     * @param commonArmorHandler the armor handler object
     */
    default void onInit(ICommonArmorHandler commonArmorHandler) {
    }

    /**
     * Called when an upgrade is toggled on/off by the player
     * @param commonArmorHandler the armor handler object
     * @param newState the new state of the upgrade
     */
    default void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
    }

    /**
     * Called when the armor is removed; carry out any necessary shutdown tasks here
     * @param commonArmorHandler the armor handler object
     */
    default void onShutdown(ICommonArmorHandler commonArmorHandler) {
    }

    /**
     * Called on both client and server when some NBT data is changed in this upgrade's armor itemstack. Can be used
     * to cache heavily-accessed NBT data for performance reasons.
     *
     * @param commonArmorHandler the armor handler object
     * @param tagName the NBT tag name
     * @param inbt the NBT data
     */
    default void onDataFieldUpdated(ICommonArmorHandler commonArmorHandler, String tagName, Tag inbt) {
    }

    /**
     * Set up player-specific extension data for this armor upgrade; since armor upgrade handlers are singleton objects,
     * any player-specific data needs to be stored separately. If your handler needs this (most don't), override this
     * method to return a supplier of a new instance of some class that implements {@link IArmorExtensionData}. This
     * data will be stored in the common armor handler for the player, and can be retrieved with
     * {@link ICommonArmorHandler#getExtensionData(IArmorUpgradeHandler)}.
     *
     * @return a supplier for the extension data for this upgrade &amp; player; supply null if there is none
     */
    @Nonnull
    default Supplier<T> extensionData() {
        return () -> null;
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
     * @return a dot-separated string key, suitable for a translation key or keybind name
     */
    static String getStringKey(ResourceLocation id) {
        return UPGRADE_PREFIX +
                (id.getNamespace().equals(PneumaticRegistry.MOD_ID) ? id.getPath() : id.toString().replace(':', '.'));
    }
}
