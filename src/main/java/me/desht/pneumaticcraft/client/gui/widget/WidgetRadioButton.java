package me.desht.pneumaticcraft.client.gui.widget;

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
    private List<ITextComponent> tooltip = new ArrayList<>();
    public List<WidgetRadioButton> otherChoices;

    public WidgetRadioButton(int x, int y, int color, ITextComponent text, Consumer<WidgetRadioButton> pressable) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, text);

        this.width = BUTTON_WIDTH + fontRenderer.func_238414_a_(getMessage());
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
        fontRenderer.func_238422_b_(matrixStack, getMessage().func_241878_f(), x + 1 + BUTTON_WIDTH,
                y + BUTTON_HEIGHT / 2f - fontRenderer.FONT_HEIGHT / 2f, enabled ? color : 0xFF888888);
    }

    private static final float N_POINTS = 12f;

    private void drawCircle(MatrixStack matrixStack, float x, float y, float radius, int color) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        int[] cols = RenderUtils.decomposeColor(color);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        for (int i = 0; i < N_POINTS; i++) {
            float sin = MathHelper.sin(i / N_POINTS * (float) Math.PI * 2f);
            float cos = MathHelper.cos(i / N_POINTS * (float) Math.PI * 2f);
            wr.pos(posMat, x + sin * radius, y + cos * radius, 0f).color(cols[1], cols[2], cols[2], cols[0]).endVertex();
        }
        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public Rectangle2d getBounds() {
        return new Rectangle2d(x, y, BUTTON_WIDTH + fontRenderer.func_238414_a_(getMessage()), BUTTON_HEIGHT);
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

    public void setTooltip(ITextComponent tooltip) {
        setTooltip(Collections.singletonList(tooltip));
    }

    public void setTooltip(List<ITextComponent> tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTooltip, boolean shiftPressed) {
        curTooltip.addAll(tooltip);
    }
}
