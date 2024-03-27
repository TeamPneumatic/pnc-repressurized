package me.desht.pneumaticcraft.common.recipes.special;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import java.util.Map;

public abstract class WrappedShapedRecipe extends ShapedRecipe {
    protected final ShapedRecipe wrapped;

    public WrappedShapedRecipe(ShapedRecipe wrapped) {
        // dummy init, since all methods are overridden below
        super("dummy", CraftingBookCategory.MISC, ShapedRecipePattern.of(Map.of('x', Ingredient.EMPTY), "x"), ItemStack.EMPTY);

        this.wrapped = wrapped;
    }

    @Override
    public CraftingBookCategory category() {
        return wrapped.category();
    }

    @Override
    public int getRecipeWidth() {
        return wrapped.getRecipeWidth();
    }

    @Override
    public int getRecipeHeight() {
        return wrapped.getRecipeHeight();
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return wrapped.matches(container, level);
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return wrapped.canCraftInDimensions(w, h);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return wrapped.getResultItem(access);
    }

    public ShapedRecipe getWrapped() {
        return wrapped;
    }

    @Override
    public boolean showNotification() {
        return wrapped.showNotification();
    }
}
