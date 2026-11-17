package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccess {
    @Accessor
    ItemStackTemplate getResult();
}
