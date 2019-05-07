package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetVerticalScrollbar;
import me.desht.pneumaticcraft.common.progwidgets.IJump;
import me.desht.pneumaticcraft.common.progwidgets.ILabel;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiUnitProgrammer extends GuiScreen {
    private final FontRenderer fontRenderer;
    private final List<IProgWidget> progWidgets;
    private final int guiLeft, guiTop;
    private final int startX, startY, areaWidth, areaHeight;
    private int translatedX, translatedY;
    private int lastMouseX, lastMouseY;
    private int lastZoom;
    private boolean wasClicking;

    private final WidgetVerticalScrollbar scaleScroll;
    private static final float SCALE_PER_STEP = 0.2F;

    public GuiUnitProgrammer(List<IProgWidget> progWidgets, FontRenderer fontRenderer, int guiLeft, int guiTop,
                             int width, int height, int xSize, int startX, int startY, int areaWidth, int areaHeight, int translatedX,
                             int translatedY, int lastZoom) {
        this.fontRenderer = fontRenderer;
        this.progWidgets = progWidgets;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        setWorldAndResolution(Minecraft.getMinecraft(), width, height);
        this.startX = startX;
        this.startY = startY;
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
        this.translatedX = translatedX;
        this.translatedY = translatedY;
        this.lastZoom = lastZoom;

        scaleScroll = new WidgetVerticalScrollbar(guiLeft + areaWidth + 8, guiTop + 40, areaHeight - 25).setStates(9).setCurrentState(lastZoom).setListening(true);
    }

    public WidgetVerticalScrollbar getScrollBar() {
        return scaleScroll;
    }

    public int getLastZoom() {
        return lastZoom;
    }

    public int getTranslatedX() {
        return translatedX;
    }

    public int getTranslatedY() {
        return translatedY;
    }

    public void renderForeground(int x, int y, IProgWidget tooltipExcludingWidget) {
        IProgWidget widget = getHoveredWidget(x, y);
        if (widget != null && widget != tooltipExcludingWidget) {
            List<String> tooltip = new ArrayList<>();
            widget.getTooltip(tooltip);

            List<String> errors = new ArrayList<>();
            widget.addErrors(errors, progWidgets);
            if (errors.size() > 0) {
                tooltip.add(TextFormatting.RED + I18n.format("gui.programmer.errors"));
                for (String s : errors) {
                    String msg = I18n.hasKey(s) ? I18n.format(s) : s;
                    String[] lines = WordUtils.wrap("- " + msg, 35).split(System.getProperty("line.separator"));
                    for (String line : lines) {
                        tooltip.add(TextFormatting.RED + /*"   " +*/ line);
                    }
                }
            }

            List<String> warnings = new ArrayList<>();
            widget.addWarnings(warnings, progWidgets);
            if (warnings.size() > 0) {
                tooltip.add(TextFormatting.YELLOW + I18n.format("gui.programmer.warnings"));
                for (String s : warnings) {
                    String msg = I18n.hasKey(s) ? I18n.format(s) : s;
                    String[] lines = WordUtils.wrap("- " + msg, 35).split(System.getProperty("line.separator"));
                    for (String line : lines) {
                        tooltip.add(TextFormatting.YELLOW + "   " + line);
                    }
                }
            }
            addAdditionalInfoToTooltip(widget, tooltip);

            if (tooltip.size() > 0) drawHoveringText(tooltip, x - guiLeft, y - guiTop, fontRenderer);
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

    protected void addAdditionalInfoToTooltip(IProgWidget widget, List<String> tooltip) {
        if (widget.getOptionWindow(null) != null) {
            tooltip.add(TextFormatting.GOLD + "Right click for options");
        }
        ThirdPartyManager.instance().docsProvider.addTooltip(tooltip, false);
    }

    public void render(int x, int y, boolean showFlow, boolean showInfo, boolean translate) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int origX = x;
        int origY = y;
        x -= translatedX;
        y -= translatedY;
        float scale = getScale();
        x = (int) (x / scale);
        y = (int) (y / scale);

        if (scaleScroll.getState() != lastZoom) {
            float shift = SCALE_PER_STEP * (scaleScroll.getState() - lastZoom);
            if (new Rectangle(guiLeft + startX, guiTop + startY, areaWidth, areaHeight).contains(origX, origY) && !scaleScroll.isDragging()) {
                translatedX += shift * x;
                translatedY += shift * y;
            } else {
                translatedX += areaWidth / 2 * shift;
                translatedY += areaHeight / 2 * shift;
            }
        }
        lastZoom = scaleScroll.getState();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glScissor((guiLeft + startX) * sr.getScaleFactor(), (sr.getScaledHeight() - areaHeight - (guiTop + startY)) * sr.getScaleFactor(), areaWidth * sr.getScaleFactor(), areaHeight * sr.getScaleFactor());
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GlStateManager.pushMatrix();
        GlStateManager.translate(translatedX, translatedY, 0);

        GlStateManager.scale(scale, scale, 1);

        if (showFlow) showFlow();

        GlStateManager.enableTexture2D();
        for (IProgWidget widget : progWidgets) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GlStateManager.scale(0.5, 0.5, 1);
            widget.render();
            GlStateManager.popMatrix();
        }

        for (IProgWidget widget : progWidgets) {
            List<String> errors = new ArrayList<>();
            widget.addErrors(errors, progWidgets);
            if (errors.size() > 0) {
                drawBorder(widget, 0xFFFF0000);
            } else {
                List<String> warnings = new ArrayList<>();
                widget.addWarnings(warnings, progWidgets);
                if (warnings.size() > 0) {
                    drawBorder(widget, 0xFFFFFF00);
                }
            }
        }

        renderAdditionally();

        GlStateManager.color(1, 1, 1, 1);

        if (showInfo) {
            for (IProgWidget widget : progWidgets) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
                GlStateManager.scale(0.5, 0.5, 1);
                widget.renderExtraInfo();
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        boolean isLeftClicking = Mouse.isButtonDown(0);
        if (translate && isLeftClicking && wasClicking && !scaleScroll.isDragging() && new Rectangle(guiLeft + startX, guiTop + startY, areaWidth, areaHeight).contains(origX, origY)) {
            translatedX += origX - lastMouseX;
            translatedY += origY - lastMouseY;
        }

        wasClicking = isLeftClicking;
        lastMouseX = origX;
        lastMouseY = origY;
    }

    protected void renderAdditionally() {

    }

    protected void drawBorder(IProgWidget widget, int color) {
        drawBorder(widget, color, 0);
    }

    protected void drawBorder(IProgWidget widget, int color, int inset) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
        GlStateManager.scale(0.5, 0.5, 1);
        drawVerticalLine(inset, inset, widget.getHeight() - inset, color);
        drawVerticalLine(widget.getWidth() - inset, inset, widget.getHeight() - inset, color);
        drawHorizontalLine(widget.getWidth() - inset, inset, inset, color);
        drawHorizontalLine(widget.getWidth() - inset, inset, widget.getHeight() - inset, color);
        GlStateManager.popMatrix();
    }

    private void showFlow() {
        GlStateManager.glLineWidth(1);
        GlStateManager.disableTexture2D();
        GlStateManager.glBegin(GL11.GL_LINES);

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
                                        GlStateManager.glVertex3f(guiLeft + x1, guiTop + y1, zLevel);
                                        GlStateManager.glVertex3f(guiLeft + x2, guiTop + y2, zLevel);
                                        Vec3d arrowVec = new Vec3d(x1 - x2, y1 - y2, 0).normalize();
                                        float arrowAngle = (float) Math.toRadians(30);
                                        float arrowSize = 5;
                                        arrowVec = new Vec3d(arrowVec.x * arrowSize, 0, arrowVec.y * arrowSize);
                                        arrowVec = arrowVec.rotateYaw(arrowAngle);
                                        GlStateManager.glVertex3f(guiLeft + midX, guiTop + midY, zLevel);
                                        GlStateManager.glVertex3f(guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, zLevel);
                                        arrowVec = arrowVec.rotateYaw(-2 * arrowAngle);
                                        GlStateManager.glVertex3f(guiLeft + midX, guiTop + midY, zLevel);
                                        GlStateManager.glVertex3f(guiLeft + midX + (float)arrowVec.x, guiTop + midY + (float)arrowVec.z, zLevel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        GlStateManager.glEnd();

        GlStateManager.enableTexture2D();
    }

    public float getScale() {
        return 2.0F - scaleScroll.getState() * SCALE_PER_STEP;
    }

    public boolean isOutsideProgrammingArea(IProgWidget widget) {
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
            translatedX = -widget.getX() * 2 + areaWidth / 2 - guiLeft;
            translatedY = -widget.getY() * 2 + areaHeight / 2 - guiTop;
        }
    }
}
