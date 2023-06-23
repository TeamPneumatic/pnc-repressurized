/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WidgetRadioButton extends AbstractWidget implements ITooltipProvider {
    private static final int BUTTON_WIDTH = 10;
    private static final int BUTTON_HEIGHT = 10;

    private boolean checked;
    public final int color;
    private final Consumer<WidgetRadioButton> pressable;
    private final Font fontRenderer = Minecraft.getInstance().font;
    private List<Component> tooltip = new ArrayList<>();
    private List<? extends WidgetRadioButton> otherChoices = null;

    public WidgetRadioButton(int x, int y, int color, Component text, Consumer<WidgetRadioButton> pressable) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, text);

        this.width = BUTTON_WIDTH + fontRenderer.width(getMessage());
        this.height = BUTTON_HEIGHT;
        this.color = color;
        this.pressable = pressable;
    }

    public WidgetRadioButton(int x, int y, int color, Component text) {
        this(x, y, color, text, null);
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int x = getX(), y = getY();
        drawCircle(matrixStack, x + BUTTON_WIDTH / 2f, y + BUTTON_HEIGHT / 2f, BUTTON_WIDTH / 2f, active ? 0xFFA0A0A0 : 0xFF999999);
        drawCircle(matrixStack, x + BUTTON_WIDTH / 2f, y + BUTTON_HEIGHT / 2f, BUTTON_WIDTH / 2f - 1, active ? 0XFF202020 : 0xFFAAAAAA);
        if (checked) {
            drawCircle(matrixStack, x + BUTTON_WIDTH / 2f, y + BUTTON_HEIGHT / 2f, 1, active ? 0xFF00C000 : 0xFFAAAAAA);
        }
        fontRenderer.draw(matrixStack, getMessage().getVisualOrderText(), x + 1 + BUTTON_WIDTH,
                y + BUTTON_HEIGHT / 2f - fontRenderer.lineHeight / 2f, active ? color : 0xFF888888);
    }

    public boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        // only intended to be called by the builder (see below)
        this.checked = checked;
    }

    private static final float N_POINTS = 12f;

    private void drawCircle(PoseStack matrixStack, float x, float y, float radius, int color) {
        BufferBuilder wr = Tesselator.getInstance().getBuilder();
        int[] cols = RenderUtils.decomposeColor(color);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        wr.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f posMat = matrixStack.last().pose();
        for (int i = 0; i < N_POINTS; i++) {
            float sin = Mth.sin(i / N_POINTS * (float) Math.PI * 2f);
            float cos = Mth.cos(i / N_POINTS * (float) Math.PI * 2f);
            wr.vertex(posMat, x + sin * radius, y + cos * radius, 0f).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active && !checked) {
            for (WidgetRadioButton radioButton : otherChoices) {
                radioButton.checked = false;
            }
            checked = true;
            if (pressable != null) pressable.accept(this);
        }
    }

    public WidgetRadioButton setTooltip(Component tooltip) {
        return setTooltip(Collections.singletonList(tooltip));
    }

    public WidgetRadioButton setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<Component> curTooltip, boolean shiftPressed) {
        curTooltip.addAll(tooltip);
    }

    void setOtherChoices(List<? extends WidgetRadioButton> choices) {
        if (otherChoices != null) throw new IllegalStateException("otherChoices has already been init'ed!");
        otherChoices = choices;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
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
