package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.common.config.ClientConfig.PathUpdateSetting;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigHelper {
    private static net.minecraftforge.fml.config.ModConfig clientConfig;
    private static net.minecraftforge.fml.config.ModConfig commonConfig;

    static void refreshClient(net.minecraftforge.fml.config.ModConfig config) {
        clientConfig = config;

        ClientConfig client = ConfigHolder.client;
        PNCConfig.Client.aphorismDrama = client.general.aphorismDrama.get();
        PNCConfig.Client.programmerDifficulty = client.general.programmerDifficulty.get();
        PNCConfig.Client.topShowsFluids = client.general.topShowsFluids.get();
        PNCConfig.Client.logisticsGuiTint = client.general.logisticsGuiTint.get();
        PNCConfig.Client.guiBevel = client.general.guiBevel.get();
        PNCConfig.Client.alwaysShowPressureDurabilityBar = client.general.alwaysShowPressureDurabilityBar.get();
        PNCConfig.Client.tubeModuleRedstoneParticles = client.general.tubeModuleRedstoneParticles.get();
        PNCConfig.Client.guiRemoteGridSnap = client.general.guiRemoteGridSnap.get();
        PNCConfig.Client.programmerGuiPauses = client.general.programmerGuiPauses.get();
        PNCConfig.Client.notifyAmadronOfferUpdates = client.general.notifyAmadronOfferUpdates.get();

        PNCConfig.Client.Armor.blockTrackerMaxTimePerTick = client.armor.blockTrackerMaxTimePerTick.get();
        PNCConfig.Client.Armor.fancyArmorModels = client.armor.fancyArmorModels.get();
        PNCConfig.Client.Armor.leggingsFOVFactor = client.armor.leggingsFOVFactor.get();
        PNCConfig.Client.Armor.pathEnabled = client.armor.pathEnabled.get();
        PNCConfig.Client.Armor.wirePath = client.armor.wirePath.get();
        PNCConfig.Client.Armor.xRayEnabled = client.armor.xRayEnabled.get();
        PNCConfig.Client.Armor.pathUpdateSetting = client.armor.pathUpdateSetting.get();
        PNCConfig.Client.Armor.showPressureNumerically = client.armor.showPressureNumerically.get();
        PNCConfig.Client.Armor.showEnchantGlint = client.armor.showEnchantGlint.get();

        PNCConfig.Client.Sound.elevatorVolumeRunning = client.sound.elevatorVolumeRunning.get();
        PNCConfig.Client.Sound.elevatorVolumeStartStop = client.sound.elevatorVolumeStartStop.get();
        PNCConfig.Client.Sound.airLeakVolume = client.sound.airLeakVolume.get();
        PNCConfig.Client.Sound.minigunVolumeDrone= client.sound.minigunVolumeDrone.get();
        PNCConfig.Client.Sound.minigunVolumeHeld = client.sound.minigunVolumeHeld.get();
        PNCConfig.Client.Sound.minigunVolumeSentryTurret = client.sound.minigunVolumeSentryTurret.get();
        PNCConfig.Client.Sound.jetbootsVolume = client.sound.jetbootsVolume.get();
        PNCConfig.Client.Sound.jetbootsVolumeBuilderMode = client.sound.jetbootsVolumeBuilderMode.get();
        PNCConfig.Client.Sound.jackhammerVolume = client.sound.jackhammerVolume.get();

        ArmorUpgradeClientRegistry.getInstance().refreshConfig();
    }

    static void refreshCommon(net.minecraftforge.fml.config.ModConfig config) {
        commonConfig = config;

        CommonConfig common = ConfigHolder.common;
        PNCConfig.Common.General.enableDroneSuffocation = common.general.enableDroneSuffocation.get();
        PNCConfig.Common.General.enableDungeonLoot = common.general.enableDungeonLoot.get();
        PNCConfig.Common.General.fuelBucketEfficiency = common.general.fuelBucketEfficiency.get();
        PNCConfig.Common.General.maxProgrammingArea = common.general.maxProgrammingArea.get();
        PNCConfig.Common.General.minFluidFuelTemperature = common.general.minFluidFuelTemperature.get();
        PNCConfig.Common.General.oilGenerationChance = common.general.oilGenerationChance.get();
        PNCConfig.Common.General.surfaceOilGenerationChance = common.general.surfaceOilGenerationChance.get();
        PNCConfig.Common.General.oilWorldGenBlacklist = common.general.oilWorldGenBlacklist.get()
                .stream().map(resourceName -> new ResourceLocation(resourceName.toLowerCase())).collect(Collectors.toSet());
        PNCConfig.Common.General.oilWorldGenCategoryBlacklist = ImmutableSet.copyOf(common.general.oilWorldGenCategoryBlacklist.get());
        PNCConfig.Common.General.useUpDyesWhenColoring = common.general.useUpDyesWhenColoring.get();
        PNCConfig.Common.General.dronesRenderHeldItem = common.general.dronesRenderHeldItem.get();
        PNCConfig.Common.General.dronesCanImportXPOrbs = common.general.dronesCanImportXPOrbs.get();
        PNCConfig.Common.General.dronesCanBePickedUp = common.general.dronesCanBePickedUp.get();
        PNCConfig.Common.General.droneDebuggerPathParticles = common.general.droneDebuggerPathParticles.get();
        PNCConfig.Common.General.vacuumTrapBlacklist = new HashSet<>(common.general.vacuumTrapBlacklist.get());
        TileEntityVacuumTrap.clearBlacklistCache();

        PNCConfig.Common.Machines.aerialInterfaceArmorCompat = common.machines.aerialInterfaceArmorCompat.get();
        PNCConfig.Common.Machines.cropSticksGrowthBoostChance = common.machines.cropSticksGrowthBoostChance.get();
        PNCConfig.Common.Machines.electricCompressorEfficiency = common.machines.electricCompressorEfficiency.get();
        PNCConfig.Common.Machines.electrostaticLightningChance = common.machines.electrostaticLightningChance.get();
        PNCConfig.Common.Machines.elevatorBaseBlocksPerBase = common.machines.elevatorBaseBlocksPerBase.get();
        PNCConfig.Common.Machines.fluxCompressorEfficiency = common.machines.fluxCompressorEfficiency.get();
        PNCConfig.Common.Machines.keroseneLampCanUseAnyFuel = common.machines.keroseneLampCanUseAnyFuel.get();
        PNCConfig.Common.Machines.keroseneLampFuelEfficiency = common.machines.keroseneLampFuelEfficiency.get();
        PNCConfig.Common.Machines.kineticCompressorEfficiency = common.machines.kineticCompressorEfficiency.get();
        PNCConfig.Common.Machines.liquidHopperDispenser = common.machines.liquidHopperDispenser.get();
        PNCConfig.Common.Machines.omniHopperDispenser = common.machines.omniHopperDispenser.get();
        PNCConfig.Common.Machines.pneumaticDynamoEfficiency = common.machines.pneumaticDynamoEfficiency.get();
        PNCConfig.Common.Machines.pneumaticEngineEfficiency = common.machines.pneumaticEngineEfficiency.get();
        PNCConfig.Common.Machines.pneumaticGeneratorEfficiency = common.machines.pneumaticGeneratorEfficiency.get();
        PNCConfig.Common.Machines.pneumaticPumpEfficiency = common.machines.pneumaticPumpEfficiency.get();
        PNCConfig.Common.Machines.speedUpgradeSpeedMultiplier = common.machines.speedUpgradeSpeedMultiplier.get();
        PNCConfig.Common.Machines.speedUpgradeUsageMultiplier = common.machines.speedUpgradeUsageMultiplier.get();
        PNCConfig.Common.Machines.seismicSensorFluids = common.machines.seismicSensorFluids.get()
                .stream().map(resourceName -> new ResourceLocation(resourceName.toLowerCase())).collect(Collectors.toSet());
        PNCConfig.Common.Machines.seismicSensorFluidTags = common.machines.seismicSensorFluidTags.get()
                .stream().map(resourceName -> new ResourceLocation(resourceName.toLowerCase())).collect(Collectors.toSet());
        PNCConfig.Common.Machines.disenchantingBlacklist = common.machines.disenchantingBlacklist.get();

        PNCConfig.Common.Armor.jetBootsAirUsage = common.armor.jetBootsAirUsage.get();
        PNCConfig.Common.Armor.armorStartupTime = common.armor.armorStartupTime.get();
        PNCConfig.Common.Armor.flippersSpeedBoostGround = common.armor.flippersSpeedBoostGround.get();
        PNCConfig.Common.Armor.flippersSpeedBoostFloating = common.armor.flippersSpeedBoostFloating.get();
        PNCConfig.Common.Armor.repairAirUsage = common.armor.repairAirUsage.get();
        PNCConfig.Common.Armor.magnetAirUsage = common.armor.magnetAirUsage.get();
        PNCConfig.Common.Armor.scubaMultiplier = common.armor.scubaMultiplier.get();

        PNCConfig.Common.Integration.ieExternalHeaterHeatPerRF = common.integration.ieExternalHeaterHeatPerRF.get();
        PNCConfig.Common.Integration.ieExternalHeaterRFperTick = common.integration.ieExternalHeaterRFperTick.get();
        PNCConfig.Common.Integration.mekThermalResistanceFactor = common.integration.mekThermalResistanceFactor.get();
        PNCConfig.Common.Integration.mekThermalEfficiencyFactor = common.integration.mekThermalEfficiencyFactor.get();
        PNCConfig.Common.Integration.cofhHoldingMultiplier = common.integration.cofhHoldingMultiplier.get();

        PNCConfig.Common.Advanced.disableKeroseneLampFakeAirBlock = common.advanced.disableKeroseneLampFakeAirBlock.get();
        PNCConfig.Common.Advanced.fluidTankUpdateRate = common.advanced.fluidTankUpdateRate.get();
        PNCConfig.Common.Advanced.pressureSyncPrecision = common.advanced.pressureSyncPrecision.get();
        PNCConfig.Common.Advanced.maxDroneChargingStationSearchRange = common.advanced.maxDroneChargingStationSearchRange.get();
        PNCConfig.Common.Advanced.maxDroneTeleportRange = common.advanced.maxDroneTeleportRange.get();
        PNCConfig.Common.Advanced.stopDroneAI = common.advanced.stopDroneAI.get();
        PNCConfig.Common.Advanced.dontUpdateInfiniteWaterSources = common.advanced.dontUpdateInfiniteWaterSources.get();
        PNCConfig.Common.Advanced.stuckDroneTeleportTicks = common.advanced.stuckDroneTeleportTicks.get();

        PNCConfig.Common.Micromissiles.baseExplosionDamage = common.micromissiles.baseExplosionDamage.get();
        PNCConfig.Common.Micromissiles.damageTerrain = common.micromissiles.damageTerrain.get();
        PNCConfig.Common.Micromissiles.launchCooldown = common.micromissiles.launchCooldown.get();
        PNCConfig.Common.Micromissiles.lifetime = common.micromissiles.lifetime.get();
        PNCConfig.Common.Micromissiles.missilePodSize = common.micromissiles.missilePodSize.get();

        PNCConfig.Common.Minigun.apAmmoDamageMultiplier = common.minigun.apAmmoDamageMultiplier.get();
        PNCConfig.Common.Minigun.apAmmoIgnoreArmorChance = common.minigun.apAmmoIgnoreArmorChance.get();
        PNCConfig.Common.Minigun.armorPiercingAmmoCartridgeSize = common.minigun.armorPiercingAmmoCartridgeSize.get();
        PNCConfig.Common.Minigun.baseDamage = common.minigun.baseDamage.get();
        PNCConfig.Common.Minigun.baseRange = common.minigun.baseRange.get();
        PNCConfig.Common.Minigun.explosiveAmmoCartridgeSize = common.minigun.explosiveAmmoCartridgeSize.get();
        PNCConfig.Common.Minigun.explosiveAmmoDamageMultiplier = common.minigun.explosiveAmmoDamageMultiplier.get();
        PNCConfig.Common.Minigun.explosiveAmmoExplosionChance = common.minigun.explosiveAmmoExplosionChance.get();
        PNCConfig.Common.Minigun.explosiveAmmoExplosionPower = common.minigun.explosiveAmmoExplosionPower.get();
        PNCConfig.Common.Minigun.explosiveAmmoTerrainDamage = common.minigun.explosiveAmmoTerrainDamage.get();
        PNCConfig.Common.Minigun.freezingAmmoBlockIceChance = common.minigun.freezingAmmoBlockIceChance.get();
        PNCConfig.Common.Minigun.freezingAmmoCartridgeSize = common.minigun.freezingAmmoCartridgeSize.get();
        PNCConfig.Common.Minigun.freezingAmmoEntityIceChance = common.minigun.freezingAmmoEntityIceChance.get();
        PNCConfig.Common.Minigun.freezingAmmoFakeIceDamage = common.minigun.freezingAmmoFakeIceDamage.get();
        PNCConfig.Common.Minigun.incendiaryAmmoBlockIgniteChance = common.minigun.incendiaryAmmoBlockIgniteChance.get();
        PNCConfig.Common.Minigun.incendiaryAmmoCartridgeSize = common.minigun.incendiaryAmmoCartridgeSize.get();
        PNCConfig.Common.Minigun.incendiaryAmmoEntityIgniteChance = common.minigun.incendiaryAmmoEntityIgniteChance.get();
        PNCConfig.Common.Minigun.incendiaryAmmoFireDuration = common.minigun.incendiaryAmmoFireDuration.get();
        PNCConfig.Common.Minigun.potionProcChance = common.minigun.potionProcChance.get();
        PNCConfig.Common.Minigun.standardAmmoCartridgeSize = common.minigun.standardAmmoCartridgeSize.get();
        PNCConfig.Common.Minigun.weightedAmmoAirUsageMultiplier = common.minigun.weightedAmmoAirUsageMultiplier.get();
        PNCConfig.Common.Minigun.weightedAmmoCartridgeSize = common.minigun.weightedAmmoCartridgeSize.get();
        PNCConfig.Common.Minigun.weightedAmmoDamageMultiplier = common.minigun.weightedAmmoDamageMultiplier.get();
        PNCConfig.Common.Minigun.weightedAmmoRangeMultiplier = common.minigun.weightedAmmoRangeMultiplier.get();
        PNCConfig.Common.Minigun.blockHitParticles = common.minigun.blockHitParticles.get();

        PNCConfig.Common.Recipes.coalToDiamondsRecipe = common.recipes.coalToDiamondsRecipe.get();
        PNCConfig.Common.Recipes.explosionCrafting = common.recipes.explosionCrafting.get();
        PNCConfig.Common.Recipes.inWorldPlasticSolidification = common.recipes.inWorldPlasticSolidification.get();
        PNCConfig.Common.Recipes.inWorldYeastCrafting = common.recipes.inWorldYeastCrafting.get();

        PNCConfig.Common.Heat.defaultBlockThermalResistance = common.heat.blockThermalResistance.get();
        PNCConfig.Common.Heat.defaultFluidThermalResistance = common.heat.fluidThermalResistance.get();
        PNCConfig.Common.Heat.airThermalResistance = common.heat.airThermalResistance.get();
        PNCConfig.Common.Heat.defaultFluidHeatCapacity = common.heat.defaultFluidHeatCapacity.get();
        PNCConfig.Common.Heat.ambientTempBiomeModifier = common.heat.ambientTemperatureBiomeModifier.get();
        PNCConfig.Common.Heat.ambientTempHeightModifier = common.heat.ambientTemperatureHeightModifier.get();
        PNCConfig.Common.Heat.addDefaultFluidEntries = common.heat.addDefaultFluidEntries.get();

        PNCConfig.Common.Amadron.numPeriodicOffers = common.amadron.numPeriodicOffers.get();
        PNCConfig.Common.Amadron.numVillagerOffers = common.amadron.numVillagerOffers.get();
        PNCConfig.Common.Amadron.reshuffleInterval = common.amadron.reshuffleInterval.get();
        PNCConfig.Common.Amadron.maxTradesPerPlayer = common.amadron.maxTradesPerPlayer.get();
        PNCConfig.Common.Amadron.notifyOfDealMade = common.amadron.notifyOfDealMade.get();
        PNCConfig.Common.Amadron.notifyOfTradeAddition = common.amadron.notifyOfTradeAddition.get();
        PNCConfig.Common.Amadron.notifyOfTradeRemoval = common.amadron.notifyOfTradeRemoval.get();

        PNCConfig.Common.Logistics.itemTransportCost = common.logistics.itemTransportCost.get();
        PNCConfig.Common.Logistics.fluidTransportCost = common.logistics.fluidTransportCost.get();
        PNCConfig.Common.Logistics.minPressure = common.logistics.minPressure.get();

        PNCConfig.Common.Jackhammer.baseAirUsage = common.jackhammer.baseAirUsage.get();
        PNCConfig.Common.Jackhammer.maxVeinMinerRange = common.jackhammer.maxVeinMinerRange.get();

        PNCConfig.Common.Villagers.addMechanicHouse = common.villagers.addMechanicHouse.get();
        PNCConfig.Common.Villagers.whichTrades = common.villagers.whichTrades.get();
    }

    private static void setValueAndSave(final net.minecraftforge.fml.config.ModConfig modConfig, final String path, final Object newValue) {
        modConfig.getConfigData().set(path, newValue);
        modConfig.save();
    }

    private static void setValuesAndSave(final net.minecraftforge.fml.config.ModConfig modConfig, final Map<String,Object>values) {
        values.forEach((k, v) -> modConfig.getConfigData().set(k, v));
        modConfig.save();
    }

    public static void setProgrammerDifficulty(WidgetDifficulty difficulty) {
        setValueAndSave(clientConfig, "general.programmer_difficulty", difficulty);
        refreshClient(clientConfig);
    }

    public static void setGuiRemoteGridSnap(boolean snap) {
        setValueAndSave(clientConfig, "general.gui_remote_grid_snap", snap);
        refreshClient(clientConfig);
    }

    public static void updateCoordTracker(boolean pathEnabled, boolean wirePath, boolean xRayEnabled, PathUpdateSetting pathUpdateSetting) {
        setValuesAndSave(clientConfig, ImmutableMap.of(
                "armor.path_enabled", pathEnabled,
                "armor.wire_path", wirePath,
                "armor.xray_enabled", xRayEnabled,
                "armor.path_update_setting", pathUpdateSetting)
        );
        refreshClient(clientConfig);
    }

    public static void setShowPressureNumerically(boolean numeric) {
        setValueAndSave(clientConfig, "armor.show_pressure_numerically", numeric);
        refreshClient(clientConfig);
    }

    public static int getOilLakeChance() {
        return ConfigHolder.common.general.oilGenerationChance.get();
    }

    public static void setShowEnchantGlint(boolean show) {
        setValueAndSave(clientConfig, "armor.show_enchant_glint", show);
        refreshClient(clientConfig);
    }
}
