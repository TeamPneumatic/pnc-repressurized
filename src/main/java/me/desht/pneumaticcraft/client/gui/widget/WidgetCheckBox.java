package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetCheckBox extends Widget implements ITaggedWidget, ITooltipProvider {
    public boolean checked;
    private final int color;
    private List<ITextComponent> tooltip = new ArrayList<>();
    private final Consumer<WidgetCheckBox> pressable;

    private static final int CHECKBOX_WIDTH = 10;
    private static final int CHECKBOX_HEIGHT = 10;
    private String tag = null;

    public WidgetCheckBox(int x, int y, int color, ITextComponent text, Consumer<WidgetCheckBox> pressable) {
        super(x, y, CHECKBOX_WIDTH, CHECKBOX_HEIGHT, text);

        this.x = x;
        this.y = y;
        this.width = CHECKBOX_WIDTH + 3 + Minecraft.getInstance().fontRenderer.func_238414_a_(text);
        this.color = color;
        this.pressable = pressable;
    }

    public WidgetCheckBox(int x, int y, int color, ITextComponent text) {
        this(x, y, color, text, null);
    }

    public WidgetCheckBox withTag(String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            fill(matrixStack, x, y, x + CHECKBOX_WIDTH, y + CHECKBOX_HEIGHT, active ? 0xFFA0A0A0 : 0xFF999999);
            fill(matrixStack, x + 1, y + 1, x + CHECKBOX_WIDTH - 1, y + CHECKBOX_HEIGHT - 1, active ? 0xFF202020 : 0xFFAAAAAA);
            if (checked) {
                drawTick(matrixStack);
            }
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            fr.func_238422_b_(matrixStack, getMessage().func_241878_f(), x + 3 + CHECKBOX_WIDTH, y + CHECKBOX_HEIGHT / 2f - fr.FONT_HEIGHT / 2f, active ? color : 0xFF888888);
        }
    }

    private void drawTick(MatrixStack matrixStack) {
        RenderSystem.disableTexture();
        if (active) {
            RenderSystem.color4f(0.5f, 1, 0.5f, 1);
        } else {
            RenderSystem.color4f(0.8f, 0.8f, 0.8f, 1);
        }
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        RenderSystem.lineWidth(2);
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        wr.pos(posMat, x + 2, y + 5, 0f).endVertex();
        wr.pos(posMat, x + 5, y + 7, 0f).endVertex();
        wr.pos(posMat, x + 8, y + 3, 0f).endVertex();
        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
        RenderSystem.color4f(0.25f, 0.25f, 0.25f, 1);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active) {
            checked = !checked;
            if (pressable != null) pressable.accept(this);
            if (tag != null) NetworkHandler.sendToServer(new PacketGuiButton(tag));
        }
    }

    public WidgetCheckBox setTooltip(ITextComponent tooltip) {
        this.tooltip.clear();
        if (tooltip != null && !tooltip.getString().isEmpty()) {
            this.tooltip.add(tooltip);
        }
        return this;
    }

    public WidgetCheckBox setTooltip(List<ITextComponent> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public List<ITextComponent> getTooltip() {
        return tooltip;
    }

    public WidgetCheckBox setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
        curTip.addAll(tooltip);
    }
}
