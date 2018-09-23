package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PressureChamberRecipe {
    public static final List<IPressureChamberRecipe> recipes = new ArrayList<>();

    public static class SimpleRecipe implements IPressureChamberRecipe {
        private final float pressureRequired;
        private final List<ItemIngredient> input;
        private final NonNullList<ItemStack> output;

        public SimpleRecipe(ItemIngredient[] input, float pressureRequired, ItemStack[] output) {
            this.input = Arrays.asList(input);
            this.output = NonNullList.from(ItemStack.EMPTY, output);
            this.pressureRequired = pressureRequired;
        }

        @Override
        public float getCraftingPressure() {
            return pressureRequired;
        }

        @Override
        public boolean isValidRecipe(@Nonnull ItemStackHandler chamberHandler) {
            for (ItemIngredient ingredient : input) {
                int amount = getFilteredChamberContents(chamberHandler, ingredient).mapToInt(ItemStack::getCount).sum();
                if (amount < ingredient.getItemAmount()) {
                    return false;
                }
            }
            return true;
        }

        private Stream<ItemStack> getFilteredChamberContents(ItemStackHandler itemsInChamber, ItemIngredient ingredient) {
            return new ItemStackHandlerIterable(itemsInChamber).stream()
                    .filter(stack -> !stack.isEmpty() && ingredient.isItemEqual(stack));
        }

        @Override
        public List<ItemIngredient> getInput() {
            return input;
        }

        @Override
        public NonNullList<ItemStack> getResult() {
            return output;
        }

        @Nonnull
        @Override
        public NonNullList<ItemStack> craftRecipe(@Nonnull ItemStackHandler chamberHandler) {
            // remove the recipe's input items from the chamber
            for (ItemIngredient ingredient : input) {
                int amountLeft = ingredient.getItemAmount();
                for (int i = 0; i < chamberHandler.getSlots(); i++) {
                    ItemStack itemInChamber = chamberHandler.getStackInSlot(i);
                    if (ingredient.isItemEqual(itemInChamber)) {
                        amountLeft -= chamberHandler.extractItem(i, amountLeft, false).getCount();
                        if (amountLeft <= 0) break;
                    }
                }
            }

            return output;
        }
    }
}
