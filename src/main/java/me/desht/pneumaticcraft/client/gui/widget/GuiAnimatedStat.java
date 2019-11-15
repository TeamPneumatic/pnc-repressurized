package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class GuiAnimatedStat extends Widget implements IGuiAnimatedStat, ITooltipSupplier/*, IGuiWidget, IWidgetListener*/ {
    private static final int ANIMATED_STAT_SPEED = 30;
    private static final int MIN_WIDTH_HEIGHT = 17;

    private IGuiAnimatedStat affectingStat;

    private StatIcon statIcon;

    private final Screen gui;
    private final List<String> textList = new ArrayList<>();
    private final List<Widget> subWidgets = new ArrayList<>();
//    private int baseX;
//    private int baseY;
    private int affectedY;
//    private int width;
//    private int height;

    private int oldBaseX;
    private int oldAffectedY;
    private int oldWidth;
    private int oldHeight;
    private boolean isClicked = false;
    private int minWidth = MIN_WIDTH_HEIGHT;
    private int minHeight = MIN_WIDTH_HEIGHT;
    private int backGroundColor;
    private Color bgColorHi, bgColorLo;
    private String title;
    private boolean leftSided; // determines if the stat is going to expand to the left or right.
    private boolean doneExpanding;
    private float textSize;
    private float textScale = 1F;
//    private IWidgetListener listener;
    private int curScroll;
    private static final int MAX_LINES = 12;
//    private int lastMouseX, lastMouseY;
    private int lineSpacing = 10;
    private int widgetOffsetLeft = 0;
    private int widgetOffsetRight = 0;
    private boolean bevel = false;
    private WidgetVerticalScrollbar scrollBar = null;

    public GuiAnimatedStat(Screen gui, String title, int xPos, int yPos, int backGroundColor,
                           IGuiAnimatedStat affectingStat, boolean leftSided) {
        super(xPos, yPos, MIN_WIDTH_HEIGHT, MIN_WIDTH_HEIGHT, title);

        this.gui = gui;
//        baseX = xPos;
//        baseY = yPos;
        this.affectingStat = affectingStat;
//        width = minWidth;
//        height = minHeight;
        this.backGroundColor = backGroundColor;
        calculateColorHighlights(this.backGroundColor);
        setTitle(title);
        statIcon = StatIcon.NONE;
        this.leftSided = leftSided;
        textSize = 1;

        affectedY = y;
        if (affectingStat != null) {
            affectedY += affectingStat.getAffectedY() + affectingStat.getHeight();
        }
    }

    public GuiAnimatedStat(Screen gui, int backgroundColor) {
        this(gui, "", 0, 0, backgroundColor, null, false);
    }

    public GuiAnimatedStat(Screen gui, int backgroundColor, ItemStack icon) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(icon);
    }

    public GuiAnimatedStat(Screen gui, int backgroundColor, String texture) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(RL(texture));
    }

    public GuiAnimatedStat(Screen gui, String title, StatIcon icon, int xPos, int yPos, int backGroundColor,
                           IGuiAnimatedStat affectingStat, boolean leftSided) {
        this(gui, title, xPos, yPos, backGroundColor, affectingStat, leftSided);
        statIcon = icon;
    }

    public GuiAnimatedStat(Screen gui, String title, StatIcon icon, int backGroundColor,
                           IGuiAnimatedStat affectingStat, ArmorHUDLayout.LayoutItem layout) {
        this(gui, title, 0, 0, backGroundColor, affectingStat, layout.isLeftSided());
        MainWindow mw = Minecraft.getInstance().mainWindow;
        int x = layout.getX() == -1 ? mw.getScaledWidth() - 2 : (int) (mw.getScaledWidth() * layout.getX());
        setBaseX(x);
        setBaseY((int) (mw.getScaledHeight() * layout.getY()));
        statIcon = icon;
    }

    @Override
    public void setParentStat(IGuiAnimatedStat stat) {
        affectingStat = stat;
    }

    public void addSubWidget(Widget widget) {
        subWidgets.add(widget);
    }

    public void removeSubWidget(Widget widget) {
        subWidgets.remove(widget);
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

        subWidgets.removeIf(w -> w == scrollBar);
        scrollBar = null;

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

    @Override
    public int getBackgroundColor() {
        return backGroundColor;
    }

    private void calculateColorHighlights(int color) {
        if (PNCConfig.Client.guiBevel) {
            float fgR = (float) (color >> 16 & 255) / 255.0F;
            float fgG = (float) (color >> 8 & 255) / 255.0F;
            float fgB = (float) (color & 255) / 255.0F;
            float fgA = (float) (color >> 24 & 255) / 255.0F;
            Color c = new Color(fgR, fgG, fgB, fgA);
            if (bevel) {
                bgColorHi = c.brighter();
                bgColorLo = c.darker();
            } else {
                bgColorHi = c.darker().darker();
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
            for (Widget widget : subWidgets) {
                if (widget == scrollBar) return;
            }
            curScroll = 0;
            addSubWidget(scrollBar = new WidgetVerticalScrollbar(leftSided ? -16 : 2, 20, (int) ((MAX_LINES * lineSpacing - 20) * textSize)).setStates(textList.size() - MAX_LINES));
        } else {
            Iterator<Widget> iterator = subWidgets.iterator();
            while (iterator.hasNext()) {
                Widget widget = iterator.next();
                if (widget == scrollBar) {
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
    public void tick() {
        oldBaseX = x;
        oldAffectedY = affectedY;
        oldWidth = width;
        oldHeight = height;

        doneExpanding = true;
        if (isClicked) {
            Pair<Integer, Integer> maxSize = calculateMaxSize();
            int maxWidth = maxSize.getLeft(), maxHeight = maxSize.getRight();

            // expand the box
            width = Math.min(maxWidth, width + ANIMATED_STAT_SPEED);
            height = Math.min(maxHeight, height + ANIMATED_STAT_SPEED);
            doneExpanding = width == maxWidth && height == maxHeight;

            Pair<Integer,Integer> size = PneumaticCraftRepressurized.proxy.getScaledScreenSize();
            if (isLeftSided()) {
                if (x >= size.getLeft()) x = size.getLeft();
            } else {
                if (x < 0) x = 1;
            }
            if (y + height >= size.getRight()) {
                y = size.getRight() - height - 1;
            }

            if (doneExpanding) {
                for (Widget widget : subWidgets) {
                    if (widget == scrollBar) {
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

        affectedY = y;
        if (affectingStat != null) {
            affectedY += affectingStat.getAffectedY() + affectingStat.getHeight();
        }
    }

    private Pair<Integer,Integer> calculateMaxSize() {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

        // scale the box down if necessary to avoid extending beyond screen edge
        // (should only be an issue for very low scaled X resolution)
        int availableWidth;
        if (gui instanceof ContainerScreen) {
            ContainerScreen gc = (ContainerScreen) gui;
            availableWidth = leftSided ? gc.getGuiLeft() : gc.width - (gc.getGuiLeft() + gc.getXSize());
        } else {
            availableWidth = Minecraft.getInstance().mainWindow.getScaledWidth();
        }

        // calculate the width and height needed for the box to fit the strings.
        int maxWidth = fontRenderer.getStringWidth(title);
        for (String line : textList) {
            maxWidth = Math.max(maxWidth, fontRenderer.getStringWidth(line));
        }
        maxWidth += 20;  // to allow space for the scrollbar, where necessary

        int maxHeight = title.isEmpty() ? 6 : 16;
        if (!textList.isEmpty()) {
            maxHeight += Math.min(MAX_LINES, textList.size()) * lineSpacing;
        }
        maxHeight -= (lineSpacing - fontRenderer.FONT_HEIGHT);

        float lastTextSize = textSize;
        if (maxWidth > availableWidth - 3) {
            textSize = (availableWidth - 3f) / maxWidth;
            maxWidth = (int) (maxWidth * textSize);
            maxHeight = (int) (maxHeight * textSize);
        } else {
            textSize = 1.0f;
        }
        if (lastTextSize != textSize) {
            float newTextSize = textSize;
            textSize = 1.0f;
            scaleTextSize(newTextSize);
        }

        //noinspection SuspiciousNameCombination
        return Pair.of(maxWidth, maxHeight);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
//        lastMouseX = mouseX;
//        lastMouseY = mouseY;
        float zLevel = 0;
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int renderBaseX = (int) (oldBaseX + (x - oldBaseX) * partialTicks);
        int renderAffectedY = (int) (oldAffectedY + (affectedY - oldAffectedY) * partialTicks);
        int renderWidth = (int) (oldWidth + (width - oldWidth) * partialTicks);
        int renderHeight = (int) (oldHeight + (height - oldHeight) * partialTicks);

        if (leftSided) renderWidth *= -1;
        AbstractGui.fill(renderBaseX, renderAffectedY, renderBaseX + renderWidth, renderAffectedY + renderHeight, backGroundColor);
        GlStateManager.disableTexture();
        GlStateManager.lineWidth(3.0F);
        GlStateManager.color4f(0, 0, 0, 1);
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
        GlStateManager.enableTexture();
        if (leftSided) renderWidth *= -1;

        // if done expanding, draw the information
        int titleYoffset = title.isEmpty() ? 3 : 12;
        if (doneExpanding) {
            GlStateManager.pushMatrix();
            GlStateManager.translated(renderBaseX + (leftSided ? -renderWidth : 16), renderAffectedY, 0);
            GlStateManager.scaled(textSize, textSize, textSize);
            GlStateManager.translated(-renderBaseX - (leftSided ? -renderWidth : 16), -renderAffectedY, 0);
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
            GlStateManager.translated(renderBaseX + (leftSided ? widgetOffsetLeft : widgetOffsetRight), renderAffectedY + (titleYoffset - 10), 0);
            GlStateManager.enableTexture();
            for (Widget widget : subWidgets)
                widget.render(mouseX - renderBaseX, mouseY - renderAffectedY, partialTicks);
            GlStateManager.popMatrix();
        }
        if (renderHeight > 16 && renderWidth > 16 && statIcon != null) {
            statIcon.render(gui, renderBaseX, renderAffectedY, leftSided);
        }
    }

    private void toggle() {
        isClicked = !isClicked;
        if (isClicked && gui instanceof GuiPneumaticContainerBase) {
            // close any open stats on the same side
            List<IGuiAnimatedStat> widgets = ((GuiPneumaticContainerBase) gui).getStatWidgets();
            widgets.stream()
                    .filter(stat -> this != stat && stat.isLeftSided() == isLeftSided()) // when the stat is on the same side, close it.
                    .forEach(IGuiAnimatedStat::closeWindow);
        }
    }

    /*
     * button: 0 = left 1 = right 2 = middle
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                if (clicked(mouseX, mouseY)) {
                    for (Widget widget : subWidgets) {
                        if (widget.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                    }
                }
                // no sub-widgets took the click; toggle ourselves
                toggle();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
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
        return x;
    }

    @Override
    public int getBaseY() {
        return y;
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
        this.y = y;
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
        this.x = x;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Rectangle2d getBounds() {
        return new Rectangle2d(x - (leftSided ? width : 0), affectedY, width, height);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {
        if (mouseIsHoveringOverIcon(mouseX, mouseY)) {
            curTooltip.add(title);
        }

        for (Widget widget : subWidgets)
            if (widget.isHovered() && widget instanceof ITooltipSupplier) {
                ((ITooltipSupplier) widget).addTooltip(mouseX, mouseY, curTooltip, shiftPressed);
            }
    }

    private boolean mouseIsHoveringOverIcon(int x, int y) {
        if (leftSided) {
            return x <= this.x && x >= this.x - 16 && y >= affectedY && y <= affectedY + 16;
        } else {
            return x >= this.x && x <= this.x + 16 && y >= affectedY && y <= affectedY + 16;
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        for (Widget widget : subWidgets) {
            if (widget.mouseScrolled(x, y, dir)) {
                return true;
            }
        }
        return false;
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public void setTexture(ResourceLocation texture) {
        this.statIcon = StatIcon.of(texture);
    }

    public void setTexture(ItemStack itemStack) {
        this.statIcon = StatIcon.of(itemStack);
    }

    public static class StatIcon {
        public static final StatIcon NONE = new StatIcon(ItemStack.EMPTY, null);

        private final ItemStack stack;
        private final ResourceLocation texture;

        private StatIcon(ItemStack stack, ResourceLocation texture) {
            this.stack = stack;
            this.texture = texture;
        }

        public static StatIcon of(ItemStack stack) {
            return new StatIcon(stack, null);
        }

        public static StatIcon of(Item item) {
            return new StatIcon(new ItemStack(item, 1), null);
        }

        public static StatIcon of(ResourceLocation texture) {
            return new StatIcon(ItemStack.EMPTY, texture);
        }

        void render(AbstractGui gui, int x, int y, boolean leftSided) {
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (texture != null) {
                GuiPneumaticContainerBase.drawTexture(texture, x - (leftSided ? 16 : 0), y);
            } else if (!stack.isEmpty() && gui != null || !(stack.getItem() instanceof BlockItem)) {
                ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
                renderItem.zLevel = 1;
                GlStateManager.pushMatrix();
                GlStateManager.translated(0, 0, -50);
                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();
                renderItem.renderItemAndEffectIntoGUI(stack, x - (leftSided ? 16 : 0), y);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.popMatrix();
                GlStateManager.enableAlphaTest();
            }
            GlStateManager.disableBlend();
        }
    }
}
