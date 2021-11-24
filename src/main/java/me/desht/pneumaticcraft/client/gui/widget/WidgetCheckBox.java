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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.ICheckboxWidget;
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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetCheckBox extends Widget implements ICheckboxWidget, ITaggedWidget, ITooltipProvider {
    public boolean checked;
    private final int color;
    private List<ITextComponent> tooltip = new ArrayList<>();
    private final Consumer<? super WidgetCheckBox> pressable;

    private static final int CHECKBOX_WIDTH = 10;
    private static final int CHECKBOX_HEIGHT = 10;
    private String tag = null;

    public WidgetCheckBox(int x, int y, int color, ITextComponent text, Consumer<? super WidgetCheckBox> pressable) {
        super(x, y, CHECKBOX_WIDTH, CHECKBOX_HEIGHT, text);

        this.x = x;
        this.y = y;
        this.width = CHECKBOX_WIDTH + 3 + Minecraft.getInstance().font.width(text);
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
            FontRenderer fr = Minecraft.getInstance().font;
            fr.draw(matrixStack, getMessage().getVisualOrderText(), x + 3 + CHECKBOX_WIDTH, y + CHECKBOX_HEIGHT / 2f - fr.lineHeight / 2f, active ? color : 0xFF888888);
        }
    }

    private void drawTick(MatrixStack matrixStack) {
        RenderSystem.disableTexture();
        int r, g, b;
        if (active) {
            r = 128; g = 255; b = 128;
        } else {
            r = g = b = 192;
        }
        BufferBuilder wr = Tessellator.getInstance().getBuilder();
        RenderSystem.lineWidth(3);
        Matrix4f posMat = matrixStack.last().pose();
        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        wr.vertex(posMat, x + 2, y + 5, 0f).color(r, g, b, 255).endVertex();
        wr.vertex(posMat, x + 5, y + 7, 0f).color(r, g, b, 255).endVertex();
        wr.vertex(posMat, x + 8, y + 3, 0f).color(r, g, b, 255).endVertex();
        Tessellator.getInstance().end();
        RenderSystem.enableTexture();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active) {
            checked = !checked;
            if (pressable != null) pressable.accept(this);
            if (tag != null) NetworkHandler.sendToServer(new PacketGuiButton(tag));
        }
    }

    public WidgetCheckBox setTooltip(List<ITextComponent> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public WidgetCheckBox setTooltipKey(String translationKey) {
        return setTooltip(Collections.singletonList(xlate(translationKey)));
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

    @Override
    public boolean isChecked() {
        return checked;
    }
}
