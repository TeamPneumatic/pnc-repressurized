package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetList<T> extends Widget implements ITooltipProvider {
    private final Consumer<WidgetList<T>> pressable;
    private final List<T> items = new ArrayList<>();
    private int selected = -1;  // < 0 indicates nothing selected
    private long lastClick = 0;
    private boolean doubleClicked;
    private int fgColor = 0x404040;
    private int selectedFg = 0xFFFFFF;
    private int selectedBg = 0x000000;
    private boolean inverseSelected = true;
    private ToolTipType toolTipType = ToolTipType.AUTO;

    public WidgetList(int xIn, int yIn, int width, int height) {
        this(xIn, yIn, width, height, null);
    }

    public WidgetList(int xIn, int yIn, int width, int height, Consumer<WidgetList<T>> pressable) {
        super(xIn, yIn, width, height, StringTextComponent.EMPTY);

        this.pressable = pressable;
    }

    public WidgetList setColor(int color) {
        this.fgColor = color;
        return this;
    }

    public WidgetList setToolTipType(ToolTipType toolTipType) {
        this.toolTipType = toolTipType;
        return this;
    }

    public WidgetList setSelectedColors(int selectedFg, int selectedBg) {
        this.selectedFg = selectedFg;
        this.selectedBg = selectedBg;
        return this;
    }

    public WidgetList inverseSelected(boolean inverse) {
        this.inverseSelected = inverse;
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            drawList(matrixStack);
        }
    }

    public boolean isDoubleClicked() {
        return doubleClicked;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public void unselectAll() {
        this.selected = -1;
    }

    public List<T> getLines() {
        return ImmutableList.copyOf(items);
    }

    public T getSelectedLine() {
        return selected >= 0 && selected < items.size() ? items.get(selected) : null;
    }

    public boolean contains(T s) {
        return items.contains(s);
    }

    public void add(T s) {
        items.add(s);
    }

    public void removeSelected() {
        if (selected >= 0 && selected < items.size()) {
            items.remove(selected);
            updateSelection();
        }
    }

    public void clear() {
        items.clear();
        setSelected(-1);
    }

    private void updateSelection() {
        if (items.isEmpty()) {
            selected = -1;
        } else if (selected >= items.size()) {
            selected = items.size() - 1;
        }
    }

    public int size() {
        return items.size();
    }

    private void drawList(MatrixStack matrixStack) {
        Minecraft mc = Minecraft.getInstance();
        int sf = mc.gameSettings.guiScale;
        int h = mc.fontRenderer.FONT_HEIGHT;
        int lines = height / h;

        matrixStack.push();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * sf, (y + height) * sf, width * sf, height * sf);
        if (inverseSelected && selected >= 0) {
            RenderSystem.disableTexture();
            fill(matrixStack, x, y + h * selected, x + width, y + h * (selected + 1), 0xFF000000 | selectedBg);
            RenderSystem.enableTexture();
        }
        matrixStack.translate(x, y, 0);
        matrixStack.scale(0.75f, 1f, 1f);
        for (int i = 0; i < items.size() && i < lines; i++) {
            mc.fontRenderer.drawString(matrixStack, items.get(i).toString(), 0, i * h, i == selected ? selectedFg : fgColor);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        matrixStack.pop();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active) {
            long now = System.currentTimeMillis();
            int h = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
            int newSel = MathHelper.clamp((int) (mouseY - this.y) / h, 0, items.size() - 1);
            doubleClicked = now - lastClick < 250 && newSel == selected;
            setSelected(newSel);
            lastClick = now;
            if (pressable != null) {
                pressable.accept(this);
            }
        }
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
        if (toolTipType == ToolTipType.NONE) return;

        int h = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
        int idx = Math.max(0, (int) (mouseY - this.y) / h);
        if (idx >= 0 && idx < items.size()) {
            String s = items.get(idx).toString();
            if (toolTipType == ToolTipType.ALWAYS || Minecraft.getInstance().fontRenderer.getStringWidth(s) * 3 / 4 > width) {
                curTip.add(new StringTextComponent(s));
            }
        }
    }

    public enum ToolTipType {
        NONE,
        AUTO,
        ALWAYS
    }
}
