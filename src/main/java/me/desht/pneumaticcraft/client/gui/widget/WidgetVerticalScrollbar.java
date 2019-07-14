package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;

public class WidgetVerticalScrollbar extends Widget {
    private static final ResourceLocation SCROLL_TEXTURE = new ResourceLocation(Textures.GUI_LOCATION + "widget/vertical_scrollbar.png");
    
    public float currentScroll;
    private int states;
    private boolean listening;
    private boolean dragging;
    private boolean wasClicking;
    private boolean enabled = true;

    public WidgetVerticalScrollbar(int x, int y, int height) {
        super(x, y, 14, height, "");
    }

    public WidgetVerticalScrollbar setStates(int states) {
        this.states = states;
        return this;
    }

    public WidgetVerticalScrollbar setCurrentState(int state) {
        Validate.isTrue(state >= 0 && state <= states, "State " + state + " out of range! Valid range [1 - " + states + "] inclusive");
        currentScroll = (float) state / states;
        return this;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        if (listening) {
            // todo verify values
            int wheel = (int)-dir;
            wheel = MathHelper.clamp(wheel, -1, 1);
            currentScroll += (float) wheel / states;
        }
        return false;
    }

    @Override
    public void onClick(double x, double y) {
        dragging = true;
    }

    @Override
    public void onRelease(double x, double y) {
        dragging = false;
    }

    @Override
    protected void onDrag(double x, double y, double dx, double dy) {
        dragging = true;
        currentScroll = (float) (y - 7 - this.y) / (height - 17);
        currentScroll = MathHelper.clamp(currentScroll, 0, 1);
    }

    public WidgetVerticalScrollbar setListening(boolean listening) {
        this.listening = listening;
        return this;
    }

    public int getState() {
        float scroll = currentScroll;
        scroll += 0.5F / states;
        return MathHelper.clamp((int) (scroll * states), 0, states);
    }

    public WidgetVerticalScrollbar setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) wasClicking = false;
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        GlStateManager.color4f(1, 1, 1, 1);
//        if (!Mouse.isButtonDown(0)) dragging = false;
//        if (!wasClicking && Mouse.isButtonDown(0) && getBounds().contains(mouseX, mouseY)) {
//            dragging = true;
//        }
//        if (!enabled) dragging = false;
//        wasClicking = Mouse.isButtonDown(0);
//        if (dragging) currentScroll = (float) (mouseY - 7 - getBounds().y) / (getBounds().height - 17);

        Minecraft.getInstance().getTextureManager().bindTexture(SCROLL_TEXTURE);
        AbstractGui.blit(x, y, 12, 0, width, 1, 26, 15);
        for (int i = 0; i < height - 2; i++)
            AbstractGui.blit(x, y + 1 + i, 12, 1, width, 1, 26, 15);
        AbstractGui.blit(x, y + height - 1, 12, 14, width, 1, 26, 15);

        if (!enabled) GlStateManager.color4f(0.6F, 0.6F, 0.6F, 1);
        AbstractGui.blit(x + 1, y + 1 + (int) ((height - 17) * currentScroll), 0, 0, 12, 15, 26, 15);
        GlStateManager.color4f(1, 1, 1, 1);
    }

    public boolean isDragging() {
        return dragging;
    }
}
