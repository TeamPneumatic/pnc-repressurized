package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.inventory.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Names.MOD_ID);

    public static final RegistryObject<ContainerType<ContainerAdvancedAirCompressor>> ADVANCED_AIR_COMPRESSOR
            = register("advanced_air_compressor", ContainerAdvancedAirCompressor::new);
    public static final RegistryObject<ContainerType<ContainerAdvancedLiquidCompressor>> ADVANCED_LIQUID_COMPRESSOR
            = register("advanced_liquid_compressor", ContainerAdvancedLiquidCompressor::new);
    public static final RegistryObject<ContainerType<ContainerAerialInterface>> AERIAL_INTERFACE
            = register("aerial_interface", ContainerAerialInterface::new);
    public static final RegistryObject<ContainerType<ContainerAirCannon>> AIR_CANNON
            = register("air_cannon", ContainerAirCannon::new);
    public static final RegistryObject<ContainerType<ContainerAirCompressor>> AIR_COMPRESSOR
            = register("air_compressor", ContainerAirCompressor::new);
    public static final RegistryObject<ContainerType<ContainerAssemblyController>> ASSEMBLY_CONTROLLER
            = register("assembly_controller", ContainerAssemblyController::new);
    public static final RegistryObject<ContainerType<ContainerChargingStation>> CHARGING_STATION
            = register("charging_station", ContainerChargingStation::new);
    public static final RegistryObject<ContainerType<ContainerChargingStationItemInventory>> CHARGING_MINIGUN
            = register("charging_minigun", ContainerChargingStationItemInventory::createMinigunContainer);
    public static final RegistryObject<ContainerType<ContainerChargingStationItemInventory>> CHARGING_DRONE
            = register("charging_drone", ContainerChargingStationItemInventory::createDroneContainer);
    public static final RegistryObject<ContainerType<ContainerChargingStationItemInventory>> CHARGING_ARMOR
            = register("charging_armor", ContainerChargingStationItemInventory::createArmorContainer);
    public static final RegistryObject<ContainerType<ContainerChargingStationItemInventory>> CHARGING_JACKHAMMER =
            register("charging_jackhammer", ContainerChargingStationItemInventory::createJackhammerContainer);
    public static final RegistryObject<ContainerType<ContainerCreativeCompressor>> CREATIVE_COMPRESSOR
            = register("creative_compressor", ContainerCreativeCompressor::new);
    public static final RegistryObject<ContainerType<ContainerElectrostaticCompressor>> ELECTROSTATIC_COMPRESSOR
            = register("electrostatic_compressor", ContainerElectrostaticCompressor::new);
    public static final RegistryObject<ContainerType<ContainerElevator>> ELEVATOR
            = register("elevator", ContainerElevator::new);
    public static final RegistryObject<ContainerType<ContainerFluxCompressor>> FLUX_COMPRESSOR
            = register("flux_compressor", ContainerFluxCompressor::new);
    public static final RegistryObject<ContainerType<ContainerGasLift>> GAS_LIFT
            = register("gas_lift", ContainerGasLift::new);
    public static final RegistryObject<ContainerType<ContainerKeroseneLamp>> KEROSENE_LAMP
            = register("kerosene_lamp", ContainerKeroseneLamp::new);
    public static final RegistryObject<ContainerType<ContainerLiquidCompressor>> LIQUID_COMPRESSOR
            = register("liquid_compressor", ContainerLiquidCompressor::new);
    public static final RegistryObject<ContainerType<ContainerLiquidHopper>> LIQUID_HOPPER
            = register("liquid_hopper", ContainerLiquidHopper::new);
    public static final RegistryObject<ContainerType<ContainerOmnidirectionalHopper>> OMNIDIRECTIONAL_HOPPER
            = register("omnidirectional_hopper", ContainerOmnidirectionalHopper::new);
    public static final RegistryObject<ContainerType<ContainerPneumaticDoorBase>> PNEUMATIC_DOOR_BASE
            = register("pneumatic_door_base", ContainerPneumaticDoorBase::new);
    public static final RegistryObject<ContainerType<ContainerPneumaticDynamo>> PNEUMATIC_DYNAMO
            = register("pneumatic_dynamo", ContainerPneumaticDynamo::new);
    public static final RegistryObject<ContainerType<ContainerPressureChamberInterface>> PRESSURE_CHAMBER_INTERFACE
            = register("pressure_chamber_interface", ContainerPressureChamberInterface::new);
    public static final RegistryObject<ContainerType<ContainerPressureChamberValve>> PRESSURE_CHAMBER_VALVE
            = register("pressure_chamber_valve", ContainerPressureChamberValve::new);
    public static final RegistryObject<ContainerType<ContainerProgrammableController>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ContainerProgrammableController::new);
    public static final RegistryObject<ContainerType<ContainerProgrammer>> PROGRAMMER
            = register("programmer", ContainerProgrammer::new);
    public static final RegistryObject<ContainerType<ContainerRefinery>> REFINERY
            = register("refinery", ContainerRefinery::new);
    public static final RegistryObject<ContainerType<ContainerSecurityStationMain>> SECURITY_STATION_MAIN
            = register("security_station_main", ContainerSecurityStationMain::new);
    public static final RegistryObject<ContainerType<ContainerSecurityStationHacking>> SECURITY_STATION_HACKING
            = register("security_station_hacking", ContainerSecurityStationHacking::new);
    public static final RegistryObject<ContainerType<ContainerSentryTurret>> SENTRY_TURRET
            = register("sentry_turret", ContainerSentryTurret::new);
    public static final RegistryObject<ContainerType<ContainerThermalCompressor>> THERMAL_COMPRESSOR
            = register("thermal_compressor", ContainerThermalCompressor::new);
    public static final RegistryObject<ContainerType<ContainerThermopneumaticProcessingPlant>> THERMOPNEUMATIC_PROCESSING_PLANT
            = register("thermopneumatic_processing_plant", ContainerThermopneumaticProcessingPlant::new);
    public static final RegistryObject<ContainerType<ContainerUniversalSensor>> UNIVERSAL_SENSOR
            = register("universal_sensor", ContainerUniversalSensor::new);
    public static final RegistryObject<ContainerType<ContainerUVLightBox>> UV_LIGHT_BOX
            = register("uv_light_box", ContainerUVLightBox::new);
    public static final RegistryObject<ContainerType<ContainerVacuumPump>> VACUUM_PUMP
            = register("vacuum_pump", ContainerVacuumPump::new);
    public static final RegistryObject<ContainerType<ContainerAmadron>> AMADRON
            = register("amadron", ContainerAmadron::new);
    public static final RegistryObject<ContainerType<ContainerAmadronAddTrade>> AMADRON_ADD_TRADE
            = register("amadron_add_trade", ContainerAmadronAddTrade::new);
    public static final RegistryObject<ContainerType<ContainerMinigunMagazine>> MINIGUN_MAGAZINE
            = register("minigun_magazine", ContainerMinigunMagazine::new);
    public static final RegistryObject<ContainerType<ContainerLogistics>> LOGISTICS_FRAME_STORAGE
            = register("logistics_frame_storage", ContainerLogistics::createStorageContainer);
    public static final RegistryObject<ContainerType<ContainerLogistics>> LOGISTICS_FRAME_PROVIDER
            = register("logistics_frame_provider", ContainerLogistics::createProviderContainer);
    public static final RegistryObject<ContainerType<ContainerLogistics>> LOGISTICS_FRAME_REQUESTER
            = register("logistics_frame_requester", ContainerLogistics::createRequesterContainer);
    public static final RegistryObject<ContainerType<ContainerInventorySearcher>> INVENTORY_SEARCHER
            = register("inventory_searcher", ContainerInventorySearcher::new);
    public static final RegistryObject<ContainerType<ContainerRemote>> REMOTE
            = register("remote", ContainerRemote::createRemoteContainer);
    public static final RegistryObject<ContainerType<ContainerRemote>> REMOTE_EDITOR
            = register("remote_editor", ContainerRemote::createRemoteEditorContainer);
    public static final RegistryObject<ContainerType<ContainerItemSearcher>> ITEM_SEARCHER
            = register("item_searcher", ContainerItemSearcher::new);
    public static final RegistryObject<ContainerType<ContainerEtchingTank>> ETCHING_TANK
            = register("etching_tank", ContainerEtchingTank::new);
    public static final RegistryObject<ContainerType<ContainerFluidTank>> FLUID_TANK
            = register("fluid_tank", ContainerFluidTank::new);
    public static final RegistryObject<ContainerType<ContainerReinforcedChest>> REINFORCED_CHEST
            = register("reinforced_chest", ContainerReinforcedChest::new);
    public static final RegistryObject<ContainerType<ContainerSmartChest>> SMART_CHEST
            = register("smart_chest", ContainerSmartChest::new);
    public static final RegistryObject<ContainerType<ContainerTagWorkbench>> TAG_MATCHER
            = register("tag_workbench", ContainerTagWorkbench::new);
    public static final RegistryObject<ContainerType<ContainerFluidMixer>> FLUID_MIXER
            = register("fluid_mixer", ContainerFluidMixer::new);
    public static final RegistryObject<ContainerType<ContainerJackhammerSetup>> JACKHAMMER_SETUP
            = register("jackhammer_setup", ContainerJackhammerSetup::new);

    private static <C extends Container, T extends ContainerType<C>> RegistryObject<T> register(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return CONTAINERS.register(name, () -> (T) IForgeContainerType.create(f));
    }
}
