package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.gui.widget.ITooltipProvider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GuiPneumaticScreenBase extends Screen {
    public int guiLeft, guiTop, xSize, ySize;

    public GuiPneumaticScreenBase(ITextComponent title) {
        super(title);
    }

    @Override
    public void init() {
        super.init();
        guiLeft = width / 2 - xSize / 2;
        guiTop = height / 2 - ySize / 2;
    }

    protected abstract ResourceLocation getTexture();

    protected void addLabel(String text, int x, int y) {
        addButton(new WidgetLabel(x, y, text));
    }

    protected void removeWidget(Widget widget) {
        buttons.remove(widget);
        children.remove(widget);
    }

    @Override
    public void tick() {
        super.tick();

        buttons.stream().filter(w -> w instanceof ITickable).forEach(w -> ((ITickable) w).tick());
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        GlStateManager.color4f(1f, 1f, 1f, 1.0f);
        if (getTexture() != null) {
            minecraft.getTextureManager().bindTexture(getTexture());
            blit(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
        super.render(x, y, partialTicks);

        GlStateManager.enableTexture();
        GlStateManager.color4f(0.25f, 0.25f, 0.25f, 1.0f);

        List<String> tooltip = new ArrayList<>();
        boolean shift = Screen.hasShiftDown();
        for (Widget widget : buttons) {
            if (widget instanceof ITooltipProvider && widget.isHovered()) {
                ((ITooltipProvider) widget).addTooltip(x, y, tooltip, shift);
            }
        }
        if (!tooltip.isEmpty()) {
            List<String> localizedTooltip = new ArrayList<>();
            for (String line : tooltip) {
                String localizedLine = I18n.format(line);
                for (String wrappedLine : localizedLine.split("\\\\n")) {
                    String[] lines = WordUtils.wrap(wrappedLine, 50).split(System.getProperty("line.separator"));
                    localizedTooltip.addAll(Arrays.asList(lines));
                }
            }
            renderTooltip(localizedTooltip, x, y, font);
        }
        GlStateManager.color4f(0.25f, 0.25f, 0.25f, 1.0f);
    }
}
