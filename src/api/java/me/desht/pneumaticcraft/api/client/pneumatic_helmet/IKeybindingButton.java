package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

/**
 * Represents a button in a options screen which can be used to rebind a key.  Don't implement this; use
 * {@link IPneumaticHelmetRegistry#makeKeybindingButton(int, KeyBinding)} to get an instance of a keybinding
 * button widget.
 * <p>
 * See also {@link IOptionPage#getKeybindingButton()}, which should be overridden to return this button instance
 * if it exists in this screen.
 */
public interface IKeybindingButton {
    boolean receiveKey(InputMappings.Type type, int keyCode);

    void receiveKeyReleased();

    /**
     * Convenience method to get this button as a widget, suitable for passing to {@link IGuiScreen#addWidget(Widget)}
     * @return this keybinding button, as a vanilla widget
     */
    default Widget asWidget() { return (Widget) this; }
}
