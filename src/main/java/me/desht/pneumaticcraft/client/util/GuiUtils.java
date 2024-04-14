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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

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

    public static void renderBlockInGui(GuiGraphics graphics, BlockState block, float x, float y, float z, float rotate, float scale) {
        // FIXME lighting angle isn't right
        final Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(-scale, -scale, -scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30F));
        poseStack.translate(-0.5F, -0.5F, 0);


        poseStack.translate(0.5F, 0, -0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotate));
        poseStack.translate(-0.5F, 0, 0.5F);

        poseStack.translate(0, 0, -1);

        mc.getBlockRenderer().renderSingleBlock(block, poseStack, graphics.bufferSource(), RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);

        poseStack.popPose();
    }

    /**
     * Draw a fluid texture, tiling as appropriate
     * @param graphics the pose stack
     * @param bounds bounds in which to draw
     * @param fluidStack the fluid to draw
     * @param tank a fluid tank; if non-null, fluid Y size is scaled according to the tank's capacity
     */
    public static void drawFluid(GuiGraphics graphics, final Rect2i bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == Fluids.EMPTY) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidStill = renderProps.getStillTexture(fluidStack);
        if (fluidStill == null) {
            fluidStill = MissingTextureAtlasSprite.getLocation();
        }
        TextureAtlasSprite fluidStillSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidStill);

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        final int xTileCount = bounds.getWidth() / TEX_WIDTH;
        final int xRemainder = bounds.getWidth() - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getFluidType().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);
        int[] cols = RenderUtils.decomposeColor(renderProps.getTintColor(fluidStack));

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = bounds.getX() + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (bounds.getWidth() > 0 && h > 0) {
                    int maskTop = TEX_HEIGHT - h;
                    int maskRight = TEX_WIDTH - w;

                    drawFluidTexture(graphics, x, y, fluidStillSprite, maskTop, maskRight, 100, cols);
                }
            }
        }
        RenderSystem.disableBlend();
    }

    private static void drawFluidTexture(GuiGraphics graphics, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel, int[] cols) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16.0f * (uMax - uMin);
        vMax = vMax - maskTop / 16.0f * (vMax - vMin);

        Matrix4f posMat = graphics.pose().last().pose();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuilder();
        worldrenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        worldrenderer.vertex(posMat, xCoord, yCoord + 16, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMax).endVertex();
        worldrenderer.vertex(posMat,xCoord + 16 - maskRight, yCoord + 16, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMax).endVertex();
        worldrenderer.vertex(posMat, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMin).endVertex();
        worldrenderer.vertex(posMat, xCoord, yCoord + maskTop, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMin).endVertex();
        tessellator.end();
    }

    public static Rect2i showPopupHelpScreen(GuiGraphics graphics, Screen screen, Font fontRenderer, List<Component> helpText) {
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
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 400);
        drawPanel(graphics, 0, 0, boxHeight, boxWidth);

        int dy = 0;
        graphics.pose().scale(fontScale, fontScale, fontScale);
        for (FormattedCharSequence line : l) {
            graphics.drawString(fontRenderer, line, 0, dy, 0xFFE0E0E0, false);
            dy += lineSpacing;
            if (maxLines-- == 0) break;
        }
        graphics.pose().popPose();

        return bounds;
    }

    public static void drawPanel(GuiGraphics graphics, int x, int y, int panelHeight, int panelWidth) {
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y + panelHeight + 8, 0xC0000000);
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y - 3, 0xFF808080);
        graphics.fill(x - 4, y + panelHeight + 8, x + panelWidth + 8, y + panelHeight + 9, 0xFF808080);
        graphics.fill(x - 4, y - 4, x - 3, y + panelHeight + 8, 0xFF808080);
        graphics.fill(x + panelWidth + 8, y - 4, x + panelWidth + 9, y + panelHeight + 8, 0xFF808080);
    }

    public static void drawScaledText(GuiGraphics graphics, Font fr, Component text, int x, int y, int color, float scale, boolean dropShadow) {
        if (scale != 1.0f) {
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(x, y, 0);
            poseStack.scale(scale, scale, scale);
            graphics.drawString(fr, text, 0, 0, color, dropShadow);
            poseStack.popPose();
        } else {
            graphics.drawString(fr, text, x, y, color, dropShadow);
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
        return Arrays.stream(StringUtils.splitByWholeSeparator(I18n.get(key, params), TRANSLATION_LINE_BREAK))
                .map(Component::literal)
                .collect(Collectors.toList());
    }

    /**
     * Render a progressing line in GUI context
     * @param graphics the matrix stack
     * @param line the line to render
     * @param color line's colour
     */
    public static void renderProgressingLine2d(GuiGraphics graphics, ProgressingLine line, int color, float lineWidth) {
        int[] cols = RenderUtils.decomposeColor(color);
        float progress = line.getProgress();

        Matrix4f posMat = graphics.pose().last().pose();
        BufferBuilder wr = Tesselator.getInstance().getBuilder();
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        wr.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        wr.vertex(posMat, line.startX, line.startY, line.startZ)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        wr.vertex(posMat, lerp(progress, line.startX, line.endX), lerp(progress, line.startY, line.endY), lerp(progress, line.startZ,line.endZ))
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        Tesselator.getInstance().end();
    }
}
