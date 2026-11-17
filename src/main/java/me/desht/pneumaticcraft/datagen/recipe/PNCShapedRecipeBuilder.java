package me.desht.pneumaticcraft.datagen.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Just like ShapedRecipeBuilder but easily subclassable (vanilla's builder can't be extended any more)
 */
public class PNCShapedRecipeBuilder implements RecipeBuilder {
    protected final HolderGetter<Item> items;
    protected final RecipeCategory category;
    protected final ItemStackTemplate result;
    protected final List<String> rows = new ArrayList<>();
    protected final Map<Character, Ingredient> key = new LinkedHashMap<>();
    protected final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    @Nullable
    protected String group;
    protected boolean showNotification = true;

    public PNCShapedRecipeBuilder(HolderGetter<Item> items, RecipeCategory category, ItemStackTemplate result) {
        this.items = items;
        this.category = category;
        this.result = result;
    }

    public PNCShapedRecipeBuilder(HolderGetter<Item> items, RecipeCategory category, ItemLike item, int count) {
        this(items, category, new ItemStackTemplate(item.asItem(), count));
    }

    public static PNCShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemLike item) {
        return shaped(items, category, item, 1);
    }

    public static PNCShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemLike item, int count) {
        return new PNCShapedRecipeBuilder(items, category, item, count);
    }

    public static PNCShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemStackTemplate result) {
        return new PNCShapedRecipeBuilder(items, category, result);
    }

    public PNCShapedRecipeBuilder define(Character character, TagKey<Item> tag) {
        return this.define(character, Ingredient.of(items.getOrThrow(tag)));
    }

    public PNCShapedRecipeBuilder define(Character character, ItemLike itemLike) {
        return this.define(character, Ingredient.of(itemLike));
    }

    public PNCShapedRecipeBuilder define(Character character, Ingredient ingredient) {
        if (this.key.containsKey(character)) {
            throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
        } else if (character == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(character, ingredient);
            return this;
        }
    }

    public PNCShapedRecipeBuilder pattern(String pat) {
        if (!this.rows.isEmpty() && pat.length() != this.rows.getFirst().length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pat);
            return this;
        }
    }

    @Override
    public PNCShapedRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public PNCShapedRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public PNCShapedRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        output.accept(id, makeRecipe(), this.advancementBuilder.build(output, id, this.category));
    }

    protected ShapedRecipe makeRecipe() {
        return new ShapedRecipe(
                RecipeBuilder.createCraftingCommonInfo(this.showNotification),
                RecipeBuilder.createCraftingBookInfo(this.category, this.group),
                ShapedRecipePattern.of(this.key, this.rows),
                this.result
        );
    }
}
