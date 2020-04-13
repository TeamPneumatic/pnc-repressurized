package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetGuiManager;
import me.desht.pneumaticcraft.client.gui.widget.WidgetVerticalScrollbar;
import me.desht.pneumaticcraft.client.util.ProgWidgetRenderer;
import me.desht.pneumaticcraft.common.progwidgets.IJump;
import me.desht.pneumaticcraft.common.progwidgets.ILabel;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiUnitProgrammer extends Screen {
    private static final float SCALE_PER_STEP = 0.2F;

    private final FontRenderer fontRenderer;
    private final List<IProgWidget> progWidgets;
    private final int guiLeft, guiTop;
    private final int startX, startY, areaWidth, areaHeight;
    private final WidgetVerticalScrollbar scaleScroll;
    private double translatedX, translatedY;
    private int lastZoom;

    public GuiUnitProgrammer(List<IProgWidget> progWidgets, FontRenderer fontRenderer, int guiLeft, int guiTop,
                             int width, int height, Rectangle2d bounds, double translatedX,
                             double translatedY, int lastZoom) {
        super(new StringTextComponent(I18n.format("block.pneumaticcraft.programmer")));
        this.fontRenderer = fontRenderer;
        this.progWidgets = progWidgets;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        init(Minecraft.getInstance(), width, height);
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

    public void renderForeground(int x, int y, IProgWidget tooltipExcludingWidget) {
        IProgWidget progWidget = getHoveredWidget(x, y);
        if (progWidget != null && progWidget != tooltipExcludingWidget) {
            List<ITextComponent> tooltip = new ArrayList<>();
            progWidget.getTooltip(tooltip);

            List<ITextComponent> errors = new ArrayList<>();
            progWidget.addErrors(errors, progWidgets);
            if (errors.size() > 0) {
                tooltip.add(xlate("gui.programmer.errors").applyTextStyles(TextFormatting.RED, TextFormatting.UNDERLINE));
                for (ITextComponent error : errors) {
                    PneumaticCraftUtils.splitString(GuiConstants.TRIANGLE_RIGHT + " " + error.getFormattedText(), 40)
                            .forEach(str -> tooltip.add(new StringTextComponent(str).applyTextStyle(TextFormatting.RED)));
                }
            }

            List<ITextComponent> warnings = new ArrayList<>();
            progWidget.addWarnings(warnings, progWidgets);
            if (warnings.size() > 0) {
                tooltip.add(xlate("gui.programmer.warnings").applyTextStyles(TextFormatting.YELLOW, TextFormatting.UNDERLINE));
                for (ITextComponent warning : warnings) {
                    PneumaticCraftUtils.splitString(GuiConstants.TRIANGLE_RIGHT + " " + warning.getFormattedText(), 40)
                            .forEach(str -> tooltip.add(new StringTextComponent(str).applyTextStyle(TextFormatting.YELLOW)));
                }
            }
            addAdditionalInfoToTooltip(progWidget, tooltip);

            if (!tooltip.isEmpty()) {
                List<String> t = tooltip.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
                renderTooltip(t, x - guiLeft, y - guiTop, fontRenderer);
            }
        }
    }

    public IProgWidget getHoveredWidget(int mouseX, int mouseY) {
        float scale = getScale();
        for (IProgWidget widget : progWidgets) {
            if (!isOutsideProgrammingArea(widget)
                    && (mouseX - translatedX) / scale - guiLeft >= widget.getX()
                    && (mouseY - translatedY) / scale - guiTop >= widget.getY()
                    && (mouseX - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2
                    && (mouseY - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2) {
                return widget;
            }
        }
        return null;
    }

    protected void addAdditionalInfoToTooltip(IProgWidget widget, List<ITextComponent> tooltip) {
        if (ProgWidgetGuiManager.hasGui(widget)) {
            tooltip.add(new StringTextComponent("Right-click for options").applyTextStyle(TextFormatting.GOLD));
        }
        ThirdPartyManager.instance().docsProvider.addTooltip(tooltip, false);
    }

    public void render(int x, int y, boolean showFlow, boolean showInfo) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (scaleScroll.getState() != lastZoom) {
            float shift = SCALE_PER_STEP * (scaleScroll.getState() - lastZoom);
            float prevScale = 2.0F - lastZoom * SCALE_PER_STEP;
            translatedX += shift * (x - translatedX) / prevScale;
            translatedY += shift * (y - translatedY) / prevScale;
        }
        lastZoom = scaleScroll.getState();

        MainWindow mw = minecraft.getMainWindow();
        int sf = minecraft.gameSettings.guiScale;
        GL11.glScissor((guiLeft + startX) * sf, (mw.getScaledHeight() - areaHeight - (guiTop + startY)) * sf, areaWidth * sf, areaHeight * sf);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        RenderSystem.pushMatrix();
        RenderSystem.translated(translatedX, translatedY, 0);

        float scale = getScale();
        RenderSystem.scaled(scale, scale, 1);

        if (showFlow) showFlow();

        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (IProgWidget widget : progWidgets) {
            RenderSystem.pushMatrix();
            RenderSystem.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            RenderSystem.scaled(0.5, 0.5, 1);
            ProgWidgetRenderer.renderProgWidget2d(widget);
            RenderSystem.popMatrix();
        }

        for (IProgWidget widget : progWidgets) {
            List<ITextComponent> errors = new ArrayList<>();
            widget.addErrors(errors, progWidgets);
            if (errors.size() > 0) {
                drawBorder(widget, 0xFFFF0000);
            } else {
                List<ITextComponent> warnings = new ArrayList<>();
                widget.addWarnings(warnings, progWidgets);
                if (warnings.size() > 0) {
                    drawBorder(widget, 0xFFFFFF00);
                }
            }
        }

        renderAdditionally();

        RenderSystem.disableBlend();
        RenderSystem.color4f(1, 1, 1, 1);

        if (showInfo) {
            for (IProgWidget widget : progWidgets) {
                RenderSystem.pushMatrix();
                RenderSystem.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
                RenderSystem.scaled(0.5, 0.5, 1);
                ProgWidgetRenderer.renderExtras(widget);
                widget.renderExtraInfo();
                RenderSystem.popMatrix();
            }
        }

        RenderSystem.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        if (mouseButton == 0 && !scaleScroll.isDragging() && new Rectangle2d(guiLeft + startX, guiTop + startY, areaWidth, areaHeight).contains((int)mouseX, (int)mouseY)) {
            translatedX += dx;
            translatedY += dy;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dir) {
        return scaleScroll.mouseScrolled(mouseX, mouseY, dir);
    }

    protected void renderAdditionally() {
        // nothing; to be overridden
    }

    protected void drawBorder(IProgWidget widget, int color) {
        drawBorder(widget, color, 0);
    }

    protected void drawBorder(IProgWidget widget, int color, int inset) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
        RenderSystem.scaled(0.5, 0.5, 1);
        vLine(inset, inset, widget.getHeight() - inset, color);
        vLine(widget.getWidth() - inset, inset, widget.getHeight() - inset, color);
        hLine(widget.getWidth() - inset, inset, inset, color);
        hLine(widget.getWidth() - inset, inset, widget.getHeight() - inset, color);
        RenderSystem.popMatrix();
    }

    private static final float ARROW_ANGLE = (float) Math.toRadians(30);
    private static final float ARROW_SIZE = 5;

    private void showFlow() {
        RenderSystem.lineWidth(1);
        RenderSystem.disableTexture();

        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        for (IProgWidget widget : progWidgets) {
            if (widget instanceof IJump) {
                for (String jumpLocation : ((IJump) widget).getPossibleJumpLocations()) {
                    if (jumpLocation != null) {
                        for (IProgWidget w : progWidgets) {
                            if (w instanceof ILabel && jumpLocation.equals(((ILabel) w).getLabel())) {
                                int x1 = widget.getX() + widget.getWidth() / 4;
                                int y1 = widget.getY() + widget.getHeight() / 4;
                                int x2 = w.getX() + w.getWidth() / 4;
                                int y2 = w.getY() + w.getHeight() / 4;
                                float midX = (x2 + x1) / 2F;
                                float midY = (y2 + y1) / 2F;
                                wr.pos(guiLeft + x1, guiTop + y1, 0.0f).endVertex();
                                wr.pos(guiLeft + x2, guiTop + y2, 0.0f).endVertex();
                                Vec3d arrowVec = new Vec3d(x1 - x2, y1 - y2, 0).normalize();
                                arrowVec = new Vec3d(arrowVec.x * ARROW_SIZE, 0, arrowVec.y * ARROW_SIZE);
                                arrowVec = arrowVec.rotateYaw(ARROW_ANGLE);
                                wr.pos(guiLeft + midX, guiTop + midY, 0.0f).endVertex();
                                wr.pos(guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, 0.0f).endVertex();
                                arrowVec = arrowVec.rotateYaw(-2 * ARROW_ANGLE);
                                wr.pos(guiLeft + midX, guiTop + midY, 0.0f).endVertex();
                                wr.pos(guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, 0.0f).endVertex();
                            }
                        }
                    }
                }
            }
        }

        Tessellator.getInstance().draw();

        RenderSystem.enableTexture();
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
}
