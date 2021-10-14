package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.villages.VillagerTradesRegistration;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Set;

/**
 * Holds "baked" config values which can be freely accessed, but any modifications will not be persisted.
 */
public class PNCConfig {
    public static class Client {
        public static boolean aphorismDrama;
        public static WidgetDifficulty programmerDifficulty;
        public static boolean topShowsFluids;
        public static boolean logisticsGuiTint;
        public static boolean guiBevel;
        public static boolean alwaysShowPressureDurabilityBar;
        public static boolean tubeModuleRedstoneParticles;
        public static boolean guiRemoteGridSnap;
        public static boolean programmerGuiPauses;
        public static boolean notifyAmadronOfferUpdates;

        public static class Armor {
            public static int blockTrackerMaxTimePerTick;
            public static boolean fancyArmorModels;
            public static double leggingsFOVFactor;
            public static boolean pathEnabled;
            public static boolean wirePath;
            public static boolean xRayEnabled;
            public static ClientConfig.PathUpdateSetting pathUpdateSetting;
            public static boolean showPressureNumerically;
            public static boolean showEnchantGlint;
        }

        public static class Sound {
            public static double elevatorVolumeRunning;
            public static double elevatorVolumeStartStop;
            public static double airLeakVolume;
            public static double minigunVolumeHeld;
            public static double minigunVolumeDrone;
            public static double minigunVolumeSentryTurret;
            public static double jetbootsVolume;
            public static double jetbootsVolumeBuilderMode;
            public static double jackhammerVolume;
        }
    }

    public static class Common {
        public static class General {
            public static int oilGenerationChance;
            public static boolean enableDungeonLoot;
            public static boolean enableDroneSuffocation;
            public static double fuelBucketEfficiency;
            public static int maxProgrammingArea;
            public static Set<ResourceLocation> oilWorldGenBlacklist;
            public static int minFluidFuelTemperature;
            public static boolean useUpDyesWhenColoring;
            public static boolean dronesRenderHeldItem;
            public static boolean dronesCanImportXPOrbs;
            public static Set<String> vacuumTrapBlacklist;
            public static Set<String> oilWorldGenCategoryBlacklist;
            public static Set<String> oilWorldGenDimensionBlacklist;
            public static int surfaceOilGenerationChance;
            public static boolean droneDebuggerPathParticles;
            public static boolean dronesCanBePickedUp;
        }

        public static class Machines {
            public static boolean aerialInterfaceArmorCompat;
            public static double cropSticksGrowthBoostChance;
            public static int electricCompressorEfficiency;
            public static int electrostaticLightningChance;
            public static int elevatorBaseBlocksPerBase;
            public static int fluxCompressorEfficiency;
            public static boolean keroseneLampCanUseAnyFuel;
            public static double keroseneLampFuelEfficiency;
            public static int kineticCompressorEfficiency;
            public static boolean liquidHopperDispenser;
            public static boolean omniHopperDispenser;
            public static int pneumaticDynamoEfficiency;
            public static int pneumaticEngineEfficiency;
            public static int pneumaticGeneratorEfficiency;
            public static int pneumaticPumpEfficiency;
            public static double speedUpgradeSpeedMultiplier;
            public static double speedUpgradeUsageMultiplier;
            public static Set<ResourceLocation> seismicSensorFluids;
            public static Set<ResourceLocation> seismicSensorFluidTags;
            public static List<String> disenchantingBlacklist;
        }

        public static class Armor {
            public static int jetBootsAirUsage;
            public static int armorStartupTime;
            public static double flippersSpeedBoostGround;
            public static double flippersSpeedBoostFloating;
            public static int repairAirUsage;
            public static int magnetAirUsage;
            public static int scubaMultiplier;
        }

        public static class Integration {
            public static double ieExternalHeaterHeatPerRF;
            public static int ieExternalHeaterRFperTick;
            public static double mekThermalResistanceFactor;
            public static double mekThermalEfficiencyFactor;
            public static double cofhHoldingMultiplier;
        }

        public static class Advanced {
            public static boolean disableKeroseneLampFakeAirBlock;
            public static int fluidTankUpdateRate;
            public static boolean stopDroneAI;
            public static int pressureSyncPrecision;
            public static boolean dontUpdateInfiniteWaterSources;
            public static int maxDroneChargingStationSearchRange;
            public static int stuckDroneTeleportTicks;
            public static int maxDroneTeleportRange;
        }

        public static class Micromissiles {
            public static double baseExplosionDamage;
            public static boolean damageTerrain;
            public static int launchCooldown;
            public static int lifetime;
            public static int missilePodSize;
        }

        public static class Minigun {
            public static double apAmmoDamageMultiplier;
            public static int apAmmoIgnoreArmorChance;
            public static int armorPiercingAmmoCartridgeSize;
            public static double baseDamage;
            public static int baseRange;
            public static int explosiveAmmoCartridgeSize;
            public static double explosiveAmmoDamageMultiplier;
            public static int explosiveAmmoExplosionChance;
            public static double explosiveAmmoExplosionPower;
            public static boolean explosiveAmmoTerrainDamage;
            public static int freezingAmmoBlockIceChance;
            public static int freezingAmmoCartridgeSize;
            public static int freezingAmmoEntityIceChance;
            public static double freezingAmmoFakeIceDamage;
            public static int incendiaryAmmoBlockIgniteChance;
            public static int incendiaryAmmoCartridgeSize;
            public static int incendiaryAmmoEntityIgniteChance;
            public static int incendiaryAmmoFireDuration;
            public static int potionProcChance;
            public static int standardAmmoCartridgeSize;
            public static double weightedAmmoAirUsageMultiplier;
            public static int weightedAmmoCartridgeSize;
            public static double weightedAmmoDamageMultiplier;
            public static double weightedAmmoRangeMultiplier;
            public static boolean blockHitParticles;
        }

        public static class Recipes {
            public static boolean explosionCrafting;
            public static boolean coalToDiamondsRecipe;
            public static boolean inWorldPlasticSolidification;
            public static boolean inWorldYeastCrafting;
        }

        public static class Amadron {
            public static int numPeriodicOffers;
            public static int numVillagerOffers;
            public static int reshuffleInterval;
            public static int maxTradesPerPlayer;
            public static boolean notifyOfTradeAddition;
            public static boolean notifyOfTradeRemoval;
            public static boolean notifyOfDealMade;
        }

        public static class Heat {
            public static double defaultBlockThermalResistance;
            public static double defaultFluidThermalResistance;
            public static double airThermalResistance;
            public static int defaultFluidHeatCapacity;
            public static double ambientTempBiomeModifier;
            public static double ambientTempHeightModifier;
            public static boolean addDefaultFluidEntries;
        }

        public static class Logistics {
            public static double itemTransportCost;
            public static double fluidTransportCost;
            public static double minPressure;
        }

        public static class Jackhammer {
            public static int maxVeinMinerRange;
            public static int baseAirUsage;
        }

        public static class Villagers {
            public static boolean addMechanicHouse;
            public static VillagerTradesRegistration.WhichTrades whichTrades;
        }
    }
}
