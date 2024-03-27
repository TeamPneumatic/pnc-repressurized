package me.desht.pneumaticcraft.common.recipes.special;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class CustomPNCRecipe extends CustomRecipe {
    public CustomPNCRecipe(CraftingBookCategory category) {
        super(category);
    }

    protected final List<ItemStack> findItems(CraftingContainer inv, List<Predicate<ItemStack>> predicates) {
        List<ItemStack> res = new ArrayList<>();

        for (var pred : predicates) {
            boolean found = false;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (pred.test(stack)) {
                    res.add(stack);
                    found = true;
                    break;
                }
            }
            if (!found) return List.of();
        }

        return res;
    }
}
