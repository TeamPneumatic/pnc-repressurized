package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JEISpawnerExtractionCategory implements IRecipeCategory<JEISpawnerExtractionCategory.Recipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    public JEISpawnerExtractionCategory() {
        this.localizedName = I18n.format("pneumaticcraft.gui.jei.title.spawnerExtraction");
        this.background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_SPAWNER_EXTRACTION, 0, 0, 120, 64);
        this.icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.SPAWNER_EXTRACTOR.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.SPAWNER_EXTRACTION;
    }

    @Override
    public Class<? extends Recipe> getRecipeClass() {
        return Recipe.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(Recipe recipe, IIngredients iIngredients) {
        iIngredients.setInputs(VanillaTypes.ITEM, ImmutableList.of(recipe.itemInput, new ItemStack(Blocks.SPAWNER)));
        iIngredients.setOutputLists(VanillaTypes.ITEM, ImmutableList.of(
                recipe.cores,
                Collections.singletonList(recipe.itemOutput2)
        ));
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, Recipe recipe, IIngredients iIngredients) {
        iRecipeLayout.getItemStacks().init(0, true, 52, 2);
        iRecipeLayout.getItemStacks().set(0, iIngredients.getInputs(VanillaTypes.ITEM).get(0));

        iRecipeLayout.getItemStacks().init(1, true, 52, 33);
        iRecipeLayout.getItemStacks().set(1, iIngredients.getInputs(VanillaTypes.ITEM).get(1));

        iRecipeLayout.getItemStacks().init(2, false, 17, 33);
        iRecipeLayout.getItemStacks().set(2, iIngredients.getOutputs(VanillaTypes.ITEM).get(0));

        iRecipeLayout.getItemStacks().init(3, false, 87, 33);
        iRecipeLayout.getItemStacks().set(3, iIngredients.getOutputs(VanillaTypes.ITEM).get(1));
    }

    private static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();
    static {
        ENTITY_TYPES.add(EntityType.ZOMBIE);
        ENTITY_TYPES.add(EntityType.SKELETON);
        ENTITY_TYPES.add(EntityType.CREEPER);
    }
    public static Collection<?> getAllRecipes() {
        List<ItemStack> cores = new ArrayList<>();
        for (EntityType<?> type : ENTITY_TYPES) {
            ItemStack core = new ItemStack(ModItems.SPAWNER_CORE.get());
            ISpawnerCoreStats stats = PneumaticRegistry.getInstance().getItemRegistry().getSpawnerCoreStats(core);
            stats.addAmount(type, 100);
            stats.serialize(core);
            cores.add(core);
        }

        return Collections.singletonList(new Recipe(
                        new ItemStack(ModBlocks.SPAWNER_EXTRACTOR.get()),
                        cores,
                        new ItemStack(ModBlocks.EMPTY_SPAWNER.get())
                )
        );
    }

    static class Recipe {
        final ItemStack itemInput;
        final List<ItemStack> cores;
        final ItemStack itemOutput2;

        Recipe(ItemStack itemInput, List<ItemStack> cores, ItemStack itemOutput2) {
            this.itemInput = itemInput;
            this.cores = cores;
            this.itemOutput2 = itemOutput2;
        }
    }
}
