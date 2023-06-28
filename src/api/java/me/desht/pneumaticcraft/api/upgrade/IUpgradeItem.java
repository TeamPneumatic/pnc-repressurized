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

package me.desht.pneumaticcraft.api.upgrade;

/**
 * Represents an item which can be used as a PneumaticCraft upgrade in machines or other items. You can implement this
 * interface on your own items, or you can use {@link IUpgradeRegistry#makeUpgradeItem(PNCUpgrade, int)} to create an
 * upgrade with default PneumaticCraft tooltip behaviour.
 * <p>
 * Items that you implement yourself should take a {@code Supplier&lt;PNCUpgrade&gt;} in their constructor, and
 * store that in a final field. A Supplier is needed because items are registered before PNCUpgrade objects.
 */
public interface IUpgradeItem {
    /**
     * Return the PNCUpgrade object associated with this item.
     *
     * @return the PNC upgrade
     */
    PNCUpgrade getUpgradeType();

    /**
     * Get the tier of this upgrade.
     *
     * @return the upgrade tier
     */
    default int getUpgradeTier() {
        return 1;
    }
}
