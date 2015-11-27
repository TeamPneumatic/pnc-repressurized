package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.WidgetVerticalScrollbar;
import pneumaticCraft.common.progwidgets.IJump;
import pneumaticCraft.common.progwidgets.ILabel;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.Loader;

public class GuiUnitProgrammer extends GuiScreen{
    private final FontRenderer fontRendererObj;
    private final List<IProgWidget> progWidgets;
    private final int guiLeft, guiTop;
    private final int startX, startY, areaWidth, areaHeight;
    private final int xSize;
    private int translatedX, translatedY;
    private int lastMouseX, lastMouseY;
    private int lastZoom;
    private boolean wasClicking;

    private final WidgetVerticalScrollbar scaleScroll;
    private static final float SCALE_PER_STEP = 0.2F;

    public GuiUnitProgrammer(List<IProgWidget> progWidgets, FontRenderer fontRendererObj, int guiLeft, int guiTop,
            int width, int height, int xSize, int startX, int startY, int areaWidth, int areaHeight, int translatedX,
            int translatedY, int lastZoom){
        this.fontRendererObj = fontRendererObj;
        this.progWidgets = progWidgets;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.width = width;
        this.height = height;
        this.xSize = xSize;
        this.startX = startX;
        this.startY = startY;
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
        this.translatedX = translatedX;
        this.translatedY = translatedY;
        this.lastZoom = lastZoom;

        scaleScroll = new WidgetVerticalScrollbar(guiLeft + 302, guiTop + 40, 129).setStates(9).setCurrentState(lastZoom).setListening(true);
    }

    public WidgetVerticalScrollbar getScrollBar(){
        return scaleScroll;
    }

    public int getLastZoom(){
        return lastZoom;
    }

    public int getTranslatedX(){
        return translatedX;
    }

    public int getTranslatedY(){
        return translatedY;
    }

    public void renderForeground(int x, int y, IProgWidget tooltipExcludingWidget){
        IProgWidget widget = getHoveredWidget(x, y);
        if(widget != null && widget != tooltipExcludingWidget) {
            List<String> tooltip = new ArrayList<String>();
            widget.getTooltip(tooltip);

            List<String> errors = new ArrayList<String>();
            widget.addErrors(errors, progWidgets);
            if(errors.size() > 0) {
                tooltip.add(EnumChatFormatting.RED + I18n.format("gui.programmer.errors"));
                for(String s : errors) {
                    String[] lines = WordUtils.wrap("-" + I18n.format(s), 30).split(System.getProperty("line.separator"));
                    for(String line : lines) {
                        tooltip.add(EnumChatFormatting.RED + "   " + line);
                    }
                }
            }

            List<String> warnings = new ArrayList<String>();
            widget.addWarnings(warnings, progWidgets);
            if(warnings.size() > 0) {
                tooltip.add(EnumChatFormatting.YELLOW + I18n.format("gui.programmer.warnings"));
                for(String s : warnings) {
                    String[] lines = WordUtils.wrap("-" + I18n.format(s), 30).split(System.getProperty("line.separator"));
                    for(String line : lines) {
                        tooltip.add(EnumChatFormatting.YELLOW + "   " + line);
                    }
                }
            }
            addAdditionalInfoToTooltip(widget, tooltip);

            if(tooltip.size() > 0) drawHoveringText(tooltip, x - guiLeft, y - guiTop, fontRendererObj);
        }

    }

    public IProgWidget getHoveredWidget(int x, int y){
        float scale = getScale();
        for(IProgWidget widget : progWidgets) {
            if(!isOutsideProgrammingArea(widget)) {
                if((x - translatedX) / scale - guiLeft >= widget.getX() && (y - translatedY) / scale - guiTop >= widget.getY() && (x - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2 && (y - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2) {
                    return widget;
                }
            }
        }
        return null;
    }

    protected void addAdditionalInfoToTooltip(IProgWidget widget, List<String> tooltip){
        if(widget.getOptionWindow(null) != null) {
            tooltip.add(EnumChatFormatting.GOLD + "Right click for options");
        }
        if(Loader.isModLoaded(ModIds.IGWMOD)) tooltip.add(I18n.format("gui.programmer.pressIForInfo"));
    }

    public void render(int x, int y, boolean showFlow, boolean showInfo, boolean translate){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int origX = x;
        int origY = y;
        x -= translatedX;
        y -= translatedY;
        float scale = getScale();
        x = (int)(x / scale);
        y = (int)(y / scale);

        if(scaleScroll.getState() != lastZoom) {
            float shift = SCALE_PER_STEP * (scaleScroll.getState() - lastZoom);
            if(new Rectangle(guiLeft + startX, guiTop + startY, areaWidth, areaHeight).contains(origX, origY)) {
                translatedX += shift * x;
                translatedY += shift * y;
            } else {
                translatedX += areaWidth / 2 * shift;
                translatedY += areaHeight / 2 * shift;
            }
        }
        lastZoom = scaleScroll.getState();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GL11.glScissor((guiLeft + startX) * sr.getScaleFactor(), (sr.getScaledHeight() - areaHeight - (guiTop + startY)) * sr.getScaleFactor(), areaWidth * sr.getScaleFactor(), areaHeight * sr.getScaleFactor());
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GL11.glPushMatrix();
        GL11.glTranslated(translatedX, translatedY, 0);

        GL11.glScaled(scale, scale, 1);

        if(showFlow) showFlow();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        for(IProgWidget widget : progWidgets) {
            GL11.glPushMatrix();
            GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GL11.glScaled(0.5, 0.5, 1);
            widget.render();
            GL11.glPopMatrix();
        }

        for(IProgWidget widget : progWidgets) {
            List<String> errors = new ArrayList<String>();
            widget.addErrors(errors, progWidgets);
            if(errors.size() > 0) {
                drawBorder(widget, 0xFFFF0000);
            } else {
                List<String> warnings = new ArrayList<String>();
                widget.addWarnings(warnings, progWidgets);
                if(warnings.size() > 0) {
                    drawBorder(widget, 0xFFFFFF00);
                }
            }
        }

        renderAdditionally();

        GL11.glColor4d(1, 1, 1, 1);

        if(showInfo) {
            for(IProgWidget widget : progWidgets) {
                GL11.glPushMatrix();
                GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
                GL11.glScaled(0.5, 0.5, 1);
                widget.renderExtraInfo();
                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        boolean isLeftClicking = Mouse.isButtonDown(0);
        if(translate && isLeftClicking && wasClicking && new Rectangle(guiLeft + startX, guiTop + startY, areaWidth, areaHeight).contains(origX, origY)) {
            translatedX += origX - lastMouseX;
            translatedY += origY - lastMouseY;
        }

        wasClicking = isLeftClicking;
        lastMouseX = origX;
        lastMouseY = origY;
    }

    protected void renderAdditionally(){

    }

    protected void drawBorder(IProgWidget widget, int color){
        GL11.glPushMatrix();
        GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
        GL11.glScaled(0.5, 0.5, 1);
        drawVerticalLine(0, 0, widget.getHeight(), color);
        drawVerticalLine(widget.getWidth(), 0, widget.getHeight(), color);
        drawHorizontalLine(widget.getWidth(), 0, 0, color);
        drawHorizontalLine(widget.getWidth(), 0, widget.getHeight(), color);
        GL11.glPopMatrix();
    }

    private void showFlow(){
        GL11.glLineWidth(1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_LINES);

        for(IProgWidget widget : progWidgets) {
            if(widget instanceof IJump) {
                List<String> jumpLocations = ((IJump)widget).getPossibleJumpLocations();
                if(jumpLocations != null) {
                    for(String jumpLocation : jumpLocations) {
                        if(jumpLocation != null) {
                            for(IProgWidget w : progWidgets) {
                                if(w instanceof ILabel) {
                                    String label = ((ILabel)w).getLabel();
                                    if(label != null && jumpLocation.equals(label)) {
                                        int x1 = widget.getX() + widget.getWidth() / 4;
                                        int y1 = widget.getY() + widget.getHeight() / 4;
                                        int x2 = w.getX() + w.getWidth() / 4;
                                        int y2 = w.getY() + w.getHeight() / 4;
                                        double midX = (x2 + x1) / 2D;
                                        double midY = (y2 + y1) / 2D;
                                        GL11.glVertex3d(guiLeft + x1, guiTop + y1, zLevel);
                                        GL11.glVertex3d(guiLeft + x2, guiTop + y2, zLevel);
                                        Vec3 arrowVec = Vec3.createVectorHelper(x1 - x2, y1 - y2, 0).normalize();
                                        float arrowAngle = (float)Math.toRadians(30);
                                        float arrowSize = 5;
                                        arrowVec.xCoord *= arrowSize;
                                        arrowVec.yCoord *= arrowSize;
                                        arrowVec.rotateAroundZ(arrowAngle);
                                        GL11.glVertex3d(guiLeft + midX, guiTop + midY, zLevel);
                                        GL11.glVertex3d(guiLeft + midX + arrowVec.xCoord, guiTop + midY + arrowVec.yCoord, zLevel);
                                        arrowVec.rotateAroundZ(-2 * arrowAngle);
                                        GL11.glVertex3d(guiLeft + midX, guiTop + midY, zLevel);
                                        GL11.glVertex3d(guiLeft + midX + arrowVec.xCoord, guiTop + midY + arrowVec.yCoord, zLevel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public float getScale(){
        return 2.0F - scaleScroll.getState() * SCALE_PER_STEP;
    }

    public boolean isOutsideProgrammingArea(IProgWidget widget){
        float scale = getScale();
        int x = (int)((widget.getX() + guiLeft) * scale);
        int y = (int)((widget.getY() + guiTop) * scale);
        x += translatedX - guiLeft;
        y += translatedY - guiTop;

        return x < startX || x + widget.getWidth() * scale / 2 > startX + areaWidth || y < startY || y + widget.getHeight() * scale / 2 > startY + areaHeight;
    }

    public void gotoPiece(IProgWidget widget){
        scaleScroll.currentScroll = 0;
        lastZoom = 0;
        translatedX = -widget.getX() * 2 + areaWidth / 2 - guiLeft;
        translatedY = -widget.getY() * 2 + areaHeight / 2 - guiTop;
    }
}
