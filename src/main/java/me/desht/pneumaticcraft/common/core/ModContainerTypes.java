package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.inventory.*;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockDefaultStorage;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockPassiveProvider;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Names.MOD_ID)
public class ModContainerTypes {
    public static final ContainerType<ContainerAdvancedAirCompressor> ADVANCED_AIR_COMPRESSOR = null;
    public static final ContainerType<ContainerAdvancedLiquidCompressor> ADVANCED_LIQUID_COMPRESSOR = null;
    public static final ContainerType<ContainerAerialInterface> AERIAL_INTERFACE = null;
    public static final ContainerType<ContainerAirCannon> AIR_CANNON = null;
    public static final ContainerType<ContainerAirCompressor> AIR_COMPRESSOR = null;
    public static final ContainerType<ContainerAssemblyController> ASSEMBLY_CONTROLLER = null;
    public static final ContainerType<ContainerChargingStation> CHARGING_STATION = null;
    public static final ContainerType<ContainerChargingStationItemInventory> CHARGING_MINIGUN = null;
    public static final ContainerType<ContainerChargingStationItemInventory> CHARGING_DRONE = null;
    public static final ContainerType<ContainerChargingStationItemInventory> CHARGING_ARMOR = null;
    public static final ContainerType<ContainerCreativeCompressor> CREATIVE_COMPRESSOR = null;
    public static final ContainerType<ContainerElectrostaticCompressor> ELECTROSTATIC_COMPRESSOR = null;
    public static final ContainerType<ContainerElevator> ELEVATOR = null;
    public static final ContainerType<ContainerFluxCompressor> FLUX_COMPRESSOR = null;
    public static final ContainerType<ContainerGasLift> GAS_LIFT = null;
    public static final ContainerType<ContainerKeroseneLamp> KEROSENE_LAMP = null;
    public static final ContainerType<ContainerLiquidCompressor> LIQUID_COMPRESSOR = null;
    public static final ContainerType<ContainerLiquidHopper> LIQUID_HOPPER = null;
    public static final ContainerType<ContainerOmnidirectionalHopper> OMNIDIRECTIONAL_HOPPER = null;
    public static final ContainerType<ContainerPneumaticDoorBase> PNEUMATIC_DOOR_BASE = null;
    public static final ContainerType<ContainerPneumaticDynamo> PNEUMATIC_DYNAMO = null;
    public static final ContainerType<ContainerPressureChamberInterface> PRESSURE_CHAMBER_INTERFACE = null;
    public static final ContainerType<ContainerPressureChamberValve> PRESSURE_CHAMBER_VALVE = null;
    public static final ContainerType<ContainerProgrammableController> PROGRAMMABLE_CONTROLLER = null;
    public static final ContainerType<ContainerProgrammer> PROGRAMMER = null;
    public static final ContainerType<ContainerRefinery> REFINERY = null;
    public static final ContainerType<ContainerSecurityStationMain> SECURITY_STATION_MAIN = null;
    public static final ContainerType<ContainerSecurityStationHacking> SECURITY_STATION_HACKING = null;
    public static final ContainerType<ContainerSentryTurret> SENTRY_TURRET = null;
    public static final ContainerType<ContainerThermalCompressor> THERMAL_COMPRESSOR = null;
    public static final ContainerType<ContainerThermopneumaticProcessingPlant> THERMOPNEUMATIC_PROCESSING_PLANT = null;
    public static final ContainerType<ContainerUniversalSensor> UNIVERSAL_SENSOR = null;
    public static final ContainerType<ContainerUVLightBox> UV_LIGHT_BOX = null;
    public static final ContainerType<ContainerVacuumPump> VACUUM_PUMP = null;
    public static final ContainerType<ContainerAmadron> AMADRON = null;
    public static final ContainerType<ContainerAmadronAddTrade> AMADRON_ADD_TRADE = null;
    public static final ContainerType<ContainerMinigunMagazine> MINIGUN_MAGAZINE = null;
    public static final ContainerType<ContainerLogistics> LOGISTICS_FRAME_STORAGE = null;
    public static final ContainerType<ContainerLogistics> LOGISTICS_FRAME_DEFAULT_STORAGE = null;
    public static final ContainerType<ContainerLogistics> LOGISTICS_FRAME_PASSIVE_PROVIDER = null;
    public static final ContainerType<ContainerLogistics> LOGISTICS_FRAME_REQUESTER = null;
    public static final ContainerType<ContainerInventorySearcher> INVENTORY_SEARCHER = null;
    public static final ContainerType<ContainerRemote> REMOTE = null;
    public static final ContainerType<ContainerRemote> REMOTE_EDITOR = null;
    public static final ContainerType<ContainerSearcher> SEARCHER = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event) {
            IForgeRegistry<ContainerType<?>> registry = event.getRegistry();

            registry.register(IForgeContainerType.create(ContainerAdvancedAirCompressor::new)
                    .setRegistryName("advanced_air_compressor"));
            registry.register(IForgeContainerType.create(ContainerAdvancedLiquidCompressor::new)
                    .setRegistryName("advanced_liquid_compressor"));
            registry.register(IForgeContainerType.create(ContainerAerialInterface::new)
                    .setRegistryName("aerial_interface"));
            registry.register(IForgeContainerType.create(ContainerAirCompressor::new)
                    .setRegistryName("air_compressor"));
            registry.register(IForgeContainerType.create(ContainerAirCannon::new)
                    .setRegistryName("air_cannon"));
            registry.register(IForgeContainerType.create(ContainerAssemblyController::new)
                    .setRegistryName("assembly_controller"));
            registry.register(IForgeContainerType.create(ContainerChargingStation::new)
                    .setRegistryName("charging_station"));
            registry.register(IForgeContainerType.create(ContainerChargingStationItemInventory::createMinigunContainer)
                    .setRegistryName("charging_minigun"));
            registry.register(IForgeContainerType.create(ContainerChargingStationItemInventory::createDroneContainer)
                    .setRegistryName("charging_drone"));
            registry.register(IForgeContainerType.create(ContainerChargingStationItemInventory::createArmorContainer)
                    .setRegistryName("charging_armor"));
            registry.register(IForgeContainerType.create(ContainerCreativeCompressor::new)
                    .setRegistryName("creative_compressor"));
            registry.register(IForgeContainerType.create(ContainerElectrostaticCompressor::new)
                    .setRegistryName("electrostatic_compressor"));
            registry.register(IForgeContainerType.create(ContainerElevator::new)
                    .setRegistryName("elevator"));
            registry.register(IForgeContainerType.create(ContainerFluxCompressor::new)
                    .setRegistryName("flux_compressor"));
            registry.register(IForgeContainerType.create(ContainerGasLift::new)
                    .setRegistryName("gas_lift"));
            registry.register(IForgeContainerType.create(ContainerKeroseneLamp::new)
                    .setRegistryName("kerosene_lamp"));
            registry.register(IForgeContainerType.create(ContainerLiquidCompressor::new)
                    .setRegistryName("liquid_compressor"));
            registry.register(IForgeContainerType.create(ContainerLiquidHopper::new)
                    .setRegistryName("liquid_hopper"));
            registry.register(IForgeContainerType.create(ContainerOmnidirectionalHopper::new)
                    .setRegistryName("omnidirectional_hopper"));
            registry.register(IForgeContainerType.create(ContainerPneumaticDoorBase::new)
                    .setRegistryName("pneumatic_door_base"));
            registry.register(IForgeContainerType.create(ContainerPneumaticDynamo::new)
                    .setRegistryName("pneumatic_dynamo"));
            registry.register(IForgeContainerType.create(ContainerPressureChamberInterface::new)
                    .setRegistryName("pressure_chamber_interface"));
            registry.register(IForgeContainerType.create(ContainerPressureChamberValve::new)
                    .setRegistryName("pressure_chamber_valve"));
            registry.register(IForgeContainerType.create(ContainerProgrammableController::new)
                    .setRegistryName("programmable_controller"));
            registry.register(IForgeContainerType.create(ContainerProgrammer::new)
                    .setRegistryName("programmer"));
            registry.register(IForgeContainerType.create(ContainerRefinery::new)
                    .setRegistryName("refinery"));
            registry.register(IForgeContainerType.create(ContainerSecurityStationMain::new)
                    .setRegistryName("security_station_main"));
            registry.register(IForgeContainerType.create(ContainerSecurityStationHacking::new)
                    .setRegistryName("security_station_hacking"));
            registry.register(IForgeContainerType.create(ContainerSentryTurret::new)
                    .setRegistryName("sentry_turret"));
            registry.register(IForgeContainerType.create(ContainerThermalCompressor::new)
                    .setRegistryName("thermal_compressor"));
            registry.register(IForgeContainerType.create(ContainerThermopneumaticProcessingPlant::new)
                    .setRegistryName("thermopneumatic_processing_plant"));
            registry.register(IForgeContainerType.create(ContainerUniversalSensor::new)
                    .setRegistryName("universal_sensor"));
            registry.register(IForgeContainerType.create(ContainerUVLightBox::new)
                    .setRegistryName("uv_light_box"));
            registry.register(IForgeContainerType.create(ContainerVacuumPump::new)
                    .setRegistryName("vacuum_pump"));
            registry.register(IForgeContainerType.create(ContainerAmadron::new)
                    .setRegistryName("amadron"));
            registry.register(IForgeContainerType.create(ContainerAmadronAddTrade::new)
                    .setRegistryName("amadron_add_trade"));
            registry.register(IForgeContainerType.create(ContainerMinigunMagazine::new)
                    .setRegistryName("minigun_magazine"));
            registry.register(IForgeContainerType.create(ContainerLogistics::createPassiveProviderContainer)
                    .setRegistryName(SemiBlockPassiveProvider.ID));
            registry.register(IForgeContainerType.create(ContainerLogistics::createRequesterContainer)
                    .setRegistryName(SemiBlockRequester.ID));
            registry.register(IForgeContainerType.create(ContainerLogistics::createDefaultStorageContainer)
                    .setRegistryName(SemiBlockDefaultStorage.ID));
            registry.register(IForgeContainerType.create(ContainerLogistics::createStorageContainer)
                    .setRegistryName(SemiBlockDefaultStorage.ID));
            registry.register(IForgeContainerType.create(ContainerInventorySearcher::new)
                    .setRegistryName("inventory_searcher"));
            registry.register(IForgeContainerType.create(ContainerRemote::createRemoteContainer)
                    .setRegistryName("remote"));
            registry.register(IForgeContainerType.create(ContainerRemote::createRemoteEditorContainer)
                    .setRegistryName("remote"));
            registry.register(IForgeContainerType.create(ContainerSearcher::new)
                    .setRegistryName("searcher"));
        }
    }
}
