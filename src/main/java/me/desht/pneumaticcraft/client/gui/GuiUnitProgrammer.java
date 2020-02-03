package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetGuiManager;
import me.desht.pneumaticcraft.client.gui.widget.WidgetVerticalScrollbar;
import me.desht.pneumaticcraft.common.progwidgets.IJump;
import me.desht.pneumaticcraft.common.progwidgets.ILabel;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;
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
        super(new StringTextComponent("Programmer"));
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
                .setStates(9)
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
                    PneumaticCraftUtils.splitString(error.getFormattedText(), 40)
                            .forEach(str -> tooltip.add(new StringTextComponent(str).applyTextStyle(TextFormatting.RED)));
                }
            }

            List<ITextComponent> warnings = new ArrayList<>();
            progWidget.addWarnings(warnings, progWidgets);
            if (warnings.size() > 0) {
                tooltip.add(xlate("gui.programmer.warnings").applyTextStyles(TextFormatting.YELLOW, TextFormatting.UNDERLINE));
                for (ITextComponent warning : warnings) {
                    PneumaticCraftUtils.splitString(warning.getFormattedText(), 40)
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

    public IProgWidget getHoveredWidget(int x, int y) {
        float scale = getScale();
        for (IProgWidget widget : progWidgets) {
            if (!isOutsideProgrammingArea(widget)) {
                if ((x - translatedX) / scale - guiLeft >= widget.getX() && (y - translatedY) / scale - guiTop >= widget.getY() && (x - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2 && (y - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2) {
                    return widget;
                }
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
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (scaleScroll.getState() != lastZoom) {
            float shift = SCALE_PER_STEP * (scaleScroll.getState() - lastZoom);
            float prevScale = 2.0F - lastZoom * SCALE_PER_STEP;
            translatedX += shift * (x - translatedX) / prevScale;
            translatedY += shift * (y - translatedY) / prevScale;
        }
        lastZoom = scaleScroll.getState();

        MainWindow mw = minecraft.mainWindow;
        int sf = minecraft.gameSettings.guiScale;
        GL11.glScissor((guiLeft + startX) * sf, (mw.getScaledHeight() - areaHeight - (guiTop + startY)) * sf, areaWidth * sf, areaHeight * sf);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GlStateManager.pushMatrix();
        GlStateManager.translated(translatedX, translatedY, 0);

        float scale = getScale();
        GlStateManager.scaled(scale, scale, 1);

        if (showFlow) showFlow();

        GlStateManager.enableTexture();
        for (IProgWidget widget : progWidgets) {
            GlStateManager.pushMatrix();
            GlStateManager.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GlStateManager.scaled(0.5, 0.5, 1);
            widget.render();
            GlStateManager.popMatrix();
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

        GlStateManager.color4f(1, 1, 1, 1);

        if (showInfo) {
            for (IProgWidget widget : progWidgets) {
                GlStateManager.pushMatrix();
                GlStateManager.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
                GlStateManager.scaled(0.5, 0.5, 1);
                widget.renderExtraInfo();
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();

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
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        return scaleScroll.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    protected void renderAdditionally() {
        // nothing; override this
    }

    protected void drawBorder(IProgWidget widget, int color) {
        drawBorder(widget, color, 0);
    }

    protected void drawBorder(IProgWidget widget, int color, int inset) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
        GlStateManager.scaled(0.5, 0.5, 1);
        vLine(inset, inset, widget.getHeight() - inset, color);
        vLine(widget.getWidth() - inset, inset, widget.getHeight() - inset, color);
        hLine(widget.getWidth() - inset, inset, inset, color);
        hLine(widget.getWidth() - inset, inset, widget.getHeight() - inset, color);
        GlStateManager.popMatrix();
    }

    private void showFlow() {
        GlStateManager.lineWidth(1);
        GlStateManager.disableTexture();
        GlStateManager.begin(GL11.GL_LINES);

        for (IProgWidget widget : progWidgets) {
            if (widget instanceof IJump) {
                List<String> jumpLocations = ((IJump) widget).getPossibleJumpLocations();
                if (jumpLocations != null) {
                    for (String jumpLocation : jumpLocations) {
                        if (jumpLocation != null) {
                            for (IProgWidget w : progWidgets) {
                                if (w instanceof ILabel) {
                                    String label = ((ILabel) w).getLabel();
                                    if (jumpLocation.equals(label)) {
                                        int x1 = widget.getX() + widget.getWidth() / 4;
                                        int y1 = widget.getY() + widget.getHeight() / 4;
                                        int x2 = w.getX() + w.getWidth() / 4;
                                        int y2 = w.getY() + w.getHeight() / 4;
                                        float midX = (x2 + x1) / 2F;
                                        float midY = (y2 + y1) / 2F;
                                        GlStateManager.vertex3f(guiLeft + x1, guiTop + y1, 0.0f);
                                        GlStateManager.vertex3f(guiLeft + x2, guiTop + y2, 0.0f);
                                        Vec3d arrowVec = new Vec3d(x1 - x2, y1 - y2, 0).normalize();
                                        float arrowAngle = (float) Math.toRadians(30);
                                        float arrowSize = 5;
                                        arrowVec = new Vec3d(arrowVec.x * arrowSize, 0, arrowVec.y * arrowSize);
                                        arrowVec = arrowVec.rotateYaw(arrowAngle);
                                        GlStateManager.vertex3f(guiLeft + midX, guiTop + midY, 0.0f);
                                        GlStateManager.vertex3f(guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, 0.0f);
                                        arrowVec = arrowVec.rotateYaw(-2 * arrowAngle);
                                        GlStateManager.vertex3f(guiLeft + midX, guiTop + midY, 0.0f);
                                        GlStateManager.vertex3f(guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, 0.0f);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        GlStateManager.end();

        GlStateManager.enableTexture();
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

        return x < startX || x + widget.getWidth() * scale / 2 > startX + areaWidth || y < startY || y + widget.getHeight() * scale / 2 > startY + areaHeight;
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
