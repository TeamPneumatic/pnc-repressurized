/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.machine.*;
import me.desht.pneumaticcraft.common.recipes.other.FuelQualityRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.special.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS 
            = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Names.MOD_ID);

    public static final Supplier<RecipeSerializer<AmadronOffer>> AMADRON_OFFERS
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.AMADRON_OFFERS,
            () -> new AmadronOffer.Serializer<>(AmadronOffer::new));

    public static final Supplier<RecipeSerializer<AssemblyRecipe>> ASSEMBLY_LASER
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.ASSEMBLY_LASER,
            () -> new AssemblyRecipeImpl.Serializer<>(AssemblyRecipeImpl::new));
    public static final Supplier<RecipeSerializer<AssemblyRecipe>> ASSEMBLY_DRILL
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL,
            () -> new AssemblyRecipeImpl.Serializer<>(AssemblyRecipeImpl::new));

    public static final Supplier<RecipeSerializer<PressureChamberRecipe>> PRESSURE_CHAMBER
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER,
            () -> new PressureChamberRecipeImpl.Serializer<>(PressureChamberRecipeImpl::new));

    public static final Supplier<SimpleCraftingRecipeSerializer<PressureEnchantingRecipe>> PRESSURE_CHAMBER_ENCHANTING
            = RECIPE_SERIALIZERS.register(PressureEnchantingRecipe.ID.getPath(),
            () -> new SimpleCraftingRecipeSerializer<>(PressureEnchantingRecipe::new));
    public static final Supplier<SimpleCraftingRecipeSerializer<PressureDisenchantingRecipe>> PRESSURE_CHAMBER_DISENCHANTING
            = RECIPE_SERIALIZERS.register(PressureDisenchantingRecipe.ID.getPath(),
            () -> new SimpleCraftingRecipeSerializer<>(PressureDisenchantingRecipe::new));

    public static final Supplier<RecipeSerializer<ExplosionCraftingRecipe>> EXPLOSION_CRAFTING
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING,
            () -> new ExplosionCraftingRecipeImpl.Serializer<>(ExplosionCraftingRecipeImpl::new));

    public static final Supplier<RecipeSerializer<HeatFrameCoolingRecipe>> HEAT_FRAME_COOLING
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING,
            () -> new HeatFrameCoolingRecipeImpl.Serializer<>(HeatFrameCoolingRecipeImpl::new));

    public static final Supplier<RecipeSerializer<RefineryRecipe>> REFINERY
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.REFINERY,
            () -> new RefineryRecipeImpl.Serializer<>(RefineryRecipeImpl::new));

    public static final Supplier<RecipeSerializer<ThermoPlantRecipe>> THERMO_PLANT
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.THERMO_PLANT,
            () -> new ThermoPlantRecipeImpl.Serializer<>(ThermoPlantRecipeImpl::new));

    public static final Supplier<RecipeSerializer<FluidMixerRecipe>> FLUID_MIXER
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.FLUID_MIXER,
            () -> new FluidMixerRecipeImpl.Serializer<>(FluidMixerRecipeImpl::new));

    public static final Supplier<RecipeSerializer<FuelQualityRecipe>> FUEL_QUALITY
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.FUEL_QUALITY,
            () -> new FuelQualityRecipeImpl.Serializer<>(FuelQualityRecipeImpl::new));

    public static final Supplier<RecipeSerializer<HeatPropertiesRecipe>> HEAT_PROPERTIES
            = RECIPE_SERIALIZERS.register(PneumaticCraftRecipeTypes.HEAT_PROPERTIES,
            () -> new HeatPropertiesRecipeImpl.Serializer<>(HeatPropertiesRecipeImpl::new));

    public static final Supplier<SimpleCraftingRecipeSerializer<GunAmmoPotionCrafting>> GUN_AMMO_POTION_CRAFTING
            = RECIPE_SERIALIZERS.register("gun_ammo_potion_crafting", () -> new SimpleCraftingRecipeSerializer<>(GunAmmoPotionCrafting::new));
    public static final Supplier<SimpleCraftingRecipeSerializer<DroneUpgradeCrafting>> DRONE_UPGRADE_CRAFTING
            = RECIPE_SERIALIZERS.register("drone_upgrade_crafting", () -> new SimpleCraftingRecipeSerializer<>(DroneUpgradeCrafting::new));
    public static final Supplier<SimpleCraftingRecipeSerializer<DroneColorCrafting>> DRONE_COLOR_CRAFTING
            = RECIPE_SERIALIZERS.register("drone_color_crafting", () -> new SimpleCraftingRecipeSerializer<>(DroneColorCrafting::new));

    public static final Supplier<ShapedPressurizableRecipe.Serializer> CRAFTING_SHAPED_PRESSURIZABLE
            = RECIPE_SERIALIZERS.register("crafting_shaped_pressurizable", ShapedPressurizableRecipe.Serializer::new);
    public static final Supplier<CompressorUpgradeCrafting.Serializer> COMPRESSOR_UPGRADE_CRAFTING
            = RECIPE_SERIALIZERS.register("compressor_upgrade_crafting", CompressorUpgradeCrafting.Serializer::new);

}
