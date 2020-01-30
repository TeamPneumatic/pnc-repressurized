package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import net.minecraft.util.ResourceLocation;

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
        public static boolean dronesRenderHeldItem;
        public static boolean semiBlockLighting;
        public static boolean guiBevel;
        public static boolean alwaysShowPressureDurabilityBar;
        public static boolean tubeModuleRedstoneParticles;
        public static boolean guiRemoteGridSnap;

        public static class Armor {
            public static int blockTrackerMaxTimePerTick;
            public static boolean fancyArmorModels;
            public static double leggingsFOVFactor;
            public static boolean pathEnabled;
            public static boolean wirePath;
            public static boolean xRayEnabled;
            public static ClientConfig.PathUpdateSetting pathUpdateSetting;
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
            public static int plasticMixerPlasticRatio;
            public static int pneumaticDynamoEfficiency;
            public static int pneumaticEngineEfficiency;
            public static int pneumaticGeneratorEfficiency;
            public static int pneumaticPumpEfficiency;
            public static double speedUpgradeSpeedMultiplier;
            public static double speedUpgradeUsageMultiplier;
            public static double thermalCompressorThermalResistance;
        }

        public static class Armor {
            public static int jetBootsAirUsage;
            public static int armorStartupTime;
        }

        public static class Integration {
            public static double ieExternalHeaterHeatPerRF;
            public static int ieExternalHeaterRFperTick;
            public static double mekHeatEfficiency;
            public static double mekThermalResistanceMult;
            public static double tanAirConAirUsageMultiplier;
            public static double tanHeatDivider;
            public static int tanRefreshInterval;
        }

        public static class Advanced {
            public static boolean disableKeroseneLampFakeAirBlock;
            public static double liquidTankUpdateThreshold;
            public static boolean stopDroneAI;
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
        }

        public static class Recipes {
            public static boolean explosionCrafting;
            public static boolean coalToDiamondsRecipe;
        }

        public static class Amadron {
            public static int numPeriodicOffers;
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
        }
    }
}
