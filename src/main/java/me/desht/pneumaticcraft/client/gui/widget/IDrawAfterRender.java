package me.desht.pneumaticcraft.client.gui.widget;

/**
 * For widgets that need to do some drawing after everything else, to ensure it's on top.
 * E.g. combo box can draw its drop-down list here
 */
public interface IDrawAfterRender {
    void renderAfterEverythingElse(int mouseX, int mouseY, float partialTick);
}
