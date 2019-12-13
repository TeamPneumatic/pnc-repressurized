package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;

import java.util.List;

/**
 * Just an interface to give access to GuiScreen#buttonList and GuiScreen#fontRenderer. An instance of this class may
 * safely be casted to {@link net.minecraft.client.gui.screen.Screen} if necessary.
 */
public interface IGuiScreen {
    /**
     * Add a new widget to the GUI
     * @param w the widget to add
     */
    <T extends Widget> T addWidget(T w);

    /**
     * Get a list of all widgets in this GUI.  Don't use this to add subwidgets; use {@link #addWidget(Widget)} instead.
     *
     * @return a list of widgets
     */
    List<Widget> getWidgetList();

    /**
     * Convenience method to get the GUI's font renderer.
     *
     * @return a font renderer
     */
    FontRenderer getFontRenderer();

    /**
     * Set the focus on a particular subwidget (generally a text field)
     */
    void setFocusedWidget(Widget w);
}
