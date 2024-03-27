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
import me.desht.pneumaticcraft.client.gui.semiblock.AbstractLogisticsScreen;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.inventory.slot.PhantomSlot;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.List;

public class LogisticsFilterGhost<S extends AbstractLogisticsScreen<T>, T extends AbstractLogisticsFrameEntity> implements IGhostIngredientHandler<S> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(S gui, ITypedIngredient<I> ingredient, boolean doStart) {
        ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
        if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
            for (Slot slot : gui.getMenu().slots) {
                if (slot instanceof PhantomSlot ps) {
                    //noinspection unchecked
                    builder.add((Target<I>) new ItemStackTarget(ps, gui));
                }
            }
            FluidUtil.getFluidContained(ingredient.getIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY)).ifPresent(fluidStack -> {
                for (int i = 0; i < AbstractLogisticsFrameEntity.FLUID_FILTER_SLOTS; i++) {
                    //noinspection unchecked
                    builder.add((Target<I>) new FluidStackItemTarget(i, gui));
                }
            });
        } else if (ingredient.getType() == NeoForgeTypes.FLUID_STACK) {
            for (int i = 0; i < AbstractLogisticsFrameEntity.FLUID_FILTER_SLOTS; i++) {
                //noinspection unchecked
                builder.add((Target<I>) new FluidStackTarget(i, gui));
            }
        }
        return builder.build();
    }

    @Override
    public void onComplete() {
    }

    private record ItemStackTarget(PhantomSlot slot, AbstractLogisticsScreen<?> gui) implements Target<ItemStack> {
        @Override
        public Rect2i getArea() {
            return new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public void accept(ItemStack ingredient) {
            gui.updateItemFilter(slot.getSlotIndex(), ingredient.copy());
        }
    }

    private record FluidStackTarget(int slotNumber, AbstractLogisticsScreen<?> gui) implements Target<FluidStack> {
        @Override
        public Rect2i getArea() {
            PointXY p = gui.getFluidSlotPos(slotNumber);
            return new Rect2i(p.x(), p.y(), 16, 16);
        }

        @Override
        public void accept(FluidStack ingredient) {
            gui.updateFluidFilter(slotNumber, ingredient.copy());
        }
    }

    // for items which contain fluids
    private record FluidStackItemTarget(int slotNumber, AbstractLogisticsScreen<?> gui) implements Target<ItemStack> {
        @Override
        public Rect2i getArea() {
            PointXY p = gui.getFluidSlotPos(slotNumber);
            return new Rect2i(p.x(), p.y(), 16, 16);
        }

        @Override
        public void accept(ItemStack ingredient) {
            FluidUtil.getFluidContained(ingredient).ifPresent(fluidStack -> gui.updateFluidFilter(slotNumber, fluidStack));
        }
    }
}
