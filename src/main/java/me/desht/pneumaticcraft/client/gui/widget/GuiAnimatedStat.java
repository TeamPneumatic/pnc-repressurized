package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.base.Strings;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * IMPORTANT: WHEN CHANGING THE PACKAGE OF THIS CLASS, ALSO EDIT GUIANIMATEDSTATSUPPLIER.JAVA!!
 */

public class GuiAnimatedStat implements IGuiAnimatedStat, IGuiWidget, IWidgetListener {
    private static final int ANIMATED_STAT_SPEED = 30;
    private static final int WIDGET_SCROLLBAR_ID = -1000;

    private IGuiAnimatedStat affectingStat;
    private ItemStack iStack = ItemStack.EMPTY;
    private String texture = "";
    private final GuiScreen gui;
    private final List<String> textList = new ArrayList<>();
    private final List<IGuiWidget> widgets = new ArrayList<>();
    private int baseX;
    private int baseY;
    private int affectedY;
    private int width;
    private int height;

    private int oldBaseX;
    private int oldAffectedY;
    private int oldWidth;
    private int oldHeight;
    private boolean isClicked = false;
    private int minWidth = 17;
    private int minHeight = 17;
    private int backGroundColor;
    private Color bgColorHi, bgColorLo;
    private String title;
    private boolean leftSided; // this boolean determines if the stat is going to expand to the left or right.
    private boolean doneExpanding;
    private RenderItem itemRenderer;
    private float textSize;
    private float textScale = 1F;
    private IWidgetListener listener;
    private int curScroll;
    private static final int MAX_LINES = 12;
    private int lastMouseX, lastMouseY;
    private int lineSpacing = 10;
    private int widgetOffsetLeft = 0;
    private int widgetOffsetRight = 0;
    private boolean bevel = false;

    public GuiAnimatedStat(GuiScreen gui, String title, int xPos, int yPos, int backGroundColor,
                           IGuiAnimatedStat affectingStat, boolean leftSided) {
        this.gui = gui;
        baseX = xPos;
        baseY = yPos;
        this.affectingStat = affectingStat;
        width = minWidth;
        height = minHeight;
        this.backGroundColor = backGroundColor;
        calculateColorHighlights(this.backGroundColor);
        setTitle(title);
        texture = "";
        this.leftSided = leftSided;
        if (gui != null) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            if (sr.getScaledWidth() < 520) {
                textSize = (sr.getScaledWidth() - 220) * 0.0033F;
            } else {
                textSize = 1F;
            }
        } else {
            textSize = 1;
        }

        affectedY = baseY;
        if (affectingStat != null) {
            affectedY += affectingStat.getAffectedY() + affectingStat.getHeight();
        }
    }

    public GuiAnimatedStat(GuiScreen gui, int backgroundColor) {
        this(gui, "", 0, 0, backgroundColor, null, false);
    }

    public GuiAnimatedStat(GuiScreen gui, int backgroundColor, ItemStack icon) {
        this(gui, backgroundColor);
        iStack = icon;
    }

    public GuiAnimatedStat(GuiScreen gui, int backgroundColor, String texture) {
        this(gui, backgroundColor);
        this.texture = texture;
    }

    public GuiAnimatedStat(GuiScreen gui, String title, @Nonnull ItemStack icon, int xPos, int yPos, int backGroundColor,
                           IGuiAnimatedStat affectingStat, boolean leftSided) {
        this(gui, title, xPos, yPos, backGroundColor, affectingStat, leftSided);
        iStack = icon;
    }

    public GuiAnimatedStat(GuiScreen gui, String title, String texture, int xPos, int yPos, int backGroundColor,
                           IGuiAnimatedStat affectingStat, boolean leftSided) {
        this(gui, title, xPos, yPos, backGroundColor, affectingStat, leftSided);
        this.texture = texture;
    }

    @Override
    public void setParentStat(IGuiAnimatedStat stat) {
        affectingStat = stat;
    }

    public void addWidget(IGuiWidget widget) {
        widgets.add(widget);
        widget.setListener(this);
    }

    public void removeWidget(IGuiWidget widget) {
        widgets.remove(widget);
    }

    public void setWidgetOffsets(int left, int right) {
        widgetOffsetLeft = left;
        widgetOffsetRight = right;
    }

    @Override
    public Rectangle getButtonScaledRectangle(int origX, int origY, int width, int height) {
        int scaledX = (int) (origX * textSize);
        int scaledY = (int) (origY * textSize);
        return new Rectangle(scaledX, scaledY, (int) (width * textSize), (int) (height * textSize));
    }

    @Override
    public void scaleTextSize(float scale) {
        textSize *= scale;
        textScale = scale;

        for (IGuiWidget widget : widgets) {
            if (widget.getID() == WIDGET_SCROLLBAR_ID) {
                widgets.remove(widget);
                break;
            }
        }
        onTextChange();
    }

    @Override
    public boolean isLeftSided() {
        return leftSided;
    }

    @Override
    public void setLeftSided(boolean leftSided) {
        this.leftSided = leftSided;
    }

    @Override
    public IGuiAnimatedStat setText(List<String> text) {
        textList.clear();
        for (String line : text) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(line), (int) (GuiConstants.MAX_CHAR_PER_LINE_LEFT / textScale)));
        }
        onTextChange();
        return this;
    }

    @Override
    public IGuiAnimatedStat setText(String text) {
        textList.clear();
        textList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(text), (int) (GuiConstants.MAX_CHAR_PER_LINE_LEFT / textScale)));
        onTextChange();
        return this;
    }

    @Override
    public void setTextWithoutCuttingString(List<String> text) {
        textList.clear();
        textList.addAll(text);
        onTextChange();
    }

    @Override
    public void appendText(List<String> text) {
        for (String line : text) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(line), (int) (GuiConstants.MAX_CHAR_PER_LINE_LEFT / textScale)));
        }
        onTextChange();
    }

    @Override
    public void addPadding(int nRows, int nCols) {
        String s = Strings.repeat(" ", nCols);
        setTextWithoutCuttingString(IntStream.range(0, nRows).mapToObj(i -> s).collect(Collectors.toList()));
    }

    @Override
    public void addPadding(List<String> text, int nRows, int nCols) {
        String s = Strings.repeat(" ", nCols);
        List<String> l = IntStream.range(0, nRows).mapToObj(i -> s).collect(Collectors.toList());
        for (int i = 0; i < text.size() && i < nRows; i++) {
            l.set(i, text.get(i));
        }
        setTextWithoutCuttingString(l);
    }

    @Override
    public void setBackGroundColor(int backGroundColor) {
        if (backGroundColor != this.backGroundColor) {
            this.backGroundColor = backGroundColor;
            calculateColorHighlights(backGroundColor);
        }
    }

    private void calculateColorHighlights(int color) {
        if (ConfigHandler.client.guiBevel) {
            float fgR = (float) (color >> 16 & 255) / 255.0F;
            float fgG = (float) (color >> 8 & 255) / 255.0F;
            float fgB = (float) (color & 255) / 255.0F;
            float fgA = (float) (color >> 24 & 255) / 255.0F;
            if (bevel) {
                bgColorHi = new Color(fgR, fgG, fgB, fgA).brighter();
                bgColorLo = new Color(fgR, fgG, fgB, fgA).darker();
            } else {
                bgColorHi = new Color(fgR, fgG, fgB, fgA).darker().darker();
                bgColorLo = bgColorHi;
            }
        } else {
            bgColorLo = bgColorHi = Color.BLACK;
        }
    }

    @Override
    public void setBeveled(boolean bevel) {
        this.bevel = bevel;
        calculateColorHighlights(backGroundColor);
    }

    private void onTextChange() {
        // add/remove a scrollbar, as necessary
        if (textList.size() > MAX_LINES) {
            for (IGuiWidget widget : widgets) {
                if (widget.getID() == WIDGET_SCROLLBAR_ID) return;
            }
            curScroll = 0;
            addWidget(new WidgetVerticalScrollbar(WIDGET_SCROLLBAR_ID, leftSided ? -16 : 2, 20, (int) ((MAX_LINES * lineSpacing - 20) * textSize)).setStates(textList.size() - MAX_LINES));
        } else {
            Iterator<IGuiWidget> iterator = widgets.iterator();
            while (iterator.hasNext()) {
                IGuiWidget widget = iterator.next();
                if (widget.getID() == WIDGET_SCROLLBAR_ID) {
                    iterator.remove();
                    curScroll = 0;
                }
            }
        }
    }

    @Override
    public void setMinDimensionsAndReset(int minWidth, int minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        width = minWidth;
        height = minHeight;
    }

    @Override
    public void update() {
        oldBaseX = baseX;
        oldAffectedY = affectedY;
        oldWidth = width;
        oldHeight = height;

        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        doneExpanding = true;
        if (isClicked) {
            // calculate the width and height needed for the box to fit the strings.
            int maxWidth = fontRenderer.getStringWidth(title);
            for (String line : textList) {
                if (fontRenderer.getStringWidth(line) > maxWidth) maxWidth = fontRenderer.getStringWidth(line);
            }
            maxWidth = (int) (maxWidth * textSize) + 20;

            int maxHeight = title.isEmpty() ? 2 : 12;
            if (!textList.isEmpty()) {
                maxHeight += 4 + Math.min(MAX_LINES, textList.size()) * lineSpacing;
            }
            maxHeight -= (lineSpacing - fontRenderer.FONT_HEIGHT);
            maxHeight = (int) (maxHeight * textSize);

            // expand the box
            width = Math.min(maxWidth, width + ANIMATED_STAT_SPEED);
            height = Math.min(maxHeight, height + ANIMATED_STAT_SPEED);
            doneExpanding = width == maxWidth && height == maxHeight;

            if (doneExpanding) {
                for (IGuiWidget widget : widgets) {
                    if (widget.getID() == WIDGET_SCROLLBAR_ID) {
                        curScroll = ((WidgetVerticalScrollbar) widget).getState();
                        break;
                    }
                }
            }
        } else {
            // contract the box
            width = Math.max(minWidth, width - ANIMATED_STAT_SPEED);
            height = Math.max(minHeight, height - ANIMATED_STAT_SPEED);
            doneExpanding = false;
        }

        affectedY = baseY;
        if (affectingStat != null) {
            affectedY += affectingStat.getAffectedY() + affectingStat.getHeight();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        float zLevel = 0;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int renderBaseX = (int) (oldBaseX + (baseX - oldBaseX) * partialTicks);
        int renderAffectedY = (int) (oldAffectedY + (affectedY - oldAffectedY) * partialTicks);
        int renderWidth = (int) (oldWidth + (width - oldWidth) * partialTicks);
        int renderHeight = (int) (oldHeight + (height - oldHeight) * partialTicks);

        if (leftSided) renderWidth *= -1;
        Gui.drawRect(renderBaseX, renderAffectedY, renderBaseX + renderWidth, renderAffectedY + renderHeight, backGroundColor);
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(3.0F);
        GlStateManager.color(0, 0, 0, 1);
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        float[] c1 = leftSided ? bgColorLo.getComponents(null) : bgColorHi.getComponents(null);
        float[] c2 = bgColorHi.getComponents(null);
        float[] c3 = leftSided ? bgColorHi.getComponents(null) : bgColorLo.getComponents(null);
        float[] c4 = bgColorLo.getComponents(null);
        wr.pos(renderBaseX, renderAffectedY, zLevel).color(c1[0], c1[1], c1[2], c1[3]).endVertex();
        wr.pos(renderBaseX + renderWidth, renderAffectedY, zLevel).color(c2[0], c2[1], c2[2], c2[3]).endVertex();
        wr.pos(renderBaseX + renderWidth, renderAffectedY + renderHeight, zLevel).color(c3[0], c3[1], c3[2],c3[3]).endVertex();
        wr.pos(renderBaseX, renderAffectedY + renderHeight, zLevel).color(c4[0], c4[1], c4[2], c4[3]).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        if (leftSided) renderWidth *= -1;

        // if done expanding, draw the information
        int titleYoffset = title.isEmpty() ? 2 : 12;
        if (doneExpanding) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(renderBaseX + (leftSided ? -renderWidth : 16), renderAffectedY, 0);
            GlStateManager.scale(textSize, textSize, textSize);
            GlStateManager.translate(-renderBaseX - (leftSided ? -renderWidth : 16), -renderAffectedY, 0);
            if (!title.isEmpty()) {
                fontRenderer.drawStringWithShadow(title, renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + 2, 0xFFFF00);
            }
            for (int i = curScroll; i < textList.size() && i < curScroll + MAX_LINES; i++) {
                if (textList.get(i).contains("\u00a70") || textList.get(i).contains(TextFormatting.DARK_RED.toString())) {
                    fontRenderer.drawString(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset, 0xFFFFFF);
                } else {
                    fontRenderer.drawStringWithShadow(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset, 0xFFFFFF);
                }
            }
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(renderBaseX + (leftSided ? widgetOffsetLeft : widgetOffsetRight), renderAffectedY + (titleYoffset - 10), 0);
            GlStateManager.enableTexture2D();
            for (IGuiWidget widget : widgets)
                widget.render(mouseX - renderBaseX, mouseY - renderAffectedY, partialTicks);
            GlStateManager.popMatrix();
        }
        if (renderHeight > 16 && renderWidth > 16) {
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (iStack.isEmpty()) {
                if (texture.contains(Textures.GUI_LOCATION)) {
                    GuiPneumaticContainerBase.drawTexture(texture, renderBaseX - (leftSided ? 16 : 0), renderAffectedY);
                } else {
                    fontRenderer.drawString(texture, renderBaseX - (leftSided ? 16 : 0), renderAffectedY, 0xFFFFFFFF);
                }
            } else if (gui != null || !(iStack.getItem() instanceof ItemBlock)) {
                if (itemRenderer == null) itemRenderer = Minecraft.getMinecraft().getRenderItem();
                itemRenderer.zLevel = 1;
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, -50);
                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();
                itemRenderer.renderItemAndEffectIntoGUI(iStack, renderBaseX - (leftSided ? 16 : 0), renderAffectedY);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.popMatrix();
                GlStateManager.enableAlpha();
            }
            GlStateManager.disableBlend();
        }
    }

    /*
     * button: 0 = left 1 = right 2 = middle
     */
    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            isClicked = !isClicked;
            listener.actionPerformed(this);
        }
        mouseX -= baseX;
        mouseY -= affectedY;
        for (IGuiWidget widget : widgets) {
            if (widget.getBounds().contains(mouseX, mouseY)) {
                widget.onMouseClicked(mouseX, mouseY, button);
                isClicked = true;
            }
        }
    }

    @Override
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button) {

    }

    @Override
    public void closeWindow() {
        isClicked = false;
    }

    @Override
    public void openWindow() {
        isClicked = true;
    }

    @Override
    public boolean isClicked() {
        return isClicked;
    }

    @Override
    public int getAffectedY() {
        return affectedY;
    }

    @Override
    public int getBaseX() {
        return baseX;
    }

    @Override
    public int getBaseY() {
        return baseY;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setBaseY(int y) {
        baseY = y;
    }

    @Override
    public void setTitle(String title) {
        this.title = I18n.format(title);
    }

    @Override
    public boolean isDoneExpanding() {
        return doneExpanding;
    }

    @Override
    public void setBaseX(int x) {
        baseX = x;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(baseX - (leftSided ? width : 0), affectedY, width, height);
    }

    @Override
    public void setListener(IWidgetListener gui) {
        listener = gui;
    }

    @Override
    public int getID() {
        return -1;
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        isClicked = !isClicked;
        listener.actionPerformed(widget);
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
        listener.onKeyTyped(widget);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {

        if (mouseIsHoveringOverIcon(mouseX, mouseY)) {
            curTooltip.add(title);
        }

        for (IGuiWidget widget : widgets)
            if (isMouseOverWidget(widget, mouseX, mouseY)) widget.addTooltip(mouseX, mouseY, curTooltip, shiftPressed);
    }

    private boolean mouseIsHoveringOverIcon(int x, int y) {
        if (leftSided) {
            return x <= baseX && x >= baseX - 16 && y >= affectedY && y <= affectedY + 16;
        } else {
            return x >= baseX && x <= baseX + 16 && y >= affectedY && y <= affectedY + 16;
        }
    }

    @Override
    public boolean onKey(char key, int keyCode) {
        for (IGuiWidget widget : widgets)
            if (widget.onKey(key, keyCode)) return true;
        return false;
    }

    private boolean isMouseOverWidget(IGuiWidget widget, int mouseX, int mouseY) {
        Rectangle rect = getBounds();
        mouseX -= rect.x;
        mouseY -= rect.y;
        return widget.getBounds().contains(mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() {
        if (getBounds().contains(lastMouseX, lastMouseY)) {
            handleMouseWheel(Mouse.getDWheel());
        }
    }

    public boolean handleMouseWheel(int mouseWheel) {
        for (IGuiWidget widget : widgets) {
            widget.handleMouseInput();
            if (widget.getID() == WIDGET_SCROLLBAR_ID) {
                int wheel = -mouseWheel;
                wheel = MathHelper.clamp(wheel, -1, 1);
                ((WidgetVerticalScrollbar) widget).currentScroll += (float) wheel / (textList.size() - MAX_LINES);
                return true;
            }
        }
        return false;
    }

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick) {

    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }
}
