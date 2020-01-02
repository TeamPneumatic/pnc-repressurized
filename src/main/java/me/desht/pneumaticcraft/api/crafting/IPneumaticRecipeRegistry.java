package me.desht.pneumaticcraft.api.crafting;

import me.desht.pneumaticcraft.api.crafting.recipe.IModRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Get an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getRecipeRegistry()}.
 * Note that all recipe types may also be manipulated via CraftTweaker.
 * <p>
 * Note that machine recipe registration is now event-based; see {@link RegisterMachineRecipesEvent}.
 *
 * @author MineMaarten
 */
public interface IPneumaticRecipeRegistry {
    /**
     * Adds an Amadron offer. Both the input and output can either be ItemStack or FluidStack.
     * This is a default offer, meaning it will be put in a clean config load. After that the user can change it at will
     * to remove this added recipe. It's a static offer, meaning if it exists in the instance, it will be there forever
     * (like the Emerald --> PCB Blueprint offer).
     *
     * @param input an ItemStack or FluidStack
     * @param output an ItemStack or FluidStack
     * @throws IllegalArgumentException if the input and output are not both either an ItemStack or FluidStack
     */
    void registerDefaultStaticAmadronOffer(AmadronTradeResource input, AmadronTradeResource output);

    /**
     * Adds an Amadron offer. Both the input and output can either be ItemStack or FluidStack.
     * This is a default offer, meaning it will be put in a clean config load. After that the user can change it at will
     * to remove this added recipe. It's a periodic offer, meaning it will be shuffled (by default) once per day between
     * other periodic offers, like the villager trade offers.
     *
     * @param input an ItemStack or FluidStack
     * @param output an ItemStack or FluidStack
     * @throws IllegalArgumentException if the input and output are not both either an ItemStack or FluidStack
     */
    void registerDefaultPeriodicAmadronOffer(AmadronTradeResource input, AmadronTradeResource output);

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
