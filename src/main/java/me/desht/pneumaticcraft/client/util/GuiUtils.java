package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GuiUtils {
    private static final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public static void drawItemStack(ItemStack stack, int x, int y) {
        RenderSystem.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepthTest();
        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        RenderHelper.disableStandardItemLighting();
        RenderSystem.disableRescaleNormal();
    }

    public static void drawItemStack(@Nonnull ItemStack stack, int x, int y, String text) {
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableDepthTest();
        RenderHelper.enableStandardItemLighting();
        RenderSystem.pushMatrix();
        FontRenderer font = null;
        if (!stack.isEmpty()) font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = Minecraft.getInstance().fontRenderer;
        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, text);
        RenderSystem.popMatrix();
        RenderHelper.disableStandardItemLighting();
        RenderSystem.disableRescaleNormal();
    }

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;

    public static void drawFluid(final Rectangle2d bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        ResourceLocation fluidStill = fluid.getAttributes().getStillTexture(fluidStack);
        TextureAtlasSprite fluidStillSprite = fluidStill != null ?
                Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(fluidStill) :
                null;

        int fluidColor = fluid.getAttributes().getColor(fluidStack);

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        glColorHex(fluidColor);

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
        RenderSystem.disableBlend();
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        float uMin = textureSprite.getMinU();
        float uMax = textureSprite.getMaxU();
        float vMin = textureSprite.getMinV();
        float vMax = textureSprite.getMaxV();
        uMax = uMax - maskRight / 16.0f * (uMax - uMin);
        vMax = vMax - maskTop / 16.0f * (vMax - vMin);

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
        RenderSystem.translated(0, 0, 400);
        AbstractGui.fill(x - 4, y - 4, x + boxWidth + 8, y + boxHeight + 8, 0xC0000000);
        AbstractGui.fill(x - 4, y - 4, x + boxWidth + 8, y - 3, 0xFF808080);
        AbstractGui.fill(x - 4, y + boxHeight + 8, x + boxWidth + 8, y + boxHeight + 9, 0xFF808080);
        AbstractGui.fill(x - 4, y - 4, x - 3, y + boxHeight + 8, 0xFF808080);
        AbstractGui.fill(x + boxWidth + 8, y - 4, x + boxWidth + 9, y + boxHeight + 8, 0xFF808080);

        for (String s : helpText) {
            fontRenderer.drawString(s, x, y, 0xFFE0E0E0);
            y += fontRenderer.FONT_HEIGHT;
        }
        RenderSystem.translated(0, 0, -400);
    }

    public static void drawTexture(ResourceLocation texture, int x, int y) {
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        RenderSystem.enableTexture();
//        RenderSystem.color4f(1, 1, 1, 1);
        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        builder.pos(x, y + 16, 0).color(255, 255, 255, 255).tex(0.0f, 1.0f).endVertex();
        builder.pos(x + 16, y + 16, 0).color(255, 255, 255, 255).tex(1.0f, 1.0f).endVertex();
        builder.pos(x + 16, y, 0).color(255, 255, 255, 255).tex(1.0f, 0.0f).endVertex();
        builder.pos(x, y, 0).color(255, 255, 255, 255).tex(0.0f, 0.0f).endVertex();
        Tessellator.getInstance().draw();
    }

    public static void drawUntexturedQuad(BufferBuilder renderer, double x, double y, double z, double width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width,  y, z).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    public static void drawOutline(BufferBuilder renderer, double x, double y, double z, double width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width,  y, z).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    public static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public static void glColorHex(int color, int alpha) {
        glColorHex(color | alpha << 24);
    }
}
