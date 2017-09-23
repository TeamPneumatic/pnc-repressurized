package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.IWidgetListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension of GuiButton that allows a invisible clickable field. It can be added in Gui's like buttons (with the buttonList).
 */

public class GuiButtonSpecial extends GuiButton implements IGuiWidget {

    private ItemStack[] renderedStacks;
    private ResourceLocation resLoc;
    private List<String> tooltipText = new ArrayList<String>();
    private final RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
    private int invisibleHoverColor;
    private boolean thisVisible = true;
    private IWidgetListener listener;

    public GuiButtonSpecial(int buttonID, int startX, int startY, int xSize, int ySize, String buttonText) {
        super(buttonID, startX, startY, xSize, ySize, buttonText);
    }

    public void setVisible(boolean visible) {
        thisVisible = visible;
    }

    public void setInvisibleHoverColor(int color) {
        invisibleHoverColor = color;
    }

    public GuiButtonSpecial setRenderStacks(ItemStack... renderedStacks) {
        this.renderedStacks = renderedStacks;
        return this;
    }

    public void setRenderedIcon(ResourceLocation resLoc) {
        this.resLoc = resLoc;
    }

    public GuiButtonSpecial setTooltipText(List<String> tooltip) {
        tooltipText = tooltip;
        return this;
    }

    public GuiButtonSpecial setTooltipText(String tooltip) {
        tooltipText.clear();
        if (tooltip != null && !tooltip.equals("")) {
            tooltipText.add(tooltip);
        }
        return this;
    }

    public void getTooltip(List<String> curTooltip) {
        if (tooltipText != null) {
            curTooltip.addAll(tooltipText);
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
    public void drawButton(Minecraft mc, int x, int y, float partialTicks) {
        if (thisVisible) super.drawButton(mc, x, y, partialTicks);

        if (visible) {
            if (renderedStacks != null) {
                int middleX = this.x + width / 2;
                int startX = middleX - renderedStacks.length * 9 + 1;
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.enableGUIStandardItemLighting();
                for (int i = 0; i < renderedStacks.length; i++) {
                    itemRenderer.renderItemAndEffectIntoGUI(renderedStacks[i], startX + i * 18, this.y + 2);
                }
                RenderHelper.disableStandardItemLighting();
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            }
            if (resLoc != null) {
                mc.getTextureManager().bindTexture(resLoc);
                drawModalRectWithCustomSizedTexture(this.x + width / 2 - 8, this.y + 2, 0, 0, 16, 16, 16, 16);
            }
            if (enabled && !thisVisible && x >= this.x && y >= this.y && x < this.x + width && y < this.y + height) {
                Gui.drawRect(this.x, this.y, this.x + width, this.y + height, invisibleHoverColor);
            }
        }
    }

    @Override
    public void setListener(IWidgetListener gui) {
        listener = gui;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTick);
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        if (mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            playPressSound(Minecraft.getMinecraft().getSoundHandler());
            listener.actionPerformed(this);
        }
    }

    @Override
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button) {

    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {
        curTooltip.addAll(tooltipText);
    }

    @Override
    public boolean onKey(char key, int keyCode) {
        return false;
    }

    @Override
    public void update() {

    }

    @Override
    public void handleMouseInput() {
    }

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick) {
    }

}
