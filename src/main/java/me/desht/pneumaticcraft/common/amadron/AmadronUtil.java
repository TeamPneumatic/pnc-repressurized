package me.desht.pneumaticcraft.common.amadron;

import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class AmadronUtil {
    public static ItemStack[] buildStacks(ItemStack stack, int units) {
        int amount = stack.getCount() * units;
        List<ItemStack> stacks = new ArrayList<>();
        while (amount > 0 && stacks.size() < ContainerAmadron.HARD_MAX_STACKS) {
            ItemStack toAdd = ItemHandlerHelper.copyStackWithSize(stack, Math.min(amount, stack.getMaxStackSize()));
            stacks.add(toAdd);
            amount -= toAdd.getCount();
        }
        return stacks.toArray(new ItemStack[0]);
    }

    public static FluidStack buildFluidStack(FluidStack fluidStack, int units) {
        FluidStack res = fluidStack.copy();
        res.setAmount(Math.min(ContainerAmadron.HARD_MAX_MB, res.getAmount() * units));
        return res;
    }
}
