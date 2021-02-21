package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * For widgets that need to do some drawing after everything else, to ensure it's on top.
 * E.g. combo box can draw its drop-down list here
 */
@FunctionalInterface
public interface IDrawAfterRender {
    void renderAfterEverythingElse(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick);
}
