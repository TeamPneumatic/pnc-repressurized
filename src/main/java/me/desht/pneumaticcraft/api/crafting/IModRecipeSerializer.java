package me.desht.pneumaticcraft.api.crafting;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.IModRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Provides recipe serialization and deserialization for PneumaticCraft machine recipes.
 *
 * @param <T> the machine recipe type
 */
public interface IModRecipeSerializer<T extends IModRecipe> {
    /**
     * Read a recipe from JSON.  Called server-side when recipes are read from data packs.
     *
     * @param recipeId the recipe ID, as returned by {@link IModRecipe#getId()}
     * @param json the JSON object
     * @return a new machine recipe, deserialized from JSON
     */
    T read(ResourceLocation recipeId, JsonObject json);

    /**
     * Read a recipe from a packet buffer. Called client-side when recipes are sync'd from server to client.
     *
     * @param recipeId the recipe ID, as returned by {@link IModRecipe#getId()}
     * @param buffer a packet buffer
     * @return a new machine recipe, deserialized from the buffer
     */
    @javax.annotation.Nullable
    T read(ResourceLocation recipeId, PacketBuffer buffer);

    /**
     * Write a recipe to a packet buffer. Called server-side when recipes are sync'd from server to client.
     *
     * @param buffer a packet buffer
     * @param recipe the recipe to serialize
     */
    void write(PacketBuffer buffer, T recipe);
}
