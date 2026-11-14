/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.util;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.util.Mth.lerp;

public class GuiUtils {
    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    public static final String TRANSLATION_LINE_BREAK = "\\n";

    public static void renderBlockInGui(GuiGraphicsExtractor graphics, BlockState block, float x, float y, float z, float rotate, float scale) {
        // TODO 26.1 render: 3D blocks in GUIs need a PictureInPictureRenderer now (only used by the JEI heat properties category)
    }

    /**
     * Draw a fluid texture, tiling as appropriate
     * @param graphics the pose stack
     * @param bounds bounds in which to draw
     * @param fluidStack the fluid to draw
     * @param tank a fluid tank; if non-null, fluid Y size is scaled according to the tank's capacity
     */
    public static void drawFluid(GuiGraphicsExtractor graphics, final Rect2i bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == Fluids.EMPTY) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite fluidStillSprite = fluidModel.stillMaterial().sprite();

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        final int xTileCount = bounds.getWidth() / TEX_WIDTH;
        final int xRemainder = bounds.getWidth() - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getFluidType().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);
        int tint = fluidModel.fluidTintSource() == null ? 0xFFFFFFFF : fluidModel.fluidTintSource().colorAsStack(fluidStack);
        if ((tint >>> 24) == 0) tint |= 0xFF000000;
        final int color = tint;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = bounds.getX() + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (bounds.getWidth() > 0 && h > 0) {
                    int maskTop = TEX_HEIGHT - h;
                    int maskRight = TEX_WIDTH - w;

                    drawFluidTexture(graphics, x, y, fluidStillSprite, maskTop, maskRight, color);
                }
            }
        }
    }

    private static void drawFluidTexture(GuiGraphicsExtractor graphics, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, int color) {
        float uMin = textureSprite.getU0();
        float vMin = textureSprite.getV0();
        float uMax0 = textureSprite.getU1();
        float vMax0 = textureSprite.getV1();
        float uMax = uMax0 - maskRight / 16.0f * (uMax0 - uMin);
        float vMax = vMax0 - maskTop / 16.0f * (vMax0 - vMin);

        GuiVertexRenderState.submit(graphics, RenderPipelines.GUI_TEXTURED, GuiVertexRenderState.textureSetup(textureSprite.atlasLocation()), b -> b
                .vertex(xCoord, yCoord + maskTop, uMin, vMin, color)
                .vertex(xCoord, yCoord + 16, uMin, vMax, color)
                .vertex(xCoord + 16 - maskRight, yCoord + 16, uMax, vMax, color)
                .vertex(xCoord + 16 - maskRight, yCoord + maskTop, uMax, vMin, color)
        );
    }

    public static Rect2i showPopupHelpScreen(GuiGraphicsExtractor graphics, Screen screen, Font fontRenderer, List<Component> helpText) {
        List<FormattedCharSequence> l = GuiUtils.wrapTextComponentList(helpText, screen.width / 2, fontRenderer);
        int lineSpacing = fontRenderer.lineHeight + 1;
        int boxHeight = l.size() * lineSpacing;
        int maxLines = boxHeight / lineSpacing;
        int boxWidth = l.stream().max(Comparator.comparingInt(fontRenderer::width)).map(fontRenderer::width).orElse(0);

        float fontScale = 1f;
        while (boxHeight > screen.height - 5) {
            boxHeight /= 2;
            boxWidth /= 2;
            fontScale /= 2;
        }

        int x, y;
        if (screen instanceof AbstractContainerScreen<?> a) {
            x = (a.getXSize() - boxWidth) / 2;
            y = (a.getYSize() - boxHeight) / 2;
        } else {
            x = (screen.width - boxWidth) / 2;
            y = (screen.height - boxHeight) / 2;
        }
        Rect2i bounds = new Rect2i(x, y, boxWidth, boxHeight);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        drawPanel(graphics, 0, 0, boxHeight, boxWidth);

        int dy = 0;
        graphics.pose().scale(fontScale, fontScale);
        for (FormattedCharSequence line : l) {
            graphics.text(fontRenderer, line, 0, dy, 0xFFE0E0E0, false);
            dy += lineSpacing;
            if (maxLines-- == 0) break;
        }
        graphics.pose().popMatrix();

        return bounds;
    }

    public static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int panelHeight, int panelWidth) {
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y + panelHeight + 8, 0xC0000000);
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y - 3, 0xFF808080);
        graphics.fill(x - 4, y + panelHeight + 8, x + panelWidth + 8, y + panelHeight + 9, 0xFF808080);
        graphics.fill(x - 4, y - 4, x - 3, y + panelHeight + 8, 0xFF808080);
        graphics.fill(x + panelWidth + 8, y - 4, x + panelWidth + 9, y + panelHeight + 8, 0xFF808080);
    }

    /**
     * Text colours without an alpha component used to be drawn opaque by the font renderer, but
     * {@link GuiGraphicsExtractor#text} now skips text with zero alpha. Same rule as the old {@code Font#adjustColor}.
     * @param color an RGB or ARGB colour
     * @return the colour, made opaque if it had (almost) no alpha
     */
    public static int textColor(int color) {
        return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
    }

    public static void drawScaledText(GuiGraphicsExtractor graphics, Font fr, Component text, int x, int y, int color, float scale, boolean dropShadow) {
        if (scale != 1.0f) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.text(fr, text, 0, 0, textColor(color), dropShadow);
            graphics.pose().popMatrix();
        } else {
            graphics.text(fr, text, x, y, textColor(color), dropShadow);
        }
    }

    public static List<FormattedCharSequence> wrapTextComponentList(List<Component> text, int maxWidth, Font font) {
        ImmutableList.Builder<FormattedCharSequence> builder = ImmutableList.builder();
        for (Component line : text) {
            builder.addAll(ComponentRenderUtils.wrapComponents(line, maxWidth, font));
        }
        return builder.build();
    }

    public static List<Component> xlateAndSplit(String key, Object... params) {
        return Arrays.stream(StringUtils.splitByWholeSeparator(Component.translatable(key, params).getString(), TRANSLATION_LINE_BREAK))
                .map(Component::literal)
                .collect(Collectors.toList());
    }

    /**
     * Render a progressing line in GUI context
     * @param graphics the matrix stack
     * @param line the line to render
     * @param color line's colour
     * @param lineWidth line width, in screen pixels
     */
    public static void renderProgressingLine2d(GuiGraphicsExtractor graphics, ProgressingLine line, int color, float lineWidth) {
        float progress = line.getProgress();

        GuiVertexRenderState.submit(graphics, b -> b.line(line.startX, line.startY,
                lerp(progress, line.startX, line.endX), lerp(progress, line.startY, line.endY), GuiVertexRenderState.pixels(lineWidth), color));
    }

    public static void drawPlayerSlots(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.GUI_SLOTS_9x3, x, y, 0, 0, 162, 54, 162, 54);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.GUI_SLOTS_9x1, x, y + 58, 0, 0, 162, 18, 162, 18);
    }

    public static void drawInsetPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        GuiUtils.drawNineSliced(graphics, Textures.GUI_INSET_PANEL, x, y, 0, width, height, 256, 128, 1);
    }

    public static void drawNineSliced(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int z, int width, int height, int texWidth, int texHeight, int corner) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);

        int centreWidth = texWidth - corner * 2;
        int centreHeight = texHeight - corner * 2;

        GuiVertexRenderState.submit(graphics, RenderPipelines.GUI_TEXTURED, GuiVertexRenderState.textureSetup(texture), wr -> {
            // corners
            addQuad(wr, 0, 0, 0, 0, corner, corner);
            addQuad(wr, width - corner, 0, texWidth - corner, 0, corner, corner);
            addQuad(wr, 0, height - corner, 0, texHeight - corner, corner, corner);
            addQuad(wr, width - corner, height - corner, texWidth - corner, texHeight - corner, corner, corner);

            // top & bottom edges
            for (int i = 0; i <= width / centreWidth; i++) {
                int x1 = corner + centreWidth * i;
                int w1 = Math.min(centreWidth, width - (i * centreWidth) - (corner * 2));
                addQuad(wr, x1, 0, corner, 0, w1, corner);
                addQuad(wr, x1, height - corner, corner, texHeight - corner, w1, corner);
            }
            // left & right edges
            for (int i = 0; i <= height / centreHeight; i++) {
                int y1 = corner + centreHeight * i;
                int h1 = Math.min(centreHeight, height - (i * centreHeight) - (corner * 2));
                addQuad(wr, 0, y1, 0, corner, corner, h1);
                addQuad(wr, width - corner, y1, texWidth - corner, corner, corner, h1);
            }

            // centre
            int cw = (width - (corner * 2)) / texWidth;
            int ch = (height - (corner * 2)) / texHeight;
            for (int ix = 0; ix <= cw; ix++) {
                for (int iy = 0; iy <= ch; iy++) {
                    int x1 = corner + (ix * centreWidth);
                    int y1 = corner + (iy * centreHeight);
                    int w1 = Math.min(centreWidth, width - (ix * centreWidth) - (corner * 2));
                    int h1 = Math.min(centreHeight, height - (iy * centreHeight) - (corner * 2));
                    addQuad(wr, x1, y1, corner, corner, w1, h1);
                }
            }
        });

        graphics.pose().popMatrix();
    }

    public static void addQuad(GuiVertexRenderState.Builder wr, float x, float y, float texX, float texY, float w, float h) {
        float u1 = texX / 256F;
        float u2 = u1 + (w / 256F);
        float v1 = texY / 256F;
        float v2 = v1 + (h / 256F);
        wr.vertex(x, y, u1, v1, 0xFFFFFFFF);
        wr.vertex(x, y + h, u1, v2, 0xFFFFFFFF);
        wr.vertex(x + w, y + h, u2, v2, 0xFFFFFFFF);
        wr.vertex(x + w, y, u2, v1, 0xFFFFFFFF);
    }

    public static void drawScreenWithTitleArea(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int titleColor) {
        GuiUtils.drawNineSliced(graphics, Textures.GUI_BLANK_256, x, y, 0, w, h, 256, 256, 4);
        graphics.horizontalLine(x + 3, x + w - 4, y + 15, 0xFF555555);
        graphics.horizontalLine(x + 3, x + w - 4, y + 16, 0xFFFFFFFF);
        graphics.fill(x + 3, y + 3, x + w - 3, y + 15, 0xFFDDD7BA);
    }
}
