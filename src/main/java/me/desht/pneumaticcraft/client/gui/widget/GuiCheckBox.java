package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiCheckBox extends Widget implements ITaggedWidget, ITooltipSupplier {
    public boolean checked, enabled = true, visible = true;
    public int color;
    private List<String> tooltip = new ArrayList<>();
    private final Consumer<GuiCheckBox> pressable;

    private static final int CHECKBOX_WIDTH = 10;
    private static final int CHECKBOX_HEIGHT = 10;
    private String tag = null;

    public GuiCheckBox(int x, int y, int color, String text, Consumer<GuiCheckBox> pressable) {
        super(x, y, CHECKBOX_WIDTH + 3 + Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(text)), CHECKBOX_HEIGHT, text);
        this.x = x;
        this.y = y;
        this.color = color;
        this.pressable = pressable;
    }

    public GuiCheckBox(int x, int y, int color, String text) {
        this(x, y, color, text, null);
    }

    public GuiCheckBox withTag(String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        if (visible) {
            fill(x, y, x + CHECKBOX_WIDTH, y + CHECKBOX_HEIGHT, enabled ? 0xFFA0A0A0 : 0xFF999999);
            fill(x + 1, y + 1, x + CHECKBOX_WIDTH - 1, y + CHECKBOX_HEIGHT - 1, enabled ? 0xFF202020 : 0xFFAAAAAA);
            if (checked) {
                GlStateManager.disableTexture();
                if (enabled) {
                    GlStateManager.color4f(0.5f, 1, 0.5f, 1);
                } else {
                    GlStateManager.color4f(0.8f, 0.8f, 0.8f, 1);
                }
                BufferBuilder wr = Tessellator.getInstance().getBuffer();
                GlStateManager.lineWidth(2);
                wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
                wr.pos(x + 2, y + 5, 0.0).endVertex();
                wr.pos(x + 5, y + 7, 0.0).endVertex();
                wr.pos(x + 8, y + 3, 0.0).endVertex();
                Tessellator.getInstance().draw();
                GlStateManager.enableTexture();
                GlStateManager.color4f(0.25f, 0.25f, 0.25f, 1);
            }
            Minecraft.getInstance().fontRenderer.drawString(I18n.format(getMessage()), x + 3 + CHECKBOX_WIDTH, y + CHECKBOX_HEIGHT / 2f - Minecraft.getInstance().fontRenderer.FONT_HEIGHT / 2f, enabled ? color : 0xFF888888);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (enabled) {
            checked = !checked;
            if (pressable != null) pressable.accept(this);
            if (tag != null) NetworkHandler.sendToServer(new PacketGuiButton(tag));
        }
    }

    public GuiCheckBox setTooltip(String tooltip) {
        this.tooltip.clear();
        if (tooltip != null && !tooltip.equals("")) {
            this.tooltip.add(tooltip);
        }
        return this;
    }

    public GuiCheckBox setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public GuiCheckBox setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public String getTooltip() {
        return tooltip.size() > 0 ? tooltip.get(0) : "";
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
        curTip.addAll(tooltip);
    }
}
