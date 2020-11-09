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
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.client.util.RenderUtils.FULL_BRIGHT;
import static me.desht.pneumaticcraft.client.util.RenderUtils.renderWithType;

public class ProgWidgetRenderer {
    /**
     * Render a progwidget into a GUI.  Do not use for in-world rendering
     * (see {@link ProgWidgetRenderer#renderProgWidget3d(MatrixStack, IRenderTypeBuffer, IProgWidget)}
     *
     * @param matrixStack the matrix stack
     * @param progWidget the progwidget
     * @param alpha transparerncy
     */
    public static void renderProgWidget2d(MatrixStack matrixStack, IProgWidget progWidget, int alpha) {
        Minecraft.getInstance().getTextureManager().bindTexture(progWidget.getTexture());
        int width = progWidget.getWidth() + (progWidget.getParameters().isEmpty() ? 0 : 10);
        int height = progWidget.getHeight() + (progWidget.hasStepOutput() ? 10 : 0);
        Pair<Float,Float> maxUV = progWidget.getMaxUV();
        float u = maxUV.getLeft();
        float v = maxUV.getRight();
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        wr.pos(posMat, 0, 0, 0).color(255, 255, 255, alpha).tex(0, 0).endVertex();
        wr.pos(posMat, 0, height, 0).color(255, 255, 255, alpha).tex(0, v).endVertex();
        wr.pos(posMat, width, height, 0).color(255, 255, 255, alpha).tex(u, v).endVertex();
        wr.pos(posMat, width, 0, 0).color(255, 255, 255, alpha).tex(u, 0).endVertex();
        Tessellator.getInstance().draw();
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
        renderWithType(matrixStack, buffer, ModRenderTypes.getTextureRenderColored(progWidget.getTexture()), (posMat, builder) -> {
            builder.pos(posMat, 0, 0, 0).color(255, 255, 255, 255).tex(0, 0).lightmap(FULL_BRIGHT).endVertex();
            builder.pos(posMat, width, 0, 0).color(255, 255, 255, 255).tex(u, 0).lightmap(FULL_BRIGHT).endVertex();
            builder.pos(posMat, width, height, 0).color(255, 255, 255, 255).tex(u, v).lightmap(FULL_BRIGHT).endVertex();
            builder.pos(posMat, 0, height, 0).color(255, 255, 255, 255).tex(0, v).lightmap(FULL_BRIGHT).endVertex();
        });
    }

    private static final Map<ResourceLocation, BiConsumer<MatrixStack, IProgWidget>> extraRenderers = new HashMap<>();

    public static void doExtraRendering2d(MatrixStack matrixStack, IProgWidget widget) {
        extraRenderers.getOrDefault(widget.getTypeID(), ProgWidgetRenderer::renderGenericExtras).accept(matrixStack, widget);
    }

    public static <P extends IProgWidget> void registerExtraRenderer(ProgWidgetType<P> type, BiConsumer<MatrixStack, P> consumer) {
        extraRenderers.put(type.getRegistryName(), (BiConsumer<MatrixStack, IProgWidget>) consumer);
    }

    public static void renderGenericExtras(MatrixStack matrixStack, IProgWidget progWidget) {
        ITextComponent info = progWidget.getExtraStringInfo();
        if (!info.equals(StringTextComponent.EMPTY)) {
            matrixStack.push();
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            List<IReorderingProcessor> splittedInfo = GuiUtils.wrapTextComponentList(Collections.singletonList(info), 150, fr);
            for (int i = 0; i < splittedInfo.size(); i++) {
                int stringWidth = fr.func_243245_a(splittedInfo.get(i));
                int startX = progWidget.getWidth() / 2 - stringWidth / 4;
                int startY = progWidget.getHeight() / 2 - (fr.FONT_HEIGHT + 1) * (splittedInfo.size() - 1) / 4 + (fr.FONT_HEIGHT + 1) * i / 2 - fr.FONT_HEIGHT / 4;
                AbstractGui.fill(matrixStack, startX * 2 - 1, startY * 2 - 1, startX * 2 + stringWidth + 1, startY * 2 + fr.FONT_HEIGHT + 1, 0xFFFFFFFF);
                GuiUtils.drawOutline(matrixStack, Tessellator.getInstance().getBuffer(), startX * 2 - 1, startY * 2 - 1, 0, stringWidth + 2, fr.FONT_HEIGHT + 2, 192, 192, 192, 255);
                fr.func_238422_b_(matrixStack, splittedInfo.get(i),startX * 2, startY * 2, 0xFF000000); // draw reordering processor w/o drop shadow
            }
            matrixStack.pop();
        }
    }

    public static void renderCraftingExtras(MatrixStack matrixStack, ProgWidgetCrafting progWidget) {
        ItemStack recipe = progWidget.getRecipeResult(ClientUtils.getClientWorld());
        if (recipe != null) {
            GuiUtils.renderItemStack(matrixStack, recipe, 8, progWidget.getHeight() / 2 - 8);
            GuiUtils.renderItemStackOverlay(matrixStack, Minecraft.getInstance().fontRenderer, recipe, 8, progWidget.getHeight() / 2 - 8, Integer.toString(recipe.getCount()));
        }
    }

    public static void renderItemFilterExtras(MatrixStack matrixStack, ProgWidgetItemFilter progWidget) {
        if (progWidget.getVariable().isEmpty()) {
            if (!progWidget.getFilter().isEmpty()) {
                GuiUtils.renderItemStack(matrixStack, progWidget.getFilter(), 10, 2);
                GuiUtils.renderItemStackOverlay(matrixStack, Minecraft.getInstance().fontRenderer, progWidget.getFilter(), 10, 2, "");
            }
        } else {
            renderGenericExtras(matrixStack, progWidget);
        }
    }
}
