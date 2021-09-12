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
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipes {
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Names.MOD_ID);

    public static final RegistryObject<IRecipeSerializer<AmadronRecipe>> AMADRON_OFFERS
            = RECIPES.register(PneumaticCraftRecipeTypes.AMADRON_OFFERS,
            () -> new AmadronOffer.Serializer<>(AmadronOffer::new));

    public static final RegistryObject<IRecipeSerializer<AssemblyRecipe>> ASSEMBLY_LASER
            = RECIPES.register(PneumaticCraftRecipeTypes.ASSEMBLY_LASER,
            () -> new AssemblyRecipeImpl.Serializer<>(AssemblyRecipeImpl::new));
    public static final RegistryObject<IRecipeSerializer<AssemblyRecipe>> ASSEMBLY_DRILL
            = RECIPES.register(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL,
            () -> new AssemblyRecipeImpl.Serializer<>(AssemblyRecipeImpl::new));

    public static final RegistryObject<IRecipeSerializer<PressureChamberRecipe>> PRESSURE_CHAMBER
            = RECIPES.register(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER,
            () -> new PressureChamberRecipeImpl.Serializer<>(PressureChamberRecipeImpl::new));
    public static final RegistryObject<SpecialRecipeSerializer<PressureEnchantingRecipe>> PRESSURE_CHAMBER_ENCHANTING
            = RECIPES.register(PressureEnchantingRecipe.ID.getPath(),
            () -> new SpecialRecipeSerializer<>(PressureEnchantingRecipe::new));
    public static final RegistryObject<SpecialRecipeSerializer<PressureDisenchantingRecipe>> PRESSURE_CHAMBER_DISENCHANTING
            = RECIPES.register(PressureDisenchantingRecipe.ID.getPath(),
            () -> new SpecialRecipeSerializer<>(PressureDisenchantingRecipe::new));

    public static final RegistryObject<IRecipeSerializer<ExplosionCraftingRecipe>> EXPLOSION_CRAFTING
            = RECIPES.register(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING,
            () -> new ExplosionCraftingRecipeImpl.Serializer<>(ExplosionCraftingRecipeImpl::new));

    public static final RegistryObject<IRecipeSerializer<HeatFrameCoolingRecipe>> HEAT_FRAME_COOLING
            = RECIPES.register(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING,
            () -> new HeatFrameCoolingRecipeImpl.Serializer<>(HeatFrameCoolingRecipeImpl::new));

    public static final RegistryObject<IRecipeSerializer<RefineryRecipe>> REFINERY
            = RECIPES.register(PneumaticCraftRecipeTypes.REFINERY,
            () -> new RefineryRecipeImpl.Serializer<>(RefineryRecipeImpl::new));

    public static final RegistryObject<IRecipeSerializer<ThermoPlantRecipe>> THERMO_PLANT
            = RECIPES.register(PneumaticCraftRecipeTypes.THERMO_PLANT,
            () -> new ThermoPlantRecipeImpl.Serializer<>(ThermoPlantRecipeImpl::new));

    public static final RegistryObject<IRecipeSerializer<FluidMixerRecipe>> FLUID_MIXER
            = RECIPES.register(PneumaticCraftRecipeTypes.FLUID_MIXER,
            () -> new FluidMixerRecipeImpl.Serializer<>(FluidMixerRecipeImpl::new));

    public static final RegistryObject<IRecipeSerializer<FuelQualityRecipe>> FUEL_QUALITY
            = RECIPES.register(PneumaticCraftRecipeTypes.FUEL_QUALITY,
            () -> new FuelQualityRecipeImpl.Serializer<>(FuelQualityRecipeImpl::new));

    public static final RegistryObject<IRecipeSerializer<HeatPropertiesRecipe>> HEAT_PROPERTIES
            = RECIPES.register(PneumaticCraftRecipeTypes.HEAT_PROPERTIES,
            () -> new HeatPropertiesRecipeImpl.Serializer<>(HeatPropertiesRecipeImpl::new));

    public static final RegistryObject<SpecialRecipeSerializer<OneProbeCrafting>> ONE_PROBE_HELMET_CRAFTING
            = RECIPES.register("one_probe_helmet_crafting", () -> new SpecialRecipeSerializer<>(OneProbeCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<GunAmmoPotionCrafting>> GUN_AMMO_POTION_CRAFTING
            = RECIPES.register("gun_ammo_potion_crafting", () -> new SpecialRecipeSerializer<>(GunAmmoPotionCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<DroneUpgradeCrafting>> DRONE_UPGRADE_CRAFTING
            = RECIPES.register("drone_upgrade_crafting", () -> new SpecialRecipeSerializer<>(DroneUpgradeCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<DroneColorCrafting>> DRONE_COLOR_CRAFTING
            = RECIPES.register("drone_color_crafting", () -> new SpecialRecipeSerializer<>(DroneColorCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<PatchouliBookCrafting>> PATCHOULI_BOOK_CRAFTING
            = RECIPES.register("patchouli_book_crafting", () -> new SpecialRecipeSerializer<>(PatchouliBookCrafting::new));

    public static final RegistryObject<ShapedPressurizableRecipe.Serializer> CRAFTING_SHAPED_PRESSURIZABLE
            = RECIPES.register("crafting_shaped_pressurizable", () -> ShapedPressurizableRecipe.SERIALIZER);
    public static final RegistryObject<ShapedRecipeNoMirror.Serializer> CRAFTING_SHAPED_NO_MIRROR
            = RECIPES.register("crafting_shaped_no_mirror", () -> ShapedRecipeNoMirror.SERIALIZER);

}
