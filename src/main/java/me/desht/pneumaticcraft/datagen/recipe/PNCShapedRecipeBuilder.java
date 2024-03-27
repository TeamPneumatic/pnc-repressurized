package me.desht.pneumaticcraft.datagen.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Just like ShapedRecipeBuilder but easily subclassable
 */
public class PNCShapedRecipeBuilder extends ShapedRecipeBuilder {
    protected final RecipeCategory category;
    protected final Item result;
    protected final int count;
    protected final ItemStack resultStack; // Neo: add stack result support
    protected final List<String> rows = Lists.newArrayList();
    protected final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    protected String group;
    protected boolean showNotification = true;

    public PNCShapedRecipeBuilder(RecipeCategory category, ItemLike item, int count) {
        this(category, new ItemStack(item, count));
    }

    public PNCShapedRecipeBuilder(RecipeCategory category, ItemStack result) {
        super(category, result.getItem(), result.getCount());
        this.category = category;
        this.result = result.getItem();
        this.count = result.getCount();
        this.resultStack = result;
    }

    public static PNCShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike) {
        return shaped(category, itemLike, 1);
    }

    public static PNCShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike, int count) {
        return new PNCShapedRecipeBuilder(category, itemLike, count);
    }

    public static PNCShapedRecipeBuilder shaped(RecipeCategory category, ItemStack result) {
        return new PNCShapedRecipeBuilder(category, result);
    }

    public PNCShapedRecipeBuilder define(Character character, TagKey<Item> tag) {
        return this.define(character, Ingredient.of(tag));
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
        if (!this.rows.isEmpty() && pat.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pat);
            return this;
        }
    }

    public PNCShapedRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public PNCShapedRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public PNCShapedRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        Advancement.Builder builder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        ShapedRecipe recipe = makeRecipe(id);
        output.accept(id, recipe, builder.build(id.withPrefix("recipes/" + category.getFolderName() + "/")));
    }

    @NotNull
    protected ShapedRecipe makeRecipe(ResourceLocation id) {
        ShapedRecipePattern shapedrecipepattern = this.ensureValid(id);
        return new ShapedRecipe(
                Objects.requireNonNullElse(this.group, ""),
                RecipeBuilder.determineBookCategory(this.category),
                shapedrecipepattern,
                this.resultStack,
                this.showNotification
        );
    }

    protected ShapedRecipePattern ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        } else {
            return ShapedRecipePattern.of(this.key, this.rows);
        }
    }
}
