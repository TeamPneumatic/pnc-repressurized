package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WidgetRadioButton extends Widget implements ITooltipProvider {
    private static final int BUTTON_WIDTH = 10;
    private static final int BUTTON_HEIGHT = 10;

    private boolean checked;
    public boolean enabled = true;
    public final int color;
    private final Consumer<WidgetRadioButton> pressable;
    private final FontRenderer fontRenderer = Minecraft.getInstance().font;
    private List<ITextComponent> tooltip = new ArrayList<>();
    private List<? extends WidgetRadioButton> otherChoices = null;

    public WidgetRadioButton(int x, int y, int color, ITextComponent text, Consumer<WidgetRadioButton> pressable) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, text);

        this.width = BUTTON_WIDTH + fontRenderer.width(getMessage());
        this.height = BUTTON_HEIGHT;
        this.color = color;
        this.pressable = pressable;
    }

    public WidgetRadioButton(int x, int y, int color, ITextComponent text) {
        this(x, y, color, text, null);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawCircle(matrixStack, x + BUTTON_WIDTH / 2f, y + BUTTON_HEIGHT / 2f, BUTTON_WIDTH / 2f, enabled ? 0xFFA0A0A0 : 0xFF999999);
        drawCircle(matrixStack, x + BUTTON_WIDTH / 2f, y + BUTTON_HEIGHT / 2f, BUTTON_WIDTH / 2f - 1, enabled ? 0XFF202020 : 0xFFAAAAAA);
        if (checked) {
            drawCircle(matrixStack, x + BUTTON_WIDTH / 2f, y + BUTTON_HEIGHT / 2f, 1, enabled ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
        fontRenderer.draw(matrixStack, getMessage().getVisualOrderText(), x + 1 + BUTTON_WIDTH,
                y + BUTTON_HEIGHT / 2f - fontRenderer.lineHeight / 2f, enabled ? color : 0xFF888888);
    }

    public boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        // only intended to be called by the builder (see below)
        this.checked = checked;
    }

    private static final float N_POINTS = 12f;

    private void drawCircle(MatrixStack matrixStack, float x, float y, float radius, int color) {
        BufferBuilder wr = Tessellator.getInstance().getBuilder();
        int[] cols = RenderUtils.decomposeColor(color);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        Matrix4f posMat = matrixStack.last().pose();
        for (int i = 0; i < N_POINTS; i++) {
            float sin = MathHelper.sin(i / N_POINTS * (float) Math.PI * 2f);
            float cos = MathHelper.cos(i / N_POINTS * (float) Math.PI * 2f);
            wr.vertex(posMat, x + sin * radius, y + cos * radius, 0f).color(cols[1], cols[2], cols[2], cols[0]).endVertex();
        }
        Tessellator.getInstance().end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public Rectangle2d getBounds() {
        return new Rectangle2d(x, y, BUTTON_WIDTH + fontRenderer.width(getMessage()), BUTTON_HEIGHT);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (enabled && !checked) {
            for (WidgetRadioButton radioButton : otherChoices) {
                radioButton.checked = false;
            }
            checked = true;
            if (pressable != null) pressable.accept(this);
        }
    }

    public WidgetRadioButton setTooltip(ITextComponent tooltip) {
        return setTooltip(Collections.singletonList(tooltip));
    }

    public WidgetRadioButton setTooltip(List<ITextComponent> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTooltip, boolean shiftPressed) {
        curTooltip.addAll(tooltip);
    }

    void setOtherChoices(List<? extends WidgetRadioButton> choices) {
        if (otherChoices != null) throw new IllegalStateException("otherChoices has already been init'ed!");
        otherChoices = choices;
    }

    /**
     * Builder to manage creating a collection of related radio buttons.
     */
    public static class Builder<T extends WidgetRadioButton> {
        private final List<T> buttons = new ArrayList<>();

        private Builder() {
        }

        public static <T extends WidgetRadioButton> Builder<T> create() {
            return new Builder<>();
        }

        public Builder<T> addRadioButton(T rb, boolean initiallyChecked) {
            rb.setChecked(initiallyChecked);
            buttons.add(rb);
            return this;
        }

        public List<T> build() {
            return build(c -> {});
        }

        public List<T> build(Consumer<T> c) {
            List<T> res = ImmutableList.copyOf(buttons);
            int checked = 0;
            for (T rb : res) {
                if (rb.isChecked()) checked++;
                rb.setOtherChoices(res);
                c.accept(rb);
            }
            if (checked != 1) throw new IllegalStateException("one and only one radio button should be checked!");
            return res;
        }
    }
}
