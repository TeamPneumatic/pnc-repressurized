package me.desht.pneumaticcraft.api.crafting.ingredient;

import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.function.Supplier;

/**
 * Provides access to any custom ingredient types registered by the mod.
 */
public interface CustomIngredientTypes {
    Supplier<IngredientType<FluidContainerIngredient>> fluidContainerType();
}
