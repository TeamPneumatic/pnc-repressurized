package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.mixin.accessors.ShapedRecipeAccess;
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
        super("dummy", wrapped.category(),
                ((ShapedRecipeAccess) wrapped).getPattern(),
                ((ShapedRecipeAccess) wrapped).getResult());

        this.wrapped = wrapped;
    }

    public ShapedRecipe getWrapped() {
        return wrapped;
    }
}
