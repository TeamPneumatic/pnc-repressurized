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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.GuiAmadronAddTrade;
import me.desht.pneumaticcraft.common.inventory.SlotPhantom;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class AmadronAddTradeGhost implements IGhostIngredientHandler<GuiAmadronAddTrade> {

    @Override
    public <I> List<Target<I>> getTargets(GuiAmadronAddTrade gui, I ingredient, boolean doStart) {
        if (ingredient instanceof ItemStack) {
            ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
            for (Slot slot : gui.getMenu().slots) {
                if (slot instanceof SlotPhantom) {
                    //noinspection unchecked
                    builder.add((Target<I>) new ItemStackTarget((SlotPhantom) slot, gui));
                }
            }
            return builder.build();
        } else if (ingredient instanceof FluidStack) {
            ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
            for (Slot slot : gui.getMenu().slots) {
                if (slot instanceof SlotPhantom) {
                    //noinspection unchecked
                    builder.add((Target<I>) new FluidStackTarget((SlotPhantom) slot, gui));
                }
            }
            return builder.build();
        }
        return Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }

    private static abstract class TargetImpl<I> implements Target<I> {
        final SlotPhantom slot;
        final GuiAmadronAddTrade gui;

        TargetImpl(SlotPhantom slot, GuiAmadronAddTrade gui) {
            this.slot = slot;
            this.gui = gui;
        }

        @Override
        public Rect2i getArea() {
            return new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
        }
    }

    private static class FluidStackTarget extends TargetImpl<FluidStack> {
        FluidStackTarget(SlotPhantom slot, GuiAmadronAddTrade gui) {
            super(slot, gui);
        }

        @Override
        public void accept(FluidStack ingredient) {
            gui.setFluid(slot.getSlotIndex(), ingredient.getFluid());
        }
    }

    private static class ItemStackTarget extends TargetImpl<ItemStack> {
        ItemStackTarget(SlotPhantom slot, GuiAmadronAddTrade gui) {
            super(slot, gui);
        }

        @Override
        public void accept(ItemStack ingredient) {
            gui.setStack(slot.getSlotIndex(), ingredient);
        }
    }
}
