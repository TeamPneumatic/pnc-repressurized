package me.desht.pneumaticcraft.common.core;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Names.MOD_ID);

    public static final RegistryObject<TileEntityType<TileEntityPressureTube>> PRESSURE_TUBE
            = register("pressure_tube", () -> new TileEntityType<>(TileEntityPressureTube::new, ImmutableSet.of(ModBlocks.PRESSURE_TUBE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAdvancedPressureTube>> ADVANCED_PRESSURE_TUBE
            = register("advanced_pressure_tube", () -> new TileEntityType<>(TileEntityAdvancedPressureTube::new, ImmutableSet.of(ModBlocks.ADVANCED_PRESSURE_TUBE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAirCompressor>> AIR_COMPRESSOR
            = register("air_compressor", () -> new TileEntityType<>(TileEntityAirCompressor::new, ImmutableSet.of(ModBlocks.AIR_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAdvancedAirCompressor>> ADVANCED_AIR_COMPRESSOR
            = register("advanced_air_compressor", () -> new TileEntityType<>(TileEntityAdvancedAirCompressor::new, ImmutableSet.of(ModBlocks.ADVANCED_AIR_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAirCannon>> AIR_CANNON
            = register("air_cannon", () -> new TileEntityType<>(TileEntityAirCannon::new, ImmutableSet.of(ModBlocks.AIR_CANNON.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityPressureChamberWall>> PRESSURE_CHAMBER_WALL
            = register("pressure_chamber_wall", () -> new TileEntityType<>(TileEntityPressureChamberWall::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_WALL.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityPressureChamberGlass>> PRESSURE_CHAMBER_GLASS
            = register("pressure_chamber_glass", () -> new TileEntityType<>(TileEntityPressureChamberGlass::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_GLASS.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityPressureChamberValve>> PRESSURE_CHAMBER_VALVE
            = register("pressure_chamber_valve", () -> new TileEntityType<>(TileEntityPressureChamberValve::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_VALVE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityChargingStation>> CHARGING_STATION
            = register("charging_station", () -> new TileEntityType<>(TileEntityChargingStation::new, ImmutableSet.of(ModBlocks.CHARGING_STATION.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityElevatorBase>> ELEVATOR_BASE
            = register("elevator_base", () -> new TileEntityType<>(TileEntityElevatorBase::new, ImmutableSet.of(ModBlocks.ELEVATOR_BASE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityElevatorFrame>> ELEVATOR_FRAME
            = register("elevator_frame", () -> new TileEntityType<>(TileEntityElevatorFrame::new, ImmutableSet.of(ModBlocks.ELEVATOR_FRAME.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityPressureChamberInterface>> PRESSURE_CHAMBER_INTERFACE
            = register("pressure_chamber_interface", () -> new TileEntityType<>(TileEntityPressureChamberInterface::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityVacuumPump>> VACUUM_PUMP
            = register("vacuum_pump", () -> new TileEntityType<>(TileEntityVacuumPump::new, ImmutableSet.of(ModBlocks.VACUUM_PUMP.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityPneumaticDoorBase>> PNEUMATIC_DOOR_BASE
            = register("pneumatic_door_base", () -> new TileEntityType<>(TileEntityPneumaticDoorBase::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DOOR_BASE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityPneumaticDoor>> PNEUMATIC_DOOR
            = register("pneumatic_door", () -> new TileEntityType<>(TileEntityPneumaticDoor::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DOOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAssemblyIOUnit>> ASSEMBLY_IO_UNIT
            = register("assembly_io_unit", () -> new TileEntityType<>(TileEntityAssemblyIOUnit::new, ImmutableSet.of(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get(), ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAssemblyPlatform>> ASSEMBLY_PLATFORM
            = register("assembly_platform", () -> new TileEntityType<>(TileEntityAssemblyPlatform::new, ImmutableSet.of(ModBlocks.ASSEMBLY_PLATFORM.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAssemblyDrill>> ASSEMBLY_DRILL
            = register("assembly_drill", () -> new TileEntityType<>(TileEntityAssemblyDrill::new, ImmutableSet.of(ModBlocks.ASSEMBLY_DRILL.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAssemblyLaser>> ASSEMBLY_LASER
            = register("assembly_laser", () -> new TileEntityType<>(TileEntityAssemblyLaser::new, ImmutableSet.of(ModBlocks.ASSEMBLY_LASER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAssemblyController>> ASSEMBLY_CONTROLLER
            = register("assembly_controller", () -> new TileEntityType<>(TileEntityAssemblyController::new, ImmutableSet.of(ModBlocks.ASSEMBLY_CONTROLLER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityUVLightBox>> UV_LIGHT_BOX
            = register("uv_light_box", () -> new TileEntityType<>(TileEntityUVLightBox::new, ImmutableSet.of(ModBlocks.UV_LIGHT_BOX.get()), null));
    public static final RegistryObject<TileEntityType<TileEntitySecurityStation>> SECURITY_STATION
            = register("security_station", () -> new TileEntityType<>(TileEntitySecurityStation::new, ImmutableSet.of(ModBlocks.SECURITY_STATION.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityUniversalSensor>> UNIVERSAL_SENSOR
            = register("universal_sensor", () -> new TileEntityType<>(TileEntityUniversalSensor::new, ImmutableSet.of(ModBlocks.UNIVERSAL_SENSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAerialInterface>> AERIAL_INTERFACE
            = register("aerial_interface", () -> new TileEntityType<>(TileEntityAerialInterface::new, ImmutableSet.of(ModBlocks.AERIAL_INTERFACE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityElectrostaticCompressor>> ELECTROSTATIC_COMPRESSOR
            = register("electrostatic_compressor", () -> new TileEntityType<>(TileEntityElectrostaticCompressor::new, ImmutableSet.of(ModBlocks.ELECTROSTATIC_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAphorismTile>> APHORISM_TILE
            = register("aphorism_tile", () -> new TileEntityType<>(TileEntityAphorismTile::new, ImmutableSet.of(ModBlocks.APHORISM_TILE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityOmnidirectionalHopper>> OMNIDIRECTIONAL_HOPPER
            = register("omnidirectional_hopper", () -> new TileEntityType<>(TileEntityOmnidirectionalHopper::new, ImmutableSet.of(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityLiquidHopper>> LIQUID_HOPPER
            = register("liquid_hopper", () -> new TileEntityType<>(TileEntityLiquidHopper::new, ImmutableSet.of(ModBlocks.LIQUID_HOPPER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityElevatorCaller>> ELEVATOR_CALLER
            = register("elevator_caller", () -> new TileEntityType<>(TileEntityElevatorCaller::new, ImmutableSet.of(ModBlocks.ELEVATOR_CALLER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityProgrammer>> PROGRAMMER
            = register("programmer", () -> new TileEntityType<>(TileEntityProgrammer::new, ImmutableSet.of(ModBlocks.PROGRAMMER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityCreativeCompressor>> CREATIVE_COMPRESSOR
            = register("creative_compressor", () -> new TileEntityType<>(TileEntityCreativeCompressor::new, ImmutableSet.of(ModBlocks.CREATIVE_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityLiquidCompressor>> LIQUID_COMPRESSOR
            = register("liquid_compressor", () -> new TileEntityType<>(TileEntityLiquidCompressor::new, ImmutableSet.of(ModBlocks.LIQUID_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityAdvancedLiquidCompressor>> ADVANCED_LIQUID_COMPRESSOR
            = register("advanced_liquid_compressor", () -> new TileEntityType<>(TileEntityAdvancedLiquidCompressor::new, ImmutableSet.of(ModBlocks.ADVANCED_LIQUID_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityDroneRedstoneEmitter>> DRONE_REDSTONE_EMITTER
            = register("drone_redstone_emitter", () -> new TileEntityType<>(TileEntityDroneRedstoneEmitter::new, ImmutableSet.of(ModBlocks.DRONE_REDSTONE_EMITTER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityCompressedIronBlock>> COMPRESSED_IRON_BLOCK
            = register("compressed_iron_block", () -> new TileEntityType<>(TileEntityCompressedIronBlock::new, ImmutableSet.of(ModBlocks.COMPRESSED_IRON_BLOCK.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityHeatSink>> HEAT_SINK
            = register("heat_sink", () -> new TileEntityType<>(TileEntityHeatSink::new, ImmutableSet.of(ModBlocks.HEAT_SINK.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityVortexTube>> VORTEX_TUBE
            = register("vortex_tube", () -> new TileEntityType<>(TileEntityVortexTube::new, ImmutableSet.of(ModBlocks.VORTEX_TUBE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityProgrammableController>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", () -> new TileEntityType<>(TileEntityProgrammableController::new, ImmutableSet.of(ModBlocks.PROGRAMMABLE_CONTROLLER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityGasLift>> GAS_LIFT
            = register("gas_lift", () -> new TileEntityType<>(TileEntityGasLift::new, ImmutableSet.of(ModBlocks.GAS_LIFT.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityRefineryController>> REFINERY
            = register("refinery", () -> new TileEntityType<>(TileEntityRefineryController::new, ImmutableSet.of(ModBlocks.REFINERY.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityRefineryOutput>> REFINERY_OUTPUT
            = register("refinery_output", () -> new TileEntityType<>(TileEntityRefineryOutput::new, ImmutableSet.of(ModBlocks.REFINERY_OUTPUT.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityThermopneumaticProcessingPlant>> THERMOPNEUMATIC_PROCESSING_PLANT
            = register("thermopneumatic_processing_plant", () -> new TileEntityType<>(TileEntityThermopneumaticProcessingPlant::new, ImmutableSet.of(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityKeroseneLamp>> KEROSENE_LAMP
            = register("kerosene_lamp", () -> new TileEntityType<>(TileEntityKeroseneLamp::new, ImmutableSet.of(ModBlocks.KEROSENE_LAMP.get()), null));
    public static final RegistryObject<TileEntityType<TileEntitySentryTurret>> SENTRY_TURRET
            = register("sentry_turret", () -> new TileEntityType<>(TileEntitySentryTurret::new, ImmutableSet.of(ModBlocks.SENTRY_TURRET.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityFluxCompressor>> FLUX_COMPRESSOR
            = register("flux_compressor", () -> new TileEntityType<>(TileEntityFluxCompressor::new, ImmutableSet.of(ModBlocks.FLUX_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityPneumaticDynamo>> PNEUMATIC_DYNAMO
            = register("pneumatic_dynamo", () -> new TileEntityType<>(TileEntityPneumaticDynamo::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DYNAMO.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityThermalCompressor>> THERMAL_COMPRESSOR
            = register("thermal_compressor", () -> new TileEntityType<>(TileEntityThermalCompressor::new, ImmutableSet.of(ModBlocks.THERMAL_COMPRESSOR.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityHeatPipe>> HEAT_PIPE
            = register("heat_pipe", () -> new TileEntityType<>(TileEntityHeatPipe::new, ImmutableSet.of(ModBlocks.HEAT_PIPE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityEtchingTank>> ETCHING_TANK
            = register("etching_tank", () -> new TileEntityType<>(TileEntityEtchingTank::new, ImmutableSet.of(ModBlocks.ETCHING_TANK.get()),
    null));
    public static final RegistryObject<TileEntityType<TileEntityFluidTank.Small>> TANK_SMALL
            = register("small_tank", () -> new TileEntityType<>(TileEntityFluidTank.Small::new, ImmutableSet.of(ModBlocks.TANK_SMALL.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityFluidTank.Medium>> TANK_MEDIUM
            = register("medium_tank", () -> new TileEntityType<>(TileEntityFluidTank.Medium::new, ImmutableSet.of(ModBlocks.TANK_MEDIUM.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityFluidTank.Large>> TANK_LARGE
            = register("large_tank", () -> new TileEntityType<>(TileEntityFluidTank.Large::new, ImmutableSet.of(ModBlocks.TANK_LARGE.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityReinforcedChest>> REINFORCED_CHEST
            = register("reinforced_chest", () -> new TileEntityType<>(TileEntityReinforcedChest::new, ImmutableSet.of(ModBlocks.REINFORCED_CHEST.get()),null));

    private static <T extends TileEntityType<?>> RegistryObject<T> register(String name, Supplier<T> sup) {
        return TILE_ENTITIES.register(name, sup);
    }
}
