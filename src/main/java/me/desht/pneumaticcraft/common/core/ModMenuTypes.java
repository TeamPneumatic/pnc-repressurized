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

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.inventory.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Names.MOD_ID);

    public static final RegistryObject<MenuType<ContainerAdvancedAirCompressor>> ADVANCED_AIR_COMPRESSOR
            = register("advanced_air_compressor", ContainerAdvancedAirCompressor::new);
    public static final RegistryObject<MenuType<ContainerAdvancedLiquidCompressor>> ADVANCED_LIQUID_COMPRESSOR
            = register("advanced_liquid_compressor", ContainerAdvancedLiquidCompressor::new);
    public static final RegistryObject<MenuType<ContainerAerialInterface>> AERIAL_INTERFACE
            = register("aerial_interface", ContainerAerialInterface::new);
    public static final RegistryObject<MenuType<ContainerAirCannon>> AIR_CANNON
            = register("air_cannon", ContainerAirCannon::new);
    public static final RegistryObject<MenuType<ContainerAirCompressor>> AIR_COMPRESSOR
            = register("air_compressor", ContainerAirCompressor::new);
    public static final RegistryObject<MenuType<ContainerAssemblyController>> ASSEMBLY_CONTROLLER
            = register("assembly_controller", ContainerAssemblyController::new);
    public static final RegistryObject<MenuType<ContainerChargingStation>> CHARGING_STATION
            = register("charging_station", ContainerChargingStation::new);
    public static final RegistryObject<MenuType<ContainerChargingStationUpgradeManager>> CHARGING_MINIGUN
            = register("charging_minigun", ContainerChargingStationUpgradeManager::createMinigunContainer);
    public static final RegistryObject<MenuType<ContainerChargingStationUpgradeManager>> CHARGING_DRONE
            = register("charging_drone", ContainerChargingStationUpgradeManager::createDroneContainer);
    public static final RegistryObject<MenuType<ContainerChargingStationUpgradeManager>> CHARGING_ARMOR
            = register("charging_armor", ContainerChargingStationUpgradeManager::createArmorContainer);
    public static final RegistryObject<MenuType<ContainerChargingStationUpgradeManager>> CHARGING_JACKHAMMER
            = register("charging_jackhammer", ContainerChargingStationUpgradeManager::createJackhammerContainer);
    public static final RegistryObject<MenuType<ContainerChargingStationUpgradeManager>> CHARGING_AMADRON
            = register("charging_amadron", ContainerChargingStationUpgradeManager::createAmadronContainer);
    public static final RegistryObject<MenuType<ContainerCreativeCompressor>> CREATIVE_COMPRESSOR
            = register("creative_compressor", ContainerCreativeCompressor::new);
    public static final RegistryObject<MenuType<ContainerElectrostaticCompressor>> ELECTROSTATIC_COMPRESSOR
            = register("electrostatic_compressor", ContainerElectrostaticCompressor::new);
    public static final RegistryObject<MenuType<ContainerElevator>> ELEVATOR
            = register("elevator", ContainerElevator::new);
    public static final RegistryObject<MenuType<ContainerFluxCompressor>> FLUX_COMPRESSOR
            = register("flux_compressor", ContainerFluxCompressor::new);
    public static final RegistryObject<MenuType<ContainerGasLift>> GAS_LIFT
            = register("gas_lift", ContainerGasLift::new);
    public static final RegistryObject<MenuType<ContainerKeroseneLamp>> KEROSENE_LAMP
            = register("kerosene_lamp", ContainerKeroseneLamp::new);
    public static final RegistryObject<MenuType<ContainerLiquidCompressor>> LIQUID_COMPRESSOR
            = register("liquid_compressor", ContainerLiquidCompressor::new);
    public static final RegistryObject<MenuType<ContainerLiquidHopper>> LIQUID_HOPPER
            = register("liquid_hopper", ContainerLiquidHopper::new);
    public static final RegistryObject<MenuType<ContainerOmnidirectionalHopper>> OMNIDIRECTIONAL_HOPPER
            = register("omnidirectional_hopper", ContainerOmnidirectionalHopper::new);
    public static final RegistryObject<MenuType<ContainerPneumaticDoorBase>> PNEUMATIC_DOOR_BASE
            = register("pneumatic_door_base", ContainerPneumaticDoorBase::new);
    public static final RegistryObject<MenuType<ContainerPneumaticDynamo>> PNEUMATIC_DYNAMO
            = register("pneumatic_dynamo", ContainerPneumaticDynamo::new);
    public static final RegistryObject<MenuType<ContainerPressureChamberInterface>> PRESSURE_CHAMBER_INTERFACE
            = register("pressure_chamber_interface", ContainerPressureChamberInterface::new);
    public static final RegistryObject<MenuType<ContainerPressureChamberValve>> PRESSURE_CHAMBER_VALVE
            = register("pressure_chamber_valve", ContainerPressureChamberValve::new);
    public static final RegistryObject<MenuType<ContainerProgrammableController>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ContainerProgrammableController::new);
    public static final RegistryObject<MenuType<ContainerProgrammer>> PROGRAMMER
            = register("programmer", ContainerProgrammer::new);
    public static final RegistryObject<MenuType<ContainerRefinery>> REFINERY
            = register("refinery", ContainerRefinery::new);
    public static final RegistryObject<MenuType<ContainerSecurityStationMain>> SECURITY_STATION_MAIN
            = register("security_station_main", ContainerSecurityStationMain::new);
    public static final RegistryObject<MenuType<ContainerSecurityStationHacking>> SECURITY_STATION_HACKING
            = register("security_station_hacking", ContainerSecurityStationHacking::new);
    public static final RegistryObject<MenuType<ContainerSentryTurret>> SENTRY_TURRET
            = register("sentry_turret", ContainerSentryTurret::new);
    public static final RegistryObject<MenuType<ContainerThermalCompressor>> THERMAL_COMPRESSOR
            = register("thermal_compressor", ContainerThermalCompressor::new);
    public static final RegistryObject<MenuType<ContainerThermopneumaticProcessingPlant>> THERMOPNEUMATIC_PROCESSING_PLANT
            = register("thermopneumatic_processing_plant", ContainerThermopneumaticProcessingPlant::new);
    public static final RegistryObject<MenuType<ContainerUniversalSensor>> UNIVERSAL_SENSOR
            = register("universal_sensor", ContainerUniversalSensor::new);
    public static final RegistryObject<MenuType<ContainerUVLightBox>> UV_LIGHT_BOX
            = register("uv_light_box", ContainerUVLightBox::new);
    public static final RegistryObject<MenuType<ContainerVacuumPump>> VACUUM_PUMP
            = register("vacuum_pump", ContainerVacuumPump::new);
    public static final RegistryObject<MenuType<ContainerAmadron>> AMADRON
            = register("amadron", ContainerAmadron::new);
    public static final RegistryObject<MenuType<ContainerAmadronAddTrade>> AMADRON_ADD_TRADE
            = register("amadron_add_trade", ContainerAmadronAddTrade::new);
    public static final RegistryObject<MenuType<ContainerMinigunMagazine>> MINIGUN_MAGAZINE
            = register("minigun_magazine", ContainerMinigunMagazine::new);
    public static final RegistryObject<MenuType<ContainerLogistics>> LOGISTICS_FRAME_STORAGE
            = register("logistics_frame_storage", ContainerLogistics::createStorageContainer);
    public static final RegistryObject<MenuType<ContainerLogistics>> LOGISTICS_FRAME_PROVIDER
            = register("logistics_frame_provider", ContainerLogistics::createProviderContainer);
    public static final RegistryObject<MenuType<ContainerLogistics>> LOGISTICS_FRAME_REQUESTER
            = register("logistics_frame_requester", ContainerLogistics::createRequesterContainer);
    public static final RegistryObject<MenuType<ContainerInventorySearcher>> INVENTORY_SEARCHER
            = register("inventory_searcher", ContainerInventorySearcher::new);
    public static final RegistryObject<MenuType<ContainerRemote>> REMOTE
            = register("remote", ContainerRemote::createRemoteContainer);
    public static final RegistryObject<MenuType<ContainerRemote>> REMOTE_EDITOR
            = register("remote_editor", ContainerRemote::createRemoteEditorContainer);
    public static final RegistryObject<MenuType<ContainerItemSearcher>> ITEM_SEARCHER
            = register("item_searcher", ContainerItemSearcher::new);
    public static final RegistryObject<MenuType<ContainerEtchingTank>> ETCHING_TANK
            = register("etching_tank", ContainerEtchingTank::new);
    public static final RegistryObject<MenuType<ContainerFluidTank>> FLUID_TANK
            = register("fluid_tank", ContainerFluidTank::new);
    public static final RegistryObject<MenuType<ContainerReinforcedChest>> REINFORCED_CHEST
            = register("reinforced_chest", ContainerReinforcedChest::new);
    public static final RegistryObject<MenuType<ContainerSmartChest>> SMART_CHEST
            = register("smart_chest", ContainerSmartChest::new);
    public static final RegistryObject<MenuType<ContainerTagWorkbench>> TAG_MATCHER
            = register("tag_workbench", ContainerTagWorkbench::new);
    public static final RegistryObject<MenuType<ContainerFluidMixer>> FLUID_MIXER
            = register("fluid_mixer", ContainerFluidMixer::new);
    public static final RegistryObject<MenuType<ContainerJackhammerSetup>> JACKHAMMER_SETUP
            = register("jackhammer_setup", ContainerJackhammerSetup::new);
    public static final RegistryObject<MenuType<ContainerVacuumTrap>> VACUUM_TRAP
            = register("vacuum_trap", ContainerVacuumTrap::new);
    public static final RegistryObject<MenuType<ContainerSpawnerExtractor>> SPAWNER_EXTRACTOR
            = register("spawner_extractor", ContainerSpawnerExtractor::new);
    public static final RegistryObject<MenuType<ContainerPressurizedSpawner>> PRESSURIZED_SPAWNER
            = register("pressurized_spawner", ContainerPressurizedSpawner::new) ;
    public static final RegistryObject<MenuType<ContainerCreativeCompressedIronBlock>> CREATIVE_COMPRESSED_IRON_BLOCK
            = register("creative_compressed_iron_block", ContainerCreativeCompressedIronBlock::new);

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> RegistryObject<T> register(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return MENU_TYPES.register(name, () -> (T) IForgeMenuType.create(f));
    }
}
