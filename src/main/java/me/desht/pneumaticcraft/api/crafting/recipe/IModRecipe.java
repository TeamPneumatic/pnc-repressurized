package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Base class for all PneumaticCraft machine recipes.
 */
public interface IModRecipe {
    /**
     * Get a unique ID for this individual recipe.
     *
     * @return a resource location
     */
    ResourceLocation getId();

    /**
     * Get the ID of this recipe type, used for serialization purposes.  A serializer must be registered for this
     * ID with {@link IPneumaticRecipeRegistry#registerSerializer(ResourceLocation, Supplier)}.  An appropriate time
     * to do this is in an event handler for {@code RegistryEvent.Register<IRecipeSerializer<?>>}.
     *
     * @return a resource location
     */
    ResourceLocation getRecipeType();

}
