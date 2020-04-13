package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.DyeColor;

import java.util.function.Consumer;

public class WidgetColorSelector extends WidgetButtonExtended {
    private boolean expanded = false;
    private DyeColor color = DyeColor.WHITE;
    private final Rectangle2d mainArea;
    private final Rectangle2d expandedArea;
    private final Consumer<WidgetColorSelector> callback;

    public WidgetColorSelector(int xIn, int yIn) {
        this(xIn, yIn, null);
    }

    public WidgetColorSelector(int xIn, int yIn, Consumer<WidgetColorSelector> callback) {
        super(xIn, yIn, 16, 16, "");

        mainArea = new Rectangle2d(xIn, yIn, width, height);
        expandedArea = new Rectangle2d(xIn, yIn + height, width * 4, height * 4);

        this.callback = callback;
    }

    public WidgetColorSelector withInitialColor(DyeColor color) {
        this.color = color;
        return this;
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        super.renderButton(mouseX, mouseY, partialTick);

        fill(x + 3, y + 3, x + width - 4, y + height - 4, 0xFF000000 | color.getColorValue());
        hLine(x + 3, x + width - 3, y + height - 4, 0xFF606060);
        vLine(x + width - 4, y + 3, y + height - 3, 0xFF606060);

        if (expanded) {
            RenderSystem.translated(0, 0, 500);
            fill(x, y - 1 + height, x + width * 4, y -1 + height * 5, 0xFF000000);
            fill(x + 1, y + height, x + width * 4 - 1, y - 2 + height * 5, 0xFF808080);
            for (DyeColor color : DyeColor.values()) {
                int dx = x + (color.getId() % 4) * 16;
                int dy = y - 1 + height + (color.getId() / 4) * 16;
                fill(dx + 3, dy + 3, dx + 13, dy + 13, 0xFF000000 | color.getColorValue());
                hLine(dx + 3, dx + 13, dy + 13, 0xFF606060);
                vLine(dx + 13, dy + 3, dy + 13, 0xFF606060);
            }
            RenderSystem.translated(0, 0, -500);
        }
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY) || expanded && expandedArea.contains((int) mouseX, (int) mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mainArea.contains((int) mouseX, (int) mouseY)) {
            expanded = !expanded;
        } else if (expandedArea.contains((int) mouseX, (int) mouseY)) {
            int dx = (int)mouseX - expandedArea.getX();
            int dy = (int)mouseY - expandedArea.getY();
            int id = dx / 16 + (dy / 16) * 4;
            color = DyeColor.byId(id);
            expanded = !expanded;
            if (callback != null) {
                callback.accept(this);
            }
        }
    }
}
