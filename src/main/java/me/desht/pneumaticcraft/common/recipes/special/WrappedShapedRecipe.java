package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.mixin.accessors.ShapedRecipeAccess;
import net.minecraft.world.item.crafting.ShapedRecipe;

public abstract class WrappedShapedRecipe extends ShapedRecipe {
    protected final ShapedRecipe wrapped;

    public WrappedShapedRecipe(ShapedRecipe wrapped) {
        super("dummy", wrapped.category(),
                ((ShapedRecipeAccess) wrapped).getPattern(),
                ((ShapedRecipeAccess) wrapped).getResult());

        this.wrapped = wrapped;
    }

    public ShapedRecipe wrapped() {
        return wrapped;
    }
}
