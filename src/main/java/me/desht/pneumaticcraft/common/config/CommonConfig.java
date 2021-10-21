package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.Lists;
import me.desht.pneumaticcraft.common.villages.VillagerTradesRegistration;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
    public static class General {
        ForgeConfigSpec.BooleanValue droneDebuggerPathParticles;
        ForgeConfigSpec.IntValue oilGenerationChance;
        ForgeConfigSpec.IntValue surfaceOilGenerationChance;
        ForgeConfigSpec.BooleanValue enableDungeonLoot;
        ForgeConfigSpec.BooleanValue enableDroneSuffocation;
        ForgeConfigSpec.DoubleValue fuelBucketEfficiency;
        ForgeConfigSpec.IntValue maxProgrammingArea;
        ForgeConfigSpec.ConfigValue<List<String>> oilWorldGenBlacklist;
        ForgeConfigSpec.ConfigValue<List<String>> oilWorldGenCategoryBlacklist;
        ForgeConfigSpec.ConfigValue<List<String>> oilWorldGenDimensionBlacklist;
        ForgeConfigSpec.ConfigValue<List<String>> vacuumTrapBlacklist;
        ForgeConfigSpec.IntValue minFluidFuelTemperature;
        ForgeConfigSpec.BooleanValue useUpDyesWhenColoring;
        ForgeConfigSpec.BooleanValue dronesRenderHeldItem;
        ForgeConfigSpec.BooleanValue dronesCanImportXPOrbs;
        ForgeConfigSpec.BooleanValue dronesCanBePickedUp;
    }
    public static class Machines {
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
        ForgeConfigSpec.IntValue pneumaticDynamoEfficiency;
        ForgeConfigSpec.IntValue pneumaticEngineEfficiency;
        ForgeConfigSpec.IntValue pneumaticGeneratorEfficiency;
        ForgeConfigSpec.IntValue pneumaticPumpEfficiency;
        ForgeConfigSpec.DoubleValue speedUpgradeSpeedMultiplier;
        ForgeConfigSpec.DoubleValue speedUpgradeUsageMultiplier;
        ForgeConfigSpec.ConfigValue<List<String>> seismicSensorFluids;
        ForgeConfigSpec.ConfigValue<List<String>> seismicSensorFluidTags;
        ForgeConfigSpec.ConfigValue<List<String>> disenchantingBlacklist;
    }
    public static class Armor {
        ForgeConfigSpec.IntValue jetBootsAirUsage;
        ForgeConfigSpec.IntValue armorStartupTime;
        ForgeConfigSpec.DoubleValue flippersSpeedBoostGround;
        ForgeConfigSpec.DoubleValue flippersSpeedBoostFloating;
        ForgeConfigSpec.IntValue repairAirUsage;
        ForgeConfigSpec.IntValue magnetAirUsage;
        ForgeConfigSpec.IntValue scubaMultiplier;
    }
    public static class Integration {
        ForgeConfigSpec.DoubleValue mekThermalEfficiencyFactor;
        ForgeConfigSpec.DoubleValue ieExternalHeaterHeatPerRF;
        ForgeConfigSpec.IntValue ieExternalHeaterRFperTick;
        ForgeConfigSpec.DoubleValue mekThermalResistanceFactor;
        ForgeConfigSpec.DoubleValue cofhHoldingMultiplier;
    }
    public static class Advanced {
        ForgeConfigSpec.IntValue stuckDroneTeleportTicks;
        ForgeConfigSpec.BooleanValue disableKeroseneLampFakeAirBlock;
        ForgeConfigSpec.IntValue fluidTankUpdateRate;
        ForgeConfigSpec.IntValue pressureSyncPrecision;
        ForgeConfigSpec.BooleanValue stopDroneAI;
        ForgeConfigSpec.BooleanValue dontUpdateInfiniteWaterSources;
        ForgeConfigSpec.IntValue maxDroneChargingStationSearchRange;
        ForgeConfigSpec.IntValue maxDroneTeleportRange;
    }
    public static class Micromissiles {
        ForgeConfigSpec.DoubleValue baseExplosionDamage;
        ForgeConfigSpec.BooleanValue damageTerrain;
        ForgeConfigSpec.IntValue launchCooldown;
        ForgeConfigSpec.IntValue lifetime;
        ForgeConfigSpec.IntValue missilePodSize;
    }
    public static class Minigun {
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
        ForgeConfigSpec.BooleanValue blockHitParticles;
    }
    public static class Recipes {
        ForgeConfigSpec.BooleanValue coalToDiamondsRecipe;
        ForgeConfigSpec.BooleanValue explosionCrafting;
        ForgeConfigSpec.BooleanValue inWorldPlasticSolidification;
        ForgeConfigSpec.BooleanValue inWorldYeastCrafting;
    }

    public static class Amadron {
        ForgeConfigSpec.IntValue numPeriodicOffers;
        ForgeConfigSpec.IntValue numVillagerOffers;
        ForgeConfigSpec.IntValue reshuffleInterval;
        ForgeConfigSpec.IntValue maxTradesPerPlayer;
        ForgeConfigSpec.BooleanValue notifyOfTradeAddition;
        ForgeConfigSpec.BooleanValue notifyOfTradeRemoval;
        ForgeConfigSpec.BooleanValue notifyOfDealMade;
    }

    public static class Heat {
        ForgeConfigSpec.BooleanValue addDefaultFluidEntries;
        ForgeConfigSpec.DoubleValue blockThermalResistance;
        ForgeConfigSpec.DoubleValue fluidThermalResistance;
        ForgeConfigSpec.IntValue defaultFluidHeatCapacity;
        ForgeConfigSpec.DoubleValue ambientTemperatureBiomeModifier;
        ForgeConfigSpec.DoubleValue ambientTemperatureHeightModifier;
        ForgeConfigSpec.DoubleValue airThermalResistance;
    }

    public static class Logistics {
        ForgeConfigSpec.DoubleValue itemTransportCost;
        ForgeConfigSpec.DoubleValue fluidTransportCost;
        ForgeConfigSpec.DoubleValue minPressure;
    }

    public static class Jackhammer {
        ForgeConfigSpec.IntValue baseAirUsage;
        ForgeConfigSpec.IntValue maxVeinMinerRange;
    }

    public static class Villagers {
        ForgeConfigSpec.BooleanValue addMechanicHouse;
        ForgeConfigSpec.EnumValue<VillagerTradesRegistration.WhichTrades> whichTrades;
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
    public final Heat heat = new Heat();
    public final Logistics logistics = new Logistics();
    public final Jackhammer jackhammer = new Jackhammer();
    public final Villagers villagers = new Villagers();

    CommonConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("General");
        general.oilGenerationChance = builder
                .worldRestart()
                .comment("Chance per chunk as a percentage to generate an Oil Lake. Set to 0 for no oil lakes. See also 'surface_oil_generation_chance'.")
                .translation("pneumaticcraft.config.common.general.oilGenerationChance")
                .defineInRange("oil_generation_chance", 15, 0, 100);
        general.surfaceOilGenerationChance = builder
                .worldRestart()
                .comment("When an Oil Lake would be generated at the surface (see 'oil_generation_chance'), percentage chance that this will actually generate a lake. Set to 0 for no surface oil lakes, and fewer lakes overall. Higher values don't guarantee surface oil lakes, but make them more likely, as well as making oil lakes more likely overall. It is recommended to adjust this value in conjunction with 'oil_generation_chance'.")
                .translation("pneumaticcraft.config.common.general.surfaceOilGenerationChance")
                .defineInRange("surface_oil_generation_chance", 25, 0, 100);
        general.enableDungeonLoot = builder
                .comment("Enable mod dungeon loot generation")
                .translation("pneumaticcraft.config.common.general.enable_dungeon_loot")
                .define("enable_dungeon_loot", true);
        general.enableDroneSuffocation = builder
                .comment("Enable Drone Suffocation Damage")
                .translation("pneumaticcraft.config.common.general.enable_drone_suffocation")
                .define("enable_drone_suffocation", true);
        general.fuelBucketEfficiency = builder
                .comment("Efficiency of fuel buckets as furnace fuel (default 0.05 means 1 bucket of LPG smelts 450 items in a vanilla furnace)")
                .translation("pneumaticcraft.config.common.general.fuel_bucket_efficiency")
                .defineInRange("fuel_bucket_efficiency", 0.05, 0.0, Double.MAX_VALUE);
        general.maxProgrammingArea = builder
                .comment("Maximum number of blocks in the area defined in an Area Programming Puzzle Piece")
                .translation("pneumaticcraft.config.common.general.max_programming_area")
                .defineInRange("max_programming_area", 250000, 1, Integer.MAX_VALUE);
        general.oilWorldGenBlacklist = builder
                .worldRestart()
                .comment("Oil worldgen blacklist by biome: add biome IDs to this list if you don't want oil lake worldgen to happen there.  This works in conjunction with 'oil_world_gen_category_blacklist' - if a biome matches either, then no oil lakes will generate there. You can wildcard this; e.g 'modid:*' blacklists ALL biomes of namespace 'modid'.")
                .translation("pneumaticcraft.config.common.general.oil_world_gen_blacklist")
                .define("oil_world_gen_blacklist", Lists.newArrayList("minecraft:soul_sand_valley", "minecraft:crimson_forest", "minecraft:warped_forest", "minecraft:nether_wastes", "minecraft:the_void", "minecraft:the_end", "minecraft:small_end_islands", "minecraft:end_midlands", "minecraft:end_highlands", "minecraft:end_barrens"));
        general.oilWorldGenCategoryBlacklist = builder
                .worldRestart()
                .comment("Oil worldgen blacklist by biome category: add biome categories to this list if you don't want oil lake worldgen to happen there. Accepted categories are: beach, desert, extreme_hills, forest, icy, jungle, mesa, mushroom, nether, none, ocean, plains, river, savanna, swamp, taiga, the_end.  This works in conjunction with 'oil_world_gen_blacklist' - if a biome matches either, then no oil lakes will generate there.")
                .translation("pneumaticcraft.config.common.general.oil_world_gen_category_blacklist")
                .define("oil_world_gen_category_blacklist", Lists.newArrayList("none"));
        general.oilWorldGenDimensionBlacklist = builder
                .worldRestart()
                .comment("Oil worldgen blacklist by dimension ID: add dimension ID's to this list if you don't want oil lake worldgen to happen there. You can wildcard this; e.g 'modid:*' blacklists ALL dimensions of namespace 'modid'.")
                .translation("pneumaticcraft.config.common.general.oil_world_gen_dimension_blacklist")
                .define("oil_world_gen_dimension_blacklist", Lists.newArrayList());
        general.minFluidFuelTemperature = builder
                .worldRestart()
                .comment("Fluids at least as hot as this temperature (Kelvin) will be auto-registered as Liquid Compressor fuels, the quality being dependent on fluid temperature.")
                .translation("pneumaticcraft.config.common.general.min_fluid_fuel_temperature")
                .defineInRange("min_fluid_fuel_temperature", 373, 0, Integer.MAX_VALUE);
        general.useUpDyesWhenColoring = builder
                .comment("Should dyes be used up when coloring things (Drones, Logistics Modules, Redstone Modules)?")
                .translation("pneumaticcraft.config.common.general.use_up_dyes_when_coloring")
                .define("use_up_dyes_when_coloring", false);
        general.dronesRenderHeldItem = builder
                .comment("Drones render their held item (the item in slot 0 of their inventory) ?  Note: this is in common config since if enabled, server needs to sync the item data to the client.")
                .translation("pneumaticcraft.config.common.general.drones_render_held_item")
                .define("drones_render_held_item", true);
        general.dronesCanImportXPOrbs = builder
                .comment("Are drones allowed to import Experience Orbs and convert them to Memory Essence fluid?")
                .translation("pneumaticcraft.config.common.general.drones_can_import_xp_orbs")
                .define("drones_can_import_xp_orbs", true);
        general.dronesCanBePickedUp = builder
                .comment("Will Drones automatically get picked up by Boats/Minecarts/etc. if they're close enough?")
                .translation("pneumaticcraft.config.common.general.drones_can_be_picked_up")
                .define("drones_can_be_picked_up", false);
        general.droneDebuggerPathParticles = builder
                .comment("Show particle trail indicating the currently-debugged drone's planned path")
                .translation("pneumaticcraft.config.common.general.drone_debugger_path_particles")
                .define("drone_debugger_path_particles", true);
        general.vacuumTrapBlacklist = builder
                .comment("Blacklisted entity type ID's or tags (use '#' prefix), which the Vacuum Trap will not try to absorb. Note that players, tamed entities, boss entities, and PneumaticCraft drones may never be absorbed, regardless of config settings.")
                .translation("pneumaticcraft.config.common.general.vacuum_trap_blacklist")
                .define("vacuum_trap_blacklist", Lists.newArrayList());
        builder.pop();

        builder.push("Machine Properties");
        machines.aerialInterfaceArmorCompat = builder
                .comment("Aerial Interface backwards compat: allow pre-0.8.0 behaviour of getting player's armor inventory from top face, even with Dispenser Upgrade installed")
                .translation("pneumaticcraft.config.common.machine_properties.aerial_interface_armor_compat")
                .define("aerial_interface_armor_compat", true);
        machines.cropSticksGrowthBoostChance = builder
                .comment("Chance per tick of Crop Supports causing a growth tick. The default, 0.002, is roughly 2.5 times faster than the vanilla growth rate")
                .translation("pneumaticcraft.config.common.machine_properties.crop_sticks_growth_boost_chance")
                .defineInRange("crop_sticks_growth_boost_chance", 0.002, 0, Double.MAX_VALUE);
        machines.electricCompressorEfficiency = builder
                .comment("Changing this value will alter the pressurized air production of the Electric Compressor. The input, EU, will stay the same")
                .translation("pneumaticcraft.config.common.machine_properties.electric_compressor_efficiency")
                .defineInRange("electric_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.electrostaticLightningChance = builder
                .comment("Base chance (1/x) per tick of a lightning strike on/around the Electrostatic Generator")
                .translation("pneumaticcraft.config.common.machine_properties.electrostatic_lightning_chance")
                .defineInRange("electrostatic_lightning_chance", 100000, 0, Integer.MAX_VALUE);
        machines.elevatorBaseBlocksPerBase = builder
                .comment("The max height of an elevator per stacked Elevator Base block.")
                .translation("pneumaticcraft.config.common.machine_properties.elevator_base_blocks_per_base")
                .defineInRange("elevator_base_blocks_per_base", 6, 1, 256);
        machines.fluxCompressorEfficiency = builder
                .comment("The amount of air produced by using 100 FE (Forge Energy) in the flux compressor")
                .translation("pneumaticcraft.config.common.machine_properties.flux_compressor_efficiency")
                .defineInRange("flux_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.keroseneLampCanUseAnyFuel = builder
                .comment("Can the Kerosene Lamp burn any kind of fuel?  If false, only Kerosene can be burnt")
                .translation("pneumaticcraft.config.common.machine_properties.kerosene_lamp_can_use_any_fuel")
                .define("kerosene_lamp_can_use_any_fuel", true);
        machines.keroseneLampFuelEfficiency = builder
                .comment("Kerosene Lamp fuel efficiency: higher values mean fuel will last longer in the lamp")
                .translation("pneumaticcraft.config.common.machine_properties.kerosene_lamp_fuel_efficiency")
                .defineInRange("kerosene_lamp_fuel_efficiency", 1.0, 0, Double.MAX_VALUE);
        machines.kineticCompressorEfficiency = builder
                .comment("The amount of air produced by using 100 MJ (Minecraft Joules) in the flux compressor")
                .translation("pneumaticcraft.config.common.machine_properties.kinetic_compressor_efficiency")
                .defineInRange("kinetic_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.liquidHopperDispenser = builder
                .comment("Can the Liquid Hopper absorb/dispense fluids into the world with a Dispenser Upgrade?")
                .translation("pneumaticcraft.config.common.machine_properties.liquid_hopper_dispenser")
                .define("liquid_hopper_dispenser", true);
        machines.omniHopperDispenser = builder
                .comment("Can the Omnidirectional Hopper dispense items into the world with a Dispenser Upgrade?")
                .translation("pneumaticcraft.config.common.machine_properties.omni_hopper_dispenser")
                .define("omni_hopper_dispenser", true);
        machines.pneumaticDynamoEfficiency = builder
                .comment("The amount of FE (Forge Energy) produced by using 100mL of air in the Pneumatic Dynamo")
                .translation("pneumaticcraft.config.common.machine_properties.pneumatic_dynamo_efficiency")
                .defineInRange("pneumatic_dynamo_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.pneumaticEngineEfficiency = builder
                .comment("The amount of MJ (Minecraft Joules) produced by using 100mL of air in the Pneumatic Dynamo")
                .translation("pneumaticcraft.config.common.machine_properties.pneumatic_engine_efficiency")
                .defineInRange("pneumatic_engine_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.pneumaticGeneratorEfficiency = builder
                .comment("Changing this value will alter the pressurized air usage of the Pneumatic Generator. The output, EU, will stay the same.")
                .translation("pneumaticcraft.config.common.machine_properties.pneumatic_generator_efficiency")
                .defineInRange("pneumatic_generator_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.pneumaticPumpEfficiency = builder
                .comment("Changing this value will alter the hydraulic bar production of the Pneumatic Pump. The input, air, will stay the same")
                .translation("pneumaticcraft.config.common.machine_properties.pneumatic_pump_efficiency")
                .defineInRange("pneumatic_pump_efficiency", 40, 0, Integer.MAX_VALUE);
        machines.speedUpgradeSpeedMultiplier = builder
                .comment("Speed multiplier per speed upgrade: speed mult = speedUpgradeSpeedMultiplier ^ num_of_speed_upgrades")
                .translation("pneumaticcraft.config.common.machine_properties.speed_upgrade_speed_multiplier")
                .defineInRange("speed_upgrade_speed_multiplier", PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER, 1.0, 2.0);
        machines.speedUpgradeUsageMultiplier = builder
                .comment("Fuel usage / heat gen multiplier per speed upgrade: usage mult = speedUpgradeUsageMultiplier ^ num_of_speed_upgrades")
                .translation("pneumaticcraft.config.common.machine_properties.speed_upgrade_usage_multiplier")
                .defineInRange("speed_upgrade_usage_multiplier", PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER, 1.0, 2.0);
        machines.seismicSensorFluids = builder
                .worldRestart()
                .comment("Fluid registry ID's that the Seismic Sensor will search for. It's recommended to use 'seismicSensorFluidTags' where possible, but this setting can be used if you want to add fluids which don't have any associated fluid tags.")
                .translation("pneumaticcraft.config.common.machines.seismic_sensor_fluids")
                .define("seismic_sensor_fluids", Lists.newArrayList());
        machines.seismicSensorFluidTags = builder
                .worldRestart()
                .comment("Fluid tag names that the Seismic Sensor will search for. Known vanilla tags are 'minecraft:water' and 'minecraft:lava'. Other available fluid tags are mod-dependent. By default, 'forge:crude_oil' is matched, allowing PneumaticCraft (and potentially other mods) crude oil.")
                .translation("pneumaticcraft.config.common.machines.seismic_sensor_fluid_tags")
                .define("seismic_sensor_fluid_tags", Lists.newArrayList("forge:crude_oil"));
        machines.disenchantingBlacklist = builder
                .comment("Blacklist items from being allowed in the Pressure Chamber disenchanting system. This is a starts-with string match, so you can match by mod, or individual item names as you need. Blacklisted by default are Quark Ancient Tomes, and all Tetra items; both can lead to enchantment duping as they have special enchantment mechanics.")
                .translation("pneumaticcraft.config.common.machines.disenchanting_blacklist")
                .define("disenchanting_blacklist", Lists.newArrayList("quark:ancient_tome", "tetra:"));
        builder.pop();

        builder.push("Pneumatic Armor");
        armor.jetBootsAirUsage = builder
                .comment("Jetboots air usage in mL/tick (per Jet Boots Upgrade)")
                .translation("pneumaticcraft.config.common.armor.jet_boots_air_usage")
                .defineInRange("jet_boots_air_usage", PneumaticValues.PNEUMATIC_JET_BOOTS_USAGE, 0, Integer.MAX_VALUE);
        armor.armorStartupTime = builder
                .comment("Base Pneumatic Armor startup time in ticks (before Speed Upgrades)")
                .translation("pneumaticcraft.config.common.armor.armor_startup_time")
                .defineInRange("armor_startup_time", 200, 20, Integer.MAX_VALUE);
        armor.flippersSpeedBoostGround = builder
                .comment("Flippers Upgrade speed boost when in water and feet on ground")
                .translation("pneumaticcraft.config.common.armor.flippers_speed_boost_ground")
                .defineInRange("flippers_speed_boost_ground", 0.03, 0, 1);
        armor.flippersSpeedBoostFloating = builder
                .comment("Flippers Upgrade speed boost when floating in water")
                .translation("pneumaticcraft.config.common.armor.flippers_speed_boost_floating")
                .defineInRange("flippers_speed_boost_floating", 0.045, 0, 1);
        armor.repairAirUsage = builder
                .comment("Air usage for armor repair, in mL per Item Life Upgrade per point of damage repaired")
                .translation("pneumaticcraft.config.common.armor.repair_air_usage")
                .defineInRange("repair_air_usage", PneumaticValues.PNEUMATIC_ARMOR_REPAIR_USAGE, 0, Integer.MAX_VALUE);
        armor.magnetAirUsage = builder
                .comment("Air usage for Magnet Upgrade, in mL per item or XP orb attracted")
                .translation("pneumaticcraft.config.common.armor.magnet_air_usage")
                .defineInRange("magnet_air_usage", PneumaticValues.MAGNET_AIR_USAGE, 0, Integer.MAX_VALUE);
        armor.scubaMultiplier = builder
                .comment("Air used per point of 'player air' restored by the Scuba Upgrade")
                .translation("pneumaticcraft.config.common.armor.scuba_multiplier")
                .defineInRange("scuba_multiplier", PneumaticValues.PNEUMATIC_HELMET_SCUBA_MULTIPLIER, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Advanced");
        advanced.disableKeroseneLampFakeAirBlock = builder
                .comment("When set to true, the Kerosene Lamp's fake air blocks won't be registered and therefore removed from the world. Useful if this causes trouble (it shouldn't though)")
                .translation("pneumaticcraft.config.common.advanced.disable_kerosene_lamp_fake_air_block")
                .define("disable_kerosene_lamp_fake_air_block", false);
        advanced.fluidTankUpdateRate = builder
                .comment("The minimum interval in ticks between which fluid tank contents should be synced to clients. Smaller values mean smoother visual tank updates, but more of a performance cost in terms of network syncing. Note that fluid tank sync packets are also only sent when a fluid tank changes by more than 1% of its capacity, or 1000mB, whichever is smaller.")
                .translation("pneumaticcraft.config.common.advanced.fluid_tank_update_rate")
                .defineInRange("fluid_tank_update_rate", 10, 1, 100);
        advanced.pressureSyncPrecision = builder
                .comment("Precision to which pressurizable item air levels are synced to client. Default of 10 is precise enough to show pressure to 1 decimal place, which is what is display in client tooltips & pneumatic armor HUD. Lower values will sync less precisely, reducing server->client network traffic. Values higher than 10 are not recommended (will cause extra network traffic for no benefit).")
                .translation("pneumaticcraft.config.common.advanced.pressurizable_sync_precision")
                .defineInRange("pressurizable_sync_precision", 10, 1, 100);
        advanced.stopDroneAI = builder
                .comment("When set to true, Drones will not execute any program. This is useful to set to true when due to a bug Drones are lagging your server or crashing it. Please report any such bugs as a PneumaticCraft: Repressurized issue so it can be investigated.")
                .translation("pneumaticcraft.config.common.advanced.stop_drone_ai")
                .define("stop_drone_ai", false);
        advanced.dontUpdateInfiniteWaterSources = builder
                .comment("Don't remove a water source block when picking up (drones, liquid hoppers, gas lift) if it has at least two water source neighbours. This can reduce lag due to frequent block updates, and can also potentially make water import much faster. Set this to false if you want no-infinite-water rules in a world, or want to limit the speed of water importing to vanilla block update rates.")
                .translation("pneumaticcraft.config.common.advanced.dont_update_infinite_water_sources")
                .define("dont_update_infinite_water_sources", true);
        advanced.maxDroneChargingStationSearchRange = builder
                .comment("How far will a drone go to find a Charging Station when it's low on air? Note: drones will teleport, possibly across the world to someone else's base, if this range is very large.")
                .translation("pneumaticcraft.config.common.advanced.max_drone_charging_station_search_range")
                .defineInRange("max_drone_charging_station_search_range", 80, 16, Integer.MAX_VALUE);
        advanced.maxDroneTeleportRange = builder
                .comment("The maximum distance that a Drone may teleport when it can't find a path to its destination. Default value of 0 means no limit. This is primarily intended to limit abuse of teleportation to other players on PvP servers, but may find other uses. Be careful about setting this value very low.")
                .translation("pneumaticcraft.config.common.advanced.max_drone_charging_station_search_range")
                .defineInRange("max_drone_teleport_range", 0, 0, Integer.MAX_VALUE);
        advanced.stuckDroneTeleportTicks = builder
                .comment("If a Drone has found a path, but gets stuck on a block along that path, it will teleport to its destination after this many ticks of being stuck. Set this to 0 to disable teleporting, which will likely leave the drone waiting there forever (or until it runs out of air). Note that getting stuck on a block is usually the fault of the mod that added the block (especially if the block has a non-full-cube shape), but if you encounter this behaviour, please report it as a PneumaticCraft: Repressurized issue so it can be investigated.")
                .translation("pneumaticcraft.config.common.advanced.stuck_drone_teleport_ticks")
                .defineInRange("stuck_drone_teleport_ticks", 20, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Micromissile Properties");
        micromissiles.baseExplosionDamage = builder
                .comment("Base explosion damage (modified by missile setup)")
                .translation("pneumaticcraft.config.common.micromissile_properties.base_explosion_damage")
                .defineInRange("base_explosion_damage", 1, 0, Double.MAX_VALUE);
        micromissiles.damageTerrain = builder
                .comment("Do micromissile explosions cause terrain damage?")
                .translation("pneumaticcraft.config.common.micromissile_properties.damage_terrain")
                .define("damage_terrain", false);
        micromissiles.launchCooldown = builder
                .comment("Cooldown for missile firing in ticks")
                .translation("pneumaticcraft.config.common.micromissile_properties.launch_cooldown")
                .defineInRange("launch_cooldown", 15, 0, Integer.MAX_VALUE);
        micromissiles.lifetime = builder
                .comment("Base missile lifetime in ticks (modified by missile setup)")
                .translation("pneumaticcraft.config.common.micromissile_properties.lifetime")
                .defineInRange("lifetime", 300, 0, Integer.MAX_VALUE);
        micromissiles.missilePodSize = builder
                .comment("Number of micromissiles per pod")
                .translation("pneumaticcraft.config.common.micromissile_properties.missile_pod_size")
                .defineInRange("missile_pod_size", 100, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Minigun Properties");
        minigun.apAmmoDamageMultiplier = builder
                .comment("Armor Piercing Ammo damage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.common.minigun_properties.ap_ammo_damage_multiplier")
                .defineInRange("ap_ammo_damage_multiplier", 1.25, 0, Double.MAX_VALUE);
        minigun.apAmmoIgnoreArmorChance = builder
                .comment("Armor Piercing Ammo percentage chance to ignore target's armor")
                .translation("pneumaticcraft.config.common.minigun_properties.ap_ammo_ignore_armor_chance")
                .defineInRange("ap_ammo_ignore_armor_chance", 100, 1, 100);
        minigun.armorPiercingAmmoCartridgeSize = builder
                .comment("Armor Piercing Ammo cartridge size")
                .translation("pneumaticcraft.config.common.minigun_properties.armor_piercing_ammo_cartridge_size")
                .defineInRange("armor_piercing_ammo_cartridge_size", 250, 1, 30000);
        minigun.baseDamage = builder
                .comment("Base bullet damage of the Sentry Gun, Handheld Minigun, and Drone Minigun, before ammo bonuses are considered")
                .translation("pneumaticcraft.config.common.minigun_properties.base_damage")
                .defineInRange("base_damage", 4, 0, Double.MAX_VALUE);
        minigun.baseRange = builder
                .comment("Base range of Minigun, before Range Upgrades are considered")
                .translation("pneumaticcraft.config.common.minigun_properties.base_range")
                .defineInRange("base_range", 50, 5, 100);
        minigun.explosiveAmmoCartridgeSize = builder
                .comment("Explosive Ammo cartridge size")
                .translation("pneumaticcraft.config.common.minigun_properties.explosive_ammo_cartridge_size")
                .defineInRange("explosive_ammo_cartridge_size", 125, 1, 30000);
        minigun.explosiveAmmoDamageMultiplier = builder
                .comment("Minigun Explosive Ammo damage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.common.minigun_properties.explosive_ammo_damage_multiplier")
                .defineInRange("explosive_ammo_damage_multiplier", 0.2, 0, Double.MAX_VALUE);
        minigun.explosiveAmmoExplosionChance = builder
                .comment("Explosive Ammo base percentage chance to cause an explosion")
                .translation("pneumaticcraft.config.common.minigun_properties.explosive_ammo_explosion_chance")
                .defineInRange("explosive_ammo_explosion_chance", 50, 0, Integer.MAX_VALUE);
        minigun.explosiveAmmoExplosionPower = builder
                .comment("Minigun Explosive Ammo explosion power (ref: 2 = creeper, 4 = TNT")
                .translation("pneumaticcraft.config.common.minigun_properties.explosive_ammo_explosion_power")
                .defineInRange("explosive_ammo_explosion_power", 1.5, 0, Double.MAX_VALUE);
        minigun.explosiveAmmoTerrainDamage = builder
                .comment("Does Minigun Explosive Ammo damage terrain?")
                .translation("pneumaticcraft.config.common.minigun_properties.explosive_ammo_terrain_damage")
                .define("explosive_ammo_terrain_damage", false);
        minigun.freezingAmmoBlockIceChance = builder
                .comment("Freezing Ammo base percentage chance to form ice or snow on blocks which have been hit")
                .translation("pneumaticcraft.config.common.minigun_properties.freezing_ammo_block_ice_chance")
                .defineInRange("freezing_ammo_block_ice_chance", 10, 0, 100);
        minigun.freezingAmmoCartridgeSize = builder
                .comment("Freezing Ammo cartridge size")
                .translation("pneumaticcraft.config.common.minigun_properties.freezing_ammo_cartridge_size")
                .defineInRange("freezing_ammo_cartridge_size", 500, 0, Integer.MAX_VALUE);
        minigun.freezingAmmoEntityIceChance = builder
                .comment("Freezing Ammo base percentage chance to form ice on entities which have been hit")
                .translation("pneumaticcraft.config.common.minigun_properties.freezing_ammo_entity_ice_chance")
                .defineInRange("freezing_ammo_entity_ice_chance", 20, 0, 100);
        minigun.freezingAmmoFakeIceDamage = builder
                .comment("Damage done to entities within the fake 'ice' blocks cause by freezing ammo")
                .translation("pneumaticcraft.config.common.minigun_properties.freezing_ammo_fake_ice_damage")
                .defineInRange("freezing_ammo_fake_ice_damage", 1, 0, Double.MAX_VALUE);
        minigun.incendiaryAmmoBlockIgniteChance = builder
                .comment("Incendiary ammo base percentage chance to ignite blocks")
                .translation("pneumaticcraft.config.common.minigun_properties.incendiary_ammo_block_ignite_chance")
                .defineInRange("incendiary_ammo_block_ignite_chance", 20, 1, 100);
        minigun.incendiaryAmmoCartridgeSize = builder
                .comment("Incendiary Ammo cartridge size")
                .translation("pneumaticcraft.config.common.minigun_properties.incendiary_ammo_cartridge_size")
                .defineInRange("incendiary_ammo_cartridge_size", 500, 1, 30000);
        minigun.incendiaryAmmoEntityIgniteChance = builder
                .comment("Incendiary ammo base percentage chance to ignite entities")
                .translation("pneumaticcraft.config.common.minigun_properties.incendiary_ammo_entity_ignite_chance")
                .defineInRange("incendiary_ammo_entity_ignite_chance", 100, 1, 100);
        minigun.incendiaryAmmoFireDuration = builder
                .comment("Incendiary ammo fire duration on target entities (seconds)")
                .translation("pneumaticcraft.config.common.minigun_properties.incendiary_ammo_fire_duration")
                .defineInRange("incendiary_ammo_fire_duration", 8, 0, Integer.MAX_VALUE);
        minigun.potionProcChance = builder
                .comment("Percentage chance per shot of potion-tipped ammo proc'ing the potion effect, before Dispenser Upgrades are considered")
                .translation("pneumaticcraft.config.common.minigun_properties.potion_proc_chance")
                .defineInRange("potion_proc_chance", 7, 1, 100);
        minigun.standardAmmoCartridgeSize = builder
                .comment("Standard Ammo cartridge size")
                .translation("pneumaticcraft.config.common.minigun_properties.standard_ammo_cartridge_size")
                .defineInRange("standard_ammo_cartridge_size", 1000, 1, 30000);
        minigun.weightedAmmoAirUsageMultiplier = builder
                .comment("Weighted Ammo air usage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.common.minigun_properties.weighted_ammo_air_usage_multiplier")
                .defineInRange("weighted_ammo_air_usage_multiplier", 8.0, 0, Double.MAX_VALUE);
        minigun.weightedAmmoCartridgeSize = builder
                .comment("Weighted Ammo cartridge size")
                .translation("pneumaticcraft.config.common.minigun_properties.weighted_ammo_cartridge_size")
                .defineInRange("weighted_ammo_cartridge_size", 250, 1, 30000);
        minigun.weightedAmmoDamageMultiplier = builder
                .comment("Weighted Ammo damage multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.common.minigun_properties.weighted_ammo_damage_multiplier")
                .defineInRange("weighted_ammo_damage_multiplier", 2.5, 0, Double.MAX_VALUE);
        minigun.weightedAmmoRangeMultiplier = builder
                .comment("Weighted Ammo range multiplier (relative to standard ammo)")
                .translation("pneumaticcraft.config.common.minigun_properties.weighted_ammo_range_multiplier")
                .defineInRange("weighted_ammo_range_multiplier", 0.2, 0, Double.MAX_VALUE);
        minigun.blockHitParticles = builder
                .comment("Show particles when a block is hit by minigun fire? Looks good, but consumes some network bandwidth.")
                .translation("pneumaticcraft.config.common.minigun_properties.block_hit_particles")
                .define("block_hit_particles", true);
        builder.pop();

        builder.push("Integration");
        integration.ieExternalHeaterHeatPerRF = builder
                .comment("Immersive Engineering: External Heater heat/RF.  The amount of PneumaticCraft heat added by 1 RF.")
                .translation("pneumaticcraft.config.common.integration.ie_external_heater_heat_per_rf")
                .defineInRange("ie_external_heater_heat_per_rf", 0.01, 0.0, Double.MAX_VALUE);
        integration.ieExternalHeaterRFperTick = builder
                .comment("Immersive Engineering: External Heater RF/t. Set to 0 to disable External Heater integration entirely.")
                .translation("pneumaticcraft.config.common.integration.ie_external_heater_r_fper_tick")
                .defineInRange("ie_external_heater_r_fper_tick", 100, 0, Integer.MAX_VALUE);
        integration.mekThermalResistanceFactor = builder
                .comment("Mekanism thermal resistance multiplier. Larger values mean slower heat transfer between Mekanism and PneumaticCraft blocks.")
                .translation("pneumaticcraft.config.common.integration.mek_thermal_resistance_factor")
                .defineInRange("mek_thermal_resistance_factor", 5.0, 1.0, Double.MAX_VALUE);
        integration.mekThermalEfficiencyFactor = builder
                .comment("Mekanism <-> PneumaticCraft heat conversion efficiency. Set to 0 to disable Mekanism heat integration entirely. Note that Mekanism and PNC use a similar heat system, but scale things quite differently (Mekanism heaters produces a LOT of heat by PneumaticCraft standards), so conversion efficiency tuning is important for inter-mod balance.")
                .translation("pneumaticcraft.config.common.integration.mek_thermal_efficiency_factor")
                .defineInRange("mek_thermal_conversion_efficiency", 0.01, 0.0, 2.0);
        integration.cofhHoldingMultiplier = builder
                .comment("Volume boost multiplier for pressurizable items with the CoFH Holding enchantment; air volume is multiplied by (1 + level_of_holding_enchantment) x this value. Set to 0 to disallow pressurizable items by enchanted with the Holding enchantment at all.")
                .translation("pneumaticcraft.config.common.integration.cofh_holding_multiplier")
                .defineInRange("cofh_holding_multiplier", 1.0, 0.0, Double.MAX_VALUE);
        builder.pop();

        builder.push("Recipes");
        recipes.explosionCrafting = builder
                .comment("Enable/disable explosion crafting (iron->compressed iron).  If you disable this, you'll need another way to get compressed iron initially. NOTE: this should be considered deprecated, and will be removed in a future release. You should control this via data pack recipes (recipe type 'pneumaticcraft:explosion_crafting').")
                .translation("pneumaticcraft.config.common.general.explosion_crafting")
                .define("explosion_crafting", true);
        recipes.coalToDiamondsRecipe = builder
                .comment("Enable crafting diamonds from coal blocks in the pressure chamber?  NOTE: this should be considered deprecated, and will be removed in a future release. You should control this via datapack recipe (default recipe ID: 'pneumaticcraft:pressure_chamber/coal_to_diamond').")
                .translation("pneumaticcraft.config.common.recipes.coal_to_diamonds")
                .define("coal_to_diamonds", true);
        recipes.inWorldPlasticSolidification = builder
                .comment("Does Molten Plastic solidify to Plastic Sheets when poured into the world? If set to false, then Heat Frame cooling is the only other default way to make Plastic Sheets.")
                .translation("pneumaticcraft.config.common.recipes.in_world_plastic_solidification")
                .define("in_world_plastic_solidification", true);
        recipes.inWorldYeastCrafting = builder
                .comment("Is in-world Yeast crafting allowed (making more Yeast Culture by pouring Water next to a Yeast Culture block with Sugar in it)? If set to false, then the default TPP Mushroom & Water -> Yeast Culture recipe is the only way to get Yeast Culture.")
                .translation("pneumaticcraft.config.common.recipes.in_world_yeast_crafting")
                .define("in_world_yeast_crafting", true);
        builder.pop();

        builder.push("Amadron");
        amadron.numPeriodicOffers = builder
                .comment("Number of periodic offers randomly selected for the 'live' offer list. Note: this a maximum, and the actual number chosen each time may be less.")
                .translation("pneumaticcraft.config.common.amadron.num_periodic_offers")
                .defineInRange("numPeriodicOffers", 10,0, Integer.MAX_VALUE);
        amadron.numVillagerOffers = builder
                .comment("Number of villager offers randomly selected for the 'live' offer list. Note: this a maximum, and the actual number chosen each time may be less.")
                .translation("pneumaticcraft.config.common.amadron.num_villager_offers")
                .defineInRange("numVillagerOffers", 20,0, Integer.MAX_VALUE);
        amadron.reshuffleInterval = builder
                .comment("Time in ticks between each periodic offer reshuffle (24000 ticks = one Minecraft day)")
                .translation("pneumaticcraft.config.common.amadron.reshuffle_interval")
                .defineInRange("reshuffleInterval", 24000,1000, Integer.MAX_VALUE);
        amadron.maxTradesPerPlayer = builder
                .comment("Max number of custom trades a player may add")
                .translation("pneumaticcraft.config.common.amadron.max_trades_per_player")
                .defineInRange("max_trades_per_player", 50, 0, Integer.MAX_VALUE);
        amadron.notifyOfTradeAddition = builder
                .comment("Broadcast a notification when player adds a custom trade")
                .translation("pneumaticcraft.config.common.amadron.notify_of_trade_addition")
                .define("notify_of_trade_addition", true);
        amadron.notifyOfTradeRemoval = builder
                .comment("Broadcast a notification when player removes a custom trade")
                .translation("pneumaticcraft.config.common.amadron.notify_of_trade_removal")
                .define("notify_of_trade_removal", true);
        amadron.notifyOfDealMade = builder
                .comment("Broadcast a notification when a custom Amadron trade is made")
                .translation("pneumaticcraft.config.common.amadron.notify_of_deal_made")
                .define("notify_of_deal_made", true);
        builder.pop();

        builder.push("Heat");
        heat.blockThermalResistance = builder
                .comment("Default thermal resistance for solid blocks")
                .translation("pneumaticcraft.config.common.blockHeatDefaults.blockThermalResistance")
                .defineInRange("blockThermalResistance", 500.0, Double.MIN_VALUE, Double.MAX_VALUE);
        heat.fluidThermalResistance = builder
                .comment("Default thermal resistance for fluid blocks")
                .translation("pneumaticcraft.config.common.blockHeatDefaults.fluidThermalResistance")
                .defineInRange("fluidThermalResistance", 100.0, Double.MIN_VALUE, Double.MAX_VALUE);
        heat.airThermalResistance = builder
                .comment("Thermal resistance of air; controls how fast blocks lose heat to air when exposed")
                .translation("pneumaticcraft.config.common.blockHeatDefaults.airThermalResistance")
                .defineInRange("airThermalResistance", 100.0, 1.0, Double.MAX_VALUE);
        heat.defaultFluidHeatCapacity = builder
                .comment("Default heat capacity for fluid blocks")
                .translation("pneumaticcraft.config.common.blockHeatDefaults.defaultFluidHeatCapacity")
                .defineInRange("defaultFluidHeatCapacity", 10000, 0, Integer.MAX_VALUE);
        heat.ambientTemperatureBiomeModifier = builder
                .comment("Ambient temperature modifier by biome (default 25 gives the Nether a heat boost of 30C)")
                .translation("pneumaticcraft.config.common.blockHeatDefaults.ambientTemperatureBiomeModifier")
                .defineInRange("ambientTemperatureBiomeModifier", 25.0, 0.0, 1000.0);
        heat.ambientTemperatureHeightModifier = builder
                .comment("Ambient temperature increase by altitude, in degrees per block below 48 (or 75% of sea level). Note that temperature decrease per block above 64 is handled by vanilla.")
                .translation("pneumaticcraft.config.common.blockHeatDefaults.ambientTemperatureHeightModifier")
                .defineInRange("ambientTemperatureHeightModifier", 0.1, 0.0, 10.0);
        heat.addDefaultFluidEntries = builder
                .comment("Automatically register heat properties for all detected modded fluids based on their self-defined temperature? (note: vanilla lava and water are always added)")
                .translation("pneumaticcraft.config.common.blockHeatDefaults.addDefaultFluidEntries")
                .define("addDefaultFluidEntries", true);
        builder.pop();

        builder.push("Logistics");
        logistics.itemTransportCost = builder
                .comment("Logistics Module air usage per item per block distance")
                .translation("pneumaticcraft.config.common.logistics.itemTransportCost")
                .defineInRange("item_transport_cost", 1.0, 0.0, Double.MAX_VALUE);
        logistics.fluidTransportCost = builder
                .comment("Logistics Module air usage per mB of fluid per block distance")
                .translation("pneumaticcraft.config.common.logistics.fluidTransportCost")
                .defineInRange("fluid_transport_cost", 0.02, 0.0, Double.MAX_VALUE);
        logistics.minPressure = builder
                .comment("Minimum pressure for a Logistics Module to function")
                .translation("pneumaticcraft.config.common.logistics.minPressure")
                .defineInRange("min_pressure", 3.0, 0.0, 20.0);
        builder.pop();

        builder.push("Jackhammer");
        jackhammer.maxVeinMinerRange = builder
                .comment("Max veinmining range (distance from mined block) for Vein Miner Plus mode")
                .translation("pneumaticcraft.config.common.jackhammer.maxVeinMinerRange")
                .defineInRange("max_vein_miner_range", 10, 1, 32);
        jackhammer.baseAirUsage = builder
                .comment("Base Jackhammer air usage per block broken (speed upgrades increase this)")
                .translation("pneumaticcraft.config.common.jackhammer.baseAirUsage")
                .defineInRange("base_air_usage", PneumaticValues.USAGE_JACKHAMMER, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Villagers");
        villagers.addMechanicHouse = builder
                .comment("Add a village house for the Pressure Mechanic? Note: setting this to false won't affect any already-generated houses, only disable new generation.")
                .translation("pneumaticcraft.config.common.villagers.add_mechanic_house")
                .worldRestart()
                .define("addMechanicHouse", true);
        villagers.whichTrades = builder
                .comment("Which trades should the Pressure Mechanic offer? ALL will offer all trades. PCB_BLUEPRINT will offer *only* the PCB Blueprint, an item required for normal progression through the mod. NONE will offer nothing (but the PCB Blueprint is also available via Amadron by default). Note that changing this won't affect any already-spawned Pressure Mechanics.")
                .translation("pneumaticcraft.config.common.villagers.mechanic_trades")
                .worldRestart()
                .defineEnum("mechanicTrades", VillagerTradesRegistration.WhichTrades.ALL);
        builder.pop();
    }
}
