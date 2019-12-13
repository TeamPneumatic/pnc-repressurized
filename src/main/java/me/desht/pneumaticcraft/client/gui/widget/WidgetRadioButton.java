package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WidgetRadioButton extends Widget implements ITooltipProvider {
    private static final int BUTTON_WIDTH = 10;
    private static final int BUTTON_HEIGHT = 10;

    public boolean checked;
    public boolean enabled = true;
    public final int color;
    private final Consumer<WidgetRadioButton> pressable;
    private final FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
    private List<String> tooltip = new ArrayList<>();
    public List<WidgetRadioButton> otherChoices;

    public WidgetRadioButton(int x, int y, int color, String text, Consumer<WidgetRadioButton> pressable) {
        super(x, y, text);

        this.width = BUTTON_WIDTH + fontRenderer.getStringWidth(getMessage());
        this.height = BUTTON_HEIGHT;
        this.color = color;
        this.pressable = pressable;
    }

    public WidgetRadioButton(int x, int y, int color, String text) {
        this(x, y, color, text, null);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        drawCircle(x + BUTTON_WIDTH / 2, y + BUTTON_HEIGHT / 2, BUTTON_WIDTH / 2, enabled ? 0xFFA0A0A0 : 0xFF999999);
        drawCircle(x + BUTTON_WIDTH / 2, y + BUTTON_HEIGHT / 2, BUTTON_WIDTH / 2 - 1, enabled ? 0XFF202020 : 0xFFAAAAAA);
        if (checked) {
            drawCircle(x + BUTTON_WIDTH / 2, y + BUTTON_HEIGHT / 2, 1, enabled ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
        fontRenderer.drawString(I18n.format(getMessage()), x + 1 + BUTTON_WIDTH,
                y + BUTTON_HEIGHT / 2f - fontRenderer.FONT_HEIGHT / 2f, enabled ? color : 0xFF888888);
    }

    private void drawCircle(int x, int y, int radius, int color) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(f1, f2, f3, f);
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        int points = 12;
        for (int i = 0; i < points; i++) {
            double sin = Math.sin((double) i / points * Math.PI * 2);
            double cos = Math.cos((double) i / points * Math.PI * 2);
            wr.pos(x + sin * radius, y + cos * radius, 0.0).endVertex();
        }
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public Rectangle2d getBounds() {
        return new Rectangle2d(x, y, BUTTON_WIDTH + fontRenderer.getStringWidth(getMessage()), BUTTON_HEIGHT);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (enabled) {
            Validate.notNull(otherChoices, "A radio button needs more than one choice! You need to set the GuiRadioButton#otherChoices field!");
            for (WidgetRadioButton radioButton : otherChoices) {
                radioButton.checked = false;
            }
            checked = true;
            if (pressable != null) pressable.accept(this);
        }
    }

    public void setTooltip(String tooltip) {
        setTooltip(Collections.singletonList(tooltip));
    }

    public void setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {
        curTooltip.addAll(tooltip);
    }
}
