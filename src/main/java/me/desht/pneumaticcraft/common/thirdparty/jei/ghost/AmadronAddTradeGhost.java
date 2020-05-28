package me.desht.pneumaticcraft.common.thirdparty.jei.ghost;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.GuiAmadronAddTrade;
import me.desht.pneumaticcraft.common.inventory.SlotPhantom;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class AmadronAddTradeGhost implements IGhostIngredientHandler<GuiAmadronAddTrade> {

    @Override
    public <I> List<Target<I>> getTargets(GuiAmadronAddTrade gui, I ingredient, boolean doStart) {
        if (ingredient instanceof ItemStack) {
            ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
            for (Slot slot : gui.getContainer().inventorySlots) {
                if (slot instanceof SlotPhantom) {
                    //noinspection unchecked
                    builder.add((Target<I>) new ItemStackTarget((SlotPhantom) slot, gui));
                }
            }
            return builder.build();
        } else if (ingredient instanceof FluidStack) {
            ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
            for (Slot slot : gui.getContainer().inventorySlots) {
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
        public Rectangle2d getArea() {
            return new Rectangle2d(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos, 16, 16);
        }
    }

    private static class FluidStackTarget extends TargetImpl<FluidStack> {
        FluidStackTarget(SlotPhantom slot, GuiAmadronAddTrade gui) {
            super(slot, gui);
        }

        @Override
        public void accept(FluidStack ingredient) {
            gui.setFluid(slot.slotNumber, ingredient.getFluid());
        }
    }

    private static class ItemStackTarget extends TargetImpl<ItemStack> {
        ItemStackTarget(SlotPhantom slot, GuiAmadronAddTrade gui) {
            super(slot, gui);
        }

        @Override
        public void accept(ItemStack ingredient) {
            gui.setStack(slot.slotNumber, ingredient);
        }
    }
}
