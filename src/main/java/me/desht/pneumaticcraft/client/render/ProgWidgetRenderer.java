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

package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCrafting;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetItemFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.client.util.RenderUtils.renderWithTypeAndFinish;
import static net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

public class ProgWidgetRenderer {
    private static final Map<ProgWidgetType<?>, BiConsumer<GuiGraphics, IProgWidget>> ITEM_RENDERERS = new HashMap<>();
    private static final Map<ProgWidgetType<?>, BiConsumer<GuiGraphics, IProgWidget>> EXTRA_RENDERERS = new HashMap<>();

    /**
     * Render a progwidget into a GUI.  Do not use for in-world rendering
     * (see {@link ProgWidgetRenderer#renderProgWidget3d(PoseStack, MultiBufferSource, IProgWidget)}
     *
     * @param graphics the matrix stack
     * @param progWidget the progwidget
     * @param alpha transparerncy
     */
    public static void renderProgWidget2d(GuiGraphics graphics, IProgWidget progWidget, int alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, progWidget.getTexture());
        int width = progWidget.getWidth() + (progWidget.getParameters().isEmpty() ? 0 : 10);
        int height = progWidget.getHeight() + (progWidget.hasStepOutput() ? 10 : 0);
        Pair<Float,Float> maxUV = progWidget.getMaxUV();
        float u = maxUV.getLeft();
        float v = maxUV.getRight();
        Matrix4f posMat = graphics.pose().last().pose();
        BufferBuilder wr = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        wr.addVertex(posMat, 0, 0, 0)
                .setUv(0, 0)
                .setColor(255, 255, 255, alpha);
        wr.addVertex(posMat, 0, height, 0)
                .setUv(0, v)
                .setColor(255, 255, 255, alpha);
        wr.addVertex(posMat, width, height, 0)
                .setUv(u, v)
                .setColor(255, 255, 255, alpha);
        wr.addVertex(posMat, width, 0, 0)
                .setUv(u, 0)
                .setColor(255, 255, 255, alpha);
        BufferUploader.drawWithShader(wr.buildOrThrow());
    }

    public static void renderProgWidget2d(GuiGraphics graphics, IProgWidget progWidget) {
        renderProgWidget2d(graphics, progWidget, 255);
    }

    /**
     * Handle in-world progwidget rendering (e.g. for drone debugging)
     *
     * @param buffer the render buffer
     * @param progWidget the progwidget
     */
    public static void renderProgWidget3d(PoseStack matrixStack, MultiBufferSource buffer, IProgWidget progWidget) {
        int width = progWidget.getWidth() + (progWidget.getParameters().isEmpty() ? 0 : 10);
        int height = progWidget.getHeight() + (progWidget.hasStepOutput() ? 10 : 0);
        Pair<Float,Float> maxUV = progWidget.getMaxUV();
        float u = maxUV.getLeft();
        float v = maxUV.getRight();
        renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getTextureRenderColored(progWidget.getTexture()), (posMat, builder) -> {
            builder.addVertex(posMat, 0, 0, 0)
                    .setColor(    255, 255, 255, 255)
                    .setUv(0, 0)
                    .setLight(FULL_BRIGHT);
            builder.addVertex(posMat, width, 0, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(u, 0)
                    .setLight(FULL_BRIGHT);
            builder.addVertex(posMat, width, height, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(u, v)
                    .setLight(FULL_BRIGHT);
            builder.addVertex(posMat, 0, height, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(0, v)
                    .setLight(FULL_BRIGHT);
        });
    }

    public static void doExtraRendering2d(GuiGraphics graphics, IProgWidget widget) {
        EXTRA_RENDERERS.getOrDefault(widget.getType(), ProgWidgetRenderer::renderGenericExtras).accept(graphics, widget);
    }

    public static void doItemRendering2d(GuiGraphics graphics, IProgWidget widget) {
        ITEM_RENDERERS.getOrDefault(widget.getType(), (p,w) -> {}).accept(graphics, widget);
    }

    public static <P extends IProgWidget> void registerExtraRenderer(ProgWidgetType<P> type, BiConsumer<GuiGraphics, P> consumer) {
        EXTRA_RENDERERS.put(type, (BiConsumer<GuiGraphics, IProgWidget>) consumer);
    }

    public static <P extends IProgWidget> void registerItemRenderer(ProgWidgetType<P> type, BiConsumer<GuiGraphics, P> consumer) {
        ITEM_RENDERERS.put(type, (BiConsumer<GuiGraphics, IProgWidget>) consumer);
    }

    /**
     * Handle general drawing, string rendering etc.
     * @param graphics the matrix stack
     * @param progWidget the widget to draw for
     */
    public static void renderGenericExtras(GuiGraphics graphics, IProgWidget progWidget) {
        List<Component> info = progWidget.getExtraStringInfo();
        if (!info.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().scale(0.5f, 0.5f, 0.5f);
            Font fr = Minecraft.getInstance().font;
            List<FormattedCharSequence> splittedInfo = GuiUtils.wrapTextComponentList(info, 150, fr);
            for (int i = 0; i < splittedInfo.size(); i++) {
                int stringWidth = fr.width(splittedInfo.get(i));
                int startX = progWidget.getWidth() / 2 - stringWidth / 4;
                int startY = progWidget.getHeight() / 2 - (fr.lineHeight + 1) * (splittedInfo.size() - 1) / 4 + (fr.lineHeight + 1) * i / 2 - fr.lineHeight / 4;
                graphics.fill(startX * 2 - 1, startY * 2 - 1, startX * 2 + stringWidth + 1, startY * 2 + fr.lineHeight + 1, 0xC0FFFFFF);
                graphics.renderOutline(startX * 2 - 1, startY * 2 - 1, stringWidth + 2, fr.lineHeight + 2, 0xFFC0C0C0);
                graphics.drawString(fr, splittedInfo.get(i),startX * 2, startY * 2, 0xFF000000, false);
            }
            graphics.pose().popPose();
        }
    }

    public static void renderCraftingItem(GuiGraphics graphics, ProgWidgetCrafting progWidget) {
        ItemStack recipe = progWidget.getRecipeResult(ClientUtils.getClientLevel());
        if (recipe != null) {
            graphics.renderItem(recipe, 8 , progWidget.getHeight() / 2 - 8);
            graphics.renderItemDecorations(Minecraft.getInstance().font,  recipe,8 , progWidget.getHeight() / 2 - 8, Integer.toString(recipe.getCount()));
        }
    }

    public static void renderItemFilterItem(GuiGraphics graphics, ProgWidgetItemFilter progWidget) {
        if (progWidget.getVariable().isEmpty() && !progWidget.getFilter().isEmpty()) {
            graphics.renderItem(progWidget.getFilter(), 10, 2);
            graphics.renderItemDecorations(Minecraft.getInstance().font, progWidget.getFilter(), 10, 2, "");
        }
    }
}
