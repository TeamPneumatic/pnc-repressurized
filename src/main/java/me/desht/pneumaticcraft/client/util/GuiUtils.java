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
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.*;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiUtils {
    private static final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    public static final String TRANSLATION_LINE_BREAK = "${br}";

    /**
     * Like {@link ItemRenderer#renderGuiItem(ItemStack, int, int)} but takes a MatrixStack
     * @param matrixStack the matrix stack
     * @param stack the item
     * @param x X pos
     * @param y Y pos
     */
    public static void renderItemStack(MatrixStack matrixStack, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            IBakedModel bakedmodel = itemRenderer.getModel(stack, null, Minecraft.getInstance().player);

            matrixStack.pushPose();

            Minecraft.getInstance().textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
            Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.translate((float)x, (float)y, 150F);
            matrixStack.translate(8.0F, 8.0F, 0.0F);
            matrixStack.scale(1.0F, -1.0F, 1.0F);
            matrixStack.scale(16.0F, 16.0F, 16.0F);
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            boolean flag = !bakedmodel.usesBlockLight();
            if (!bakedmodel.usesBlockLight()) {
                RenderHelper.setupForFlatItems();
            }
            itemRenderer.render(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, buffer, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            buffer.endBatch();
            RenderSystem.enableDepthTest();
            if (flag) {
                RenderHelper.setupFor3DItems();
            }

            RenderSystem.disableAlphaTest();
            RenderSystem.disableRescaleNormal();
            matrixStack.popPose();
        }
    }

    public static void renderItemStackOverlay(MatrixStack matrixStack, FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                matrixStack.translate(0.0D, 0.0D, 250F);
                IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
                fr.drawInBatch(s, (float)(xPosition + 19 - 2 - fr.width(s)), (float)(yPosition + 6 + 3), 16777215, true, matrixStack.last().pose(), buffer, false, 0, 15728880);
                buffer.endBatch();
            }

            if (stack.getItem().showDurabilityBar(stack)) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuilder();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int i = Math.round(13.0F - (float)health * 13.0F);
                int j = stack.getItem().getRGBDurabilityForDisplay(stack);
                drawUntexturedQuad(matrixStack, bufferbuilder, xPosition + 2, yPosition + 13, 0F, 13, 2, 0, 0, 0, 255);
                drawUntexturedQuad(matrixStack, bufferbuilder, xPosition + 2, yPosition + 13, 0F, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
            float f3 = clientplayerentity == null ? 0.0F : clientplayerentity.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f3 > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator1 = Tessellator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuilder();
                drawUntexturedQuad(matrixStack, bufferbuilder1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 0F, 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
            matrixStack.popPose();
        }
    }

    public static void renderBlockInGui(MatrixStack matrixStack, BlockState block, float x, float y, float z, float rotate, float scale) {
        final Minecraft mc = Minecraft.getInstance();
        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        matrixStack.scale(scale, -scale, scale);
        matrixStack.translate(-0.5F, -1F, 0);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));

        matrixStack.translate(0.5F, 0, -0.5F);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotate));
        matrixStack.translate(-0.5F, 0, 0.5F);

        matrixStack.translate(0, 0, -1);

        mc.getTextureManager().bind(PlayerContainer.BLOCK_ATLAS);
        final IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        mc.getBlockRenderer().renderBlock(block, matrixStack, buffers, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        buffers.endBatch();

        matrixStack.popPose();
    }

    public static void drawFluid(MatrixStack matrixStack, final Rectangle2d bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        ResourceLocation fluidStill = fluid.getAttributes().getStillTexture(fluidStack);
        if (fluidStill == null) fluidStill = MissingTextureSprite.getLocation();
        TextureAtlasSprite fluidStillSprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(fluidStill);

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        Minecraft.getInstance().getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        final int xTileCount = bounds.getWidth() / TEX_WIDTH;
        final int xRemainder = bounds.getWidth() - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getAttributes().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);
        int[] cols = RenderUtils.decomposeColor(fluid.getAttributes().getColor(fluidStack));

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = bounds.getX() + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (bounds.getWidth() > 0 && h > 0) {
                    int maskTop = TEX_HEIGHT - h;
                    int maskRight = TEX_WIDTH - w;

                    drawFluidTexture(matrixStack, x, y, fluidStillSprite, maskTop, maskRight, 100, cols);
                }
            }
        }
        RenderSystem.disableBlend();
    }

    private static void drawFluidTexture(MatrixStack matrixStack, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel, int[] cols) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16.0f * (uMax - uMin);
        vMax = vMax - maskTop / 16.0f * (vMax - vMin);

        Matrix4f posMat = matrixStack.last().pose();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuilder();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        worldrenderer.vertex(posMat, xCoord, yCoord + 16, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMax).endVertex();
        worldrenderer.vertex(posMat,xCoord + 16 - maskRight, yCoord + 16, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMax).endVertex();
        worldrenderer.vertex(posMat, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMin).endVertex();
        worldrenderer.vertex(posMat, xCoord, yCoord + maskTop, zLevel).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMin).endVertex();
        tessellator.end();
    }

    public static void showPopupHelpScreen(MatrixStack matrixStack, Screen screen, FontRenderer fontRenderer, List<ITextComponent> helpText) {
        List<IReorderingProcessor> l = GuiUtils.wrapTextComponentList(helpText, screen.width / 2, fontRenderer);
        int lineSpacing = fontRenderer.lineHeight + 1;
        int boxHeight = Math.min(screen.height, l.size() * lineSpacing);
        int maxLines = boxHeight / lineSpacing;
        int boxWidth = l.stream().max(Comparator.comparingInt(fontRenderer::width)).map(fontRenderer::width).orElse(0);

        int x, y;
        if (screen instanceof ContainerScreen) {
            x = (((ContainerScreen<?>) screen).getXSize() - boxWidth) / 2;
            y = (((ContainerScreen<?>) screen).getYSize() - boxHeight) / 2;
        } else {
            x = (screen.width - boxWidth) / 2;
            y = (screen.height - boxHeight) / 2;
        }
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 400);
        drawPanel(matrixStack, x, y, boxHeight, boxWidth);

        for (IReorderingProcessor line : l) {
            fontRenderer.draw(matrixStack, line, x, y, 0xFFE0E0E0);  // draw reordering processor w/o drop shadow
            y += lineSpacing;
            if (maxLines-- == 0) break;
        }
        matrixStack.popPose();
    }

    public static void drawPanel(MatrixStack matrixStack, int x, int y, int panelHeight, int panelWidth) {
        AbstractGui.fill(matrixStack,x - 4, y - 4, x + panelWidth + 8, y + panelHeight + 8, 0xC0000000);
        AbstractGui.fill(matrixStack,x - 4, y - 4, x + panelWidth + 8, y - 3, 0xFF808080);
        AbstractGui.fill(matrixStack,x - 4, y + panelHeight + 8, x + panelWidth + 8, y + panelHeight + 9, 0xFF808080);
        AbstractGui.fill(matrixStack,x - 4, y - 4, x - 3, y + panelHeight + 8, 0xFF808080);
        AbstractGui.fill(matrixStack,x + panelWidth + 8, y - 4, x + panelWidth + 9, y + panelHeight + 8, 0xFF808080);
    }


    public static void drawTexture(MatrixStack matrixStack, ResourceLocation texture, int x, int y) {
        Minecraft.getInstance().getTextureManager().bind(texture);
        RenderSystem.enableTexture();
        Matrix4f posMat = matrixStack.last().pose();
        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        builder.vertex(posMat, x, y + 16, 0).color(255, 255, 255, 255).uv(0.0f, 1.0f).endVertex();
        builder.vertex(posMat, x + 16, y + 16, 0).color(255, 255, 255, 255).uv(1.0f, 1.0f).endVertex();
        builder.vertex(posMat, x + 16, y, 0).color(255, 255, 255, 255).uv(1.0f, 0.0f).endVertex();
        builder.vertex(posMat, x, y, 0).color(255, 255, 255, 255).uv(0.0f, 0.0f).endVertex();
        Tessellator.getInstance().end();
    }

    public static void drawUntexturedQuad(MatrixStack matrixStack, BufferBuilder renderer, float x, float y, float z, float width, float height, int red, int green, int blue, int alpha) {
        Matrix4f posMat = matrixStack.last().pose();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.vertex(posMat, x, y, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width,  y, z).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();
    }

    public static void drawOutline(MatrixStack matrixStack, BufferBuilder renderer, float x, float y, float z, float width, float height, int red, int green, int blue, int alpha) {
        Matrix4f posMat = matrixStack.last().pose();
        renderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        renderer.vertex(posMat, x, y, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width, y + height, z).color(red, green, blue, alpha).endVertex();
        renderer.vertex(posMat, x + width,  y, z).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();
    }

    /**
     * Set the colour from a 32-bit int
     * @param color the colour to use, in ARGB format
     */
    public static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public static void drawScaledText(MatrixStack matrixStack, FontRenderer fr, String text, int x, int y, int color, float scale) {
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

    public static List<IReorderingProcessor> wrapTextComponentList(List<ITextComponent> text, int maxWidth, FontRenderer font) {
        ImmutableList.Builder<IReorderingProcessor> builder = ImmutableList.builder();
        for (ITextComponent line : text) {
            // note: using workaround method below instead of RenderComponentsUtil.wrapComponents()
            builder.addAll(wrapComponents(line, maxWidth, font));
        }
        return builder.build();
    }

    public static List<ITextComponent> xlateAndSplit(String key, Object... params) {
        return Arrays.stream(StringUtils.splitByWholeSeparator(I18n.get(key, params), TRANSLATION_LINE_BREAK))
                .map(StringTextComponent::new)
                .collect(Collectors.toList());
    }

    //
    // next 2 methods are temporary workaround for clashing 'pStyle' params in ITextProperties.of() leading to styles being lost
    //
    private static final IReorderingProcessor INDENT = IReorderingProcessor.codepoint(' ', Style.EMPTY);
    private static List<IReorderingProcessor> wrapComponents(ITextProperties textProperties, int maxWidth, FontRenderer font) {
        // use instead of RenderComponentsUtil.wrapComponents()
        TextPropertiesManager textpropertiesmanager = new TextPropertiesManager();
        textProperties.visit((style, str) -> {
            textpropertiesmanager.append(makeProperties(str, style));
            return Optional.empty();
        }, Style.EMPTY);
        List<IReorderingProcessor> list = Lists.newArrayList();
        font.getSplitter().splitLines(textpropertiesmanager.getResultOrEmpty(), maxWidth, Style.EMPTY, (props, bool) -> {
            IReorderingProcessor ireorderingprocessor = LanguageMap.getInstance().getVisualOrder(props);
            list.add(bool ? IReorderingProcessor.composite(INDENT, ireorderingprocessor) : ireorderingprocessor);
        });
        return list.isEmpty() ? Lists.newArrayList(IReorderingProcessor.EMPTY) : list;
    }

    private static ITextProperties makeProperties(final String pText, final Style pStyle) {
        return new ITextProperties() {
            public <T> Optional<T> visit(ITextProperties.ITextAcceptor<T> pAcceptor) {
                return pAcceptor.accept(pText);
            }
            public <T> Optional<T> visit(ITextProperties.IStyledTextAcceptor<T> pAcceptor, Style pStyle2) {
                return pAcceptor.accept(pStyle2.applyTo(pStyle), pText);
            }
        };
    }
}
