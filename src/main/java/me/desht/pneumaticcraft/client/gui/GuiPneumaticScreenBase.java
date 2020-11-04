package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.widget.ITickableWidget;
import me.desht.pneumaticcraft.client.gui.widget.ITooltipProvider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
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

    protected WidgetLabel addLabel(ITextComponent text, int x, int y) {
        return addLabel(text, x, y, WidgetLabel.Alignment.LEFT);
    }

    protected WidgetLabel addLabel(ITextComponent text, int x, int y, WidgetLabel.Alignment alignment) {
        return addButton(new WidgetLabel(x, y, text).setAlignment(alignment));
    }

    protected void removeWidget(Widget widget) {
        buttons.remove(widget);
        children.remove(widget);
    }

    @Override
    public void tick() {
        super.tick();

        buttons.stream().filter(w -> w instanceof ITickableWidget).forEach(w -> ((ITickableWidget) w).tickWidget());
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        RenderSystem.color4f(1f, 1f, 1f, 1.0f);

        if (getTexture() != null) {
            minecraft.getTextureManager().bindTexture(getTexture());
            blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
        }

        super.render(matrixStack, x, y, partialTicks);

        List<ITextComponent> tooltip = new ArrayList<>();
        boolean shift = Screen.hasShiftDown();
        buttons.stream()
                .filter(widget -> widget instanceof ITooltipProvider && widget.isHovered())
                .forEach(widget -> ((ITooltipProvider) widget).addTooltip(x, y, tooltip, shift));

        if (!tooltip.isEmpty()) {
            int max = Math.min(xSize * 4 / 3, width / 3);
            renderTooltip(matrixStack, GuiUtils.wrapTextComponentList(tooltip, max, font), x, y);
        }
    }
}
