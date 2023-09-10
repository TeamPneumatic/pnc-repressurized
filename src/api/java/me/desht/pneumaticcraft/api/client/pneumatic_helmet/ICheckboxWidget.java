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

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * Represents a checkbox widget which can have a key bound to toggle it; use this to create a checkbox to toggle
 * a toggleable upgrade on &amp; off.
 * <p>
 * Do not implement this class yourself; you can create and retrieve instances of it via
 * {@link IClientArmorRegistry#makeKeybindingCheckBox(ResourceLocation, int, int, int, Consumer)}. This would
 * typically be done in {@link IOptionPage#populateGui(IGuiScreen)}.
 */
public interface ICheckboxWidget {
    /**
     * Is this checkbox currently checked?
     *
     * @return true if checked, false if not
     */
    boolean isChecked();

    /**
     * Convenience method to cast this to a Widget, suitable for passing to {@link IGuiScreen#addWidget(AbstractWidget)},
     * or calling other Widget methods on it.
     *
     * @return this checkbox as a Widget
     */
    default AbstractWidget asWidget() {
        return (AbstractWidget) this;
    }

    /**
     * Get the upgrade ID for this checkbox; the upgrade that is toggled when the checkbox is clicked.
     * @return the upgrade ID
     */
    default ResourceLocation getUpgradeId() {
        return null;
    }

    /**
     * Set the upgrade ID of the owning upgrade. Use this for sub-controls, e.g. the builder mode setting on jet boots.
     * @param owningId the upgrade ID of the owning upgrade
     * @return this widget, for fluency
     */
    default ICheckboxWidget withOwnerUpgradeID(ResourceLocation owningId) {
        return null;
    }
}
