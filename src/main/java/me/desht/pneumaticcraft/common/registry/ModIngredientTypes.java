package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.crafting.ingredient.CustomIngredientTypes;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import me.desht.pneumaticcraft.api.lib.Names;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES
            = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, Names.MOD_ID);

    public static final Supplier<IngredientType<FluidIngredient>> FLUID
            = INGREDIENT_TYPES.register("fluid", () -> new IngredientType<>(FluidIngredient.FLUID_CODEC_NON_EMPTY));
    public static final Supplier<IngredientType<StackedIngredient>> STACKED
            = INGREDIENT_TYPES.register("stacked", () -> new IngredientType<>(StackedIngredient.CODEC_NONEMPTY));

    public enum Getter implements CustomIngredientTypes {
        INSTANCE;

        @Override
        public Supplier<IngredientType<FluidIngredient>> fluidType() {
            return FLUID;
        }

        @Override
        public Supplier<IngredientType<StackedIngredient>> stackedItemType() {
            return STACKED;
        }
    }
}
