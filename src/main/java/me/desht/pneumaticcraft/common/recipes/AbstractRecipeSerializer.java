package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.IModRecipeSerializer;
import me.desht.pneumaticcraft.api.crafting.recipe.IModRecipe;
import net.minecraft.network.PacketBuffer;

public abstract class AbstractRecipeSerializer<T extends IModRecipe> implements IModRecipeSerializer<T> {
    @Override
    public void write(PacketBuffer buffer, T recipe) {
        buffer.writeResourceLocation(recipe.getRecipeType());
        buffer.writeResourceLocation(recipe.getId());
    }
}
