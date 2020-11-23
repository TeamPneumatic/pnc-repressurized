package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCrafting;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static me.desht.pneumaticcraft.client.util.RenderUtils.FULL_BRIGHT;
import static me.desht.pneumaticcraft.client.util.RenderUtils.renderWithType;

public class ProgWidgetRenderer {
    /**
     * Render a progwidget into a GUI.  Do not use for in-world rendering
     * (see {@link ProgWidgetRenderer#renderProgWidget3d(MatrixStack, IRenderTypeBuffer, IProgWidget)}
     *
     * @param progWidget the progwidget
     */
    public static void renderProgWidget2d(IProgWidget progWidget) {
        Minecraft.getInstance().getTextureManager().bindTexture(progWidget.getTexture());
        int width = progWidget.getWidth() + (progWidget.getParameters().isEmpty() ? 0 : 10);
        int height = progWidget.getHeight() + (progWidget.hasStepOutput() ? 10 : 0);
        Pair<Float,Float> maxUV = progWidget.getMaxUV();
        float u = maxUV.getLeft();
        float v = maxUV.getRight();
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0, 0, 0).tex(0, 0).endVertex();
        wr.pos(0, height, 0).tex(0, v).endVertex();
        wr.pos(width, height, 0).tex(u, v).endVertex();
        wr.pos(width, 0, 0).tex(u, 0).endVertex();
        Tessellator.getInstance().draw();
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

    public static void renderExtras(IProgWidget progWidget) {
        String info = progWidget.getExtraStringInfo();
        if (info != null && !info.isEmpty()) {
            RenderSystem.pushMatrix();
            RenderSystem.scaled(0.5, 0.5, 0.5);
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            List<String> splittedInfo = PneumaticCraftUtils.splitString(info, 20);
            for (int i = 0; i < splittedInfo.size(); i++) {
                int stringLength = fr.getStringWidth(splittedInfo.get(i));
                int startX = progWidget.getWidth() / 2 - stringLength / 4;
                int startY = progWidget.getHeight() / 2 - (fr.FONT_HEIGHT + 1) * (splittedInfo.size() - 1) / 4 + (fr.FONT_HEIGHT + 1) * i / 2 - fr.FONT_HEIGHT / 4;
                AbstractGui.fill(startX * 2 - 1, startY * 2 - 1, startX * 2 + stringLength + 1, startY * 2 + fr.FONT_HEIGHT + 1, 0xC0FFFFFF);
                fr.drawString(splittedInfo.get(i), startX * 2, startY * 2, 0xFF000000);
            }
            RenderSystem.popMatrix();
            RenderSystem.color4f(1, 1, 1, 1);
        }
    }

    public static void renderCraftingExtras(ProgWidgetCrafting progWidget) {
        ItemStack recipe = progWidget.getRecipeResult(ClientUtils.getClientWorld());
        if (recipe != null) {
            GuiUtils.drawItemStack(recipe, 8, progWidget.getHeight() / 2 - 8, recipe.getCount() + "");
        }
    }

    public static void renderItemFilterExtras(ProgWidgetItemFilter progWidget) {
        if (progWidget.getVariable().isEmpty()) {
            if (!progWidget.getFilter().isEmpty()) {
                GuiUtils.drawItemStack(progWidget.getFilter(), 10, 2, "");
            }
        } else {
            renderExtras(progWidget);
        }
    }
}
