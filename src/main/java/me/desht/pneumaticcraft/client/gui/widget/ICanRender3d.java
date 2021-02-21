package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;

/**
 * Implement this on widgets which need to render in a 3d context (i.e. Pneumatic Armor 3d renderer)
 */
@FunctionalInterface
public interface ICanRender3d {
    void render3d(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks);
}
