package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.crafting.ingredient.CustomIngredientTypes;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidContainerIngredient;
import me.desht.pneumaticcraft.api.lib.Names;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES
            = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, Names.MOD_ID);

    public static final Supplier<IngredientType<FluidContainerIngredient>> FLUID_CONTAINER
            = INGREDIENT_TYPES.register("fluid_container",
            () -> new IngredientType<>(FluidContainerIngredient.MAP_CODEC, FluidContainerIngredient.STREAM_CODEC));


    public enum Getter implements CustomIngredientTypes {
        INSTANCE;

        @Override
        public Supplier<IngredientType<FluidContainerIngredient>> fluidContainerType() {
            return FLUID_CONTAINER;
        }
    }
}
