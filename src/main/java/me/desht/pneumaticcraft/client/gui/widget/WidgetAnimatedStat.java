package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class WidgetAnimatedStat extends Widget implements IGuiAnimatedStat, ITooltipProvider {
    private static final int MIN_WIDTH_HEIGHT = 17;
    private static final int MAX_VISIBLE_LINES = 12;
    private static final int SCROLLBAR_MARGIN_WIDTH = 20;
    private static final int TOP_MARGIN_HEIGHT = 20;

    private IGuiAnimatedStat statAbove;
    private StatIcon statIcon;
    private final Screen gui;
    // the text we want to draw
    private final List<ITextComponent> textComponents = new ArrayList<>();
    // the text which is actually renderered, after having been wrapped to fit the stat's width
    private final List<IReorderingProcessor> reorderingProcessors = new ArrayList<>();
    // for each rendered line, should it be drawn with a drop shadow?
    private final List<Boolean> dropShadows = new ArrayList<>();
    private final List<Widget> subWidgets = new ArrayList<>();
    private int effectiveY;  // where the widget is actually rendered (if it has a "parent" stat, it will always render below that)
    private int reservedLines = 0; // space at the top where text isn't rendered
    private boolean autoLineWrap = true;

    // for interpolation purposes, to smoothly animate the widget expanding/contracting
    private int prevX;
    private int prevEffectiveY;
    private int prevWidth;
    private int prevHeight;

    private boolean isClicked = false;  // is the stat currently open?

    private int minWidth = MIN_WIDTH_HEIGHT;
    private int minHeight = MIN_WIDTH_HEIGHT;
    private int minExpandedHeight;
    private int minExpandedWidth;
    private int expandedWidth; // width of the stat when expanded
    private int expandedHeight; // height of the stat when expanded

    private int backGroundColor;
    private TintColor bgColorHi, bgColorLo;
    private boolean leftSided; // determines if the stat expands to the left or right
    private boolean doneExpanding;  // when true, the stat is fully open and text/subwidgets can be rendered
    private int curScroll;  // current scroll position
    private int lineSpacing = 10;
    private int widgetOffsetLeft = 0;
    private int widgetOffsetRight = 0;
    private boolean bevel = false;
    private WidgetVerticalScrollbar scrollBar = null;
    private boolean needTextRecalc = true;
    private int foregroundColor = 0xFFFFFFFF;
    private int titleColor = 0xFFFFFF00;
    private List<ITextComponent> extraTooltipText = new ArrayList<>();

    public WidgetAnimatedStat(Screen gui, ITextComponent title, int xPos, int yPos, int backGroundColor,
                              IGuiAnimatedStat statAbove, boolean leftSided) {
        super(xPos, yPos, MIN_WIDTH_HEIGHT, MIN_WIDTH_HEIGHT, title);

        this.gui = gui;
        this.statAbove = statAbove;
        this.leftSided = leftSided;
        this.statIcon = StatIcon.NONE;
        this.backGroundColor = backGroundColor;
        calculateColorHighlights(this.backGroundColor);

        this.effectiveY = y;
        if (statAbove != null) {
            this.effectiveY += statAbove.getEffectiveY() + statAbove.getStatHeight();
        }
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor) {
        this(gui, StringTextComponent.EMPTY, 0, 0, backgroundColor, null, false);
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor, ItemStack icon) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(icon);
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor, ResourceLocation texture) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(texture);
    }

    public WidgetAnimatedStat(Screen gui, ITextComponent title, StatIcon icon, int xPos, int yPos, int backGroundColor,
                              IGuiAnimatedStat statAbove, boolean leftSided) {
        this(gui, title, xPos, yPos, backGroundColor, statAbove, leftSided);
        statIcon = icon;
    }

    public WidgetAnimatedStat(Screen gui, ITextComponent title, StatIcon icon, int backGroundColor,
                              IGuiAnimatedStat statAbove, ArmorHUDLayout.LayoutItem layout) {
        this(gui, title, 0, 0, backGroundColor, statAbove, layout.isLeftSided());
        MainWindow mw = Minecraft.getInstance().getMainWindow();
        int x = layout.getX() == -1 ? mw.getScaledWidth() - 2 : (int) (mw.getScaledWidth() * layout.getX());
        setBaseX(x);
        setBaseY((int) (mw.getScaledHeight() * layout.getY()));
        statIcon = icon;
    }

    @Override
    public void setMessage(ITextComponent message) {
        super.setMessage(message);
        needTextRecalc = true;
    }

    @Override
    public void setParentStat(IGuiAnimatedStat stat) {
        statAbove = stat;
    }

    public void addSubWidget(Widget widget) {
        subWidgets.add(widget);
    }

    public void removeSubWidget(Widget widget) {
        subWidgets.remove(widget);
    }

    public void setSubwidgetRenderOffsets(int left, int right) {
        widgetOffsetLeft = left;
        widgetOffsetRight = right;
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
    public void setAutoLineWrap(boolean wrap) {
        autoLineWrap = wrap;
    }

    @Override
    public ITextComponent getTitle() {
        return getMessage();
    }

    @Override
    public void setTitle(ITextComponent title) {
        setMessage(title);
    }

    @Override
    public IGuiAnimatedStat setText(List<ITextComponent> text) {
        textComponents.clear();
        textComponents.addAll(text);
        needTextRecalc = true;
        return this;
    }

    @Override
    public IGuiAnimatedStat setText(ITextComponent text) {
        textComponents.clear();
        textComponents.add(text);
        needTextRecalc = true;
        return this;
    }

    @Override
    public void appendText(List<ITextComponent> text) {
        textComponents.addAll(text);
        needTextRecalc = true;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        if (backgroundColor != this.backGroundColor) {
            this.backGroundColor = backgroundColor;
            calculateColorHighlights(backgroundColor);
        }
    }

    @Override
    public void setForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @Override
    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
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

    private int getVisibleLines() {
        return MAX_VISIBLE_LINES - reservedLines;
    }

    @Override
    public void setReservedLines(int reservedLines) {
        this.reservedLines = MathHelper.clamp(reservedLines, 0, MAX_VISIBLE_LINES - 1);
        needTextRecalc = true;
    }

    private void recalcText() {
        reorderingProcessors.clear();
        dropShadows.clear();

        FontRenderer font = Minecraft.getInstance().fontRenderer;
        int titleWidth = font.getStringPropertyWidth(getMessage());
        if (autoLineWrap) {
            int availableWidth = calculateAvailableWidth();
            reorderingProcessors.addAll(GuiUtils.wrapTextComponentList(textComponents, availableWidth, font));
            expandedWidth = Math.min(availableWidth, Math.max(titleWidth, minExpandedWidth));
        } else {
            expandedWidth = titleWidth;
            textComponents.forEach(c -> reorderingProcessors.add(c.func_241878_f()));
        }
        reorderingProcessors.forEach(processedLine -> {
            expandedWidth = Math.max(expandedWidth, font.func_243245_a(processedLine));
            dropShadows.add(needsDropShadow(processedLine));
        });
        expandedWidth += SCROLLBAR_MARGIN_WIDTH;
        int topMargin = reorderingProcessors.isEmpty() ? font.FONT_HEIGHT : TOP_MARGIN_HEIGHT;
        expandedHeight = Math.max(minExpandedHeight, topMargin + Math.min(MAX_VISIBLE_LINES, reorderingProcessors.size()) * font.FONT_HEIGHT) + 3;

        addOrRemoveScrollbar();

        needTextRecalc = false;
    }

    /**
     * Calculate the maximum available width that a line of wrapped text can be.  Note that the actual widget might
     * not necessarily end up this wide, depending on the text contents.
     *
     * @return the maximum available width for the animated stat, not including the gutter width for the scrollbar/icon
     */
    private int calculateAvailableWidth() {
        int availableWidth;
        if (gui instanceof ContainerScreen) {
            ContainerScreen<?> gc = (ContainerScreen<?>) gui;
            availableWidth = Math.min(Math.max(minExpandedWidth, gc.getXSize()), leftSided ? gc.getGuiLeft() : gc.width - (gc.getGuiLeft() + gc.getXSize()));
        } else if (gui instanceof GuiPneumaticScreenBase) {
            GuiPneumaticScreenBase g = (GuiPneumaticScreenBase) gui;
            availableWidth = Math.min(Math.max(minExpandedWidth, g.xSize), leftSided ? g.guiLeft : g.xSize - (g.guiLeft + g.xSize));
        } else {
            availableWidth = leftSided ? x : Minecraft.getInstance().getMainWindow().getScaledWidth() - x;
        }
        return availableWidth - 5 - SCROLLBAR_MARGIN_WIDTH;  // leave at least 5 pixel margin from edge of screen
    }

    private boolean needsDropShadow(IReorderingProcessor line) {
        StyleChecker styleChecker = new StyleChecker(foregroundColor);
        line.accept(styleChecker);
        return styleChecker.isLightColor();
    }

    private void addOrRemoveScrollbar() {
        if (reorderingProcessors.size() > getVisibleLines()) {
            if (subWidgets.contains(scrollBar)) return;
            // need to add a scrollbar
            curScroll = 0;
            int scrollbarHeight = getVisibleLines() * lineSpacing - TOP_MARGIN_HEIGHT;
            int yOffset = reservedLines > 0 ? reservedLines * Minecraft.getInstance().fontRenderer.FONT_HEIGHT : 0;
            addSubWidget(scrollBar = new WidgetVerticalScrollbar(leftSided ? -16 : 2, TOP_MARGIN_HEIGHT + yOffset, scrollbarHeight)
                    .setStates(reorderingProcessors.size() - getVisibleLines())
                    .setListening(true));
        } else if (subWidgets.removeIf(w -> w == scrollBar)) {
            // removing existing scrollbar
            curScroll = 0;
            scrollBar = null;
        }
    }

    @Override
    public void setMinimumContractedDimensions(int minWidth, int minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        width = minWidth;
        height = minHeight;
    }

    @Override
    public void setMinimumExpandedDimensions(int minWidth, int minHeight) {
        if (minExpandedWidth != minWidth) needTextRecalc = true;
        minExpandedWidth = minWidth;
        minExpandedHeight = minHeight;
    }

    @Override
    public void tickWidget() {
        if (needTextRecalc) recalcText();

        prevX = x;
        prevEffectiveY = effectiveY;
        prevWidth = width;
        prevHeight = height;

        doneExpanding = true;
        // 4 ticks to fully expand/contract
        int expandX = expandedWidth / 4;
        int expandY = expandedHeight / 4;

        if (isClicked) {
            // expand the box
            width = Math.min(expandedWidth, width + expandX);
            height = Math.min(expandedHeight, height + expandY);
            doneExpanding = width == expandedWidth && height == expandedHeight;

            int scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
            int scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
            if (isLeftSided()) {
                if (x >= scaledWidth) x = scaledWidth;
            } else {
                if (x < 0) x = 1;
            }
            if (y + height >= scaledHeight) {
                y = scaledHeight - height - 1;
            }

            if (doneExpanding && scrollBar != null) curScroll = scrollBar.getState();
        } else {
            // contract the box
            width = Math.max(minWidth, width - expandX);
            height = Math.max(minHeight, height - expandY);
            doneExpanding = false;
        }

        effectiveY = y;
        if (statAbove != null) {
            effectiveY += statAbove.getEffectiveY() + statAbove.getStatHeight();
        }
    }

    @Override
    public void renderStat(MatrixStack matrixStack, int x, int y, float partialTicks) {
        // just delegate to the renderButton() method
        // a separately-named interface method is used to avoid AbstractMethodError problems arising
        // from having a renderButton() method in IGuiAnimatedStat
        renderButton(matrixStack, x, y, partialTicks);
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
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        int baseX = leftSided ? this.x - this.width : this.x;
        this.isHovered = mouseX >= baseX && mouseY >= this.effectiveY && mouseX < baseX + this.width && mouseY < this.effectiveY + this.height;

        float zLevel = 0;
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int renderBaseX = (int) MathHelper.lerp(partialTicks, prevX, x);
        int renderAffectedY = (int) MathHelper.lerp(partialTicks, prevEffectiveY, effectiveY);
        int renderWidth = (int) MathHelper.lerp(partialTicks, prevWidth, width);
        int renderHeight = (int) MathHelper.lerp(partialTicks, prevHeight, height);

        if (leftSided) renderWidth *= -1;
        AbstractGui.fill(matrixStack, renderBaseX, renderAffectedY, renderBaseX + renderWidth, renderAffectedY + renderHeight, backGroundColor);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(3.0F);
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
        if (doneExpanding) {
            if (needTextRecalc) recalcText();

            String title = getMessage().getString();
            int titleYoffset = title.isEmpty() ? 3 : 12;
            if (!title.isEmpty()) {
                fontRenderer.drawStringWithShadow(matrixStack, title, renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + 2, titleColor);
            }
            for (int i = curScroll; i < reorderingProcessors.size() && i < curScroll + getVisibleLines(); i++) {
                IReorderingProcessor line = reorderingProcessors.get(i);
                int renderX = renderBaseX + (leftSided ? -renderWidth + 2 : 18);
                int renderY = renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset + reservedLines * fontRenderer.FONT_HEIGHT;
                if (dropShadows.get(i)) {
                    fontRenderer.func_238407_a_(matrixStack, line,renderX, renderY, foregroundColor);
                } else {
                    fontRenderer.func_238422_b_(matrixStack, line,renderX, renderY, foregroundColor);
                }
            }

            matrixStack.push();
            matrixStack.translate(renderBaseX + (leftSided ? widgetOffsetLeft : widgetOffsetRight), renderAffectedY + (titleYoffset - 10), 0);
            RenderSystem.enableTexture();
            subWidgets.forEach(widget -> widget.render(matrixStack, mouseX - renderBaseX, mouseY - renderAffectedY, partialTicks));
            matrixStack.pop();
        }
        if (renderHeight > 16 && renderWidth > 16 && statIcon != null) {
            statIcon.render(matrixStack, renderBaseX, renderAffectedY, leftSided);
        }
    }

    @Override
    public void renderStat(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        // used by the Block Tracker & Entity Tracker armor upgrades
        if (needTextRecalc) recalcText();

        int renderBaseX = (int) MathHelper.lerp(partialTicks, prevX, x);
        int renderEffectiveY = (int) MathHelper.lerp(partialTicks, prevEffectiveY, effectiveY);
        int renderWidth = (int) MathHelper.lerp(partialTicks, prevWidth, width);
        int renderHeight = (int) MathHelper.lerp(partialTicks, prevHeight, height);

        // quad bg
        int[] cols = RenderUtils.decomposeColor(backGroundColor);
        RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getUntexturedQuad(true), (posMat, builder) -> {
            int rw = leftSided ? -renderWidth : renderWidth;
            builder.pos(posMat, (float)renderBaseX, (float)renderEffectiveY + renderHeight, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
            builder.pos(posMat, (float)renderBaseX + rw, (float)renderEffectiveY + renderHeight, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
            builder.pos(posMat, (float)renderBaseX + rw, (float)renderEffectiveY, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
            builder.pos(posMat, (float)renderBaseX, (float)renderEffectiveY, 0.0F)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .lightmap(RenderUtils.FULL_BRIGHT)
                    .endVertex();
        });

        // line loops border
        RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getLineLoopsTransparent(5.0f), (posMat, builder) -> {
            int rw = leftSided ? -renderWidth : renderWidth;
            float[] c1 = leftSided ? bgColorLo.getComponents(null) : bgColorHi.getComponents(null);
            float[] c2 = bgColorHi.getComponents(null);
            float[] c3 = leftSided ? bgColorHi.getComponents(null) : bgColorLo.getComponents(null);
            float[] c4 = bgColorLo.getComponents(null);
            builder.pos(posMat, renderBaseX, renderEffectiveY, 0).color(c1[0], c1[1], c1[2], c1[3]).endVertex();
            builder.pos(posMat, renderBaseX + rw, renderEffectiveY, 0).color(c2[0], c2[1], c2[2], c2[3]).endVertex();
            builder.pos(posMat, renderBaseX + rw, renderEffectiveY + renderHeight, 0).color(c3[0], c3[1], c3[2],c3[3]).endVertex();
            builder.pos(posMat, renderBaseX, renderEffectiveY + renderHeight, 0).color(c4[0], c4[1], c4[2], c4[3]).endVertex();
        });

        if (doneExpanding) {
            matrixStack.push();
            // text title
            String title = getMessage().getString();
            if (!title.isEmpty()) {
                RenderUtils.renderString3d(TextFormatting.UNDERLINE + title, renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderEffectiveY + 2, titleColor, matrixStack, buffer, false, true);
            }
            // text lines
            int titleYoffset = title.isEmpty() ? 3 : 12;
            FontRenderer font = Minecraft.getInstance().fontRenderer;
            for (int i = curScroll; i < textComponents.size() && i < curScroll + getVisibleLines(); i++) {
                int renderX = renderBaseX + (leftSided ? -renderWidth + 2 : 18);
                int renderY = renderEffectiveY + (i - curScroll) * lineSpacing + titleYoffset + reservedLines * font.FONT_HEIGHT;
                font.func_238416_a_(reorderingProcessors.get(i), renderX, renderY, foregroundColor, dropShadows.get(i),
                        matrixStack.getLast().getMatrix(), buffer, true, 0, RenderUtils.FULL_BRIGHT);
            }

            matrixStack.push();
            matrixStack.translate(renderBaseX + (leftSided ? widgetOffsetLeft : widgetOffsetRight), renderEffectiveY + (titleYoffset - 10), 0);
            subWidgets.stream()
                    .filter(widget -> widget instanceof ICanRender3d)
                    .forEach(widget -> ((ICanRender3d) widget).render3d(matrixStack, buffer, partialTicks));
            matrixStack.pop();

            matrixStack.pop();
        }

        // no subwidget drawing in 3d rendering

        if (renderHeight > 16 && renderWidth > 16 && statIcon != null) {
            statIcon.render3d(matrixStack, buffer, renderBaseX, renderEffectiveY);
        }
    }

    private void toggle() {
        isClicked = !isClicked;
        if (isClicked && gui instanceof GuiPneumaticContainerBase) {
            // close any other open stat on the same side of the gui
            List<IGuiAnimatedStat> otherStats = ((GuiPneumaticContainerBase<?,?>) gui).getStatWidgets();
            otherStats.stream()
                    .filter(stat -> this != stat && stat.isLeftSided() == isLeftSided())
                    .forEach(IGuiAnimatedStat::closeStat);
            // focus on the first textfield child of this widget, if any
            subWidgets.stream()
                    .filter(w -> w instanceof TextFieldWidget)
                    .findFirst()
                    .ifPresent(w -> ((TextFieldWidget) w).setFocused2(true));
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
                if (widget.mouseClicked(mouseX - this.x, mouseY - this.effectiveY, button)) {
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
                if (widget.mouseReleased(mouseX - this.x, mouseY - this.effectiveY, button)) {
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
    public boolean charTyped(char codePoint, int modifiers) {
        for (Widget widget : subWidgets) {
            if (widget.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void closeStat() {
        isClicked = false;
    }

    @Override
    public void openStat() {
        isClicked = true;
    }

    @Override
    public boolean isStatOpen() {
        return isClicked;
    }

    @Override
    public int getEffectiveY() {
        return effectiveY;
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
    public int getStatHeight() {
        return getHeightRealms();  // nice bit of mapping there...
    }

    @Override
    public int getStatWidth() {
        return getWidth();
    }

    @Override
    public void setBaseY(int y) {
        this.y = y;
    }

    @Override
    public void setBaseX(int x) {
        this.x = x;
    }

    @Override
    public boolean isDoneExpanding() {
        return doneExpanding;
    }

    @Override
    public Rectangle2d getBounds() {
        return new Rectangle2d(x - (leftSided ? width : 0), effectiveY, width, height);
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTooltip, boolean shiftPressed) {
        if (mouseIsHoveringOverIcon(mouseX, mouseY)) {
            curTooltip.add(getMessage());
            curTooltip.addAll(getExtraTooltipText());
        }

        for (Widget widget : subWidgets)
            if (widget.isHovered() && widget instanceof ITooltipProvider) {
                ((ITooltipProvider) widget).addTooltip(mouseX, mouseY, curTooltip, shiftPressed);
            }
    }

    public void setExtraTooltipText(List<ITextComponent> extraTooltipText) {
        this.extraTooltipText = extraTooltipText;
    }

    private List<ITextComponent> getExtraTooltipText() {
        return extraTooltipText;
    }

    private boolean mouseIsHoveringOverIcon(double x, double y) {
        if (leftSided) {
            return x <= this.x && x >= this.x - 16 && y >= effectiveY && y <= effectiveY + 16;
        } else {
            return x >= this.x && x <= this.x + 16 && y >= effectiveY && y <= effectiveY + 16;
        }
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    @Override
    public void setTexture(ResourceLocation texture) {
        this.statIcon = StatIcon.of(texture);
    }

    @Override
    public void setTexture(ItemStack itemStack) {
        this.statIcon = StatIcon.of(itemStack);
    }

    public static class StatIcon {
        public static final StatIcon NONE = StatIcon.of(ItemStack.EMPTY);

        private final Either<ItemStack,ResourceLocation> texture;

        private StatIcon(Either<ItemStack,ResourceLocation> texture) {
            this.texture = texture;
        }

        public static StatIcon of(ItemStack stack) {
            return new StatIcon(Either.left(stack));
        }

        public static StatIcon of(IItemProvider item) {
            return new StatIcon(Either.left(new ItemStack(item, 1)));
        }

        public static StatIcon of(ResourceLocation texture) {
            return new StatIcon(Either.right(texture));
        }

        void render(MatrixStack matrixStack, int x, int y, boolean leftSided) {
            RenderSystem.color4f(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            texture.ifLeft(stack ->  GuiUtils.renderItemStack(matrixStack, stack, x - (leftSided ? 16 : 0), y))
                    .ifRight(resLoc -> GuiUtils.drawTexture(matrixStack, resLoc, x - (leftSided ? 16 : 0), y));
            RenderSystem.disableBlend();
        }

        public void render3d(MatrixStack matrixStack, IRenderTypeBuffer buffer, int x, int y) {
            texture.ifLeft(stack -> {
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, ClientUtils.getClientWorld(), null);
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStack, buffer, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ibakedmodel);
            }).ifRight(resLoc ->
                    RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getTextureRenderColored(resLoc),
                            (posMat, builder) -> RenderUtils.drawTexture(matrixStack, builder, x, y, RenderUtils.FULL_BRIGHT)));
        }
    }

    private static class StyleChecker implements ICharacterConsumer {
        private static final int THRESHOLD = 129;

        Style style = Style.EMPTY;
        private final int defColor;

        private StyleChecker(int defColor) {
            this.defColor = defColor;
        }

        @Override
        public boolean accept(int p_accept_1_, Style p_accept_2_, int p_accept_3_) {
            if (style == Style.EMPTY) style = p_accept_2_;
            return true;
        }

        public boolean isLightColor() {
            int c = style == null || style.isEmpty() || style.getColor() == null ? defColor : style.getColor().getColor();
            return isLightColor(new TintColor(c));
        }

        private boolean isLightColor(TintColor bg) {
            // calculate a foreground color which suitably contrasts with the given background color
            int luminance = (int) Math.sqrt(
                    bg.getRed() * bg.getRed() * 0.241 +
                            bg.getGreen() * bg.getGreen() * 0.691 +
                            bg.getBlue() * bg.getBlue() * 0.068
            );
            return luminance > THRESHOLD;
        }
    }
}
