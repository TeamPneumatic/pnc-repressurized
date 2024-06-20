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

package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetGuiManager;
import me.desht.pneumaticcraft.client.gui.widget.WidgetVerticalScrollbar;
import me.desht.pneumaticcraft.client.render.ProgWidgetRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.IJump;
import me.desht.pneumaticcraft.common.drone.progwidgets.ILabel;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgrammerWidgetAreaRenderer {
    private static final float SCALE_PER_STEP = 0.2F;

    private final List<IProgWidget> progWidgets;
    private final int guiLeft, guiTop;
    private final int startX, startY, areaWidth, areaHeight;
    private final WidgetVerticalScrollbar scaleScroll;
    private double translatedX, translatedY;
    private int lastZoom;
    private final List<List<Component>> widgetErrors = new ArrayList<>();
    private final List<List<Component>> widgetWarnings = new ArrayList<>();
    private int totalErrors = 0;
    private int totalWarnings = 0;

    public ProgrammerWidgetAreaRenderer(List<IProgWidget> progWidgets, int guiLeft, int guiTop,
                                        Rect2i bounds, double translatedX, double translatedY, int lastZoom) {
        this.progWidgets = progWidgets;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.startX = bounds.getX();
        this.startY = bounds.getY();
        this.areaWidth = bounds.getWidth();
        this.areaHeight = bounds.getHeight();
        this.translatedX = translatedX;
        this.translatedY = translatedY;
        this.lastZoom = lastZoom;

        scaleScroll = new WidgetVerticalScrollbar(guiLeft + areaWidth + 8, guiTop + 40, areaHeight - 25)
                .setStates((int)((2.0F / SCALE_PER_STEP) - 1))
                .setCurrentState(lastZoom)
                .setListening(true);
    }

    public WidgetVerticalScrollbar getScrollBar() {
        return scaleScroll;
    }

    int getLastZoom() {
        return lastZoom;
    }

    double getTranslatedX() {
        return translatedX;
    }

    double getTranslatedY() {
        return translatedY;
    }

    private void addMessages(List<Component> tooltip, List<Component> msgList, String key, ChatFormatting color) {
        if (!msgList.isEmpty()) {
            tooltip.add(xlate(key).withStyle(color, ChatFormatting.UNDERLINE));
            for (Component msg : msgList) {
                tooltip.add(Component.literal(Symbols.TRIANGLE_RIGHT + " ").append(msg).withStyle(color));
            }
        }
    }

    public void renderForeground(GuiGraphics graphics, int x, int y, IProgWidget tooltipExcludingWidget, Font font) {
        int idx = getHoveredWidgetIndex(x, y);
        if (idx >= 0) {
            IProgWidget progWidget = progWidgets.get(idx);
            if (progWidget != null && progWidget != tooltipExcludingWidget) {
                List<Component> tooltip = new ArrayList<>();
                progWidget.getTooltip(tooltip);
                if (widgetErrors.size() == progWidgets.size())
                    addMessages(tooltip, widgetErrors.get(idx), "pneumaticcraft.gui.programmer.errors", ChatFormatting.RED);
                if (widgetWarnings.size() == progWidgets.size())
                    addMessages(tooltip, widgetWarnings.get(idx), "pneumaticcraft.gui.programmer.warnings", ChatFormatting.YELLOW);
                addAdditionalInfoToTooltip(progWidget, tooltip);
                if (!tooltip.isEmpty()) {
                    graphics.renderTooltip(font, GuiUtils.wrapTextComponentList(tooltip, areaWidth * 2 / 3, font), x - guiLeft, y - guiTop);
                }
            }
        }
    }

    private int getHoveredWidgetIndex(int mouseX, int mouseY) {
        // iterate backwards because later-added widgets are more logically on top
        // widgets don't normally overlap, but there are circumstances where they can
        float scale = getScale();
        for (int i = progWidgets.size() - 1; i >= 0; i--) {
            IProgWidget widget = progWidgets.get(i);
            if (!isOutsideProgrammingArea(widget)
                    && (mouseX - translatedX) / scale - guiLeft >= widget.getX()
                    && (mouseY - translatedY) / scale - guiTop >= widget.getY()
                    && (mouseX - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2f
                    && (mouseY - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2f) {
                return i;
            }
        }
        return -1;
    }

    public IProgWidget getHoveredWidget(int mouseX, int mouseY) {
        int i = getHoveredWidgetIndex(mouseX, mouseY);
        return i >= 0 ? progWidgets.get(i) : null;
    }

    protected void addAdditionalInfoToTooltip(IProgWidget widget, List<Component> tooltip) {
        if (ProgWidgetGuiManager.hasGui(widget)) {
            tooltip.add(xlate("pneumaticcraft.gui.programmer.rightClickForOptions").withStyle(ChatFormatting.GOLD));
        }
        ThirdPartyManager.instance().getDocsProvider().addTooltip(tooltip, false);
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            PneumaticCraftUtils.getRegistryName(PNCRegistries.PROG_WIDGETS_REGISTRY, widget.getType())
                            .ifPresent(regName -> tooltip.add(Component.literal(regName.toString()).withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    public void tick() {
        if ((ClientUtils.getClientLevel().getGameTime() & 0xf) == 0 || widgetErrors.size() != progWidgets.size() || widgetWarnings.size() != progWidgets.size()) {
            widgetErrors.clear();
            widgetWarnings.clear();
            totalErrors = totalWarnings = 0;
            for (IProgWidget widget : progWidgets) {
                List<Component> e = new ArrayList<>();
                widget.addErrors(e, progWidgets);
                widgetErrors.add(e.isEmpty() ? Collections.emptyList() : e);
                totalErrors += e.size();
                List<Component> w = new ArrayList<>();
                widget.addWarnings(w, progWidgets);
                widgetWarnings.add(w.isEmpty() ? Collections.emptyList() : w);
                totalWarnings += w.size();
            }
        }
    }

    public void render(GuiGraphics graphics, int x, int y, boolean showFlow, boolean showInfo) {
        if (scaleScroll.getState() != lastZoom) {
            float shift = SCALE_PER_STEP * (scaleScroll.getState() - lastZoom);
            float prevScale = 2.0F - lastZoom * SCALE_PER_STEP;
            translatedX += shift * (x - translatedX) / prevScale;
            translatedY += shift * (y - translatedY) / prevScale;
        }
        lastZoom = scaleScroll.getState();

        graphics.enableScissor(guiLeft + startX, guiTop + startY, guiLeft + startX + areaWidth, guiTop + startY + areaHeight);

        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();
        poseStack.translate(translatedX, translatedY, 0);

        float scale = getScale();
        poseStack.scale(scale, scale, 1);

        if (showFlow) showFlow(graphics);

//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (IProgWidget widget : progWidgets) {
            poseStack.pushPose();
            poseStack.translate(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            poseStack.scale(0.5f, 0.5f, 1.0f);
            ProgWidgetRenderer.renderProgWidget2d(graphics, widget);
            poseStack.popPose();
        }

        if (widgetErrors.size() == progWidgets.size() && widgetWarnings.size() == progWidgets.size()) {
            for (int i = 0; i < progWidgets.size(); i++) {
                if (!widgetErrors.get(i).isEmpty()) {
                    drawBorder(graphics, progWidgets.get(i), 0xFFFF0000);
                } else if (!widgetWarnings.get(i).isEmpty()) {
                    drawBorder(graphics, progWidgets.get(i), 0xFFFFFF00);
                }
            }
        }

        renderAdditionally(graphics);

//        RenderSystem.disableBlend();

        if (showInfo) {
            for (IProgWidget widget : progWidgets) {
                poseStack.pushPose();
                poseStack.translate(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
                poseStack.scale(0.5f, 0.5f, 1.0f);
                ProgWidgetRenderer.doItemRendering2d(graphics, widget);
                ProgWidgetRenderer.doExtraRendering2d(graphics, widget);
                poseStack.popPose();
            }
        }

        poseStack.popPose();

        graphics.disableScissor();
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        if (mouseButton == 0 && !scaleScroll.isDragging() && new Rect2i(guiLeft + startX, guiTop + startY, areaWidth, areaHeight).contains((int)mouseX, (int)mouseY)) {
            translatedX += dx;
            translatedY += dy;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double dirX, double dirY) {
        return scaleScroll.mouseScrolled(mouseX, mouseY, dirX, dirY);
    }

    protected void renderAdditionally(GuiGraphics graphics) {
        // nothing; to be overridden
    }

    protected void drawBorder(GuiGraphics graphics, IProgWidget widget, int color) {
        drawBorder(graphics, widget, color, 0);
    }

    protected void drawBorder(GuiGraphics graphics, IProgWidget widget, int color, int inset) {
        int inset2 = inset + inset;
        graphics.pose().pushPose();
        graphics.pose().translate(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
        graphics.pose().scale(0.5f, 0.5f, 1f);
        graphics.renderOutline(inset, inset, widget.getWidth() - inset2, widget.getHeight() - inset2, color);
        graphics.pose().popPose();
    }

    private static final float ARROW_ANGLE = (float) Math.toRadians(30);
    private static final float ARROW_SIZE = 5;

    private void showFlow(GuiGraphics graphics) {
        RenderSystem.lineWidth(1);

        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder wr = Tesselator.getInstance().getBuilder();
        wr.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);

        Map<String, List<IProgWidget>> labelWidgets = new HashMap<>();
        for (IProgWidget w : progWidgets) {
            if (w instanceof ILabel l) {
                labelWidgets.computeIfAbsent(l.getLabel(), k -> new ArrayList<>()).add(w);
            }
        }

        Matrix4f posMat = graphics.pose().last().pose();
        for (IProgWidget widget : progWidgets) {
            if (widget instanceof IJump jump) {
                for (String jumpLocation : jump.getPossibleJumpLocations()) {
                    for (IProgWidget labelWidget : labelWidgets.getOrDefault(jumpLocation, Collections.emptyList())) {
                        int x1 = widget.getX() + widget.getWidth() / 4;
                        int y1 = widget.getY() + widget.getHeight() / 4;
                        int x2 = labelWidget.getX() + labelWidget.getWidth() / 4;
                        int y2 = labelWidget.getY() + labelWidget.getHeight() / 4;
                        float midX = (x2 + x1) / 2F;
                        float midY = (y2 + y1) / 2F;
                        wr.vertex(posMat,guiLeft + x1, guiTop + y1, 0.0f).endVertex();
                        wr.vertex(posMat,guiLeft + x2, guiTop + y2, 0.0f).endVertex();
                        Vec3 arrowVec = new Vec3(x1 - x2, y1 - y2, 0).normalize();
                        arrowVec = new Vec3(arrowVec.x * ARROW_SIZE, 0, arrowVec.y * ARROW_SIZE);
                        arrowVec = arrowVec.yRot(ARROW_ANGLE);
                        wr.vertex(posMat,guiLeft + midX, guiTop + midY, 0.0f).endVertex();
                        wr.vertex(posMat,guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, 0.0f).endVertex();
                        arrowVec = arrowVec.yRot(-2 * ARROW_ANGLE);
                        wr.vertex(posMat,guiLeft + midX, guiTop + midY, 0.0f).endVertex();
                        wr.vertex(posMat,guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, 0.0f).endVertex();
                    }
                }
            }
        }

        Tesselator.getInstance().end();
    }

    public float getScale() {
        return 2.0F - scaleScroll.getState() * SCALE_PER_STEP;
    }

    boolean isOutsideProgrammingArea(IProgWidget widget) {
        float scale = getScale();
        int x = (int) ((widget.getX() + guiLeft) * scale);
        int y = (int) ((widget.getY() + guiTop) * scale);
        x += translatedX - guiLeft;
        y += translatedY - guiTop;

        return x < startX || x + widget.getWidth() * scale / 2 > startX + areaWidth
                || y < startY || y + widget.getHeight() * scale / 2 > startY + areaHeight;
    }

    public void gotoPiece(IProgWidget widget) {
        if (widget != null) {
            scaleScroll.currentScroll = 0;
            lastZoom = 0;
            translatedX = -widget.getX() * 2d + areaWidth / 2d - guiLeft;
            translatedY = -widget.getY() * 2d + areaHeight / 2d - guiTop;
        }
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public int getTotalWarnings() {
        return totalWarnings;
    }
}
