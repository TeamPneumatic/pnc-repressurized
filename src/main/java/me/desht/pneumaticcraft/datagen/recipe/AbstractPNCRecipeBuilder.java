package me.desht.pneumaticcraft.datagen.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPNCRecipeBuilder implements RecipeBuilder {
    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return Items.AIR;
    }
}
