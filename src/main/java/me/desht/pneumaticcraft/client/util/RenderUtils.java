package me.desht.pneumaticcraft.client.util;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
    public static void glColorHex(int color, float brightness) {
        float alpha = (color >> 24 & 255) / 255F;
        float div = 255F / brightness;
        float red = (color >> 16 & 255) / div;
        float green = (color >> 8 & 255) / div;
        float blue = (color & 255) / div;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColorHex(int color, int alpha) {
        glColorHex(color | alpha << 24);
    }

    public static void render3DArrow() {
        GlStateManager.disableTexture2D();
        double arrowTipLength = 0.2;
        double arrowTipRadius = 0.25;
        double baseLength = 0.3;
        double baseRadius = 0.15;

        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
            wr.pos(sin, 0, cos).endVertex();
        }
        Tessellator.getInstance().draw();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
            wr.pos(sin, baseLength, cos).endVertex();
        }
        Tessellator.getInstance().draw();
        wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
            wr.pos(sin, 0, cos).endVertex();
            wr.pos(sin, baseLength, cos).endVertex();
        }
        Tessellator.getInstance().draw();

        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(0, baseLength + arrowTipLength, 0).endVertex();
        for (int i = 0; i < PneumaticCraftUtils.sin.length; i++) {
            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
            wr.pos(sin, baseLength, cos).endVertex();
        }
        wr.pos(0, baseLength, arrowTipRadius).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
    }
}
