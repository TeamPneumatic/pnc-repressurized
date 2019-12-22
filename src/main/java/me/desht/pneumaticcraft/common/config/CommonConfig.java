package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.Lists;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
    public class General {
        ForgeConfigSpec.IntValue oilGenerationChance;
        ForgeConfigSpec.BooleanValue enableDungeonLoot;
        ForgeConfigSpec.BooleanValue enableDroneSuffocation;
        ForgeConfigSpec.DoubleValue fuelBucketEfficiency;
        ForgeConfigSpec.IntValue maxProgrammingArea;
        ForgeConfigSpec.ConfigValue<List<String>> oilWorldGenBlacklist;
        ForgeConfigSpec.IntValue minFluidFuelTemperature;
        ForgeConfigSpec.BooleanValue useUpDyesWhenColoring;
    }
    public class Machines {
        ForgeConfigSpec.BooleanValue aerialInterfaceArmorCompat;
        ForgeConfigSpec.DoubleValue cropSticksGrowthBoostChance;
        ForgeConfigSpec.IntValue electricCompressorEfficiency;
        ForgeConfigSpec.IntValue electrostaticLightningChance;
        ForgeConfigSpec.IntValue elevatorBaseBlocksPerBase;
        ForgeConfigSpec.IntValue fluxCompressorEfficiency;
        ForgeConfigSpec.BooleanValue keroseneLampCanUseAnyFuel;
        ForgeConfigSpec.DoubleValue keroseneLampFuelEfficiency;
        ForgeConfigSpec.IntValue kineticCompressorEfficiency;
        ForgeConfigSpec.BooleanValue liquidHopperDispenser;
        ForgeConfigSpec.BooleanValue omniHopperDispenser;
        ForgeConfigSpec.IntValue plasticMixerPlasticRatio;
        ForgeConfigSpec.IntValue pneumaticDynamoEfficiency;
        ForgeConfigSpec.IntValue pneumaticEngineEfficiency;
        ForgeConfigSpec.IntValue pneumaticGeneratorEfficiency;
        ForgeConfigSpec.IntValue pneumaticPumpEfficiency;
        ForgeConfigSpec.DoubleValue speedUpgradeSpeedMultiplier;
        ForgeConfigSpec.DoubleValue speedUpgradeUsageMultiplier;
        ForgeConfigSpec.DoubleValue thermalCompressorThermalResistance;
    }
    public class Armor {
        ForgeConfigSpec.IntValue jetBootsAirUsage;
        ForgeConfigSpec.IntValue armorStartupTime;
    }
    public class Integration {
        ForgeConfigSpec.DoubleValue ieExternalHeaterHeatPerRF;
        ForgeConfigSpec.IntValue ieExternalHeaterRFperTick;
        ForgeConfigSpec.DoubleValue mekHeatEfficiency;
        ForgeConfigSpec.DoubleValue mekThermalResistanceMult;
        ForgeConfigSpec.DoubleValue tanAirConAirUsageMultiplier;
        ForgeConfigSpec.DoubleValue tanHeatDivider;
        ForgeConfigSpec.IntValue tanRefreshInterval;
    }
    public class Advanced {
        ForgeConfigSpec.BooleanValue disableKeroseneLampFakeAirBlock;
        ForgeConfigSpec.DoubleValue liquidTankUpdateThreshold;
        ForgeConfigSpec.BooleanValue stopDroneAI;
    }
    public class Micromissiles {
        ForgeConfigSpec.DoubleValue baseExplosionDamage;
        ForgeConfigSpec.BooleanValue damageTerrain;
        ForgeConfigSpec.IntValue launchCooldown;
        ForgeConfigSpec.IntValue lifetime;
        ForgeConfigSpec.IntValue missilePodSize;
    }
    public class Minigun {
        ForgeConfigSpec.DoubleValue apAmmoDamageMultiplier;
        ForgeConfigSpec.IntValue apAmmoIgnoreArmorChance;
        ForgeConfigSpec.IntValue armorPiercingAmmoCartridgeSize;
        ForgeConfigSpec.DoubleValue baseDamage;
        ForgeConfigSpec.IntValue baseRange;
        ForgeConfigSpec.IntValue explosiveAmmoCartridgeSize;
        ForgeConfigSpec.DoubleValue explosiveAmmoDamageMultiplier;
        ForgeConfigSpec.IntValue explosiveAmmoExplosionChance;
        ForgeConfigSpec.DoubleValue explosiveAmmoExplosionPower;
        ForgeConfigSpec.BooleanValue explosiveAmmoTerrainDamage;
        ForgeConfigSpec.IntValue freezingAmmoBlockIceChance;
        ForgeConfigSpec.IntValue freezingAmmoCartridgeSize;
        ForgeConfigSpec.IntValue freezingAmmoEntityIceChance;
        ForgeConfigSpec.DoubleValue freezingAmmoFakeIceDamage;
        ForgeConfigSpec.IntValue incendiaryAmmoBlockIgniteChance;
        ForgeConfigSpec.IntValue incendiaryAmmoCartridgeSize;
        ForgeConfigSpec.IntValue incendiaryAmmoEntityIgniteChance;
        ForgeConfigSpec.IntValue incendiaryAmmoFireDuration;
        ForgeConfigSpec.IntValue potionProcChance;
        ForgeConfigSpec.IntValue standardAmmoCartridgeSize;
        ForgeConfigSpec.DoubleValue weightedAmmoAirUsageMultiplier;
        ForgeConfigSpec.IntValue weightedAmmoCartridgeSize;
        ForgeConfigSpec.DoubleValue weightedAmmoDamageMultiplier;
        ForgeConfigSpec.DoubleValue weightedAmmoRangeMultiplier;
    }
    public class Recipes {
        ForgeConfigSpec.BooleanValue coalToDiamondsRecipe;
        ForgeConfigSpec.BooleanValue explosionCrafting;
    }

    public class Amadron {
        ForgeConfigSpec.IntValue maxTradesPerPlayer;
        ForgeConfigSpec.BooleanValue notifyOfTradeAddition;
        ForgeConfigSpec.BooleanValue notifyOfTradeRemoval;
        ForgeConfigSpec.BooleanValue notifyOfDealMade;
    }

    public class BlockHeatDefaults {
        ForgeConfigSpec.DoubleValue blockThermalResistance;
        ForgeConfigSpec.DoubleValue fluidThermalResistance;
        ForgeConfigSpec.IntValue fluidTotalHeatCapacity;
        ForgeConfigSpec.DoubleValue ambientTemperatureBiomeModifier;
        ForgeConfigSpec.DoubleValue ambientTemperatureHeightModifier;
    }

    public final General general = new General();
    public final Machines machines = new Machines();
    public final Armor armor = new Armor();
    public final Advanced advanced = new Advanced();
    public final Integration integration = new Integration();
    public final Micromissiles micromissiles = new Micromissiles();
    public final Minigun minigun = new Minigun();
    public final Recipes recipes = new Recipes();
    public final Amadron amadron = new Amadron();
    public final BlockHeatDefaults blockHeatDefaults = new BlockHeatDefaults();

    CommonConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("General");
        general.oilGenerationChance = builder
                .worldRestart()
                .comment("Chance per chunk in percentage to generate an Oil Lake. Set to 0 for no oil lakes.")
                .translation("pneumaticcraft.config.server.general.oilGenerationChance")
                .defineInRange("oil_generation_chance", 15, 0, 100);
        general.enableDungeonLoot = builder
                .comment("Enable mod dungeon loot generation")
                .translation("pneumaticcraft.config.server.general.enable_dungeon_loot")
                .define("enable_dungeon_loot", true);
        general.enableDroneSuffocation = builder
                .comment("Enable Drone Suffocation Damage")
                .translation("pneumaticcraft.config.server.general.enable_drone_suffocation")
                .define("enable_drone_suffocation", true);
        general.fuelBucketEfficiency = builder
                .comment("Efficiency of fuel buckets as furnace fuel (default 0.05 means 1 bucket of LPG smelts 450 items in a vanilla furnace)")
                .translation("pneumaticcraft.config.server.general.fuel_bucket_efficiency")
                .defineInRange("fuel_bucket_efficiency", 0.05, 0.0, Double.MAX_VALUE);
        general.maxProgrammingArea = builder
                .comment("Maximum number of blocks in the area defined in an Area Programming Puzzle Piece")
                .translation("pneumaticcraft.config.server.general.max_programming_area")
                .defineInRange("max_programming_area", 250000, 1, Integer.MAX_VALUE);
        general.oilWorldGenBlacklist = builder
                .worldRestart()
                .comment("Oil worldgen blacklist: add dimension IDs to this list if you don't want oil worldgen to happen there.")
                .translation("pneumaticcraft.config.server.general.oil_world_gen_blacklist")
                .define("oil_world_gen_blacklist", Lists.newArrayList("minecraft:the_nether", "minecraft:the_end"));
        general.minFluidFuelTemperature = builder
                .worldRestart()
                .comment("Fluids at least as hot as this temperature (Kelvin) will be auto-registered as Liquid Compressor fuels, the quality being dependent on fluid temperature.")
                .translation("pneumaticcraft.config.server.general.min_fluid_fuel_temperature")
                .defineInRange("min_fluid_fuel_temperature", 373, 0, Integer.MAX_VALUE);
        general.useUpDyesWhenColoring = builder
                .comment("Should dyes be used up when coloring things (Drones, Logistics Modules, Redstone Modules)?")
                .translation("pneumaticcraft.config.server.general.use_up_dyes_when_coloring")
                .define("use_up_dyes_when_coloring", false);
        builder.pop();

        builder.push("Machine Properties");
        machines.aerialInterfaceArmorCompat = builder
                .comment("Aerial Interface backwards compat: allow pre-0.8.0 behaviour of getting player's armor inventory from top face, even with Dispenser Upgrade installed")
                .translation("pneumaticcraft.config.server.machine_properties.aerial_interface_armor_compat")
                .define("aerial_interface_armor_compat", true);
        machines.cropSticksGrowthBoostChance = builder
                .comment("Chance per tick of Crop Supports causing a growth tick. The default, 0.002, is roughly 2.5 times faster than the vanilla growth rate")
                .translation("pneumaticcraft.config.server.machine_properties.crop_sticks_growth_boost_chance")
                .defineInRange("crop_sticks_growth_boost_chance", 0.002, 0, Double.MAX_VALUE);
        machines.electricCompressorEfficiency = builder
                .comment("Changing this value will alter the pressurized air production of the Electric Compressor. The input, EU, will stay the same")
                .translation("pneumaticcraft.config.server.machine_properties.electric_compressor_efficiency")
                .defineInRange("electric_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.electrostaticLightningChance = builder
                .comment("Base chance (1/x) per tick of a lightning strike on/around the Electrostatic Generator")
                .translation("pneumaticcraft.config.server.machine_properties.electrostatic_lightning_chance")
                .defineInRange("electrostatic_lightning_chance", 100000, 0, Integer.MAX_VALUE);
        machines.elevatorBaseBlocksPerBase = builder
                .comment("The max height of an elevator per stacked Elevator Base.")
                .translation("pneumaticcraft.config.server.machine_properties.elevator_base_blocks_per_base")
                .defineInRange("elevator_base_blocks_per_base", 4, 1, 256);
        machines.fluxCompressorEfficiency = builder
                .comment("Changing this value will alter the pressurized air production of the Flux Compressor. The input, RF, will stay the same")
                .translation("pneumaticcraft.config.server.machine_properties.flux_compressor_efficiency")
                .defineInRange("flux_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.keroseneLampCanUseAnyFuel = builder
                .comment("Can the Kerosene Lamp burn any kind of fuel?  If false, only Kerosene can be burnt")
                .translation("pneumaticcraft.config.server.machine_properties.kerosene_lamp_can_use_any_fuel")
                .define("kerosene_lamp_can_use_any_fuel", true);
        machines.keroseneLampFuelEfficiency = builder
                .comment("Kerosene Lamp fuel efficiency: higher values mean fuel will last longer in the lamp")
                .translation("pneumaticcraft.config.server.machine_properties.kerosene_lamp_fuel_efficiency")
                .defineInRange("kerosene_lamp_fuel_efficiency", 1.0, 0, Double.MAX_VALUE);
        machines.kineticCompressorEfficiency = builder
                .comment("Changing this value will alter the pressurized air production of the Kinetic Compressor. The input, MJ, will stay the same")
                .translation("pneumaticcraft.config.server.machine_properties.kinetic_compressor_efficiency")
                .defineInRange("kinetic_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.liquidHopperDispenser = builder
                .comment("Can the Liquid Hopper absorb/dispense fluids into the world with a Dispenser Upgrade?")
                .translation("pneumaticcraft.config.server.machine_properties.liquid_hopper_dispenser")
                .define("liquid_hopper_dispenser", true);
        machines.omniHopperDispenser = builder
                .comment("Can the Omnidirectional Hopper dispense items into the world with a Dispenser Upgrade?")
                .translation("pneumaticcraft.config.server.machine_properties.omni_hopper_dispenser")
                .define("omni_hopper_dispenser", true);
        machines.plasticMixerPlasticRatio = builder
                .comment("The ratio of liquid plastic to solid plastic sheets in the Plastic Mixer, in mB per sheet.  If set to 0, no default liquid->solid plastic recipe will be added (but CraftTweaker or API can be used to add recipes)")
                .translation("pneumaticcraft.config.server.machine_properties.plastic_mixer_plastic_ratio")
                .defineInRange("plastic_mixer_plastic_ratio", 1000, 0, Integer.MAX_VALUE);
        machines.pneumaticDynamoEfficiency = builder
                .comment("Changing this value will alter the pressurized air usage of the Pneumatic Dynamo. The output, RF, will stay the same")
                .translation("pneumaticcraft.config.server.machine_properties.pneumatic_dynamo_efficiency")
                .defineInRange("pneumatic_dynamo_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.pneumaticEngineEfficiency = builder
                .comment("Changing this value will alter the pressurized air usage of the Pneumatic Engine. The output, MJ, will stay the same")
                .translation("pneumaticcraft.config.server.machine_properties.pneumatic_engine_efficiency")
                .defineInRange("pneumatic_engine_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.pneumaticGeneratorEfficiency = builder
                .comment("Changing this value will alter the pressurized air usage of the Pneumatic Generator. The output, EU, will stay the same.")
                .translation("pneumaticcraft.config.server.machine_properties.pneumatic_generator_efficiency")
                .defineInRange("pneumatic_generator_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.pneumaticPumpEfficiency = builder
                .comment("Changing this value will alter the hydraulic bar production of the Pneumatic Pump. The input, air, will stay the same")
                .translation("pneumaticcraft.config.server.machine_properties.pneumatic_pump_efficiency")
                .defineInRange("pneumatic_pump_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.speedUpgradeSpeedMultiplier = builder
                .comment("Speed multiplier per speed upgrade: speed mult = speedUpgradeSpeedMultiplier ^ num_of_speed_upgrades")
                .translation("pneumaticcraft.config.server.machine_properties.speed_upgrade_speed_multiplier")
                .defineInRange("speed_upgrade_speed_multiplier", PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER, 1.0, 2.0);
        machines.speedUpgradeUsageMultiplier = builder
                .comment("Fuel usage / heat gen multiplier per speed upgrade: usage mult = speedUpgradeUsageMultiplier ^ num_of_speed_upgrades")
                .translation("pneumaticcraft.config.server.machine_properties.speed_upgrade_usage_multiplier")
                .defineInRange("speed_upgrade_usage_multiplier", PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER, 1.0, 2.0);
        machines.thermalCompressorThermalResistance = builder
                .comment("Thermal resistance between opposite faces of the Thermal Compressor. Higher values means heat leaks across (equalizes) more slowly, making the compressor better at converting heat differential to pressure.")
                .translation("pneumaticcraft.config.server.machine_properties.thermal_compressor_thermal_resistance")
                .defineInRange("thermal_compressor_thermal_resistance", 150, 0, Double.MAX_VALUE);
        builder.pop();

        builder.push("Pneumatic Armor");
        armor.jetBootsAirUsage = builder
                .comment("Jetboots air usage in mL/tick (per Jet Boots Upgrade)")
                .translation("pneumaticcraft.config.server.armor.jet_boots_air_usage")
                .defineInRange("jet_boots_air_usage", PneumaticValues.PNEUMATIC_JET_BOOTS_USAGE, 0, Integer.MAX_VALUE);
        armor.armorStartupTime = builder
                .comment("Base Pneumatic Armor startup time in ticks (before Speed Upgrades)")
                .translation("pneumaticcraft.config.server.armor.armor_startup_time")
                .defineInRange("armor_startup_time", 200, 20, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Advanced");
        advanced.disableKeroseneLampFakeAirBlock = builder
                .comment("When set to true, the Kerosene Lamp's fake air blocks won't be registered and therefore removed from the world. Useful if this causes trouble (it shouldn't though)")
                .translation("pneumaticcraft.config.server.advanced.disable_kerosene_lamp_fake_air_block")
                .define("disable_kerosene_lamp_fake_air_block", false);
        advanced.liquidTankUpdateThreshold = builder
                .comment("The amount by which any liquid tank's contents must change, as a proportion of the tank's total capacity, to trigger an update to clients. Larger values mean fewer updates but less granularity in client-side fluid rendering.")
                .translation("pneumaticcraft.config.server.advanced.liquid_tank_update_threshold")
                .defineInRange("liquid_tank_update_threshold", 0.01, 0.0001, 1);
        advanced.stopDroneAI = builder
                .comment("When set to true, Drones will not execute any program. This is useful to set to true when due to a bug Drones are lagging your server or crashing it. Please report the bug if you encounter it.")
                .translation("pneumaticcraft.config.server.advanced.stop_drone_ai")
                .define("stop_drone_ai", false);
        builder.pop();

        builder.push("Micromissile Properties");
        micromissiles.baseExplosionDamage = builder
                .comment("Base explosion damage (modified by missile setup)")
                .translation("pneumaticcraft.config.server.micromissile_properties.base_explosion_damage")
                .defineInRange("base_explosion_damage", 1, 0, Double.MAX_VALUE);
        micromissiles.damageTerrain = builder
                .comment("Do micromissile explosions cause terrain damage?")
                .translation("pneumaticcraft.config.server.micromissile_properties.damage_terrain")
                .define("damage_terrain", false);
        micromissiles.launchCooldown = builder
                .comment("Cooldown for missile firing in ticks")
                .translation("pneumaticcraft.config.server.micromissile_properties.launch_cooldown")
                .defineInRange("launch_cooldown", 15, 0, Integer.MAX_VALUE);
        micromissiles.lifetime = builder
                .comment("Base missile lifetime in ticks (modified by missile setup)")
                .translation("pneumaticcraft.config.server.micromissile_properties.lifetime")
                .defineInRange("lifetime", 300, 0, Integer.MAX_VALUE);
        micromissiles.missilePodSize = builder
                .comment("Number of micromissiles per pod")
                .translation("pneumaticcraft.config.server.micromissile_properties.missile_pod_size")
                .defineInRange("missile_pod_size", 100, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Minigun Properties");
        minigun.apAmmoDamageMultiplier = builder
                .comment("Armor Piercing Ammo damage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.server.minigun_properties.ap_ammo_damage_multiplier")
                .defineInRange("ap_ammo_damage_multiplier", 1.25, 0, Double.MAX_VALUE);
        minigun.apAmmoIgnoreArmorChance = builder
                .comment("Armor Piercing Ammo percentage chance to ignore target's armor")
                .translation("pneumaticcraft.config.server.minigun_properties.ap_ammo_ignore_armor_chance")
                .defineInRange("ap_ammo_ignore_armor_chance", 100, 1, 100);
        minigun.armorPiercingAmmoCartridgeSize = builder
                .comment("Armor Piercing Ammo cartridge size")
                .translation("pneumaticcraft.config.server.minigun_properties.armor_piercing_ammo_cartridge_size")
                .defineInRange("armor_piercing_ammo_cartridge_size", 250, 1, 30000);
        minigun.baseDamage = builder
                .comment("Base bullet damage of the Sentry Gun, Handheld Minigun, and Drone Minigun, before ammo bonuses are considered")
                .translation("pneumaticcraft.config.server.minigun_properties.base_damage")
                .defineInRange("base_damage", 4, 0, Double.MAX_VALUE);
        minigun.baseRange = builder
                .comment("Base range of Minigun, before Range Upgrades are considered")
                .translation("pneumaticcraft.config.server.minigun_properties.base_range")
                .defineInRange("base_range", 50, 5, 100);
        minigun.explosiveAmmoCartridgeSize = builder
                .comment("Explosive Ammo cartridge size")
                .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_cartridge_size")
                .defineInRange("explosive_ammo_cartridge_size", 125, 1, 30000);
        minigun.explosiveAmmoDamageMultiplier = builder
                .comment("Minigun Explosive Ammo damage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_damage_multiplier")
                .defineInRange("explosive_ammo_damage_multiplier", 0.2, 0, Double.MAX_VALUE);
        minigun.explosiveAmmoExplosionChance = builder
                .comment("Explosive Ammo base percentage chance to cause an explosion")
                .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_explosion_chance")
                .defineInRange("explosive_ammo_explosion_chance", 50, 0, Integer.MAX_VALUE);
        minigun.explosiveAmmoExplosionPower = builder
                .comment("Minigun Explosive Ammo explosion power (ref: 2 = creeper, 4 = TNT")
                .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_explosion_power")
                .defineInRange("explosive_ammo_explosion_power", 1.5, 0, Double.MAX_VALUE);
        minigun.explosiveAmmoTerrainDamage = builder
                .comment("Does Minigun Explosive Ammo damage terrain?")
                .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_terrain_damage")
                .define("explosive_ammo_terrain_damage", false);
        minigun.freezingAmmoBlockIceChance = builder
                .comment("Freezing Ammo base percentage chance to form ice or snow on blocks which have been hit")
                .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_block_ice_chance")
                .defineInRange("freezing_ammo_block_ice_chance", 20, 0, 100);
        minigun.freezingAmmoCartridgeSize = builder
                .comment("Freezing Ammo cartridge size")
                .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_cartridge_size")
                .defineInRange("freezing_ammo_cartridge_size", 500, 0, Integer.MAX_VALUE);
        minigun.freezingAmmoEntityIceChance = builder
                .comment("Freezing Ammo base percentage chance to form ice on entities which have been hit")
                .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_entity_ice_chance")
                .defineInRange("freezing_ammo_entity_ice_chance", 20, 0, 100);
        minigun.freezingAmmoFakeIceDamage = builder
                .comment("Damage done to entities within the fake 'ice' blocks cause by freezing ammo")
                .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_fake_ice_damage")
                .defineInRange("freezing_ammo_fake_ice_damage", 1, 0, Double.MAX_VALUE);
        minigun.incendiaryAmmoBlockIgniteChance = builder
                .comment("Incendiary ammo base percentage chance to ignite blocks")
                .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_block_ignite_chance")
                .defineInRange("incendiary_ammo_block_ignite_chance", 20, 1, 100);
        minigun.incendiaryAmmoCartridgeSize = builder
                .comment("Incendiary Ammo cartridge size")
                .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_cartridge_size")
                .defineInRange("incendiary_ammo_cartridge_size", 500, 1, 30000);
        minigun.incendiaryAmmoEntityIgniteChance = builder
                .comment("Incendiary ammo base percentage chance to ignite entities")
                .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_entity_ignite_chance")
                .defineInRange("incendiary_ammo_entity_ignite_chance", 100, 1, 100);
        minigun.incendiaryAmmoFireDuration = builder
                .comment("Incendiary ammo fire duration on target entities (seconds)")
                .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_fire_duration")
                .defineInRange("incendiary_ammo_fire_duration", 8, 0, Integer.MAX_VALUE);
        minigun.potionProcChance = builder
                .comment("Percentage chance per shot of potion-tipped ammo proc'ing the potion effect, before Dispenser Upgrades are considered")
                .translation("pneumaticcraft.config.server.minigun_properties.potion_proc_chance")
                .defineInRange("potion_proc_chance", 7, 1, 100);
        minigun.standardAmmoCartridgeSize = builder
                .comment("Standard Ammo cartridge size")
                .translation("pneumaticcraft.config.server.minigun_properties.standard_ammo_cartridge_size")
                .defineInRange("standard_ammo_cartridge_size", 1000, 1, 30000);
        minigun.weightedAmmoAirUsageMultiplier = builder
                .comment("Weighted Ammo air usage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_air_usage_multiplier")
                .defineInRange("weighted_ammo_air_usage_multiplier", 8.0, 0, Double.MAX_VALUE);
        minigun.weightedAmmoCartridgeSize = builder
                .comment("Weighted Ammo cartridge size")
                .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_cartridge_size")
                .defineInRange("weighted_ammo_cartridge_size", 250, 1, 30000);
        minigun.weightedAmmoDamageMultiplier = builder
                .comment("Weighted Ammo damage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_damage_multiplier")
                .defineInRange("weighted_ammo_damage_multiplier", 2.5, 0, Double.MAX_VALUE);
        minigun.weightedAmmoRangeMultiplier = builder
                .comment("Weighted Ammo range multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_range_multiplier")
                .defineInRange("weighted_ammo_range_multiplier", 0.2, 0, Double.MAX_VALUE);
        builder.pop();

        builder.push("Integration");
        integration.ieExternalHeaterHeatPerRF = builder
                .comment("Immersive Engineering: External Heater heat/RF.  The amount of PneumaticCraft heat added by 1 RF.")
                .translation("pneumaticcraft.config.server.integration.ie_external_heater_heat_per_rf")
                .defineInRange("ie_external_heater_heat_per_rf", 0.01, 0.0, Double.MAX_VALUE);
        integration.ieExternalHeaterRFperTick = builder
                .comment("Immersive Engineering: External Heater RF/t. Set to 0 to disable External Heater integration entirely.")
                .translation("pneumaticcraft.config.server.integration.ie_external_heater_r_fper_tick")
                .defineInRange("ie_external_heater_r_fper_tick", 100, 0, Integer.MAX_VALUE);
        integration.mekHeatEfficiency = builder
                .comment("Mekanism heat conversion efficiency.  Smaller values mean Mekanism heat is worth less PneumaticCraft heat. Set to 0.0 to disable Mekanism heat integration entirely.")
                .translation("pneumaticcraft.config.server.integration.mek_heat_efficiency")
                .defineInRange("mek_heat_efficiency", 0.4, 0.0, Double.MAX_VALUE);
        integration.mekThermalResistanceMult = builder
                .comment("Mekanism thermal resistance multiplier. Larger values mean slower heat transfer.")
                .translation("pneumaticcraft.config.server.integration.mek_thermal_resistance_mult")
                .defineInRange("mek_thermal_resistance_mult", 100.0, 1.0, Double.MAX_VALUE);
        integration.tanAirConAirUsageMultiplier = builder
                .comment("ToughAsNails: air usage multiplier for the Pneumatic Chestplate Air Conditioning Upgrade.")
                .translation("pneumaticcraft.config.server.integration.tan_air_con_air_usage_multiplier")
                .defineInRange("tan_air_con_air_usage_multiplier", 1.5, 0.0, Double.MAX_VALUE);
        integration.tanHeatDivider = builder
                .comment("Tough As Nails temperature divider; smaller values make PneumaticCraft heat sources have a more pronounced effect on your temperature. Set to 0 to ignore PneumaticCraft heat sources.")
                .translation("pneumaticcraft.config.server.integration.tan_heat_divider")
                .defineInRange("tan_heat_divider", 10.0, 1.0f, Double.MAX_VALUE);
        integration.tanRefreshInterval = builder
                .comment("Interval in ticks with which to refresh heat information from PneumaticCraft heat sources to Tough As Nails. A larger interval is kinder to the server but will provide less precise temperature data to TAN.")
                .translation("pneumaticcraft.config.server.integration.tan_refresh_interval")
                .defineInRange("tan_refresh_interval", 40, 1, 200);
        builder.pop();

        builder.push("Recipes");
        recipes.explosionCrafting = builder
                .comment("Enable/disable explosion crafting (iron->compressed iron).  If you disable this, you'll need another way to get compressed iron initially.")
                .translation("pneumaticcraft.config.server.general.explosion_crafting")
                .define("explosion_crafting", true);
        recipes.coalToDiamondsRecipe = builder
                .comment("Enable crafting diamonds from coal blocks in the pressure chamber")
                .translation("pneumaticcraft.config.server.recipes.coal_to_diamonds")
                .define("coal_to_diamonds", true);
        builder.pop();

        builder.push("Amadron");
        amadron.maxTradesPerPlayer = builder
                .comment("Max number of custom trades a player may add")
                .translation("pneumaticcraft.config.server.amadron.max_trades_per_player")
                .defineInRange("max_trades_per_player", 50, 1, Integer.MAX_VALUE);
        amadron.notifyOfTradeAddition = builder
                .comment("Broadcast a notification when player adds a custom trade")
                .translation("pneumaticcraft.config.server.amadron.notify_of_trade_addition")
                .define("notify_of_trade_addition", true);
        amadron.notifyOfTradeRemoval = builder
                .comment("Broadcast a notification when player removes a custom trade")
                .translation("pneumaticcraft.config.server.amadron.notify_of_trade_removal")
                .define("notify_of_trade_removal", true);
        amadron.notifyOfDealMade = builder
                .comment("Broadcast a notification when an Amadron trade is made")
                .translation("pneumaticcraft.config.server.amadron.notify_of_deal_made")
                .define("notify_of_deal_made", true);
        builder.pop();

        builder.push("Block Heat Defaults");
        blockHeatDefaults.blockThermalResistance = builder
                .comment("Default thermal resistance for solid blocks")
                .translation("pneumaticcraft.config.server.blockHeatDefaults.blockThermalResistance")
                .defineInRange("blockThermalResistance", 500.0, Double.MIN_VALUE, Double.MAX_VALUE);
        blockHeatDefaults.fluidThermalResistance = builder
                .comment("Default thermal resistance for fluid blocks")
                .translation("pneumaticcraft.config.server.blockHeatDefaults.fluidThermalResistance")
                .defineInRange("fluidThermalResistance", 100.0, Double.MIN_VALUE, Double.MAX_VALUE);
        blockHeatDefaults.fluidTotalHeatCapacity = builder
                .comment("Default heat capacity for a fluid block")
                .translation("pneumaticcraft.config.server.blockHeatDefaults.fluidTotalHeatCapacity")
                .defineInRange("fluidTotalHeatCapacity", 10000, 0, Integer.MAX_VALUE);
        blockHeatDefaults.ambientTemperatureBiomeModifier = builder
                .comment("Ambient temperature modifier by biome (default 25 gives the Nether a heat boost of 30C)")
                .translation("pneumaticcraft.config.server.blockHeatDefaults.ambientTemperatureBiomeModifier")
                .defineInRange("ambientTemperatureBiomeModifier", 25.0, 0.0, 1000.0);
        blockHeatDefaults.ambientTemperatureHeightModifier = builder
                .comment("Ambient temperature modifier by height (in degrees per block above 80 or below 40)")
                .translation("pneumaticcraft.config.server.blockHeatDefaults.ambientTemperatureHeightModifier")
                .defineInRange("ambientTemperatureHeightModifier", 0.1, 0.0, 10.0);
        builder.pop();
    }
}
