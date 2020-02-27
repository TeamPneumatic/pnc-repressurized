package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuiUtils {
    private static final HashMap<String, ResourceLocation> resourceMap = new HashMap<>();
    private static final int CIRCLE_POINTS = 500;
    public static final double PRESSURE_GAUGE_RADIUS = 20D;
    private static final double START_ANGLE = 240D / 180D * Math.PI;
    private static final double STOP_ANGLE = -60D / 180D * Math.PI;
    private static final int GAUGE_POINTS = (int) ((START_ANGLE - STOP_ANGLE) / (2D * Math.PI) * CIRCLE_POINTS);
    private static final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public static void drawPressureGauge(FontRenderer fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        drawPressureGauge(fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0xFF000000);
    }

    public static void drawPressureGauge(FontRenderer fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        GlStateManager.disableTexture();
        GlStateManager.lineWidth(2.0F);
        // Draw the green and red surface in the gauge.
        GlStateManager.color4f(0.7F, 0, 0, 1);
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(xPos, yPos, 0.0).endVertex();
        // System.out.println("gauge points: "+ GAUGE_POINTS);
        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        boolean changedColorGreen = false;
        boolean changedColorYellow = false;
        for (int i = 0; i < GAUGE_POINTS; i++) {
            if (i == explodeBoundary && !changedColorGreen) {
                Tessellator.getInstance().draw();
                if (minWorkingPressure < 0 && minWorkingPressure >= -1) {
                    GlStateManager.color4f(0.9F, 0.9F, 0, 1);
                } else {
                    GlStateManager.color4f(0, 0.7F, 0, 1);
                }
                wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
                wr.pos(xPos, yPos, 0.0).endVertex();
                i--;
                changedColorGreen = true;
            }
            if (i == workingBoundary && !changedColorYellow) {
                Tessellator.getInstance().draw();
                if (minWorkingPressure < 0 && minWorkingPressure >= -1) {
                    GlStateManager.color4f(0, 0.7F, 0, 1);
                } else {
                    GlStateManager.color4f(0.9F, 0.9F, 0, 1);
                }
                wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
                wr.pos(xPos, yPos, 0.0).endVertex();
                i--;
                changedColorYellow = true;
            }
            double angle = (double) -i / (double) CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
            wr.pos(Math.cos(angle) * PRESSURE_GAUGE_RADIUS + xPos, Math.sin(angle) * PRESSURE_GAUGE_RADIUS + yPos, 0.0).endVertex();
        }
        Tessellator.getInstance().draw();

        float fgR = (float)(fgColor >> 16 & 255) / 255.0F;
        float fgB = (float)(fgColor >> 8 & 255) / 255.0F;
        float fgG = (float)(fgColor & 255) / 255.0F;
        float fgA = (float)(fgColor >> 24 & 255) / 255.0F;

        // Draw the black surrounding circle
        GlStateManager.color4f(fgR, fgG, fgB, fgA);

        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = (double) i / (double) CIRCLE_POINTS * 2D * Math.PI;
            wr.pos(Math.cos(angle) * PRESSURE_GAUGE_RADIUS + xPos, Math.sin(angle) * PRESSURE_GAUGE_RADIUS + yPos, 0.0).endVertex();
        }
        Tessellator.getInstance().draw();

        // Draw the scale
        int currentScale = (int) maxPressure;
        List<int[]> textScalers = new ArrayList<>();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            double angle = (double) -i / (double) CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                textScalers.add(new int[]{currentScale, (int) (Math.cos(angle) * PRESSURE_GAUGE_RADIUS * 1.3D), (int) (Math.sin(angle) * PRESSURE_GAUGE_RADIUS * 1.3D)});
                currentScale--;
                // System.out.println("curr: "+ currentScale);
                wr.pos(Math.cos(angle) * PRESSURE_GAUGE_RADIUS * 0.9D + xPos, Math.sin(angle) * PRESSURE_GAUGE_RADIUS * 0.9D + yPos, 0.0).endVertex();
                wr.pos(Math.cos(angle) * PRESSURE_GAUGE_RADIUS * 1.1D + xPos, Math.sin(angle) * PRESSURE_GAUGE_RADIUS * 1.1D + yPos, 0.0).endVertex();

            }
        }
        Tessellator.getInstance().draw();

        // Draw the needle.
        GlStateManager.color4f(fgR, fgG, fgB, fgA);
        double angleIndicator = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        angleIndicator = -angleIndicator / CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        wr.pos(Math.cos(angleIndicator + 0.89D * Math.PI) * PRESSURE_GAUGE_RADIUS * 0.3D + xPos, Math.sin(angleIndicator + 0.89D * Math.PI) * PRESSURE_GAUGE_RADIUS * 0.3D + yPos, 0.0).endVertex();
        wr.pos(Math.cos(angleIndicator + 1.11D * Math.PI) * PRESSURE_GAUGE_RADIUS * 0.3D + xPos, Math.sin(angleIndicator + 1.11D * Math.PI) * PRESSURE_GAUGE_RADIUS * 0.3D + yPos, 0.0).endVertex();
        wr.pos(Math.cos(angleIndicator) * PRESSURE_GAUGE_RADIUS * 0.8D + xPos, Math.sin(angleIndicator) * PRESSURE_GAUGE_RADIUS * 0.8D + yPos, 0.0).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.enableTexture();

        // draw the numbers next to the scaler.
        while (textScalers.size() > 10) {
            int divider = textScalers.size() / 5;
            for (int i = textScalers.size() - 1; i >= 0; i--) {
                if (i % divider != 0) textScalers.remove(i);
            }
        }
        for (int[] scaler : textScalers) {
            GlStateManager.pushMatrix();
            GlStateManager.translated(xPos + scaler[1] - 1.5, yPos + scaler[2] - 1.5, 0);
            GlStateManager.scaled(0.5, 0.5, 1);
            fontRenderer.drawString("" + scaler[0], 0, 0, fgColor);
            GlStateManager.popMatrix();
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawItemStack(ItemStack stack, int x, int y) {
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepthTest();
        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;

    public static void drawFluid(final Rectangle2d bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        AtlasTexture textureMapBlocks = Minecraft.getInstance().getTextureMap();
        ResourceLocation fluidStill = fluid.getAttributes().getStill(fluidStack);
        TextureAtlasSprite fluidStillSprite = null;
        if (fluidStill != null) {
            fluidStillSprite = textureMapBlocks.getSprite(fluidStill);
        }

        int fluidColor = fluid.getAttributes().getColor(fluidStack);

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        RenderUtils.glColorHex(fluidColor, 255);

        final int xTileCount = bounds.getWidth() / TEX_WIDTH;
        final int xRemainder = bounds.getWidth() - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getAttributes().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = bounds.getX() + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (bounds.getWidth() > 0 && h > 0) {
                    int maskTop = TEX_HEIGHT - h;
                    int maskRight = TEX_WIDTH - w;

                    drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 100);
                }
            }
        }
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - maskRight / 16.0 * (uMax - uMin);
        vMax = vMax - maskTop / 16.0 * (vMax - vMin);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        worldrenderer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        worldrenderer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        worldrenderer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    public static void showPopupHelpScreen(Screen screen, FontRenderer fontRenderer, List<String> helpText) {
        int boxWidth = 0;
        int boxHeight = helpText.size() * fontRenderer.FONT_HEIGHT;
        for (String s : helpText) {
            boxWidth = Math.max(boxWidth, fontRenderer.getStringWidth(s));
        }

        int x, y;
        if (screen instanceof ContainerScreen) {
            x = (((ContainerScreen) screen).getXSize() - boxWidth) / 2;
            y = (((ContainerScreen) screen).getYSize() - boxHeight) / 2;
        } else {
            x = (screen.width - boxWidth) / 2;
            y = (screen.height - boxHeight) / 2;
        }
        GlStateManager.translated(0, 0, 400);
        AbstractGui.fill(x - 4, y - 4, x + boxWidth + 8, y + boxHeight + 8, 0xC0000000);
        AbstractGui.fill(x - 4, y - 4, x + boxWidth + 8, y - 3, 0xFF808080);
        AbstractGui.fill(x - 4, y + boxHeight + 8, x + boxWidth + 8, y + boxHeight + 9, 0xFF808080);
        AbstractGui.fill(x - 4, y - 4, x - 3, y + boxHeight + 8, 0xFF808080);
        AbstractGui.fill(x + boxWidth + 8, y - 4, x + boxWidth + 9, y + boxHeight + 8, 0xFF808080);

        for (String s : helpText) {
            fontRenderer.drawString(s, x, y, 0xFFE0E0E0);
            y += fontRenderer.FONT_HEIGHT;
        }
        GlStateManager.translated(0, 0, -300);
    }

    public static void drawTexture(ResourceLocation texture, int x, int y) {
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        GlStateManager.enableTexture();
        GlStateManager.color4f(1, 1, 1, 1);
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + 16, 0).tex(0.0, 1.0).endVertex();
        wr.pos(x + 16, y + 16, 0).tex(1.0, 1.0).endVertex();
        wr.pos(x + 16, y, 0).tex(1.0, 0.0).endVertex();
        wr.pos(x, y, 0).tex(0.0, 0.0).endVertex();
        Tessellator.getInstance().draw();
    }
}
