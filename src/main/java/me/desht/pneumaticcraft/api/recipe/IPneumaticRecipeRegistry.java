package me.desht.pneumaticcraft.api.recipe;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeResource;

/**
 * Get an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getRecipeRegistry()}.
 * Note that all recipe types may also be manipulated via CraftTweaker.
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
    void registerDefaultStaticAmadronOffer(TradeResource input, TradeResource output);

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
    void registerDefaultPeriodicAmadronOffer(TradeResource input, TradeResource output);
}
