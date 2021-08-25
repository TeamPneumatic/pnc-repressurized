package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Represents a checkbox widget which can have a key bound to toggle it; use this to create a checkbox to toggle
 * a toggleable upgrade on & off.
 * <p>
 * Do not implement this class yourself; you can create & retrieve instances of it via
 * {@link IPneumaticHelmetRegistry#makeKeybindingCheckBox(ResourceLocation, int, int, int, Consumer)}.
 */
public interface ICheckboxWidget {
    /**
     * Is this checkbox currently checked?
     *
     * @return true if checked, false if not
     */
    boolean isChecked();

    /**
     * Convenience method to cast this to a Widget, suitable for passing to {@link IGuiScreen#addWidget(Widget)}, or
     * calling other Widget methods on it.
     *
     * @return this checkbox as a Widget
     */
    default Widget asWidget() {
        return (Widget) this;
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
