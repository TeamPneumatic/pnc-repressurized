package me.desht.pneumaticcraft.common.recipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

public enum PlasticMixerRegistry {
    INSTANCE;

    private final Map<String, Integer> fluidRatios = new HashMap<>();
    private final Map<Item, Boolean> validItems = new HashMap<>();

    private final List<PlasticMixerRecipe> recipes = new ArrayList<>();

    public void addPlasticMixerRecipe(@Nonnull FluidStack fluid, @Nonnull ItemStack stack, int temperature, boolean allowMelting, boolean allowSolidifying) {
        if (fluid.amount > 0 && !stack.isEmpty()) {
            recipes.add(new PlasticMixerRecipe(fluid, ItemHandlerHelper.copyStackWithSize(stack, 1), temperature, allowMelting, allowSolidifying));
            fluidRatios.put(fluid.getFluid().getName(), fluid.amount);
            validItems.put(stack.getItem(), allowMelting);
        } else {
            recipes.removeIf(record -> record.getFluidStack().getFluid() == fluid.getFluid());
            fluidRatios.remove(fluid.getFluid().getName());
            validItems.remove(stack.getItem());
        }
    }

    public PlasticMixerRecipe getRecipe(FluidStack input) {
        if (input == null || input.amount == 0) return null;

        for (PlasticMixerRecipe record : recipes) {
            if (record.fluidStack.getFluid() == input.getFluid() && record.fluidStack.amount <= input.amount) {
                return record;
            }
        }

        return null;
    }

    public PlasticMixerRecipe getRecipe(ItemStack stack) {
        for (PlasticMixerRecipe record : recipes) {
            if (stack.getItem() == record.itemStack.getItem()) {
                return record;
            }
        }
        return null;
    }

    public int getFluidRatio(Fluid fluid) {
        return fluidRatios.getOrDefault(fluid.getName(), 0);
    }

    public void clear() {
        recipes.clear();
        fluidRatios.clear();
        validItems.clear();
    }

    public Iterable<? extends PlasticMixerRecipe> allRecipes() {
        return recipes;
    }

    public boolean isValidInputItem(ItemStack stack) {
        return validItems.getOrDefault(stack.getItem(), false);
    }

    public boolean isValidOutputItem(ItemStack stack) {
        return validItems.containsKey(stack.getItem());
    }

    public static class PlasticMixerRecipe {
        private final FluidStack fluidStack;
        private final ItemStack itemStack;
        private final int temperature;
        private final boolean allowMelting;
        private final boolean allowSolidifying;

        PlasticMixerRecipe(FluidStack fluidStack, ItemStack itemStack, int temperature, boolean allowMelting, boolean allowSolidifying) {
            this.fluidStack = fluidStack;
            this.itemStack = itemStack;
            this.temperature = temperature;
            this.allowMelting = allowMelting;
            this.allowSolidifying = allowSolidifying;
        }

        public FluidStack getFluidStack() {
            return fluidStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public boolean allowMelting() {
            return allowMelting;
        }

        public boolean allowSolidifying() {
            return allowSolidifying;
        }

        public int getTemperature() {
            return temperature;
        }
    }
}
