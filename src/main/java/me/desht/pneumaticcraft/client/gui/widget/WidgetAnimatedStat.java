package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.base.Strings;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.client.event.ClientEventHandler;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
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
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class WidgetAnimatedStat extends Widget implements IGuiAnimatedStat, ITooltipProvider {
    private static final int ANIMATED_STAT_SPEED = 30;
    private static final int MIN_WIDTH_HEIGHT = 17;
    private static final int MAX_LINES = 12;

    // avoid drop shadows on dark coloured text, because it looks terrible
    private static final Pattern DARK_FORMATTING = Pattern.compile("\\u00A7[0123458]");

    private IGuiAnimatedStat affectingStat;

    private StatIcon statIcon;

    private final Screen gui;
    private final List<String> textList = new ArrayList<>();
    private final List<Widget> subWidgets = new ArrayList<>();
    private int affectedY;

    private int oldBaseX;
    private int oldAffectedY;
    private int oldWidth;
    private int oldHeight;
    private boolean isClicked = false;
    private int minWidth = MIN_WIDTH_HEIGHT;
    private int minHeight = MIN_WIDTH_HEIGHT;
    private int backGroundColor;
    private TintColor bgColorHi, bgColorLo;
    private String title;
    private boolean leftSided; // determines if the stat is going to expand to the left or right.
    private boolean doneExpanding;
    private float textSize;
    private float textScale = 1F;
    private int curScroll;
    private int lineSpacing = 10;
    private int widgetOffsetLeft = 0;
    private int widgetOffsetRight = 0;
    private boolean bevel = false;
    private WidgetVerticalScrollbar scrollBar = null;

    public WidgetAnimatedStat(Screen gui, String title, int xPos, int yPos, int backGroundColor,
                              IGuiAnimatedStat affectingStat, boolean leftSided) {
        super(xPos, yPos, MIN_WIDTH_HEIGHT, MIN_WIDTH_HEIGHT, title);

        this.gui = gui;
        this.affectingStat = affectingStat;
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

    public WidgetAnimatedStat(Screen gui, int backgroundColor) {
        this(gui, "", 0, 0, backgroundColor, null, false);
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor, ItemStack icon) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(icon);
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor, String texture) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(RL(texture));
    }

    public WidgetAnimatedStat(Screen gui, String title, StatIcon icon, int xPos, int yPos, int backGroundColor,
                              IGuiAnimatedStat affectingStat, boolean leftSided) {
        this(gui, title, xPos, yPos, backGroundColor, affectingStat, leftSided);
        statIcon = icon;
    }

    public WidgetAnimatedStat(Screen gui, String title, StatIcon icon, int backGroundColor,
                              IGuiAnimatedStat affectingStat, ArmorHUDLayout.LayoutItem layout) {
        this(gui, title, 0, 0, backGroundColor, affectingStat, layout.isLeftSided());
        MainWindow mw = Minecraft.getInstance().getMainWindow();
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
    public Rectangle2d getButtonScaledRectangle(int origX, int origY, int width, int height) {
        int scaledX = (int) (origX * textSize);
        int scaledY = (int) (origY * textSize);
        return new Rectangle2d(scaledX, scaledY, (int) (width * textSize), (int) (height * textSize));
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
            textList.addAll(PneumaticCraftUtils.splitString(I18n.format(line), (int) (GuiConstants.MAX_CHAR_PER_LINE_LEFT / textScale)));
        }
        onTextChange();
        return this;
    }

    @Override
    public IGuiAnimatedStat setText(String text) {
        textList.clear();
        textList.addAll(PneumaticCraftUtils.splitString(I18n.format(text), (int) (GuiConstants.MAX_CHAR_PER_LINE_LEFT / textScale)));
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
            textList.addAll(PneumaticCraftUtils.splitString(I18n.format(line), (int) (GuiConstants.MAX_CHAR_PER_LINE_LEFT / textScale)));
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
    public void setBackgroundColor(int backgroundColor) {
        if (backgroundColor != this.backGroundColor) {
            this.backGroundColor = backgroundColor;
            calculateColorHighlights(backgroundColor);
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
            TintColor c = new TintColor(fgR, fgG, fgB, fgA);
            if (bevel) {
                bgColorHi = c.brighter();
                bgColorLo = c.darker();
            } else {
                bgColorHi = c.darker().darker();
                bgColorLo = bgColorHi;
            }
        } else {
            bgColorLo = bgColorHi = TintColor.BLACK;
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
            int h = (int) ((MAX_LINES * lineSpacing - 20) * textSize);
            addSubWidget(scrollBar = new WidgetVerticalScrollbar(leftSided ? -16 : 2, 20, h)
                    .setStates(textList.size() - MAX_LINES)
                    .setListening(true));
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
    public void tickWidget() {
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

            Pair<Integer,Integer> size = ClientEventHandler.getScaledScreenSize();
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
            availableWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
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
    public void render(int x, int y, float partialTicks) {
        if (this.visible) {
            int baseX = leftSided ? this.x - this.width : this.x;
            this.isHovered = x >= baseX && y >= this.affectedY && x < baseX + this.width && y < this.affectedY + this.height;
            renderButton(x, y, partialTicks);
        }
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (leftSided) {
            return this.active && this.visible
                    && mouseX >= (double)this.x - this.width
                    && mouseX < (double)this.x
                    && mouseY >= (double)this.y
                    && mouseY < (double)(this.y + this.height);
        } else {
            return super.clicked(mouseX, mouseY);
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        float zLevel = 0;
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int renderBaseX = (int) (oldBaseX + (x - oldBaseX) * partialTicks);
        int renderAffectedY = (int) (oldAffectedY + (affectedY - oldAffectedY) * partialTicks);
        int renderWidth = (int) (oldWidth + (width - oldWidth) * partialTicks);
        int renderHeight = (int) (oldHeight + (height - oldHeight) * partialTicks);

        if (leftSided) renderWidth *= -1;
        AbstractGui.fill(renderBaseX, renderAffectedY, renderBaseX + renderWidth, renderAffectedY + renderHeight, backGroundColor);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(3.0F);
        RenderSystem.color4f(0, 0, 0, 1);
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
        RenderSystem.enableTexture();
        if (leftSided) renderWidth *= -1;

        // if done expanding, draw the information
        int titleYoffset = title.isEmpty() ? 3 : 12;
        if (doneExpanding) {
            RenderSystem.pushMatrix();

            RenderSystem.translated(renderBaseX + (leftSided ? -renderWidth : 16), renderAffectedY, 0);
            RenderSystem.scaled(textSize, textSize, textSize);
            RenderSystem.translated(-renderBaseX - (leftSided ? -renderWidth : 16), -renderAffectedY, 0);
            if (!title.isEmpty()) {
                fontRenderer.drawStringWithShadow(title, renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + 2, 0xFFFF00);
            }
            for (int i = curScroll; i < textList.size() && i < curScroll + MAX_LINES; i++) {
                if (DARK_FORMATTING.matcher(textList.get(i)).find()) {
                    fontRenderer.drawString(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset, 0xFFFFFF);
                } else {
                    fontRenderer.drawStringWithShadow(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset, 0xFFFFFF);
                }
            }
            RenderSystem.popMatrix();

            RenderSystem.pushMatrix();
            RenderSystem.translated(renderBaseX + (leftSided ? widgetOffsetLeft : widgetOffsetRight), renderAffectedY + (titleYoffset - 10), 0);
            RenderSystem.enableTexture();

            subWidgets.forEach(widget -> widget.render(mouseX - renderBaseX, mouseY - renderAffectedY, partialTicks));

            RenderSystem.popMatrix();
        }
        if (renderHeight > 16 && renderWidth > 16 && statIcon != null) {
            statIcon.render(renderBaseX, renderAffectedY, leftSided);
        }
    }

    public void render3d(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        int renderBaseX = (int) (oldBaseX + (x - oldBaseX) * partialTicks);
        int renderAffectedY = (int) (oldAffectedY + (affectedY - oldAffectedY) * partialTicks);
        int renderWidth = (int) (oldWidth + (width - oldWidth) * partialTicks);
        int renderHeight = (int) (oldHeight + (height - oldHeight) * partialTicks);

        // quad bg
        int[] cols = RenderUtils.decomposeColor(backGroundColor);
        RenderUtils.renderWithType(matrixStack, buffer, ModRenderTypes.getUntexturedQuad(true), (posMat, builder) -> {
            int rw = leftSided ? -renderWidth : renderWidth;
            builder.pos(posMat, (float)renderBaseX, (float)renderAffectedY + renderHeight, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
            builder.pos(posMat, (float)renderBaseX + rw, (float)renderAffectedY + renderHeight, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
            builder.pos(posMat, (float)renderBaseX + rw, (float)renderAffectedY, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
            builder.pos(posMat, (float)renderBaseX, (float)renderAffectedY, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
        });

        // line loops border
        RenderUtils.renderWithType(matrixStack, buffer, ModRenderTypes.getLineLoopsTransparent(5.0f), (posMat, builder) -> {
            int rw = leftSided ? -renderWidth : renderWidth;
            float[] c1 = leftSided ? bgColorLo.getComponents(null) : bgColorHi.getComponents(null);
            float[] c2 = bgColorHi.getComponents(null);
            float[] c3 = leftSided ? bgColorHi.getComponents(null) : bgColorLo.getComponents(null);
            float[] c4 = bgColorLo.getComponents(null);
            builder.pos(posMat, renderBaseX, renderAffectedY, 0).color(c1[0], c1[1], c1[2], c1[3]).endVertex();
            builder.pos(posMat, renderBaseX + rw, renderAffectedY, 0).color(c2[0], c2[1], c2[2], c2[3]).endVertex();
            builder.pos(posMat, renderBaseX + rw, renderAffectedY + renderHeight, 0).color(c3[0], c3[1], c3[2],c3[3]).endVertex();
            builder.pos(posMat, renderBaseX, renderAffectedY + renderHeight, 0).color(c4[0], c4[1], c4[2], c4[3]).endVertex();
        });

        if (doneExpanding) {
            matrixStack.push();
            matrixStack.translate(renderBaseX + (leftSided ? -renderWidth : 16), renderAffectedY, 0);
            matrixStack.scale(textSize, textSize, textSize);
            matrixStack.translate(-renderBaseX - (leftSided ? -renderWidth : 16), -renderAffectedY, 0);
            // text title
            if (!title.isEmpty()) {
                RenderUtils.renderString3d(TextFormatting.BOLD + title, renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + 2, 0xFFFFFF00, matrixStack, buffer, false, true);
            }
            // text lines
            int titleYoffset = title.isEmpty() ? 3 : 12;
            for (int i = curScroll; i < textList.size() && i < curScroll + MAX_LINES; i++) {
                if (DARK_FORMATTING.matcher(textList.get(i)).find()) {
                    RenderUtils.renderString3d(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset, 0xFFFFFF, matrixStack, buffer, false, true);
                } else {
                    RenderUtils.renderString3d(textList.get(i), renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset, 0xFFFFFF, matrixStack, buffer, false, true);
                }
            }

            matrixStack.push();
            matrixStack.translate(renderBaseX + (leftSided ? widgetOffsetLeft : widgetOffsetRight), renderAffectedY + (titleYoffset - 10), 0);
            subWidgets.stream()
                    .filter(widget -> widget instanceof ICanRender3d)
                    .forEach(widget -> ((ICanRender3d) widget).render3d(matrixStack, buffer, partialTicks));
            matrixStack.pop();

            matrixStack.pop();
        }

        // no subwidget drawing

        if (renderHeight > 16 && renderWidth > 16 && statIcon != null) {
            statIcon.render3d(matrixStack, buffer, renderBaseX, renderAffectedY, leftSided);
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
            for (Widget w : subWidgets) {
                if (w instanceof TextFieldWidget) ((TextFieldWidget) w).setFocused2(true);
            }
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && getBounds().contains((int)mouseX, (int)mouseY);
    }

    /*
     * button: 0 = left 1 = right 2 = middle
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered()) {
            for (Widget widget : subWidgets) {
                if (widget.mouseClicked(mouseX - this.x, mouseY - this.affectedY, button)) {
                    return true;
                }
            }
            // no sub-widgets took the click; toggle this animated stat open/closed
            toggle();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isHovered()) {
            for (Widget widget : subWidgets) {
                if (widget.mouseReleased(mouseX - this.x, mouseY - this.affectedY, button)) {
                    return true;
                }
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isHovered()) {
            Rectangle2d bounds = getBounds();
            for (Widget widget : subWidgets) {
                if (widget.mouseDragged(mouseX - bounds.getX(), mouseY - bounds.getY(), button, dragX, dragY)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        Rectangle2d bounds = getBounds();
        for (Widget widget : subWidgets) {
            if (widget.mouseScrolled(x - bounds.getX(), y - bounds.getY(), dir)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : subWidgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers) || (widget instanceof TextFieldWidget && widget.isFocused()) && keyCode != GLFW.GLFW_KEY_ESCAPE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        for (Widget widget : subWidgets) {
            if (widget.charTyped(p_charTyped_1_, p_charTyped_2_)) {
                return true;
            }
        }
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
    public void addTooltip(double mouseX, double mouseY, List<String> curTooltip, boolean shiftPressed) {
        if (mouseIsHoveringOverIcon(mouseX, mouseY)) {
            curTooltip.add(title);
        }

        for (Widget widget : subWidgets)
            if (widget.isHovered() && widget instanceof ITooltipProvider) {
                ((ITooltipProvider) widget).addTooltip(mouseX, mouseY, curTooltip, shiftPressed);
            }
    }

    private boolean mouseIsHoveringOverIcon(double x, double y) {
        if (leftSided) {
            return x <= this.x && x >= this.x - 16 && y >= affectedY && y <= affectedY + 16;
        } else {
            return x >= this.x && x <= this.x + 16 && y >= affectedY && y <= affectedY + 16;
        }
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

        void render(int x, int y, boolean leftSided) {
            RenderSystem.color4f(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (texture != null) {
                GuiUtils.drawTexture(texture, x - (leftSided ? 16 : 0), y);
            } else if (!stack.isEmpty()) {
                GuiUtils.drawItemStack(stack, x - (leftSided ? 16 : 0), y);
            }
            RenderSystem.disableBlend();
        }

        public void render3d(MatrixStack matrixStack, IRenderTypeBuffer buffer, int x, int y, boolean leftSided) {
            if (texture != null) {
                RenderUtils.renderWithType(matrixStack, buffer, ModRenderTypes.getTextureRenderColored(texture),
                        (posMat, builder) -> RenderUtils.drawTexture(matrixStack, builder, x, y, RenderUtils.FULL_BRIGHT));
            } else {
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, ClientUtils.getClientWorld(), null);
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStack, buffer, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ibakedmodel);
            }
        }
    }
}
