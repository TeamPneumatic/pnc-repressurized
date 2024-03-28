package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccess {
    @Accessor
    ItemStack getResult();

    @Accessor
    ShapedRecipePattern getPattern();
}
