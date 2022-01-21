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

package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.ShapedPressurizableRecipe;
import me.desht.pneumaticcraft.api.crafting.ShapedRecipeNoMirror;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.machine.*;
import me.desht.pneumaticcraft.common.recipes.other.FuelQualityRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.special.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Names.MOD_ID);

    public static final RegistryObject<RecipeSerializer<AmadronRecipe>> AMADRON_OFFERS
            = RECIPES.register(PneumaticCraftRecipeTypes.AMADRON_OFFERS,
            () -> new AmadronOffer.Serializer<>(AmadronOffer::new));

    public static final RegistryObject<RecipeSerializer<AssemblyRecipe>> ASSEMBLY_LASER
            = RECIPES.register(PneumaticCraftRecipeTypes.ASSEMBLY_LASER,
            () -> new AssemblyRecipeImpl.Serializer<>(AssemblyRecipeImpl::new));
    public static final RegistryObject<RecipeSerializer<AssemblyRecipe>> ASSEMBLY_DRILL
            = RECIPES.register(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL,
            () -> new AssemblyRecipeImpl.Serializer<>(AssemblyRecipeImpl::new));

    public static final RegistryObject<RecipeSerializer<PressureChamberRecipe>> PRESSURE_CHAMBER
            = RECIPES.register(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER,
            () -> new PressureChamberRecipeImpl.Serializer<>(PressureChamberRecipeImpl::new));
    public static final RegistryObject<SimpleRecipeSerializer<PressureEnchantingRecipe>> PRESSURE_CHAMBER_ENCHANTING
            = RECIPES.register(PressureEnchantingRecipe.ID.getPath(),
            () -> new SimpleRecipeSerializer<>(PressureEnchantingRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<PressureDisenchantingRecipe>> PRESSURE_CHAMBER_DISENCHANTING
            = RECIPES.register(PressureDisenchantingRecipe.ID.getPath(),
            () -> new SimpleRecipeSerializer<>(PressureDisenchantingRecipe::new));

    public static final RegistryObject<RecipeSerializer<ExplosionCraftingRecipe>> EXPLOSION_CRAFTING
            = RECIPES.register(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING,
            () -> new ExplosionCraftingRecipeImpl.Serializer<>(ExplosionCraftingRecipeImpl::new));

    public static final RegistryObject<RecipeSerializer<HeatFrameCoolingRecipe>> HEAT_FRAME_COOLING
            = RECIPES.register(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING,
            () -> new HeatFrameCoolingRecipeImpl.Serializer<>(HeatFrameCoolingRecipeImpl::new));

    public static final RegistryObject<RecipeSerializer<RefineryRecipe>> REFINERY
            = RECIPES.register(PneumaticCraftRecipeTypes.REFINERY,
            () -> new RefineryRecipeImpl.Serializer<>(RefineryRecipeImpl::new));

    public static final RegistryObject<RecipeSerializer<ThermoPlantRecipe>> THERMO_PLANT
            = RECIPES.register(PneumaticCraftRecipeTypes.THERMO_PLANT,
            () -> new ThermoPlantRecipeImpl.Serializer<>(ThermoPlantRecipeImpl::new));

    public static final RegistryObject<RecipeSerializer<FluidMixerRecipe>> FLUID_MIXER
            = RECIPES.register(PneumaticCraftRecipeTypes.FLUID_MIXER,
            () -> new FluidMixerRecipeImpl.Serializer<>(FluidMixerRecipeImpl::new));

    public static final RegistryObject<RecipeSerializer<FuelQualityRecipe>> FUEL_QUALITY
            = RECIPES.register(PneumaticCraftRecipeTypes.FUEL_QUALITY,
            () -> new FuelQualityRecipeImpl.Serializer<>(FuelQualityRecipeImpl::new));

    public static final RegistryObject<RecipeSerializer<HeatPropertiesRecipe>> HEAT_PROPERTIES
            = RECIPES.register(PneumaticCraftRecipeTypes.HEAT_PROPERTIES,
            () -> new HeatPropertiesRecipeImpl.Serializer<>(HeatPropertiesRecipeImpl::new));

    public static final RegistryObject<SimpleRecipeSerializer<OneProbeCrafting>> ONE_PROBE_HELMET_CRAFTING
            = RECIPES.register("one_probe_helmet_crafting", () -> new SimpleRecipeSerializer<>(OneProbeCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<GunAmmoPotionCrafting>> GUN_AMMO_POTION_CRAFTING
            = RECIPES.register("gun_ammo_potion_crafting", () -> new SimpleRecipeSerializer<>(GunAmmoPotionCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<DroneUpgradeCrafting>> DRONE_UPGRADE_CRAFTING
            = RECIPES.register("drone_upgrade_crafting", () -> new SimpleRecipeSerializer<>(DroneUpgradeCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<DroneColorCrafting>> DRONE_COLOR_CRAFTING
            = RECIPES.register("drone_color_crafting", () -> new SimpleRecipeSerializer<>(DroneColorCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<PatchouliBookCrafting>> PATCHOULI_BOOK_CRAFTING
            = RECIPES.register("patchouli_book_crafting", () -> new SimpleRecipeSerializer<>(PatchouliBookCrafting::new));

    public static final RegistryObject<ShapedPressurizableRecipe.Serializer> CRAFTING_SHAPED_PRESSURIZABLE
            = RECIPES.register("crafting_shaped_pressurizable", () -> ShapedPressurizableRecipe.SERIALIZER);
    public static final RegistryObject<ShapedRecipeNoMirror.Serializer> CRAFTING_SHAPED_NO_MIRROR
            = RECIPES.register("crafting_shaped_no_mirror", () -> ShapedRecipeNoMirror.SERIALIZER);

}
