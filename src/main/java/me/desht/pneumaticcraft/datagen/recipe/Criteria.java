package me.desht.pneumaticcraft.datagen.recipe;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;

public class Criteria {
    private static InventoryChangeTrigger.Instance hasItem(net.minecraft.util.IItemProvider itemIn) {
        return hasItem(ItemPredicate.Builder.create().item(itemIn).build());
    }

    private static InventoryChangeTrigger.Instance hasItem(Tag<Item> tagIn) {
        return hasItem(ItemPredicate.Builder.create().tag(tagIn).build());
    }

    private static InventoryChangeTrigger.Instance hasItem(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.Instance(MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, predicates);
    }

    public static RecipeCriterion has(IItemProvider provider) {
        return RecipeCriterion.of(provider.asItem().getRegistryName().getPath(), hasItem(provider.asItem()));
    }

    public static RecipeCriterion has(Ingredient ingredient) {
        Item item = ingredient.getMatchingStacks()[0].getItem();
        return RecipeCriterion.of(item.getRegistryName().getPath(), hasItem(item));
    }

    public static class RecipeCriterion {

        public final String name;
        public final ICriterionInstance criterion;

        private RecipeCriterion(String name, ICriterionInstance criterion) {
            this.name = name;
            this.criterion = criterion;
        }

        public static RecipeCriterion of(String name, ICriterionInstance criterion) {
            return new RecipeCriterion(name, criterion);
        }
    }
}
