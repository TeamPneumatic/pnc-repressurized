package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of GuiButtonExt that add: 1) a string tag which is sent to the server when clicked (PacketGuiButton),
 * 2) ability to draw itemstack or textured icons & 3) can render its area when invisible
 */
public class WidgetButtonExtended extends ExtendedButton implements ITaggedWidget, ITooltipProvider {
    public enum IconPosition { MIDDLE, LEFT, RIGHT }
    private ItemStack[] renderedStacks;

    private ResourceLocation resLoc;
    private List<String> tooltipText = new ArrayList<>();
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    private int invisibleHoverColor;
    private boolean thisVisible = true;
    private IconPosition iconPosition = IconPosition.MIDDLE;
    private String tag = null;

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, String buttonText, IPressable pressable) {
        super(startX, startY, xSize, ySize, buttonText, pressable);
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, String buttonText) {
        this(startX, startY, xSize, ySize, buttonText, b -> {});
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
        if (tag != null && !tag.isEmpty()) NetworkHandler.sendToServer(new PacketGuiButton(tag, ClientUtils.hasShiftDown()));
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
        return this;
    }

    public WidgetButtonExtended setRenderedIcon(ResourceLocation resLoc) {
        this.resLoc = resLoc;
        return this;
    }

    public WidgetButtonExtended setTooltipText(List<String> tooltip) {
        tooltipText.clear();
        tooltipText.addAll(tooltip);
        return this;
    }

    public WidgetButtonExtended setTooltipText(String tooltip) {
        tooltipText.clear();
        if (tooltip != null && !tooltip.equals("")) {
            tooltipText.add(tooltip);
        }
        return this;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
        if (tooltipText != null) {
            curTip.addAll(tooltipText);
        }
    }

    public String getTooltip() {
        return tooltipText.size() > 0 ? tooltipText.get(0) : "";
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void renderButton(int x, int y, float partialTicks) {
        if (thisVisible) super.renderButton(x, y, partialTicks);

        if (visible) {
            if (renderedStacks != null) {
                int startX = getIconX();
                RenderSystem.enableRescaleNormal();
                RenderHelper.enableStandardItemLighting();
//                RenderHelper.enableGUIStandardItemLighting();
                for (int i = 0; i < renderedStacks.length; i++) {
                    itemRenderer.renderItemAndEffectIntoGUI(renderedStacks[i], startX + i * 18, this.y + 2);
                }
                RenderHelper.disableStandardItemLighting();
                RenderSystem.disableRescaleNormal();
            }
            if (resLoc != null) {
                GuiUtils.drawTexture(resLoc, this.x + width / 2 - 8, this.y + 2);
            }
            if (active && !thisVisible && x >= this.x && y >= this.y && x < this.x + width && y < this.y + height) {
                AbstractGui.fill(this.x, this.y, this.x + width, this.y + height, invisibleHoverColor);
            }
        }
    }

    private int getIconX() {
        switch (iconPosition) {
            case LEFT: return x - 1 - 18 * renderedStacks.length;
            case RIGHT: return x + width + 1;
            case MIDDLE: default: return x + width / 2 - renderedStacks.length * 9 + 1;
        }
    }
}
