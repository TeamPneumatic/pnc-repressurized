package me.desht.pneumaticcraft.common.thirdparty.jei.ghost;

import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetItemFilter;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class ProgWidgetItemFilterGhost implements IGhostIngredientHandler<GuiProgWidgetItemFilter> {
    @Override
    public <I> List<Target<I>> getTargets(GuiProgWidgetItemFilter gui, I ingredient, boolean doStart) {
        //noinspection unchecked
        return Collections.singletonList((Target<I>) new ItemTarget(gui));
    }

    @Override
    public void onComplete() {
    }

    static class ItemTarget implements Target<ItemStack> {
        private final GuiProgWidgetItemFilter gui;
        private final Rectangle2d area;

        ItemTarget(GuiProgWidgetItemFilter gui) {
            this.gui = gui;
            this.area = new Rectangle2d(gui.guiLeft + 50, gui.guiTop + 52, 16, 16);
        }

        @Override
        public Rectangle2d getArea() {
            return area;
        }

        @Override
        public void accept(ItemStack ingredient) {
            gui.setFilterStack(ingredient);
        }
    }
}
