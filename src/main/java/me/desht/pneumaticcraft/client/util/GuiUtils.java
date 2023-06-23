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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
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
    private static final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    public static final String TRANSLATION_LINE_BREAK = "${br}";

    public static void renderBlockInGui(PoseStack matrixStack, BlockState block, float x, float y, float z, float rotate, float scale) {
        // FIXME lighting angle isn't right
        final Minecraft mc = Minecraft.getInstance();
        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        matrixStack.scale(scale, -scale, scale);
        matrixStack.translate(-0.5F, -1F, 0);

        matrixStack.mulPose(Axis.XP.rotationDegrees(30F));

        matrixStack.translate(0.5F, 0, -0.5F);
        matrixStack.mulPose(Axis.YP.rotationDegrees(rotate));
        matrixStack.translate(-0.5F, 0, 0.5F);

        matrixStack.translate(0, 0, -1);

        bindTexture(InventoryMenu.BLOCK_ATLAS);
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        mc.getBlockRenderer().renderSingleBlock(block, matrixStack, buffers, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
        buffers.endBatch();

        matrixStack.popPose();
    }

    /**
     * Draw a fluid texture, tiling as appropriate
     * @param poseStack the pose stack
     * @param bounds bounds in which to draw
     * @param fluidStack the fluid to draw
     * @param tank a fluid tank; if non-null, fluid Y size is scaled according to the tank's capacity
     */
    public static void drawFluid(PoseStack poseStack, final Rect2i bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidStill = renderProps.getStillTexture(fluidStack);
        if (fluidStill == null) fluidStill = MissingTextureAtlasSprite.getLocation();
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

                    drawFluidTexture(poseStack, x, y, fluidStillSprite, maskTop, maskRight, 100, cols);
                }
            }
        }
        RenderSystem.disableBlend();
    }

    private static void drawFluidTexture(PoseStack matrixStack, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel, int[] cols) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16.0f * (uMax - uMin);
        vMax = vMax - maskTop / 16.0f * (vMax - vMin);

        Matrix4f posMat = matrixStack.last().pose();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuilder();
        worldrenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        worldrenderer.vertex(posMat, xCoord, yCoord + 16, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMax).endVertex();
        worldrenderer.vertex(posMat,xCoord + 16 - maskRight, yCoord + 16, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMax).endVertex();
        worldrenderer.vertex(posMat, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMin).endVertex();
        worldrenderer.vertex(posMat, xCoord, yCoord + maskTop, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMin).endVertex();
        tessellator.end();
    }

    public static Rect2i showPopupHelpScreen(PoseStack matrixStack, Screen screen, Font fontRenderer, List<Component> helpText) {
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
        if (screen instanceof AbstractContainerScreen a) {
            x = (a.getXSize() - boxWidth) / 2;
            y = (a.getYSize() - boxHeight) / 2;
        } else {
            x = (screen.width - boxWidth) / 2;
            y = (screen.height - boxHeight) / 2;
        }
        Rect2i bounds = new Rect2i(x, y, boxWidth, boxHeight);
        matrixStack.pushPose();
        matrixStack.translate(x, y, 400);
        drawPanel(matrixStack, 0, 0, boxHeight, boxWidth);

        int dy = 0;
        matrixStack.scale(fontScale, fontScale, fontScale);
        for (FormattedCharSequence line : l) {
            fontRenderer.draw(matrixStack, line, 0, dy, 0xFFE0E0E0);
            dy += lineSpacing;
            if (maxLines-- == 0) break;
        }
        matrixStack.popPose();

        return bounds;
    }

    public static void drawPanel(PoseStack matrixStack, int x, int y, int panelHeight, int panelWidth) {
        GuiComponent.fill(matrixStack,x - 4, y - 4, x + panelWidth + 8, y + panelHeight + 8, 0xC0000000);
        GuiComponent.fill(matrixStack,x - 4, y - 4, x + panelWidth + 8, y - 3, 0xFF808080);
        GuiComponent.fill(matrixStack,x - 4, y + panelHeight + 8, x + panelWidth + 8, y + panelHeight + 9, 0xFF808080);
        GuiComponent.fill(matrixStack,x - 4, y - 4, x - 3, y + panelHeight + 8, 0xFF808080);
        GuiComponent.fill(matrixStack,x + panelWidth + 8, y - 4, x + panelWidth + 9, y + panelHeight + 8, 0xFF808080);
    }

    public static void drawTexture(PoseStack matrixStack, ResourceLocation texture, int x, int y) {
        bindTexture(texture);
        Matrix4f posMat = matrixStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(posMat, x, y + 16, 0).uv(0.0f, 1.0f).endVertex();
        builder.vertex(posMat, x + 16, y + 16, 0).uv(1.0f, 1.0f).endVertex();
        builder.vertex(posMat, x + 16, y, 0).uv(1.0f, 0.0f).endVertex();
        builder.vertex(posMat, x, y, 0).uv(0.0f, 0.0f).endVertex();
        Tesselator.getInstance().end();
    }

    public static void drawUntexturedQuad(PoseStack matrixStack, BufferBuilder renderer, float x, float y, float z, float width, float height, int red, int green, int blue, int alpha) {
        Matrix4f posMat = matrixStack.last().pose();
        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(posMat, x, y, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width,  y, z).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }

    public static void drawOutline(PoseStack matrixStack, BufferBuilder renderer, float x, float y, float z, float width, float height, int red, int green, int blue, int alpha) {
        Matrix4f posMat = matrixStack.last().pose();
        renderer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(posMat, x, y, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width,  y, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x, y, z).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }

    public static void drawScaledText(PoseStack matrixStack, Font fr, String text, int x, int y, int color, float scale) {
        if (scale != 1.0f) {
            matrixStack.pushPose();
            matrixStack.translate(x, y, 0);
            matrixStack.scale(scale, scale, scale);
            fr.draw(matrixStack, text, 0, 0, color);
            matrixStack.popPose();
        } else {
            fr.draw(matrixStack, text, x, y, color);
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

    public static void bindTexture(ResourceLocation texture, float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void bindTexture(ResourceLocation texture) {
        bindTexture(texture, 1f, 1f, 1f, 1f);
    }

    /**
     * Render a progressing line in GUI context
     * @param matrixStack the matrix stack
     * @param line the line to render
     * @param color line's colour
     */
    public static void renderProgressingLine2d(PoseStack matrixStack, ProgressingLine line, int color, float lineWidth) {
        int[] cols = RenderUtils.decomposeColor(color);
        float progress = line.getProgress();
        Matrix4f posMat = matrixStack.last().pose();
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
