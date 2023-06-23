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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extension of GuiButtonExt that add: 1) a string tag which is sent to the server when clicked (PacketGuiButton),
 * 2) ability to draw itemstack or textured icons, and 3) can render its area when invisible
 */
public class WidgetButtonExtended extends ExtendedButton implements ITaggedWidget, ITooltipProvider {
    private int iconSpacing = 18;

    public enum IconPosition { MIDDLE, LEFT, RIGHT }
    private ItemStack[] renderedStacks;
    private ResourceLocation resLoc;
    private final List<Component> tooltipText = new ArrayList<>();
    private int invisibleHoverColor;
    private boolean thisVisible = true;
    private IconPosition iconPosition = IconPosition.MIDDLE;
    private String tag = null;
    private boolean renderStackSize = false;
    private boolean highlightInactive = false;

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, Component buttonText, Button.OnPress pressable) {
        super(startX, startY, xSize, ySize, buttonText, pressable);
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, Component buttonText) {
        this(startX, startY, xSize, ySize, buttonText, b -> {});
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, String buttonText, OnPress pressable) {
        super(startX, startY, xSize, ySize, Component.literal(buttonText), pressable);
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, String buttonText) {
        this(startX, startY, xSize, ySize, buttonText, b -> {});
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize) {
        this(startX, startY, xSize, ySize, Component.empty(), b -> {});
    }

    /**
     * Added a string tag to the button.  This will be sent to the server as the payload of a {@link PacketGuiButton}
     * packet when the button is clicked.
     *
     * @param tag a string tag containing any arbitrary information
     * @return the button, for fluency
     */
    public WidgetButtonExtended withTag(String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public void onPress() {
        super.onPress();
        if (tag != null && !tag.isEmpty()) NetworkHandler.sendToServer(new PacketGuiButton(tag));
    }

    @Override
    public String getTag() {
        return tag;
    }

    public WidgetButtonExtended setVisible(boolean visible) {
        thisVisible = visible;
        return this;
    }

    public WidgetButtonExtended setInvisibleHoverColor(int color) {
        invisibleHoverColor = color;
        return this;
    }

    public WidgetButtonExtended setIconPosition(IconPosition iconPosition) {
        this.iconPosition = iconPosition;
        return this;
    }

    public WidgetButtonExtended setRenderStacks(ItemStack... renderedStacks) {
        this.renderedStacks = renderedStacks;
        this.resLoc = null;
        return this;
    }

    public WidgetButtonExtended setRenderedIcon(ResourceLocation resLoc) {
        this.resLoc = resLoc;
        this.renderedStacks = null;
        return this;
    }

    public WidgetButtonExtended setIconSpacing(int spacing) {
        this.iconSpacing = spacing;
        return this;
    }

    public WidgetButtonExtended setTexture(Object texture) {
        if (texture instanceof ItemStack) {
            setRenderStacks((ItemStack) texture);
        } else if (texture instanceof ResourceLocation) {
            setRenderedIcon((ResourceLocation) texture);
        } else {
            throw new IllegalArgumentException("texture must be an ItemStack or ResourceLocation!");
        }
        return this;
    }

    public WidgetButtonExtended setTooltipKey(String key, Object... params) {
        return setTooltipText(GuiUtils.xlateAndSplit(key, params));
    }

    public WidgetButtonExtended setTooltipText(Component tooltip) {
        return setTooltipText(Collections.singletonList(tooltip));
    }

    public WidgetButtonExtended setTooltipText(List<Component> tooltip) {
        tooltipText.clear();
        tooltipText.addAll(tooltip);
        return this;
    }

    public void setHighlightWhenInactive(boolean highlight) {
        this.highlightInactive = highlight;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<Component> curTip, boolean shift) {
        curTip.addAll(tooltipText);
    }

    public boolean hasTooltip() {
        return !tooltipText.isEmpty();
    }

    public List<Component> getTooltip() {
        return tooltipText;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setRenderStackSize(boolean renderStackSize) {
        this.renderStackSize = renderStackSize;
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int x, int y, float partialTicks) {
        if (thisVisible && visible && !active && highlightInactive) {
            Gui.fill(matrixStack, this.getX() - 1, this.getY() - 1, this.getX() + getWidth() + 1, this.getY() + getHeight() + 1, 0xFF00FFFF);
        }

        if (thisVisible) super.renderWidget(matrixStack, x, y, partialTicks);

        if (visible) {
            if (renderedStacks != null) {
                int startX = getIconX();
                for (int i = renderedStacks.length - 1; i >= 0; i--) {
                    Minecraft.getInstance().getItemRenderer().renderGuiItem(matrixStack, renderedStacks[i], startX + i * iconSpacing, this.getY() + 2);
                    if (renderStackSize) {
                        Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(matrixStack, Minecraft.getInstance().font, renderedStacks[i], startX + i * iconSpacing, this.getY() + 2, null);
                    }
                }
            }
            if (resLoc != null) {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GuiUtils.drawTexture(matrixStack, resLoc, this.getX() + width / 2 - 8, this.getY() + 2);
                RenderSystem.disableBlend();
            }
            if (active && !thisVisible && x >= this.getX() && y >= this.getY() && x < this.getX() + width && y < this.getY() + height) {
                GuiComponent.fill(matrixStack, this.getX(), this.getY(), this.getX() + width, this.getY() + height, invisibleHoverColor);
            }
        }
    }

    private int getIconX() {
        return switch (iconPosition) {
            case LEFT -> getX() - 1 - 18 * renderedStacks.length;
            case RIGHT -> getX() + width + 1;
            case MIDDLE -> getX() + width / 2 - renderedStacks.length * 9 + 1;
        };
    }
}
