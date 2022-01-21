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
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
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
    public static void renderItemStack(PoseStack matrixStack, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            BakedModel bakedmodel = itemRenderer.getModel(stack, null, Minecraft.getInstance().player, 0);

            matrixStack.pushPose();

            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
//            RenderSystem.enableRescaleNormal();
//            RenderSystem.enableAlphaTest();
//            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            matrixStack.translate((float)x, (float)y, 150F);
            matrixStack.translate(8.0F, 8.0F, 0.0F);
            matrixStack.scale(1.0F, -1.0F, 1.0F);
            matrixStack.scale(16.0F, 16.0F, 16.0F);
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            boolean flag = !bakedmodel.usesBlockLight();
            if (!bakedmodel.usesBlockLight()) {
                Lighting.setupForFlatItems();
            }
            itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, matrixStack, buffer, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            buffer.endBatch();
            RenderSystem.enableDepthTest();
            if (flag) {
                Lighting.setupFor3DItems();
            }

//            RenderSystem.disableAlphaTest();
//            RenderSystem.disableRescaleNormal();
            matrixStack.popPose();
        }
    }

    public static void renderItemStackOverlay(PoseStack matrixStack, Font fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                matrixStack.translate(0.0D, 0.0D, 250F);
                MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                fr.drawInBatch(s, (float)(xPosition + 19 - 2 - fr.width(s)), (float)(yPosition + 6 + 3), 16777215, true, matrixStack.last().pose(), buffer, false, 0, 15728880);
                buffer.endBatch();
            }

            if (stack.getItem().isBarVisible(stack)) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
//                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuilder();
                int barWidth = stack.getItem().getBarWidth(stack);
//                double health = stack.getItem().getDurabilityForDisplay(stack);
//                int i = Math.round(13.0F - (float)health * 13.0F);
                int barColor = stack.getItem().getBarColor(stack);
                drawUntexturedQuad(matrixStack, bufferbuilder, xPosition + 2, yPosition + 13, 0F, 13, 2, 0, 0, 0, 255);
                drawUntexturedQuad(matrixStack, bufferbuilder, xPosition + 2, yPosition + 13, 0F, barWidth, 1, barColor >> 16 & 255, barColor >> 8 & 255, barColor & 255, 255);
                RenderSystem.enableBlend();
//                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer player = Minecraft.getInstance().player;
            float f3 = player == null ? 0.0F : player.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f3 > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tessellator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuilder();
                drawUntexturedQuad(matrixStack, bufferbuilder1, xPosition, yPosition + Mth.floor(16.0F * (1.0F - f3)), 0F, 16, Mth.ceil(16.0F * f3), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
            matrixStack.popPose();
        }
    }

    public static void renderBlockInGui(PoseStack matrixStack, BlockState block, float x, float y, float z, float rotate, float scale) {
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

        bindTexture(InventoryMenu.BLOCK_ATLAS);
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        mc.getBlockRenderer().renderSingleBlock(block, matrixStack, buffers, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        buffers.endBatch();

        matrixStack.popPose();
    }

    public static void drawFluid(PoseStack matrixStack, final Rect2i bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        ResourceLocation fluidStill = fluid.getAttributes().getStillTexture(fluidStack);
        if (fluidStill == null) fluidStill = MissingTextureAtlasSprite.getLocation();
        TextureAtlasSprite fluidStillSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidStill);

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        bindTexture(TextureAtlas.LOCATION_BLOCKS);
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

    public static void showPopupHelpScreen(PoseStack matrixStack, Screen screen, Font fontRenderer, List<Component> helpText) {
        List<FormattedCharSequence> l = GuiUtils.wrapTextComponentList(helpText, screen.width / 2, fontRenderer);
        int lineSpacing = fontRenderer.lineHeight + 1;
        int boxHeight = Math.min(screen.height, l.size() * lineSpacing);
        int maxLines = boxHeight / lineSpacing;
        int boxWidth = l.stream().max(Comparator.comparingInt(fontRenderer::width)).map(fontRenderer::width).orElse(0);

        int x, y;
        if (screen instanceof AbstractContainerScreen) {
            x = (((AbstractContainerScreen<?>) screen).getXSize() - boxWidth) / 2;
            y = (((AbstractContainerScreen<?>) screen).getYSize() - boxHeight) / 2;
        } else {
            x = (screen.width - boxWidth) / 2;
            y = (screen.height - boxHeight) / 2;
        }
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 400);
        drawPanel(matrixStack, x, y, boxHeight, boxWidth);

        for (FormattedCharSequence line : l) {
            fontRenderer.draw(matrixStack, line, x, y, 0xFFE0E0E0);  // draw reordering processor w/o drop shadow
            y += lineSpacing;
            if (maxLines-- == 0) break;
        }
        matrixStack.popPose();
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
        RenderSystem.enableTexture();
        Matrix4f posMat = matrixStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        builder.vertex(posMat, x, y + 16, 0).color(255, 255, 255, 255).uv(0.0f, 1.0f).endVertex();
        builder.vertex(posMat, x + 16, y + 16, 0).color(255, 255, 255, 255).uv(1.0f, 1.0f).endVertex();
        builder.vertex(posMat, x + 16, y, 0).color(255, 255, 255, 255).uv(1.0f, 0.0f).endVertex();
        builder.vertex(posMat, x, y, 0).color(255, 255, 255, 255).uv(0.0f, 0.0f).endVertex();
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
        Tesselator.getInstance().end();
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
        RenderSystem.setShaderColor(red, green, blue, alpha);
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
            // note: using workaround method below instead of RenderComponentsUtil.wrapComponents()
            builder.addAll(wrapComponents(line, maxWidth, font));
        }
        return builder.build();
    }

    public static List<Component> xlateAndSplit(String key, Object... params) {
        return Arrays.stream(StringUtils.splitByWholeSeparator(I18n.get(key, params), TRANSLATION_LINE_BREAK))
                .map(TextComponent::new)
                .collect(Collectors.toList());
    }

    //
    // next 2 methods are temporary workaround for clashing 'pStyle' params in ITextProperties.of() leading to styles being lost
    //
    private static final FormattedCharSequence INDENT = FormattedCharSequence.codepoint(' ', Style.EMPTY);
    private static List<FormattedCharSequence> wrapComponents(FormattedText textProperties, int maxWidth, Font font) {
        // use instead of RenderComponentsUtil.wrapComponents()
        ComponentCollector textpropertiesmanager = new ComponentCollector();
        textProperties.visit((style, str) -> {
            textpropertiesmanager.append(makeProperties(str, style));
            return Optional.empty();
        }, Style.EMPTY);
        List<FormattedCharSequence> list = Lists.newArrayList();
        font.getSplitter().splitLines(textpropertiesmanager.getResultOrEmpty(), maxWidth, Style.EMPTY, (props, bool) -> {
            FormattedCharSequence ireorderingprocessor = Language.getInstance().getVisualOrder(props);
            list.add(bool ? FormattedCharSequence.composite(INDENT, ireorderingprocessor) : ireorderingprocessor);
        });
        return list.isEmpty() ? Lists.newArrayList(FormattedCharSequence.EMPTY) : list;
    }

    private static FormattedText makeProperties(final String pText, final Style pStyle) {
        return new FormattedText() {
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor) {
                return pAcceptor.accept(pText);
            }
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style pStyle2) {
                return pAcceptor.accept(pStyle2.applyTo(pStyle), pText);
            }
        };
    }

    public static void bindTexture(ResourceLocation texture, float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void bindTexture(ResourceLocation texture) {
        bindTexture(texture, 1f, 1f, 1f, 1f);
    }
}
