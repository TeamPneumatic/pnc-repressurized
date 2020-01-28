package me.desht.pneumaticcraft.api.crafting;

import me.desht.pneumaticcraft.api.crafting.recipe.IModRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Get an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getRecipeRegistry()}.
 * <p>
 * Note that machine recipes are now loaded from datapack, as well as via an event: {@link RegisterMachineRecipesEvent}.
 *
 * @author MineMaarten, desht
 */
public interface IPneumaticRecipeRegistry {
    /**
     * Register a recipe serializer for the given recipe type, which is the value of {@link IModRecipe#getRecipeType()}.
     * The serializer will be used for loading recipes from JSON data packs, as well as sync'ing recipes from server
     * to client.
     *
     * @param recipeType a recipe type resource location
     * @param serializer a serializer for this recipe type
     */
    void registerSerializer(ResourceLocation recipeType, Supplier<IModRecipeSerializer<? extends IModRecipe>> serializer);
}
