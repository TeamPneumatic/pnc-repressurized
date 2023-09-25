/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.jei.ghost;

import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetItemFilterScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class ProgWidgetItemFilterGhost implements IGhostIngredientHandler<ProgWidgetItemFilterScreen> {
    @Override
    public <I> List<Target<I>> getTargets(ProgWidgetItemFilterScreen gui, I ingredient, boolean doStart) {
        //noinspection unchecked
        return gui.itemX >= 0 && ingredient instanceof ItemStack ?
                Collections.singletonList((Target<I>) new ItemTarget(gui)) :
                Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }

    static class ItemTarget implements Target<ItemStack> {
        private final ProgWidgetItemFilterScreen gui;
        private final Rect2i area;

        ItemTarget(ProgWidgetItemFilterScreen gui) {
            this.gui = gui;
            this.area = new Rect2i(gui.guiLeft + gui.itemX + 1, gui.guiTop + 52, 16, 16);
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(ItemStack ingredient) {
            gui.setFilterStack(ingredient);
        }
    }
}
