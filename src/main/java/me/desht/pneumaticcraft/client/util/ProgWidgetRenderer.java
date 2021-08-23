package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCrafting;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.client.util.RenderUtils.FULL_BRIGHT;
import static me.desht.pneumaticcraft.client.util.RenderUtils.renderWithTypeAndFinish;

public class ProgWidgetRenderer {
    private static final Map<ResourceLocation, BiConsumer<MatrixStack, IProgWidget>> EXTRA_RENDERERS = new HashMap<>();

    /**
     * Render a progwidget into a GUI.  Do not use for in-world rendering
     * (see {@link ProgWidgetRenderer#renderProgWidget3d(MatrixStack, IRenderTypeBuffer, IProgWidget)}
     *
     * @param matrixStack the matrix stack
     * @param progWidget the progwidget
     * @param alpha transparerncy
     */
    public static void renderProgWidget2d(MatrixStack matrixStack, IProgWidget progWidget, int alpha) {
        Minecraft.getInstance().getTextureManager().bind(progWidget.getTexture());
        int width = progWidget.getWidth() + (progWidget.getParameters().isEmpty() ? 0 : 10);
        int height = progWidget.getHeight() + (progWidget.hasStepOutput() ? 10 : 0);
        Pair<Float,Float> maxUV = progWidget.getMaxUV();
        float u = maxUV.getLeft();
        float v = maxUV.getRight();
        Matrix4f posMat = matrixStack.last().pose();
        BufferBuilder wr = Tessellator.getInstance().getBuilder();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        wr.vertex(posMat, 0, 0, 0).color(255, 255, 255, alpha).uv(0, 0).endVertex();
        wr.vertex(posMat, 0, height, 0).color(255, 255, 255, alpha).uv(0, v).endVertex();
        wr.vertex(posMat, width, height, 0).color(255, 255, 255, alpha).uv(u, v).endVertex();
        wr.vertex(posMat, width, 0, 0).color(255, 255, 255, alpha).uv(u, 0).endVertex();
        Tessellator.getInstance().end();
    }

    public static void renderProgWidget2d(MatrixStack matrixStack, IProgWidget progWidget) {
        renderProgWidget2d(matrixStack, progWidget, 255);
    }

    /**
     * Handle in-world progwidget rendering (e.g. for drone debugging)
     *
     * @param buffer the render buffer
     * @param progWidget the progwidget
     */
    public static void renderProgWidget3d(MatrixStack matrixStack, IRenderTypeBuffer buffer, IProgWidget progWidget) {
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

    public static void doExtraRendering2d(MatrixStack matrixStack, IProgWidget widget) {
        EXTRA_RENDERERS.getOrDefault(widget.getTypeID(), ProgWidgetRenderer::renderGenericExtras).accept(matrixStack, widget);
    }

    public static <P extends IProgWidget> void registerExtraRenderer(ProgWidgetType<P> type, BiConsumer<MatrixStack, P> consumer) {
        EXTRA_RENDERERS.put(type.getRegistryName(), (BiConsumer<MatrixStack, IProgWidget>) consumer);
    }

    public static void renderGenericExtras(MatrixStack matrixStack, IProgWidget progWidget) {
        List<ITextComponent> info = progWidget.getExtraStringInfo();
        if (!info.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            FontRenderer fr = Minecraft.getInstance().font;
            List<IReorderingProcessor> splittedInfo = GuiUtils.wrapTextComponentList(info, 150, fr);
            for (int i = 0; i < splittedInfo.size(); i++) {
                int stringWidth = fr.width(splittedInfo.get(i));
                int startX = progWidget.getWidth() / 2 - stringWidth / 4;
                int startY = progWidget.getHeight() / 2 - (fr.lineHeight + 1) * (splittedInfo.size() - 1) / 4 + (fr.lineHeight + 1) * i / 2 - fr.lineHeight / 4;
                AbstractGui.fill(matrixStack, startX * 2 - 1, startY * 2 - 1, startX * 2 + stringWidth + 1, startY * 2 + fr.lineHeight + 1, 0xC0FFFFFF);
                GuiUtils.drawOutline(matrixStack, Tessellator.getInstance().getBuilder(), startX * 2 - 1, startY * 2 - 1, 0, stringWidth + 2, fr.lineHeight + 2, 192, 192, 192, 255);
                fr.draw(matrixStack, splittedInfo.get(i),startX * 2, startY * 2, 0xFF000000); // draw reordering processor w/o drop shadow
            }
            matrixStack.popPose();
        }
    }

    public static void renderCraftingExtras(MatrixStack matrixStack, ProgWidgetCrafting progWidget) {
        ItemStack recipe = progWidget.getRecipeResult(ClientUtils.getClientWorld());
        if (recipe != null) {
            GuiUtils.renderItemStack(matrixStack, recipe, 8, progWidget.getHeight() / 2 - 8);
            GuiUtils.renderItemStackOverlay(matrixStack, Minecraft.getInstance().font, recipe, 8, progWidget.getHeight() / 2 - 8, Integer.toString(recipe.getCount()));
        }
    }

    public static void renderItemFilterExtras(MatrixStack matrixStack, ProgWidgetItemFilter progWidget) {
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
