package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
    public static void glColorHex(int color, float brightness) {
        float alpha = (color >> 24 & 255) / 255F;
        float div = 255F / brightness;
        float red = (color >> 16 & 255) / div;
        float green = (color >> 8 & 255) / div;
        float blue = (color & 255) / div;
        GlStateManager.color4f(red, green, blue, alpha);
    }

    public static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        GlStateManager.color4f(red, green, blue, alpha);
    }

    public static void glColorHex(int color, int alpha) {
        glColorHex(color | alpha << 24);
    }

    public static void render3DArrow() {
        GlStateManager.disableTexture();
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
        GlStateManager.enableTexture();
    }

    public static void renderFrame(AxisAlignedBB aabb, double fw) {
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.maxZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.maxZ + fw));

        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.minY + fw, aabb.maxZ - fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ - fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.minY + fw, aabb.maxZ - fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ - fw));

        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ + fw));
    }

    private static void renderOffsetAABB(AxisAlignedBB aabb) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL);

        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).normal(0, 0, -1).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(0, 0, -1).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).normal(0, 0, -1).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.minZ).normal(0, 0, -1).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(0, 0, 1).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.minZ).normal(0, -1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).normal(0, -1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(0, -1, 0).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).normal(0, -1, 0).endVertex();

        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(0, 1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(0, 1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(0, 1, 0).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).normal(0, 1, 0).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).normal(-1, 0, 0).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(-1, 0, 0).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).normal(-1, 0, 0).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.minZ).normal(-1, 0, 0).endVertex();

        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).normal(1, 0, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(1, 0, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(1, 0, 0).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(1, 0, 0).endVertex();
        Tessellator.getInstance().draw();
    }

    /**
     * Rotates the render matrix dependant on the rotation of a block. Used in many render methods.
     *
     * @param facing block facing direction
     * @return the angle (in degrees) of resulting rotation around the Y axis
     */
    public static double rotateMatrixByMetadata(Direction facing) {
        float metaRotation;
        switch (facing) {
            case UP:
                metaRotation = 0;
                GlStateManager.rotated(90, 1, 0, 0);
                GlStateManager.translated(0, -1, -1);
                break;
            case DOWN:
                metaRotation = 0;
                GlStateManager.rotated(-90, 1, 0, 0);
                GlStateManager.translated(0, -1, 1);
                break;
            case NORTH:
                metaRotation = 0;
                break;
            case EAST:
                metaRotation = 90;
                break;
            case SOUTH:
                metaRotation = 180;
                break;
            default:
                metaRotation = 270;
                break;
        }
        GlStateManager.rotated(metaRotation, 0, 1, 0);
        return metaRotation;
    }

    public static void renderItemAt(ItemStack stack, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();

        GlStateManager.translated(x, y, z);
        Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
