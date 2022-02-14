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
import com.mojang.math.Matrix4f;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCrafting;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.client.util.RenderUtils.FULL_BRIGHT;
import static me.desht.pneumaticcraft.client.util.RenderUtils.renderWithTypeAndFinish;

public class ProgWidgetRenderer {
    private static final Map<ResourceLocation, BiConsumer<PoseStack, IProgWidget>> EXTRA_RENDERERS = new HashMap<>();

    /**
     * Render a progwidget into a GUI.  Do not use for in-world rendering
     * (see {@link ProgWidgetRenderer#renderProgWidget3d(PoseStack, MultiBufferSource, IProgWidget)}
     *
     * @param matrixStack the matrix stack
     * @param progWidget the progwidget
     * @param alpha transparerncy
     */
    public static void renderProgWidget2d(PoseStack matrixStack, IProgWidget progWidget, int alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, progWidget.getTexture());
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha / 255f);
        int width = progWidget.getWidth() + (progWidget.getParameters().isEmpty() ? 0 : 10);
        int height = progWidget.getHeight() + (progWidget.hasStepOutput() ? 10 : 0);
        Pair<Float,Float> maxUV = progWidget.getMaxUV();
        float u = maxUV.getLeft();
        float v = maxUV.getRight();
        Matrix4f posMat = matrixStack.last().pose();
        BufferBuilder wr = Tesselator.getInstance().getBuilder();
        wr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        wr.vertex(posMat, 0, 0, 0).color(255, 255, 255, alpha).uv(0, 0).endVertex();
        wr.vertex(posMat, 0, height, 0).color(255, 255, 255, alpha).uv(0, v).endVertex();
        wr.vertex(posMat, width, height, 0).color(255, 255, 255, alpha).uv(u, v).endVertex();
        wr.vertex(posMat, width, 0, 0).color(255, 255, 255, alpha).uv(u, 0).endVertex();
        Tesselator.getInstance().end();
    }

    public static void renderProgWidget2d(PoseStack matrixStack, IProgWidget progWidget) {
        renderProgWidget2d(matrixStack, progWidget, 255);
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
            builder.vertex(posMat, 0, 0, 0).color(255, 255, 255, 255).uv(0, 0).uv2(FULL_BRIGHT).endVertex();
            builder.vertex(posMat, width, 0, 0).color(255, 255, 255, 255).uv(u, 0).uv2(FULL_BRIGHT).endVertex();
            builder.vertex(posMat, width, height, 0).color(255, 255, 255, 255).uv(u, v).uv2(FULL_BRIGHT).endVertex();
            builder.vertex(posMat, 0, height, 0).color(255, 255, 255, 255).uv(0, v).uv2(FULL_BRIGHT).endVertex();
        });
    }

    public static void doExtraRendering2d(PoseStack matrixStack, IProgWidget widget) {
        EXTRA_RENDERERS.getOrDefault(widget.getTypeID(), ProgWidgetRenderer::renderGenericExtras).accept(matrixStack, widget);
    }

    public static <P extends IProgWidget> void registerExtraRenderer(ProgWidgetType<P> type, BiConsumer<PoseStack, P> consumer) {
        EXTRA_RENDERERS.put(type.getRegistryName(), (BiConsumer<PoseStack, IProgWidget>) consumer);
    }

    public static void renderGenericExtras(PoseStack matrixStack, IProgWidget progWidget) {
        List<Component> info = progWidget.getExtraStringInfo();
        if (!info.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            Font fr = Minecraft.getInstance().font;
            List<FormattedCharSequence> splittedInfo = GuiUtils.wrapTextComponentList(info, 150, fr);
            for (int i = 0; i < splittedInfo.size(); i++) {
                int stringWidth = fr.width(splittedInfo.get(i));
                int startX = progWidget.getWidth() / 2 - stringWidth / 4;
                int startY = progWidget.getHeight() / 2 - (fr.lineHeight + 1) * (splittedInfo.size() - 1) / 4 + (fr.lineHeight + 1) * i / 2 - fr.lineHeight / 4;
                GuiComponent.fill(matrixStack, startX * 2 - 1, startY * 2 - 1, startX * 2 + stringWidth + 1, startY * 2 + fr.lineHeight + 1, 0xC0FFFFFF);
                GuiUtils.drawOutline(matrixStack, Tesselator.getInstance().getBuilder(), startX * 2 - 1, startY * 2 - 1, 0, stringWidth + 2, fr.lineHeight + 2, 192, 192, 192, 255);
                fr.draw(matrixStack, splittedInfo.get(i),startX * 2, startY * 2, 0xFF000000); // draw reordering processor w/o drop shadow
            }
            matrixStack.popPose();
        }
    }

    public static void renderCraftingExtras(PoseStack matrixStack, ProgWidgetCrafting progWidget) {
        ItemStack recipe = progWidget.getRecipeResult(ClientUtils.getClientLevel());
        if (recipe != null) {
            GuiUtils.renderItemStack(matrixStack, recipe, 8, progWidget.getHeight() / 2 - 8);
            GuiUtils.renderItemStackOverlay(matrixStack, Minecraft.getInstance().font, recipe, 8, progWidget.getHeight() / 2 - 8, Integer.toString(recipe.getCount()));
        }
    }

    public static void renderItemFilterExtras(PoseStack matrixStack, ProgWidgetItemFilter progWidget) {
        if (progWidget.getVariable().isEmpty()) {
            if (!progWidget.getFilter().isEmpty()) {
                GuiUtils.renderItemStack(matrixStack, progWidget.getFilter(), 10, 2);
                GuiUtils.renderItemStackOverlay(matrixStack, Minecraft.getInstance().font, progWidget.getFilter(), 10, 2, "");
            }
        } else {
            renderGenericExtras(matrixStack, progWidget);
        }
    }
}
