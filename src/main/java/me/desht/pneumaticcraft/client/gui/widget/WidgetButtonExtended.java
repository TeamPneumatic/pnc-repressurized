package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extension of GuiButtonExt that add: 1) a string tag which is sent to the server when clicked (PacketGuiButton),
 * 2) ability to draw itemstack or textured icons & 3) can render its area when invisible
 */
public class WidgetButtonExtended extends ExtendedButton implements ITaggedWidget, ITooltipProvider {
    private int iconSpacing = 18;

    public enum IconPosition { MIDDLE, LEFT, RIGHT }
    private ItemStack[] renderedStacks;

    private ResourceLocation resLoc;
    private final List<ITextComponent> tooltipText = new ArrayList<>();
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    private int invisibleHoverColor;
    private boolean thisVisible = true;
    private IconPosition iconPosition = IconPosition.MIDDLE;
    private String tag = null;

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, ITextComponent buttonText, Button.IPressable pressable) {
        super(startX, startY, xSize, ySize, buttonText, pressable);
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, ITextComponent buttonText) {
        this(startX, startY, xSize, ySize, buttonText, b -> {});
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, String buttonText, IPressable pressable) {
        super(startX, startY, xSize, ySize, new StringTextComponent(buttonText), pressable);
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize, String buttonText) {
        this(startX, startY, xSize, ySize, buttonText, b -> {});
    }

    public WidgetButtonExtended(int startX, int startY, int xSize, int ySize) {
        this(startX, startY, xSize, ySize, StringTextComponent.EMPTY, b -> {});
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

    public WidgetButtonExtended setTooltipText(ITextComponent tooltip) {
        return setTooltipText(Collections.singletonList(tooltip));
    }

    public WidgetButtonExtended setTooltipText(List<ITextComponent> tooltip) {
        tooltipText.clear();
        tooltipText.addAll(tooltip);
        return this;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
        curTip.addAll(tooltipText);
    }

    public boolean hasTooltip() {
        return !tooltipText.isEmpty();
    }

    public List<ITextComponent> getTooltip() {
        return tooltipText;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int x, int y, float partialTicks) {
        if (thisVisible) super.renderButton(matrixStack, x, y, partialTicks);

        if (visible) {
            if (renderedStacks != null) {
                int startX = getIconX();
                RenderHelper.enableStandardItemLighting();
                for (int i = renderedStacks.length - 1; i >= 0; i--) {
                    GuiUtils.renderItemStack(matrixStack, renderedStacks[i], startX + i * iconSpacing, this.y + 2);
                }
                RenderHelper.disableStandardItemLighting();
            }
            if (resLoc != null) {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GuiUtils.drawTexture(matrixStack, resLoc, this.x + width / 2 - 8, this.y + 2);
                RenderSystem.disableBlend();
            }
            if (active && !thisVisible && x >= this.x && y >= this.y && x < this.x + width && y < this.y + height) {
                AbstractGui.fill(matrixStack, this.x, this.y, this.x + width, this.y + height, invisibleHoverColor);
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
