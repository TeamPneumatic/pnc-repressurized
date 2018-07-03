package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;
import org.lwjgl.input.Mouse;

public class WidgetVerticalScrollbar extends WidgetBase {
    public float currentScroll;
    private int states;
    private boolean listening;
    private boolean dragging;
    private boolean wasClicking;
    private boolean enabled = true;
    private static ResourceLocation scrollTexture = new ResourceLocation(Textures.GUI_LOCATION + "widget/vertical_scrollbar.png");

    public WidgetVerticalScrollbar(int x, int y, int height) {
        this(-1, x, y, height);
    }

    public WidgetVerticalScrollbar(int id, int x, int y, int height) {
        super(id, x, y, 14, height);
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
    public void handleMouseInput() {
        if (listening) {
            int wheel = -Mouse.getDWheel();
            wheel = MathHelper.clamp(wheel, -1, 1);
            currentScroll += (float) wheel / states;
        }
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
        GlStateManager.color(1, 1, 1, 1);
        if (!Mouse.isButtonDown(0)) dragging = false;
        if (!wasClicking && Mouse.isButtonDown(0) && getBounds().contains(mouseX, mouseY)) {
            dragging = true;
        }
        if (!enabled) dragging = false;
        wasClicking = Mouse.isButtonDown(0);
        if (dragging) currentScroll = (float) (mouseY - 7 - getBounds().y) / (getBounds().height - 17);
        currentScroll = MathHelper.clamp(currentScroll, 0, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(scrollTexture);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 12, 0, getBounds().width, 1, 26, 15);
        for (int i = 0; i < getBounds().height - 2; i++)
            Gui.drawModalRectWithCustomSizedTexture(x, y + 1 + i, 12, 1, getBounds().width, 1, 26, 15);
        Gui.drawModalRectWithCustomSizedTexture(x, y + getBounds().height - 1, 12, 14, getBounds().width, 1, 26, 15);

        if (!enabled) GlStateManager.color(0.6F, 0.6F, 0.6F, 1);
        Gui.drawModalRectWithCustomSizedTexture(x + 1, y + 1 + (int) ((getBounds().height - 17) * currentScroll), 0, 0, 12, 15, 26, 15);
        GlStateManager.color(1, 1, 1, 1);
    }

    public boolean isDragging() {
        return dragging;
    }
}
