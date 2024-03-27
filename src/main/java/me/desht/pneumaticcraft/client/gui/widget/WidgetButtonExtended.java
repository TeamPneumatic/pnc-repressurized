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
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jline.reader.Widget;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Extension of GuiButtonExt that add: 1) a string tag which is sent to the server when clicked (PacketGuiButton),
 * 2) ability to draw itemstack or textured icons, and 3) can render its area when invisible
 */
public class WidgetButtonExtended extends ExtendedButton implements ITaggedWidget {
    private int iconSpacing = 18;
    private Supplier<List<Component>> tooltipSupplier;

    public enum IconPosition { MIDDLE, LEFT, RIGHT }
    private ItemStack[] renderedStacks;
    private ResourceLocation resLoc;
    private int invisibleHoverColor;
    private boolean thisVisible = true;
    private IconPosition iconPosition = IconPosition.MIDDLE;
    private String tag = null;
    private Supplier<String> tagSupplier = null;
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
        this.tagSupplier = null;
        return this;
    }

    public WidgetButtonExtended withTag(Supplier<String> tagSupplier) {
        this.tag = null;
        this.tagSupplier = tagSupplier;
        return this;
    }

    public WidgetButtonExtended withCustomTooltip(Supplier<List<Component>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }

    @Override
    public void onPress() {
        super.onPress();

        String tag1 = getTag();
        if (tag1 != null && !tag1.isEmpty()) NetworkHandler.sendToServer(new PacketGuiButton(tag1));
    }

    @Override
    public String getTag() {
        return tagSupplier == null ? tag : tagSupplier.get();
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

    public void setHighlightWhenInactive(boolean highlight) {
        this.highlightInactive = highlight;
    }

    public void setRenderStackSize(boolean renderStackSize) {
        this.renderStackSize = renderStackSize;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int x, int y, float partialTicks) {
        if (thisVisible && visible && !active && highlightInactive) {
            graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + getWidth() + 1, this.getY() + getHeight() + 1, 0xFF00FFFF);
        }

        if (thisVisible) super.renderWidget(graphics, x, y, partialTicks);

        if (visible) {
            if (renderedStacks != null) {
                int startX = getIconX();
                for (int i = renderedStacks.length - 1; i >= 0; i--) {
                    graphics.renderItem(renderedStacks[i], startX + i * iconSpacing, this.getY() + 2);
                    if (renderStackSize) {
                        graphics.renderItemDecorations(Minecraft.getInstance().font, renderedStacks[i], startX + i * iconSpacing, this.getY() + 2, null);
                    }
                }
            }
            if (resLoc != null) {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                graphics.blit(resLoc, this.getX() + width / 2 - 8, this.getY() + 2, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }
            if (active && !thisVisible && x >= this.getX() && y >= this.getY() && x < this.getX() + width && y < this.getY() + height) {
                graphics.fill(this.getX(), this.getY(), this.getX() + width, this.getY() + height, invisibleHoverColor);
            }
            if (isHovered && tooltipSupplier != null) {
                graphics.renderTooltip(Minecraft.getInstance().font, tooltipSupplier.get(), Optional.empty(), x, y);
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

    public WidgetButtonExtended setTooltipText(Component comp) {
        setTooltip(Tooltip.create(comp));
        return this;
    }

    public WidgetButtonExtended setTooltipText(List<Component> comps) {
        setTooltip(Tooltip.create(PneumaticCraftUtils.combineComponents(comps)));
        return this;
    }

    public WidgetButtonExtended setTooltipKey(String tip) {
        setTooltip(Tooltip.create(xlate(tip)));
        return this;
    }
}
