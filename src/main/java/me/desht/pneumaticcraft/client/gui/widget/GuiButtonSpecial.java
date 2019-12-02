package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of GuiButton that allows a invisible clickable field. It can be added in Gui's like buttons (with the buttonList).
 */
public class GuiButtonSpecial extends GuiButtonExt implements ITaggedWidget, ITooltipSupplier {
    public enum IconPosition { MIDDLE, LEFT, RIGHT }
    private ItemStack[] renderedStacks;

    private ResourceLocation resLoc;
    private List<String> tooltipText = new ArrayList<>();
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    private int invisibleHoverColor;
    private boolean thisVisible = true;
    private IconPosition iconPosition = IconPosition.MIDDLE;
    private String tag = null;

    public GuiButtonSpecial(int startX, int startY, int xSize, int ySize, String buttonText, IPressable pressable) {
        super(startX, startY, xSize, ySize, buttonText, pressable);
    }

    public GuiButtonSpecial(int startX, int startY, int xSize, int ySize, String buttonText) {
        this(startX, startY, xSize, ySize, buttonText, b -> {});
    }

    /**
     * Added a string tag to the button.  This will be sent to the server as the payload of a {@link PacketGuiButton}
     * packet when the button is clicked.
     *
     * @param tag a string tag containing any arbitrary information
     * @return the button, for fluency
     */
    public GuiButtonSpecial withTag(String tag) {
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

    public void setVisible(boolean visible) {
        thisVisible = visible;
    }

    public void setInvisibleHoverColor(int color) {
        invisibleHoverColor = color;
    }

    public void setIconPosition(IconPosition iconPosition) {
        this.iconPosition = iconPosition;
    }

    public GuiButtonSpecial setRenderStacks(ItemStack... renderedStacks) {
        this.renderedStacks = renderedStacks;
        return this;
    }

    public GuiButtonSpecial setRenderedIcon(ResourceLocation resLoc) {
        this.resLoc = resLoc;
        return this;
    }

    public GuiButtonSpecial setTooltipText(List<String> tooltip) {
        tooltipText.clear();
        tooltipText.addAll(tooltip);
        return this;
    }

    public GuiButtonSpecial setTooltipText(String tooltip) {
        tooltipText.clear();
        if (tooltip != null && !tooltip.equals("")) {
            tooltipText.add(tooltip);
        }
        return this;
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
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
    public void render(int x, int y, float partialTicks) {
        if (thisVisible) super.render(x, y, partialTicks);

        if (visible) {
            if (renderedStacks != null) {
                int startX = getIconX();
                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();
                for (int i = 0; i < renderedStacks.length; i++) {
                    itemRenderer.renderItemAndEffectIntoGUI(renderedStacks[i], startX + i * 18, this.y + 2);
                }
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
            }
            if (resLoc != null) {
                Minecraft.getInstance().getTextureManager().bindTexture(resLoc);
                blit(this.x + width / 2 - 8, this.y + 2, 0, 0, 16, 16, 16, 16);
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

//    @Override
//    public void setListener(IWidgetListener gui) {
//        listener = gui;
//    }
//
//    @Override
//    public int getID() {
//        return id;
//    }
//
////    @Override
////    public void render(int mouseX, int mouseY, float partialTick) {
////        drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTick);
////    }
//
//    @Override
//    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
//        if (mousePressed(Minecraft.getInstance(), mouseX, mouseY)) {
//            playDownSound(Minecraft.getInstance().getSoundHandler());
//            listener.actionPerformed(this);
//        }
//    }
//
//    @Override
//    public boolean onMouseClickedOutsideBounds(double mouseX, double mouseY, int button) {
//        return false;
//    }
//
//    @Override
//    public Rectangle getBounds() {
//        return new Rectangle(x, y, width, height);
//    }
//
//    @Override
//    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {
//        if (visible) curTooltip.addAll(tooltipText);
//    }
//
//    @Override
//    public boolean onKey(char key, int keyCode) {
//        return false;
//    }
//
//    @Override
//    public void update() {
//
//    }
//
//    @Override
//    public void handleMouseInput() {
//    }
//
//    @Override
//    public void postRender(int mouseX, int mouseY, float partialTick) {
//    }

}
