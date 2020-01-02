package me.desht.pneumaticcraft.common.thirdparty.jei.extension;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractShapelessExtension implements ICustomCraftingCategoryExtension {
    private final List<List<ItemStack>> inputs;
    private final ItemStack output;
    private final ResourceLocation name;

    AbstractShapelessExtension(SpecialRecipe recipe, ItemStack result, IItemProvider... items) {
        inputs = new ArrayList<>();
        for (IItemProvider provider : items) {
            inputs.add(Collections.singletonList(new ItemStack(provider)));
        }
        output = result;
        name = recipe.getId();
    }

    @Override
    public void setIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients) {
        recipeLayout.getItemStacks().set(ingredients);
        recipeLayout.setShapeless();
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    public ItemStack getOutput() {
        return output;
    }
}
