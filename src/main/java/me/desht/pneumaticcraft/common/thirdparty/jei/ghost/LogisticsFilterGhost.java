package me.desht.pneumaticcraft.common.thirdparty.jei.ghost;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsBase;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.inventory.SlotPhantom;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class LogisticsFilterGhost<T extends EntityLogisticsFrame> implements IGhostIngredientHandler<GuiLogisticsBase<T>> {
    @Override
    public <I> List<Target<I>> getTargets(GuiLogisticsBase<T> gui, I ingredient, boolean doStart) {
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
            for (int i = 0; i < EntityLogisticsFrame.FLUID_FILTER_SLOTS; i++) {
                //noinspection unchecked
                builder.add((Target<I>) new FluidStackTarget(i, gui));
            }
            return builder.build();
        }
        return Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }

    private static class ItemStackTarget implements Target<ItemStack> {
        final SlotPhantom slot;
        final GuiLogisticsBase<?> gui;

        ItemStackTarget(SlotPhantom slot, GuiLogisticsBase<?> gui) {
            this.slot = slot;
            this.gui = gui;
        }

        @Override
        public Rectangle2d getArea() {
            return new Rectangle2d(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public void accept(ItemStack ingredient) {
            gui.updateItemFilter(slot.getSlotIndex(), ingredient.copy());
        }
    }

    private static class FluidStackTarget implements Target<FluidStack> {
        final int slotNumber;
        final GuiLogisticsBase<?> gui;

        FluidStackTarget(int slotNumber, GuiLogisticsBase<?> gui) {
            this.slotNumber = slotNumber;
            this.gui = gui;
        }

        @Override
        public Rectangle2d getArea() {
            PointXY p = gui.getFluidSlotPos(slotNumber);
            return new Rectangle2d(p.x, p.y, 16, 16);
        }

        @Override
        public void accept(FluidStack ingredient) {
            gui.updateFluidFilter(slotNumber, ingredient.copy());
        }
    }
}
