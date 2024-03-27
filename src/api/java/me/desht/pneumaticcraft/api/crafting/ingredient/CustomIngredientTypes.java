package me.desht.pneumaticcraft.api.crafting.ingredient;

import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.function.Supplier;

public interface CustomIngredientTypes {
    Supplier<IngredientType<FluidIngredient>> fluidType();

    Supplier<IngredientType<StackedIngredient>> stackedItemType();
}
