package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModTileEntityTypes {
    public static final TileEntityType<TileEntityPressureTube> PRESSURE_TUBE = null;
    public static final TileEntityType<TileEntityAdvancedPressureTube> ADVANCED_PRESSURE_TUBE = null;
    public static final TileEntityType<TileEntityAirCompressor> AIR_COMPRESSOR = null;
    public static final TileEntityType<TileEntityAdvancedAirCompressor> ADVANCED_AIR_COMPRESSOR = null;
    public static final TileEntityType<TileEntityAirCannon> AIR_CANNON = null;
    public static final TileEntityType<TileEntityPressureChamberWall> PRESSURE_CHAMBER_WALL = null;
    public static final TileEntityType<TileEntityPressureChamberGlass> PRESSURE_CHAMBER_GLASS = null;
    public static final TileEntityType<TileEntityPressureChamberValve> PRESSURE_CHAMBER_VALVE = null;
    public static final TileEntityType<TileEntityChargingStation> CHARGING_STATION = null;
    public static final TileEntityType<TileEntityElevatorBase> ELEVATOR_BASE = null;
    public static final TileEntityType<TileEntityElevatorFrame> ELEVATOR_FRAME = null;
    public static final TileEntityType<TileEntityPressureChamberInterface> PRESSURE_CHAMBER_INTERFACE = null;
    public static final TileEntityType<TileEntityVacuumPump> VACUUM_PUMP = null;
    public static final TileEntityType<TileEntityPneumaticDoorBase> PNEUMATIC_DOOR_BASE = null;
    public static final TileEntityType<TileEntityPneumaticDoor> PNEUMATIC_DOOR = null;
    public static final TileEntityType<TileEntityAssemblyIOUnit> ASSEMBLY_IO_UNIT = null;
    public static final TileEntityType<TileEntityAssemblyPlatform> ASSEMBLY_PLATFORM = null;
    public static final TileEntityType<TileEntityAssemblyDrill> ASSEMBLY_DRILL = null;
    public static final TileEntityType<TileEntityAssemblyLaser> ASSEMBLY_LASER = null;
    public static final TileEntityType<TileEntityAssemblyController> ASSEMBLY_CONTROLLER = null;
    public static final TileEntityType<?> UV_LIGHT_BOX = null;
    public static final TileEntityType<?> SECURITY_STATION = null;
    public static final TileEntityType<?> UNIVERSAL_SENSOR = null;
    public static final TileEntityType<?> AERIAL_INTERFACE = null;
    public static final TileEntityType<?> ELECTROSTATIC_COMPRESSOR = null;
    public static final TileEntityType<?> APHORISM_TILE = null;
    public static final TileEntityType<?> OMNIDIRECTIONAL_HOPPER = null;
    public static final TileEntityType<?> LIQUID_HOPPER = null;
    public static final TileEntityType<?> ELEVATOR_CALLER = null;
    public static final TileEntityType<?> PROGRAMMER = null;
    public static final TileEntityType<?> CREATIVE_COMPRESSOR = null;
    public static final TileEntityType<?> LIQUID_COMPRESSOR = null;
    public static final TileEntityType<?> ADVANCED_LIQUID_COMPRESSOR = null;
    public static final TileEntityType<?> DRONE_REDSTONE_EMITTER = null;
    public static final TileEntityType<?> COMPRESSED_IRON_BLOCK = null;
    public static final TileEntityType<?> HEAT_SINK = null;
    public static final TileEntityType<?> VORTEX_TUBE = null;
    public static final TileEntityType<?> PROGRAMMABLE_CONTROLLER = null;
    public static final TileEntityType<?> GAS_LIFT = null;
    public static final TileEntityType<TileEntityRefineryController> REFINERY = null;
    public static final TileEntityType<TileEntityRefineryOutput> REFINERY_OUTPUT = null;
    public static final TileEntityType<?> THERMOPNEUMATIC_PROCESSING_PLANT = null;
    public static final TileEntityType<?> KEROSENE_LAMP = null;
    public static final TileEntityType<?> SENTRY_TURRET = null;
    public static final TileEntityType<?> FLUX_COMPRESSOR = null;
    public static final TileEntityType<?> PNEUMATIC_DYNAMO = null;
    public static final TileEntityType<?> THERMAL_COMPRESSOR = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPressureTube::new, ModBlocks.PRESSURE_TUBE)
                    .build(null).setRegistryName(RL("pressure_tube")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAdvancedPressureTube::new, ModBlocks.ADVANCED_PRESSURE_TUBE)
                    .build(null).setRegistryName(RL("advanced_pressure_tube")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAirCompressor::new, ModBlocks.AIR_COMPRESSOR)
                    .build(null).setRegistryName(RL("air_compressor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAdvancedAirCompressor::new, ModBlocks.ADVANCED_AIR_COMPRESSOR)
                    .build(null).setRegistryName(RL("advanced_air_compressor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAirCannon::new, ModBlocks.AIR_CANNON)
                    .build(null).setRegistryName(RL("air_cannon")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPressureChamberWall::new, ModBlocks.PRESSURE_CHAMBER_WALL)
                    .build(null).setRegistryName(RL("pressure_chamber_wall")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPressureChamberGlass::new, ModBlocks.PRESSURE_CHAMBER_GLASS)
                    .build(null).setRegistryName(RL("pressure_chamber_glass")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPressureChamberValve::new, ModBlocks.PRESSURE_CHAMBER_VALVE)
                    .build(null).setRegistryName(RL("pressure_chamber_valve")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityChargingStation::new, ModBlocks.CHARGING_STATION)
                    .build(null).setRegistryName(RL("charging_station")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityElevatorBase::new, ModBlocks.ELEVATOR_BASE)
                    .build(null).setRegistryName(RL("elevator_base")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityElevatorFrame::new, ModBlocks.ELEVATOR_FRAME)
                    .build(null).setRegistryName(RL("elevator_frame")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPressureChamberInterface::new, ModBlocks.PRESSURE_CHAMBER_INTERFACE)
                    .build(null).setRegistryName(RL("pressure_chamber_interface")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityVacuumPump::new, ModBlocks.VACUUM_PUMP)
                    .build(null).setRegistryName(RL("vacuum_pump")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPneumaticDoorBase::new, ModBlocks.PNEUMATIC_DOOR_BASE)
                    .build(null).setRegistryName(RL("pneumatic_door_base")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPneumaticDoor::new, ModBlocks.PNEUMATIC_DOOR)
                    .build(null).setRegistryName(RL("pneumatic_door")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAssemblyIOUnit::new, ModBlocks.ASSEMBLY_IO_UNIT_IMPORT, ModBlocks.ASSEMBLY_IO_UNIT_EXPORT)
                    .build(null).setRegistryName(RL("assembly_io_unit")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAssemblyPlatform::new, ModBlocks.ASSEMBLY_PLATFORM)
                    .build(null).setRegistryName(RL("assembly_platform")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAssemblyDrill::new, ModBlocks.ASSEMBLY_DRILL)
                    .build(null).setRegistryName(RL("assembly_drill")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAssemblyLaser::new, ModBlocks.ASSEMBLY_LASER)
                    .build(null).setRegistryName(RL("assembly_laser")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAssemblyController::new, ModBlocks.ASSEMBLY_CONTROLLER)
                    .build(null).setRegistryName(RL("assembly_controller")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityUVLightBox::new, ModBlocks.UV_LIGHT_BOX)
                    .build(null).setRegistryName(RL("uv_light_box")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntitySecurityStation::new, ModBlocks.SECURITY_STATION)
                    .build(null).setRegistryName(RL("security_station")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityUniversalSensor::new, ModBlocks.UNIVERSAL_SENSOR)
                    .build(null).setRegistryName(RL("universal_sensor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAerialInterface::new, ModBlocks.AERIAL_INTERFACE)
                    .build(null).setRegistryName(RL("aerial_interface")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityElectrostaticCompressor::new, ModBlocks.ELECTROSTATIC_COMPRESSOR)
                    .build(null).setRegistryName(RL("electrostatic_compressor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAphorismTile::new, ModBlocks.APHORISM_TILE)
                    .build(null).setRegistryName(RL("aphorism_tile")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityOmnidirectionalHopper::new, ModBlocks.OMNIDIRECTIONAL_HOPPER)
                    .build(null).setRegistryName(RL("omnidirectional_hopper")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityLiquidHopper::new, ModBlocks.LIQUID_HOPPER)
                    .build(null).setRegistryName(RL("liquid_hopper")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityElevatorCaller::new, ModBlocks.ELEVATOR_CALLER)
                    .build(null).setRegistryName(RL("elevator_caller")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityProgrammer::new, ModBlocks.PROGRAMMER)
                    .build(null).setRegistryName(RL("programmer")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityCreativeCompressor::new, ModBlocks.CREATIVE_COMPRESSOR)
                    .build(null).setRegistryName(RL("creative_compressor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityLiquidCompressor::new, ModBlocks.LIQUID_COMPRESSOR)
                    .build(null).setRegistryName(RL("liquid_compressor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityAdvancedLiquidCompressor::new, ModBlocks.ADVANCED_LIQUID_COMPRESSOR)
                    .build(null).setRegistryName(RL("advanced_liquid_compressor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityDroneRedstoneEmitter::new, ModBlocks.DRONE_REDSTONE_EMITTER)
                    .build(null).setRegistryName(RL("drone_redstone_emitter")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityCompressedIronBlock::new, ModBlocks.COMPRESSED_IRON_BLOCK)
                    .build(null).setRegistryName(RL("compressed_iron_block")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityHeatSink::new, ModBlocks.HEAT_SINK)
                    .build(null).setRegistryName(RL("heat_sink")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityVortexTube::new, ModBlocks.VORTEX_TUBE)
                    .build(null).setRegistryName(RL("vortex_tube")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityProgrammableController::new, ModBlocks.PROGRAMMABLE_CONTROLLER)
                    .build(null).setRegistryName(RL("programmable_controller")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityGasLift::new, ModBlocks.GAS_LIFT)
                    .build(null).setRegistryName(RL("gas_lift")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityRefineryController::new, ModBlocks.REFINERY)
                    .build(null).setRegistryName(RL("refinery")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityRefineryOutput::new, ModBlocks.REFINERY_OUTPUT)
                    .build(null).setRegistryName(RL("refinery_output")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityThermopneumaticProcessingPlant::new, ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT)
                    .build(null).setRegistryName(RL("thermopneumatic_processing_plant")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityKeroseneLamp::new, ModBlocks.KEROSENE_LAMP)
                    .build(null).setRegistryName(RL("kerosene_lamp")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntitySentryTurret::new, ModBlocks.SENTRY_TURRET)
                    .build(null).setRegistryName(RL("sentry_turret")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityFluxCompressor::new, ModBlocks.FLUX_COMPRESSOR)
                    .build(null).setRegistryName(RL("flux_compressor")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityPneumaticDynamo::new, ModBlocks.PNEUMATIC_DYNAMO)
                    .build(null).setRegistryName(RL("pneumatic_dynamo")));
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityThermalCompressor::new, ModBlocks.THERMAL_COMPRESSOR)
                    .build(null).setRegistryName(RL("thermal_compressor")));

        }
    }
}
