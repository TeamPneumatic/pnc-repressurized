package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.Lists;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigHandler {
    public static ClientConfig client;
    public static CommonConfig server;
    private static ForgeConfigSpec configCommonSpec;
    private static ForgeConfigSpec configClientSpec;
    static ModConfig clientConfig;
	static ModConfig commonConfig;

    private static final ISubConfig[] subConfigs = new ISubConfig[] {
            new AmadronOfferSettings(),
            AmadronOfferStaticConfig.INSTANCE,
            AmadronOfferPeriodicConfig.INSTANCE,
            new ProgWidgetConfig(),
            HelmetWidgetDefaults.INSTANCE,
            ThirdPartyConfig.INSTANCE,
            MicromissileDefaults.INSTANCE,
            BlockHeatPropertiesConfig.INSTANCE,
            ArmorHUDLayout.INSTANCE
    };

    public static void init() {
        Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        configCommonSpec = commonPair.getRight();
        server = commonPair.getLeft();
        Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        configClientSpec = clientPair.getRight();
        client = clientPair.getLeft();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configCommonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configClientSpec);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigHandler::modConfig);
    }

    private static void modConfig(final ModConfig.ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == configClientSpec) {
            refreshClient(config);
        } else if (config.getSpec() == configCommonSpec) {
            refreshCommon(config);
        }
    }

    private static void refreshClient(ModConfig config) {
        clientConfig = config;

        Config.Client.aphorismDrama = client.aphorismDrama.get();
        Config.Client.fancyArmorModels = client.fancyArmorModels.get();
        Config.Client.programmerDifficulty = client.programmerDifficulty.get();
        Config.Client.topShowsFluids = client.topShowsFluids.get();
        Config.Client.logisticsGuiTint = client.logisticsGuiTint.get();
        Config.Client.dronesRenderHeldItem = client.dronesRenderHeldItem.get();
        Config.Client.semiBlockLighting = client.semiBlockLighting.get();
        Config.Client.guiBevel = client.guiBevel.get();
        Config.Client.alwaysShowPressureDurabilityBar = client.alwaysShowPressureDurabilityBar.get();
        Config.Client.tubeModuleRedstoneParticles = client.tubeModuleRedstoneParticles.get();
        Config.Client.blockTrackerMaxTimePerTick = client.blockTrackerMaxTimePerTick.get();
        Config.Client.guiRemoteGridSnap = client.guiRemoteGridSnap.get();
        Config.Client.leggingsFOVFactor = client.leggingsFOVFactor.get();
    }

    private static void refreshCommon(ModConfig config) {
        commonConfig = config;

        Config.Common.General.compressedIngotLossRate = server.general.compressedIngotLossRate.get();
        Config.Common.General.enableDroneSuffocation = server.general.enableDroneSuffocation.get();
        Config.Common.General.enableDungeonLoot = server.general.enableDungeonLoot.get();
        Config.Common.General.explosionCrafting = server.general.explosionCrafting.get();
        Config.Common.General.fuelBucketEfficiency = server.general.fuelBucketEfficiency.get();
        Config.Common.General.maxProgrammingArea = server.general.maxProgrammingArea.get();
        Config.Common.General.minFluidFuelTemperature = server.general.minFluidFuelTemperature.get();
        Config.Common.General.oilGenerationChance = server.general.oilGenerationChance.get();
        Config.Common.General.oilWorldGenBlacklist = server.general.oilWorldGenBlacklist.get();
        Config.Common.General.useUpDyesWhenColoring = server.general.useUpDyesWhenColoring.get();

        Config.Common.Machines.aerialInterfaceArmorCompat = server.machines.aerialInterfaceArmorCompat.get();
        Config.Common.Machines.cropSticksGrowthBoostChance = server.machines.cropSticksGrowthBoostChance.get();
        Config.Common.Machines.electricCompressorEfficiency = server.machines.electricCompressorEfficiency.get();
        Config.Common.Machines.electrostaticLightningChance = server.machines.electrostaticLightningChance.get();
        Config.Common.Machines.elevatorBaseBlocksPerBase = server.machines.elevatorBaseBlocksPerBase.get();
        Config.Common.Machines.fluxCompressorEfficiency = server.machines.fluxCompressorEfficiency.get();
        Config.Common.Machines.keroseneLampCanUseAnyFuel = server.machines.keroseneLampCanUseAnyFuel.get();
        Config.Common.Machines.keroseneLampFuelEfficiency = server.machines.keroseneLampFuelEfficiency.get();
        Config.Common.Machines.kineticCompressorEfficiency = server.machines.kineticCompressorEfficiency.get();
        Config.Common.Machines.liquidHopperDispenser = server.machines.liquidHopperDispenser.get();
        Config.Common.Machines.omniHopperDispenser = server.machines.omniHopperDispenser.get();
        Config.Common.Machines.plasticMixerPlasticRatio = server.machines.plasticMixerPlasticRatio.get();
        Config.Common.Machines.pneumaticDynamoEfficiency = server.machines.pneumaticDynamoEfficiency.get();
        Config.Common.Machines.pneumaticEngineEfficiency = server.machines.pneumaticEngineEfficiency.get();
        Config.Common.Machines.pneumaticGeneratorEfficiency = server.machines.pneumaticGeneratorEfficiency.get();
        Config.Common.Machines.pneumaticPumpEfficiency = server.machines.pneumaticPumpEfficiency.get();
        Config.Common.Machines.speedUpgradeSpeedMultiplier = server.machines.speedUpgradeSpeedMultiplier.get();
        Config.Common.Machines.speedUpgradeUsageMultiplier = server.machines.speedUpgradeUsageMultiplier.get();
        Config.Common.Machines.thermalCompressorThermalResistance = server.machines.thermalCompressorThermalResistance.get();

        Config.Common.Armor.jetBootsAirUsage = server.armor.jetBootsAirUsage.get();
        Config.Common.Armor.armorStartupTime = server.armor.armorStartupTime.get();

        Config.Common.Integration.ieExternalHeaterHeatPerRF = server.integration.ieExternalHeaterHeatPerRF.get();
        Config.Common.Integration.ieExternalHeaterRFperTick = server.integration.ieExternalHeaterRFperTick.get();
        Config.Common.Integration.mekHeatEfficiency = server.integration.mekHeatEfficiency.get();
        Config.Common.Integration.mekThermalResistanceMult = server.integration.mekThermalResistanceMult.get();
        Config.Common.Integration.tanAirConAirUsageMultiplier = server.integration.tanAirConAirUsageMultiplier.get();
        Config.Common.Integration.tanHeatDivider = server.integration.tanHeatDivider.get();
        Config.Common.Integration.tanRefreshInterval = server.integration.tanRefreshInterval.get();

        Config.Common.Advanced.disableKeroseneLampFakeAirBlock = server.advanced.disableKeroseneLampFakeAirBlock.get();
        Config.Common.Advanced.liquidTankUpdateThreshold = server.advanced.liquidTankUpdateThreshold.get();
        Config.Common.Advanced.stopDroneAI = server.advanced.stopDroneAI.get();
     
        Config.Common.Micromissiles.baseExplosionDamage = server.micromissiles.baseExplosionDamage.get();
        Config.Common.Micromissiles.damageTerrain = server.micromissiles.damageTerrain.get();
        Config.Common.Micromissiles.launchCooldown = server.micromissiles.launchCooldown.get();
        Config.Common.Micromissiles.lifetime = server.micromissiles.lifetime.get();
        Config.Common.Micromissiles.missilePodSize = server.micromissiles.missilePodSize.get();
     
        Config.Common.Minigun.apAmmoDamageMultiplier = server.minigun.apAmmoDamageMultiplier.get();
        Config.Common.Minigun.apAmmoIgnoreArmorChance = server.minigun.apAmmoIgnoreArmorChance.get();
        Config.Common.Minigun.armorPiercingAmmoCartridgeSize = server.minigun.armorPiercingAmmoCartridgeSize.get();
        Config.Common.Minigun.baseDamage = server.minigun.baseDamage.get();
        Config.Common.Minigun.baseRange = server.minigun.baseRange.get();
        Config.Common.Minigun.explosiveAmmoCartridgeSize = server.minigun.explosiveAmmoCartridgeSize.get();
        Config.Common.Minigun.explosiveAmmoDamageMultiplier = server.minigun.explosiveAmmoDamageMultiplier.get();
        Config.Common.Minigun.explosiveAmmoExplosionChance = server.minigun.explosiveAmmoExplosionChance.get();
        Config.Common.Minigun.explosiveAmmoExplosionPower = server.minigun.explosiveAmmoExplosionPower.get();
        Config.Common.Minigun.explosiveAmmoTerrainDamage = server.minigun.explosiveAmmoTerrainDamage.get();
        Config.Common.Minigun.freezingAmmoBlockIceChance = server.minigun.freezingAmmoBlockIceChance.get();
        Config.Common.Minigun.freezingAmmoCartridgeSize = server.minigun.freezingAmmoCartridgeSize.get();
        Config.Common.Minigun.freezingAmmoEntityIceChance = server.minigun.freezingAmmoEntityIceChance.get();
        Config.Common.Minigun.freezingAmmoFakeIceDamage = server.minigun.freezingAmmoFakeIceDamage.get();
        Config.Common.Minigun.incendiaryAmmoBlockIgniteChance = server.minigun.incendiaryAmmoBlockIgniteChance.get();
        Config.Common.Minigun.incendiaryAmmoCartridgeSize = server.minigun.incendiaryAmmoCartridgeSize.get();
        Config.Common.Minigun.incendiaryAmmoEntityIgniteChance = server.minigun.incendiaryAmmoEntityIgniteChance.get();
        Config.Common.Minigun.incendiaryAmmoFireDuration = server.minigun.incendiaryAmmoFireDuration.get();
        Config.Common.Minigun.potionProcChance = server.minigun.potionProcChance.get();
        Config.Common.Minigun.standardAmmoCartridgeSize = server.minigun.standardAmmoCartridgeSize.get();
        Config.Common.Minigun.weightedAmmoAirUsageMultiplier = server.minigun.weightedAmmoAirUsageMultiplier.get();
        Config.Common.Minigun.weightedAmmoCartridgeSize = server.minigun.weightedAmmoCartridgeSize.get();
        Config.Common.Minigun.weightedAmmoDamageMultiplier = server.minigun.weightedAmmoDamageMultiplier.get();
        Config.Common.Minigun.weightedAmmoRangeMultiplier = server.minigun.weightedAmmoRangeMultiplier.get();
    }

    public static class ClientConfig {
        public ForgeConfigSpec.BooleanValue aphorismDrama;
        public ForgeConfigSpec.BooleanValue fancyArmorModels;
        public ForgeConfigSpec.EnumValue<GuiProgrammer.Difficulty> programmerDifficulty;
        public ForgeConfigSpec.BooleanValue topShowsFluids;
        public ForgeConfigSpec.BooleanValue logisticsGuiTint;
        public ForgeConfigSpec.BooleanValue dronesRenderHeldItem;
        public ForgeConfigSpec.BooleanValue semiBlockLighting;
        public ForgeConfigSpec.BooleanValue guiBevel;
        public ForgeConfigSpec.BooleanValue alwaysShowPressureDurabilityBar;
        public ForgeConfigSpec.BooleanValue tubeModuleRedstoneParticles;
        public ForgeConfigSpec.IntValue blockTrackerMaxTimePerTick;
        public ForgeConfigSpec.DoubleValue leggingsFOVFactor;
        public ForgeConfigSpec.BooleanValue guiRemoteGridSnap;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            aphorismDrama = builder
                    .comment("Enable Aphorism Tile Drama!  http://mc-drama.herokuapp.com/")
                    .translation("pneumaticcraft.config.client.general.aphorism_drama")
                    .define("aphorism_drama", true);
            fancyArmorModels = builder
                    .comment("Use fancy models for Pneumatic Armor (currently unimplemented)")
                    .translation("pneumaticcraft.config.client.general.fancy_armor_models")
                    .define("fancy_armor_models", true);
            programmerDifficulty = builder
                    .comment("Defines which widgets are shown in the Programmer GUI: easy, medium, or advanced")
                    .translation("pneumaticcraft.config.client.general.fancy_armor_models")
                    .defineEnum("programmer_difficulty", GuiProgrammer.Difficulty.EASY);
            topShowsFluids = builder
                    .comment("Show tank fluids with the The One Probe. Note that TOP also has support for showing tanks, which may or may not be enabled.")
                    .translation("pneumaticcraft.config.client.general.top_shows_fluids")
                    .define("top_shows_fluids", true);
            logisticsGuiTint = builder
                    .comment("Tint Logistics configuration GUI backgrounds according to the colour of the logistics frame you are configuring.")
                    .translation("pneumaticcraft.config.client.general.logistics_gui_tint")
                    .define("logistics_gui_tint", true);
            dronesRenderHeldItem = builder
                    .comment("Drones render their held item (the item in slot 0 of their inventory) ?")
                    .translation("pneumaticcraft.config.client.general.drones_render_held_item")
                    .define("drones_render_held_item", true);
            semiBlockLighting = builder
                    .comment("Use block lighting for semiblocks (logistics frames, heat frames...). May cause occasional lighting issues - semiblocks appearing unlit - disable this if that's a problem.")
                    .translation("pneumaticcraft.config.client.general.semi_block_lighting")
                    .define("semi_block_lighting", true);
            guiBevel = builder
                    .comment("Should GUI side tabs be shown with a beveled edge? Setting to false uses a plain black edge, as in earlier versions of the mod.")
                    .translation("pneumaticcraft.config.client.general.gui_bevel")
                    .define("gui_bevel", true);
            alwaysShowPressureDurabilityBar = builder
                    .comment("Always show the pressure durability bar for pressurizable items, even when full?")
                    .translation("pneumaticcraft.config.client.general.always_show_pressure_durability_bar")
                    .define("always_show_pressure_durability_bar", true);
            tubeModuleRedstoneParticles = builder
                    .comment("Should tube modules emit redstone play redstone particle effects when active?")
                    .translation("pneumaticcraft.config.client.general.tube_module_redstone_particles")
                    .define("tube_module_redstone_particles", true);
            blockTrackerMaxTimePerTick = builder
                    .comment("Maximum time, as a percentage of the tick, that the Pneumatic Helmet Block Tracker may take when active and scanning blocks. Larger values mean more rapid update of block information, but potentially greater impact on client FPS.")
                    .translation("pneumaticcraft.config.client.general.block_tracker_max_time_per_tick")
                    .defineInRange("block_tracker_max_time_per_tick", 10, 1, 100);
            leggingsFOVFactor = builder
                    .comment("Intensity of the FOV modification when using Pneumatic Leggings speed boost: 0.0 for no FOV modification, higher values zoom out more.  Note: non-zero values may cause FOV clashes with other mods.")
                    .translation("pneumaticcraft.config.client.general.leggings_fov_factor")
                    .defineInRange("leggings_fov_factor", 0.0, 0.0, 1.0);
            guiRemoteGridSnap = builder
                    .comment("Should widgets in the GUI Remote Editor be snapped to a 4x4 grid?")
                    .translation("pneumaticcraft.config.client.general.gui_remote_grid_snap")
                    .define("gui_remote_grid_snap", true);
            builder.pop();
        }
    }

    static class CommonConfig {
        public class General {
            public ForgeConfigSpec.DoubleValue oilGenerationChance;
            public ForgeConfigSpec.IntValue compressedIngotLossRate;
            public ForgeConfigSpec.BooleanValue enableDungeonLoot;
            public ForgeConfigSpec.BooleanValue enableDroneSuffocation;
            public ForgeConfigSpec.DoubleValue fuelBucketEfficiency;
            public ForgeConfigSpec.IntValue maxProgrammingArea;
            public ForgeConfigSpec.BooleanValue explosionCrafting;
            public ForgeConfigSpec.ConfigValue<List<Integer>> oilWorldGenBlacklist;
            public ForgeConfigSpec.IntValue minFluidFuelTemperature;
            public ForgeConfigSpec.BooleanValue useUpDyesWhenColoring;
        }
        public class Machines {
            public ForgeConfigSpec.BooleanValue aerialInterfaceArmorCompat;
            public ForgeConfigSpec.DoubleValue cropSticksGrowthBoostChance;
            public ForgeConfigSpec.IntValue electricCompressorEfficiency;
            public ForgeConfigSpec.IntValue electrostaticLightningChance;
            public ForgeConfigSpec.IntValue elevatorBaseBlocksPerBase;
            public ForgeConfigSpec.IntValue fluxCompressorEfficiency;
            public ForgeConfigSpec.BooleanValue keroseneLampCanUseAnyFuel;
            public ForgeConfigSpec.DoubleValue keroseneLampFuelEfficiency;
            public ForgeConfigSpec.IntValue kineticCompressorEfficiency;
            public ForgeConfigSpec.BooleanValue liquidHopperDispenser;
            public ForgeConfigSpec.BooleanValue omniHopperDispenser;
            public ForgeConfigSpec.IntValue plasticMixerPlasticRatio;
            public ForgeConfigSpec.IntValue pneumaticDynamoEfficiency;
            public ForgeConfigSpec.IntValue pneumaticEngineEfficiency;
            public ForgeConfigSpec.IntValue pneumaticGeneratorEfficiency;
            public ForgeConfigSpec.IntValue pneumaticPumpEfficiency;
            public ForgeConfigSpec.DoubleValue speedUpgradeSpeedMultiplier;
            public ForgeConfigSpec.DoubleValue speedUpgradeUsageMultiplier;
            public ForgeConfigSpec.DoubleValue thermalCompressorThermalResistance;
        }
        public class Armor {
            public ForgeConfigSpec.IntValue jetBootsAirUsage;
            public ForgeConfigSpec.IntValue armorStartupTime;
        }
        public class Integration {
            public ForgeConfigSpec.DoubleValue ieExternalHeaterHeatPerRF;
            public ForgeConfigSpec.IntValue ieExternalHeaterRFperTick;
            public ForgeConfigSpec.DoubleValue mekHeatEfficiency;
            public ForgeConfigSpec.DoubleValue mekThermalResistanceMult;
            public ForgeConfigSpec.DoubleValue tanAirConAirUsageMultiplier;
            public ForgeConfigSpec.DoubleValue tanHeatDivider;
            public ForgeConfigSpec.IntValue tanRefreshInterval;
        }
        public class Advanced {
            public ForgeConfigSpec.BooleanValue disableKeroseneLampFakeAirBlock;
            public ForgeConfigSpec.DoubleValue liquidTankUpdateThreshold;
            public ForgeConfigSpec.BooleanValue stopDroneAI;
        }
        public class Micromissiles {
            public ForgeConfigSpec.DoubleValue baseExplosionDamage;
            public ForgeConfigSpec.BooleanValue damageTerrain;
            public ForgeConfigSpec.IntValue launchCooldown;
            public ForgeConfigSpec.IntValue lifetime;
            public ForgeConfigSpec.IntValue missilePodSize;
        }
        public class Minigun {
            public ForgeConfigSpec.DoubleValue apAmmoDamageMultiplier;
            public ForgeConfigSpec.IntValue apAmmoIgnoreArmorChance;
            public ForgeConfigSpec.IntValue armorPiercingAmmoCartridgeSize;
            public ForgeConfigSpec.DoubleValue baseDamage;
            public ForgeConfigSpec.IntValue baseRange;
            public ForgeConfigSpec.IntValue explosiveAmmoCartridgeSize;
            public ForgeConfigSpec.DoubleValue explosiveAmmoDamageMultiplier;
            public ForgeConfigSpec.IntValue explosiveAmmoExplosionChance;
            public ForgeConfigSpec.DoubleValue explosiveAmmoExplosionPower;
            public ForgeConfigSpec.BooleanValue explosiveAmmoTerrainDamage;
            public ForgeConfigSpec.IntValue freezingAmmoBlockIceChance;
            public ForgeConfigSpec.IntValue freezingAmmoCartridgeSize;
            public ForgeConfigSpec.IntValue freezingAmmoEntityIceChance;
            public ForgeConfigSpec.DoubleValue freezingAmmoFakeIceDamage;
            public ForgeConfigSpec.IntValue incendiaryAmmoBlockIgniteChance;
            public ForgeConfigSpec.IntValue incendiaryAmmoCartridgeSize;
            public ForgeConfigSpec.IntValue incendiaryAmmoEntityIgniteChance;
            public ForgeConfigSpec.IntValue incendiaryAmmoFireDuration;
            public ForgeConfigSpec.IntValue potionProcChance;
            public ForgeConfigSpec.IntValue standardAmmoCartridgeSize;
            public ForgeConfigSpec.DoubleValue weightedAmmoAirUsageMultiplier;
            public ForgeConfigSpec.IntValue weightedAmmoCartridgeSize;
            public ForgeConfigSpec.DoubleValue weightedAmmoDamageMultiplier;
            public ForgeConfigSpec.DoubleValue weightedAmmoRangeMultiplier;
        }

        public final General general = new General();
        public final Machines machines = new Machines();
        public final Armor armor = new Armor();
        public final Advanced advanced = new Advanced();
        public final Integration integration = new Integration();
        public final Micromissiles micromissiles = new Micromissiles();
        public final Minigun minigun = new Minigun();

        public CommonConfig(ForgeConfigSpec.Builder builder) {

            builder.push("General");
            general.oilGenerationChance = builder
                    .worldRestart()
                    .comment("Chance per chunk in percentage to generate an Oil Lake. Set to 0 for no spawns")
                    .translation("pneumaticcraft.config.server.general.oilGenerationChance")
                    .defineInRange("oil_generation_chance", 15.0, 0.0, 100.0);
            general.compressedIngotLossRate = builder
                    .comment("Loss percentage (on average) of Compressed Iron ingots/blocks when exposed to an explosion.")
                    .translation("pneumaticcraft.config.server.general.compressedIngotLossRate")
                    .defineInRange("compressedIngotLossRate", 20, 0, 100);
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
            general.explosionCrafting = builder
                    .comment("Enable/disable explosion crafting (iron->compressed iron).  If you disable this, you'll need another way to get compressed iron initially.")
                    .translation("pneumaticcraft.config.server.general.explosion_crafting")
                    .define("explosion_crafting", true);
            general.oilWorldGenBlacklist = builder
                    .worldRestart()
                    .comment("Oil worldgen blacklist: add dimension IDs to this list if you don't want oil worldgen to happen there.")
                    .translation("pneumaticcraft.config.server.general.oil_world_gen_blacklist")
                    .define("oil_world_gen_blacklist", Lists.newArrayList(-1, 1));
            general.minFluidFuelTemperature = builder
                    .worldRestart()
                    .comment("Fluids as hot or hotter than this temperature (Kelvin) will be auto-registered as Liquid Compressor fuels, the quality being dependent on fluid temperature.")
                    .translation("pneumaticcraft.config.server.general.min_fluid_fuel_temperature")
                    .defineInRange("min_fluid_fuel_temperature", 373, 0, Integer.MAX_VALUE);
            general.useUpDyesWhenColoring = builder
                    .comment("Should dyes be used when coloring things (Drones, Logistics Modules, Redstone Modules)?")
                    .translation("pneumaticcraft.config.server.general.use_up_dyes_when_coloring")
                    .define("use_up_dyes_when_coloring", false);
            builder.pop();

            builder.push("Machine Properties");
            machines.aerialInterfaceArmorCompat = builder
                    .comment("Aerial Interface backwards compat: allow pre-0.8.0 behaviour of getting player's armor inventory from top face, even with Dispenser Upgrade installed")
                    .translation("pneumaticcraft.config.server.machine_properties.aerial_interface_armor_compat")
                    .define("pneumaticcraft.config.server.machine_properties.aerial_interface_armor_compat", true);
            machines.cropSticksGrowthBoostChance = builder
                    .comment("Chance per tick of Crop Supports causing a growth tick. The default, 0.002, is roughly 2.5 times faster than the vanilla growth rate")
                    .translation("pneumaticcraft.config.server.machine_properties.crop_sticks_growth_boost_chance")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.crop_sticks_growth_boost_chance", 0.002, 0, Double.MAX_VALUE);
            machines.electricCompressorEfficiency = builder
                    .comment("Changing this value will alter the pressurized air production of the Electric Compressor. The input, EU, will stay the same")
                    .translation("pneumaticcraft.config.server.machine_properties.electric_compressor_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.electric_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
            machines.electrostaticLightningChance = builder
                    .comment("Base chance (1/x) per tick of a lightning strike on/around the Electrostatic Generator")
                    .translation("pneumaticcraft.config.server.machine_properties.electrostatic_lightning_chance")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.electrostatic_lightning_chance", 100000, 0, Integer.MAX_VALUE);
            machines.elevatorBaseBlocksPerBase = builder
                    .comment("The max height of an elevator per stacked Elevator Base.")
                    .translation("pneumaticcraft.config.server.machine_properties.elevator_base_blocks_per_base")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.elevator_base_blocks_per_base", 4, 1, 256);
            machines.fluxCompressorEfficiency = builder
                    .comment("Changing this value will alter the pressurized air production of the Flux Compressor. The input, RF, will stay the same")
                    .translation("pneumaticcraft.config.server.machine_properties.flux_compressor_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.flux_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
            machines.keroseneLampCanUseAnyFuel = builder
                    .comment("Can the Kerosene Lamp burn any kind of fuel?  If false, only Kerosene can be burnt")
                    .translation("pneumaticcraft.config.server.machine_properties.kerosene_lamp_can_use_any_fuel")
                    .define("pneumaticcraft.config.server.machine_properties.kerosene_lamp_can_use_any_fuel", true);
            machines.keroseneLampFuelEfficiency = builder
                    .comment("Kerosene Lamp fuel efficiency: higher values mean fuel will last longer in the lamp")
                    .translation("pneumaticcraft.config.server.machine_properties.kerosene_lamp_fuel_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.kerosene_lamp_fuel_efficiency", 1.0, 0, Double.MAX_VALUE);
            machines.kineticCompressorEfficiency = builder
                    .comment("Changing this value will alter the pressurized air production of the Kinetic Compressor. The input, MJ, will stay the same")
                    .translation("pneumaticcraft.config.server.machine_properties.kinetic_compressor_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.kinetic_compressor_efficiency", 40, 0, Integer.MAX_VALUE);
            machines.liquidHopperDispenser = builder
                    .comment("Can the Liquid Hopper absorb/dispense fluids into the world with a Dispenser Upgrade?")
                    .translation("pneumaticcraft.config.server.machine_properties.liquid_hopper_dispenser")
                    .define("pneumaticcraft.config.server.machine_properties.liquid_hopper_dispenser", true);
            machines.omniHopperDispenser = builder
                    .comment("Can the Omnidirectional Hopper dispense items into the world with a Dispenser Upgrade?")
                    .translation("pneumaticcraft.config.server.machine_properties.omni_hopper_dispenser")
                    .define("pneumaticcraft.config.server.machine_properties.omni_hopper_dispenser", true);
            machines.plasticMixerPlasticRatio = builder
                    .comment("The ratio of liquid plastic to solid plastic sheets in the Plastic Mixer, in mB per sheet.  If set to 0, no default liquid->solid plastic recipe will be added (but CraftTweaker or API can be used to add recipes)")
                    .translation("pneumaticcraft.config.server.machine_properties.plastic_mixer_plastic_ratio")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.plastic_mixer_plastic_ratio", 1000, 0, Integer.MAX_VALUE);
            machines.pneumaticDynamoEfficiency = builder
                    .comment("Changing this value will alter the pressurized air usage of the Pneumatic Dynamo. The output, RF, will stay the same")
                    .translation("pneumaticcraft.config.server.machine_properties.pneumatic_dynamo_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.pneumatic_dynamo_efficiency", 40, 0, Integer.MAX_VALUE);
            machines.pneumaticEngineEfficiency = builder
                    .comment("Changing this value will alter the pressurized air usage of the Pneumatic Engine. The output, MJ, will stay the same")
                    .translation("pneumaticcraft.config.server.machine_properties.pneumatic_engine_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.pneumatic_engine_efficiency", 40, 0, Integer.MAX_VALUE);
            machines.pneumaticGeneratorEfficiency = builder
                    .comment("Changing this value will alter the pressurized air usage of the Pneumatic Generator. The output, EU, will stay the same.")
                    .translation("pneumaticcraft.config.server.machine_properties.pneumatic_generator_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.pneumatic_generator_efficiency", 40, 0, Integer.MAX_VALUE);
            machines.pneumaticPumpEfficiency = builder
                    .comment("Changing this value will alter the hydraulic bar production of the Pneumatic Pump. The input, air, will stay the same")
                    .translation("pneumaticcraft.config.server.machine_properties.pneumatic_pump_efficiency")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.pneumatic_pump_efficiency", 40, 0, Integer.MAX_VALUE);
            machines.speedUpgradeSpeedMultiplier = builder
                    .comment("Speed multiplier per speed upgrade: speed mult = speedUpgradeSpeedMultiplier ^ num_of_speed_upgrades")
                    .translation("pneumaticcraft.config.server.machine_properties.speed_upgrade_speed_multiplier")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.speed_upgrade_speed_multiplier", PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER, 1.0, 2.0);
            machines.speedUpgradeUsageMultiplier = builder
                    .comment("Fuel usage / heat gen multiplier per speed upgrade: usage mult = speedUpgradeUsageMultiplier ^ num_of_speed_upgrades")
                    .translation("pneumaticcraft.config.server.machine_properties.speed_upgrade_usage_multiplier")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.speed_upgrade_usage_multiplier", PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER, 1.0, 2.0);
            machines.thermalCompressorThermalResistance = builder
                    .comment("Thermal resistance between opposite faces of the Thermal Compressor. Higher values means heat leaks across (equalizes) more slowly, making the compressor better at converting heat differential to pressure.")
                    .translation("pneumaticcraft.config.server.machine_properties.thermal_compressor_thermal_resistance")
                    .defineInRange("pneumaticcraft.config.server.machine_properties.thermal_compressor_thermal_resistance", 150, 0, Double.MAX_VALUE);
            builder.pop();

            builder.push("Pneumatic Armor");
            armor.jetBootsAirUsage = builder
                    .comment("Jetboots air usage in mL/tick (per Jet Boots Upgrade)")
                    .translation("pneumaticcraft.config.server.armor.jet_boots_air_usage")
                    .defineInRange("jet_boots_air_usage", PneumaticValues.PNEUMATIC_JET_BOOTS_USAGE, 0, Integer.MAX_VALUE);
            armor.armorStartupTime = builder
                    .comment("Armor base startup time in ticks (before Speed Upgrades)")
                    .translation("pneumaticcraft.config.server.armor.armor_startup_time")
                    .defineInRange("armor_startup_time", 200, 20, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Advanced");
            advanced.disableKeroseneLampFakeAirBlock = builder
                    .comment("When set to true, the Kerosene Lamp's fake air blocks won't be registered and therefore removed from the world. Useful if this causes trouble (it shouldn't though)")
                    .translation("pneumaticcraft.config.server.advanced.disable_kerosene_lamp_fake_air_block")
                    .define("pneumaticcraft.config.server.advanced.disable_kerosene_lamp_fake_air_block", false);
            advanced.liquidTankUpdateThreshold = builder
                    .comment("The amount by which any liquid tank's contents must change, as a proportion of the tank's total capacity, to trigger an update to clients. Larger values mean fewer updates but less granularity in client-side fluid rendering.")
                    .translation("pneumaticcraft.config.server.advanced.liquid_tank_update_threshold")
                    .defineInRange("pneumaticcraft.config.server.advanced.liquid_tank_update_threshold", 0.01, 0.0001, 1);
            advanced.stopDroneAI = builder
                    .comment("When set to true, Drones will not execute any program. This is useful to set to true when due to a bug Drones are lagging your server or crashing it. Please report the bug if you encounter it.")
                    .translation("pneumaticcraft.config.server.advanced.stop_drone_ai")
                    .define("pneumaticcraft.config.server.advanced.stop_drone_ai", false);
            builder.pop();

            builder.push("Micromissile Properties");
            micromissiles.baseExplosionDamage = builder
                    .comment("Base explosion damage (modified by missile setup)")
                    .translation("pneumaticcraft.config.server.micromissile_properties.base_explosion_damage")
                    .defineInRange("pneumaticcraft.config.server.micromissile_properties.base_explosion_damage", 1, 0, Double.MAX_VALUE);
            micromissiles.damageTerrain = builder
                    .comment("Do micromissile explosions cause terrain damage?")
                    .translation("pneumaticcraft.config.server.micromissile_properties.damage_terrain")
                    .define("pneumaticcraft.config.server.micromissile_properties.damage_terrain", false);
            micromissiles.launchCooldown = builder
                    .comment("Cooldown for missile firing in ticks")
                    .translation("pneumaticcraft.config.server.micromissile_properties.launch_cooldown")
                    .defineInRange("pneumaticcraft.config.server.micromissile_properties.launch_cooldown", 15, 0, Integer.MAX_VALUE);
            micromissiles.lifetime = builder
                    .comment("Base missile lifetime in ticks (modified by missile setup)")
                    .translation("pneumaticcraft.config.server.micromissile_properties.lifetime")
                    .defineInRange("pneumaticcraft.config.server.micromissile_properties.lifetime", 300, 0, Integer.MAX_VALUE);
            micromissiles.missilePodSize = builder
                    .comment("Number of micromissiles per pod")
                    .translation("pneumaticcraft.config.server.micromissile_properties.missile_pod_size")
                    .defineInRange("pneumaticcraft.config.server.micromissile_properties.missile_pod_size", 100, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Minigun Properties");
            minigun.apAmmoDamageMultiplier = builder
                    .comment("Armor Piercing Ammo damage multiplier (relative to standard ammo)")
                    .translation("pneumaticcraft.config.server.minigun_properties.ap_ammo_damage_multiplier")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.ap_ammo_damage_multiplier", 1.25, 0, Double.MAX_VALUE);
            minigun.apAmmoIgnoreArmorChance = builder
                    .comment("Armor Piercing Ammo percentage chance to ignore target's armor")
                    .translation("pneumaticcraft.config.server.minigun_properties.ap_ammo_ignore_armor_chance")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.ap_ammo_ignore_armor_chance", 100, 1, 100);
            minigun.armorPiercingAmmoCartridgeSize = builder
                    .comment("Armor Piercing Ammo cartridge size")
                    .translation("pneumaticcraft.config.server.minigun_properties.armor_piercing_ammo_cartridge_size")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.armor_piercing_ammo_cartridge_size", 250, 1, 30000);
            minigun.baseDamage = builder
                    .comment("Base bullet damage of the Sentry Gun, Handheld Minigun, and Drone Minigun, before ammo bonuses are considered")
                    .translation("pneumaticcraft.config.server.minigun_properties.base_damage")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.base_damage", 4, 0, Double.MAX_VALUE);
            minigun.baseRange = builder
                    .comment("Base range of Minigun, before Range Upgrades are considered")
                    .translation("pneumaticcraft.config.server.minigun_properties.base_range")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.base_range", 50, 5, 100);
            minigun.explosiveAmmoCartridgeSize = builder
                    .comment("Explosive Ammo cartridge size")
                    .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_cartridge_size")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.explosive_ammo_cartridge_size", 125, 1, 30000);
            minigun.explosiveAmmoDamageMultiplier = builder
                    .comment("Minigun Explosive Ammo damage multiplier (relative to standard ammo)")
                    .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_damage_multiplier")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.explosive_ammo_damage_multiplier", 0.2, 0, Double.MAX_VALUE);
            minigun.explosiveAmmoExplosionChance = builder
                    .comment("Explosive Ammo base percentage chance to cause an explosion")
                    .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_explosion_chance")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.explosive_ammo_explosion_chance", 50, 0, Integer.MAX_VALUE);
            minigun.explosiveAmmoExplosionPower = builder
                    .comment("Minigun Explosive Ammo explosion power (ref: 2 = creeper, 4 = TNT")
                    .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_explosion_power")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.explosive_ammo_explosion_power", 1.5, 0, Double.MAX_VALUE);
            minigun.explosiveAmmoTerrainDamage = builder
                    .comment("Does Minigun Explosive Ammo damage terrain?")
                    .translation("pneumaticcraft.config.server.minigun_properties.explosive_ammo_terrain_damage")
                    .define("pneumaticcraft.config.server.minigun_properties.explosive_ammo_terrain_damage", false);
            minigun.freezingAmmoBlockIceChance = builder
                    .comment("Freezing Ammo base percentage chance to form ice or snow on blocks which have been hit")
                    .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_block_ice_chance")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.freezing_ammo_block_ice_chance", 20, 0, 100);
            minigun.freezingAmmoCartridgeSize = builder
                    .comment("Freezing Ammo cartridge size")
                    .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_cartridge_size")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.freezing_ammo_cartridge_size", 500, 0, Integer.MAX_VALUE);
            minigun.freezingAmmoEntityIceChance = builder
                    .comment("Freezing Ammo base percentage chance to form ice on entities which have been hit")
                    .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_entity_ice_chance")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.freezing_ammo_entity_ice_chance", 20, 0, 100);
            minigun.freezingAmmoFakeIceDamage = builder
                    .comment("Damage done to entities within the fake 'ice' blocks cause by freezing ammo")
                    .translation("pneumaticcraft.config.server.minigun_properties.freezing_ammo_fake_ice_damage")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.freezing_ammo_fake_ice_damage", 1, 0, Double.MAX_VALUE);
            minigun.incendiaryAmmoBlockIgniteChance = builder
                    .comment("Incendiary ammo base percentage chance to ignite blocks")
                    .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_block_ignite_chance")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_block_ignite_chance", 20, 1, 100);
            minigun.incendiaryAmmoCartridgeSize = builder
                    .comment("Incendiary Ammo cartridge size")
                    .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_cartridge_size")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_cartridge_size", 500, 1, 30000);
            minigun.incendiaryAmmoEntityIgniteChance = builder
                    .comment("Incendiary ammo base percentage chance to ignite entities")
                    .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_entity_ignite_chance")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_entity_ignite_chance", 100, 1, 100);
            minigun.incendiaryAmmoFireDuration = builder
                    .comment("Incendiary ammo fire duration on target entities (seconds)")
                    .translation("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_fire_duration")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.incendiary_ammo_fire_duration", 8, 0, Integer.MAX_VALUE);
            minigun.potionProcChance = builder
                    .comment("Percentage chance per shot of potion-tipped ammo proc'ing the potion effect, before Dispenser Upgrades are considered")
                    .translation("pneumaticcraft.config.server.minigun_properties.potion_proc_chance")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.potion_proc_chance", 7, 1, 100);
            minigun.standardAmmoCartridgeSize = builder
                    .comment("Standard Ammo cartridge size")
                    .translation("pneumaticcraft.config.server.minigun_properties.standard_ammo_cartridge_size")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.standard_ammo_cartridge_size", 1000, 1, 30000);
            minigun.weightedAmmoAirUsageMultiplier = builder
                    .comment("Weighted Ammo air usage multiplier (relative to standard ammo)")
                    .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_air_usage_multiplier")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.weighted_ammo_air_usage_multiplier", 8.0, 0, Double.MAX_VALUE);
            minigun.weightedAmmoCartridgeSize = builder
                    .comment("Weighted Ammo cartridge size")
                    .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_cartridge_size")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.weighted_ammo_cartridge_size", 250, 1, 30000);
            minigun.weightedAmmoDamageMultiplier = builder
                    .comment("Weighted Ammo damage multiplier (relative to standard ammo)")
                    .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_damage_multiplier")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.weighted_ammo_damage_multiplier", 2.5, 0, Double.MAX_VALUE);
            minigun.weightedAmmoRangeMultiplier = builder
                    .comment("Weighted Ammo range multiplier (relative to standard ammo)")
                    .translation("pneumaticcraft.config.server.minigun_properties.weighted_ammo_range_multiplier")
                    .defineInRange("pneumaticcraft.config.server.minigun_properties.weighted_ammo_range_multiplier", 0.2, 0, Double.MAX_VALUE);
            builder.pop();

            builder.push("Integration");
            integration.ieExternalHeaterHeatPerRF = builder
                    .comment("Immersive Engineering: External Heater heat/RF.  The amount of PneumaticCraft heat added by 1 RF.")
                    .translation("pneumaticcraft.config.server.integration.ie_external_heater_heat_per_rf")
                    .defineInRange("pneumaticcraft.config.server.integration.ie_external_heater_heat_per_rf", 0.01, 0.0, Double.MAX_VALUE);
            integration.ieExternalHeaterRFperTick = builder
                    .comment("Immersive Engineering: External Heater RF/t. Set to 0 to disable External Heater integration entirely.")
                    .translation("pneumaticcraft.config.server.integration.ie_external_heater_r_fper_tick")
                    .defineInRange("pneumaticcraft.config.server.integration.ie_external_heater_r_fper_tick", 100, 0, Integer.MAX_VALUE);
            integration.mekHeatEfficiency = builder
                    .comment("Mekanism heat conversion efficiency.  Smaller values mean Mekanism heat is worth less PneumaticCraft heat. Set to 0.0 to disable Mekanism heat integration entirely.")
                    .translation("pneumaticcraft.config.server.integration.mek_heat_efficiency")
                    .defineInRange("pneumaticcraft.config.server.integration.mek_heat_efficiency", 0.4, 0.0, Double.MAX_VALUE);
            integration.mekThermalResistanceMult = builder
                    .comment("Mekanism thermal resistance multiplier. Larger values mean slower heat transfer.")
                    .translation("pneumaticcraft.config.server.integration.mek_thermal_resistance_mult")
                    .defineInRange("pneumaticcraft.config.server.integration.mek_thermal_resistance_mult", 100.0, 1.0, Double.MAX_VALUE);
            integration.tanAirConAirUsageMultiplier = builder
                    .comment("ToughAsNails: air usage multiplier for the Pneumatic Chestplate Air Conditioning Upgrade.")
                    .translation("pneumaticcraft.config.server.integration.tan_air_con_air_usage_multiplier")
                    .defineInRange("pneumaticcraft.config.server.integration.tan_air_con_air_usage_multiplier", 1.5, 0.0, Double.MAX_VALUE);
            integration.tanHeatDivider = builder
                    .comment("Tough As Nails temperature divider; smaller values make PneumaticCraft heat sources have a more pronounced effect on your temperature. Set to 0 to ignore PneumaticCraft heat sources.")
                    .translation("pneumaticcraft.config.server.integration.tan_heat_divider")
                    .defineInRange("pneumaticcraft.config.server.integration.tan_heat_divider", 10.0, 1.0f, Double.MAX_VALUE);
            integration.tanRefreshInterval = builder
                    .comment("Interval in ticks with which to refresh heat information from PneumaticCraft heat sources to Tough As Nails. A larger interval is kinder to the server but will provide less precise temperature data to TAN.")
                    .translation("pneumaticcraft.config.server.integration.tan_refresh_interval")
                    .defineInRange("pneumaticcraft.config.server.integration.tan_refresh_interval", 40, 1, 200);
            builder.pop();

        }
    }

    public static void onPreInit(File configFile) {
        PneumaticCraftRepressurized.proxy.initConfig();

        for(ISubConfig subConfig : subConfigs) {
            File subFolder = new File(configFile.getAbsolutePath().substring(0, configFile.getAbsolutePath().length() - 4) + File.separator);
            if (subFolder.exists() || subFolder.mkdirs()) {
                File subFile = new File(subFolder, subConfig.getConfigFilename() + ".cfg");
                try {
                    subConfig.preInit(subFile);
                } catch(IOException e) {
                    PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onPostInit() {
        for(ISubConfig subConfig : subConfigs) {
            try {
                subConfig.postInit();
            } catch(IOException e) {
                PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                e.printStackTrace();
            } catch (ClassCastException e) {
                PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                e.printStackTrace();
            }
        }
    }

    public static void setProgrammerDifficulty(int difficulty) {
        client.programmerDifficulty = difficulty;
        sync();
    }

    public static int getProgrammerDifficulty() {
        return client.programmerDifficulty = server.advanced.programmerDifficulty.get();
    }

    @Mod.EventBusSubscriber
    public static class ConfigSyncHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(Names.MOD_ID)) {
                ConfigManager.sync(Names.MOD_ID, Config.Type.INSTANCE);
                PneumaticCraftRepressurized.logger.info("Configuration has been saved.");
            }
        }

    }

}
