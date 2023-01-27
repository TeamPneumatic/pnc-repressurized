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

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.DroneInterfaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Names.MOD_ID);

    public static final RegistryObject<BlockEntityType<PressureTubeBlockEntity>> PRESSURE_TUBE
            = register("pressure_tube", () -> new BlockEntityType<>(PressureTubeBlockEntity::new, ImmutableSet.of(ModBlocks.PRESSURE_TUBE.get()), null));
    public static final RegistryObject<BlockEntityType<AdvancedPressureTubeBlockEntity>> ADVANCED_PRESSURE_TUBE
            = register("advanced_pressure_tube", () -> new BlockEntityType<>(AdvancedPressureTubeBlockEntity::new, ImmutableSet.of(ModBlocks.ADVANCED_PRESSURE_TUBE.get()), null));
    public static final RegistryObject<BlockEntityType<AirCompressorBlockEntity>> AIR_COMPRESSOR
            = register("air_compressor", () -> new BlockEntityType<>(AirCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.AIR_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<AdvancedAirCompressorBlockEntity>> ADVANCED_AIR_COMPRESSOR
            = register("advanced_air_compressor", () -> new BlockEntityType<>(AdvancedAirCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.ADVANCED_AIR_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<AirCannonBlockEntity>> AIR_CANNON
            = register("air_cannon", () -> new BlockEntityType<>(AirCannonBlockEntity::new, ImmutableSet.of(ModBlocks.AIR_CANNON.get()), null));
    public static final RegistryObject<BlockEntityType<ManualCompressorBlockEntity>> MANUAL_COMPRESSOR
            = register("manual_compressor", () -> new BlockEntityType<>(ManualCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.MANUAL_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<PressureChamberWallBlockEntity>> PRESSURE_CHAMBER_WALL
            = register("pressure_chamber_wall", () -> new BlockEntityType<>(PressureChamberWallBlockEntity::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_WALL.get(), ModBlocks.PRESSURE_CHAMBER_GLASS.get()), null));
    public static final RegistryObject<BlockEntityType<PressureChamberValveBlockEntity>> PRESSURE_CHAMBER_VALVE
            = register("pressure_chamber_valve", () -> new BlockEntityType<>(PressureChamberValveBlockEntity::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_VALVE.get()), null));
    public static final RegistryObject<BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION
            = register("charging_station", () -> new BlockEntityType<>(ChargingStationBlockEntity::new, ImmutableSet.of(ModBlocks.CHARGING_STATION.get()), null));
    public static final RegistryObject<BlockEntityType<ElevatorBaseBlockEntity>> ELEVATOR_BASE
            = register("elevator_base", () -> new BlockEntityType<>(ElevatorBaseBlockEntity::new, ImmutableSet.of(ModBlocks.ELEVATOR_BASE.get()), null));
    public static final RegistryObject<BlockEntityType<ElevatorFrameBlockEntity>> ELEVATOR_FRAME
            = register("elevator_frame", () -> new BlockEntityType<>(ElevatorFrameBlockEntity::new, ImmutableSet.of(ModBlocks.ELEVATOR_FRAME.get()), null));
    public static final RegistryObject<BlockEntityType<PressureChamberInterfaceBlockEntity>> PRESSURE_CHAMBER_INTERFACE
            = register("pressure_chamber_interface", () -> new BlockEntityType<>(PressureChamberInterfaceBlockEntity::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get()), null));
    public static final RegistryObject<BlockEntityType<VacuumPumpBlockEntity>> VACUUM_PUMP
            = register("vacuum_pump", () -> new BlockEntityType<>(VacuumPumpBlockEntity::new, ImmutableSet.of(ModBlocks.VACUUM_PUMP.get()), null));
    public static final RegistryObject<BlockEntityType<PneumaticDoorBaseBlockEntity>> PNEUMATIC_DOOR_BASE
            = register("pneumatic_door_base", () -> new BlockEntityType<>(PneumaticDoorBaseBlockEntity::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DOOR_BASE.get()), null));
    public static final RegistryObject<BlockEntityType<PneumaticDoorBlockEntity>> PNEUMATIC_DOOR
            = register("pneumatic_door", () -> new BlockEntityType<>(PneumaticDoorBlockEntity::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DOOR.get()), null));
    public static final RegistryObject<BlockEntityType<AssemblyIOUnitBlockEntity>> ASSEMBLY_IO_UNIT
            = register("assembly_io_unit", () -> new BlockEntityType<>(AssemblyIOUnitBlockEntity::new, ImmutableSet.of(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get(), ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get()), null));
    public static final RegistryObject<BlockEntityType<AssemblyPlatformBlockEntity>> ASSEMBLY_PLATFORM
            = register("assembly_platform", () -> new BlockEntityType<>(AssemblyPlatformBlockEntity::new, ImmutableSet.of(ModBlocks.ASSEMBLY_PLATFORM.get()), null));
    public static final RegistryObject<BlockEntityType<AssemblyDrillBlockEntity>> ASSEMBLY_DRILL
            = register("assembly_drill", () -> new BlockEntityType<>(AssemblyDrillBlockEntity::new, ImmutableSet.of(ModBlocks.ASSEMBLY_DRILL.get()), null));
    public static final RegistryObject<BlockEntityType<AssemblyLaserBlockEntity>> ASSEMBLY_LASER
            = register("assembly_laser", () -> new BlockEntityType<>(AssemblyLaserBlockEntity::new, ImmutableSet.of(ModBlocks.ASSEMBLY_LASER.get()), null));
    public static final RegistryObject<BlockEntityType<AssemblyControllerBlockEntity>> ASSEMBLY_CONTROLLER
            = register("assembly_controller", () -> new BlockEntityType<>(AssemblyControllerBlockEntity::new, ImmutableSet.of(ModBlocks.ASSEMBLY_CONTROLLER.get()), null));
    public static final RegistryObject<BlockEntityType<UVLightBoxBlockEntity>> UV_LIGHT_BOX
            = register("uv_light_box", () -> new BlockEntityType<>(UVLightBoxBlockEntity::new, ImmutableSet.of(ModBlocks.UV_LIGHT_BOX.get()), null));
    public static final RegistryObject<BlockEntityType<SecurityStationBlockEntity>> SECURITY_STATION
            = register("security_station", () -> new BlockEntityType<>(SecurityStationBlockEntity::new, ImmutableSet.of(ModBlocks.SECURITY_STATION.get()), null));
    public static final RegistryObject<BlockEntityType<UniversalSensorBlockEntity>> UNIVERSAL_SENSOR
            = register("universal_sensor", () -> new BlockEntityType<>(UniversalSensorBlockEntity::new, ImmutableSet.of(ModBlocks.UNIVERSAL_SENSOR.get()), null));
    public static final RegistryObject<BlockEntityType<AerialInterfaceBlockEntity>> AERIAL_INTERFACE
            = register("aerial_interface", () -> new BlockEntityType<>(AerialInterfaceBlockEntity::new, ImmutableSet.of(ModBlocks.AERIAL_INTERFACE.get()), null));
    public static final RegistryObject<BlockEntityType<ElectrostaticCompressorBlockEntity>> ELECTROSTATIC_COMPRESSOR
            = register("electrostatic_compressor", () -> new BlockEntityType<>(ElectrostaticCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.ELECTROSTATIC_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<AphorismTileBlockEntity>> APHORISM_TILE
            = register("aphorism_tile", () -> new BlockEntityType<>(AphorismTileBlockEntity::new, ImmutableSet.of(ModBlocks.APHORISM_TILE.get()), null));
    public static final RegistryObject<BlockEntityType<OmnidirectionalHopperBlockEntity>> OMNIDIRECTIONAL_HOPPER
            = register("omnidirectional_hopper", () -> new BlockEntityType<>(OmnidirectionalHopperBlockEntity::new, ImmutableSet.of(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), null));
    public static final RegistryObject<BlockEntityType<LiquidHopperBlockEntity>> LIQUID_HOPPER
            = register("liquid_hopper", () -> new BlockEntityType<>(LiquidHopperBlockEntity::new, ImmutableSet.of(ModBlocks.LIQUID_HOPPER.get()), null));
    public static final RegistryObject<BlockEntityType<ElevatorCallerBlockEntity>> ELEVATOR_CALLER
            = register("elevator_caller", () -> new BlockEntityType<>(ElevatorCallerBlockEntity::new, ImmutableSet.of(ModBlocks.ELEVATOR_CALLER.get()), null));
    public static final RegistryObject<BlockEntityType<ProgrammerBlockEntity>> PROGRAMMER
            = register("programmer", () -> new BlockEntityType<>(ProgrammerBlockEntity::new, ImmutableSet.of(ModBlocks.PROGRAMMER.get()), null));
    public static final RegistryObject<BlockEntityType<CreativeCompressorBlockEntity>> CREATIVE_COMPRESSOR
            = register("creative_compressor", () -> new BlockEntityType<>(CreativeCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.CREATIVE_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<LiquidCompressorBlockEntity>> LIQUID_COMPRESSOR
            = register("liquid_compressor", () -> new BlockEntityType<>(LiquidCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.LIQUID_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<AdvancedLiquidCompressorBlockEntity>> ADVANCED_LIQUID_COMPRESSOR
            = register("advanced_liquid_compressor", () -> new BlockEntityType<>(AdvancedLiquidCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.ADVANCED_LIQUID_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<DroneRedstoneEmitterBlockEntity>> DRONE_REDSTONE_EMITTER
            = register("drone_redstone_emitter", () -> new BlockEntityType<>(DroneRedstoneEmitterBlockEntity::new, ImmutableSet.of(ModBlocks.DRONE_REDSTONE_EMITTER.get()), null));
    public static final RegistryObject<BlockEntityType<CompressedIronBlockBlockEntity>> COMPRESSED_IRON_BLOCK
            = register("compressed_iron_block", () -> new BlockEntityType<>(CompressedIronBlockBlockEntity::new, ImmutableSet.of(ModBlocks.COMPRESSED_IRON_BLOCK.get()), null));
    public static final RegistryObject<BlockEntityType<HeatSinkBlockEntity>> HEAT_SINK
            = register("heat_sink", () -> new BlockEntityType<>(HeatSinkBlockEntity::new, ImmutableSet.of(ModBlocks.HEAT_SINK.get()), null));
    public static final RegistryObject<BlockEntityType<VortexTubeBlockEntity>> VORTEX_TUBE
            = register("vortex_tube", () -> new BlockEntityType<>(VortexTubeBlockEntity::new, ImmutableSet.of(ModBlocks.VORTEX_TUBE.get()), null));
    public static final RegistryObject<BlockEntityType<ProgrammableControllerBlockEntity>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", () -> new BlockEntityType<>(ProgrammableControllerBlockEntity::new, ImmutableSet.of(ModBlocks.PROGRAMMABLE_CONTROLLER.get()), null));
    public static final RegistryObject<BlockEntityType<GasLiftBlockEntity>> GAS_LIFT
            = register("gas_lift", () -> new BlockEntityType<>(GasLiftBlockEntity::new, ImmutableSet.of(ModBlocks.GAS_LIFT.get()), null));
    public static final RegistryObject<BlockEntityType<RefineryControllerBlockEntity>> REFINERY
            = register("refinery", () -> new BlockEntityType<>(RefineryControllerBlockEntity::new, ImmutableSet.of(ModBlocks.REFINERY.get()), null));
    public static final RegistryObject<BlockEntityType<RefineryOutputBlockEntity>> REFINERY_OUTPUT
            = register("refinery_output", () -> new BlockEntityType<>(RefineryOutputBlockEntity::new, ImmutableSet.of(ModBlocks.REFINERY_OUTPUT.get()), null));
    public static final RegistryObject<BlockEntityType<ThermopneumaticProcessingPlantBlockEntity>> THERMOPNEUMATIC_PROCESSING_PLANT
            = register("thermopneumatic_processing_plant", () -> new BlockEntityType<>(ThermopneumaticProcessingPlantBlockEntity::new, ImmutableSet.of(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()), null));
    public static final RegistryObject<BlockEntityType<KeroseneLampBlockEntity>> KEROSENE_LAMP
            = register("kerosene_lamp", () -> new BlockEntityType<>(KeroseneLampBlockEntity::new, ImmutableSet.of(ModBlocks.KEROSENE_LAMP.get()), null));
    public static final RegistryObject<BlockEntityType<SentryTurretBlockEntity>> SENTRY_TURRET
            = register("sentry_turret", () -> new BlockEntityType<>(SentryTurretBlockEntity::new, ImmutableSet.of(ModBlocks.SENTRY_TURRET.get()), null));
    public static final RegistryObject<BlockEntityType<FluxCompressorBlockEntity>> FLUX_COMPRESSOR
            = register("flux_compressor", () -> new BlockEntityType<>(FluxCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.FLUX_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<PneumaticDynamoBlockEntity>> PNEUMATIC_DYNAMO
            = register("pneumatic_dynamo", () -> new BlockEntityType<>(PneumaticDynamoBlockEntity::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DYNAMO.get()), null));
    public static final RegistryObject<BlockEntityType<ThermalCompressorBlockEntity>> THERMAL_COMPRESSOR
            = register("thermal_compressor", () -> new BlockEntityType<>(ThermalCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.THERMAL_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<HeatPipeBlockEntity>> HEAT_PIPE
            = register("heat_pipe", () -> new BlockEntityType<>(HeatPipeBlockEntity::new, ImmutableSet.of(ModBlocks.HEAT_PIPE.get()), null));
    public static final RegistryObject<BlockEntityType<EtchingTankBlockEntity>> ETCHING_TANK
            = register("etching_tank", () -> new BlockEntityType<>(EtchingTankBlockEntity::new, ImmutableSet.of(ModBlocks.ETCHING_TANK.get()),
    null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Small>> TANK_SMALL
            = register("small_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Small::new, ImmutableSet.of(ModBlocks.TANK_SMALL.get()), null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Medium>> TANK_MEDIUM
            = register("medium_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Medium::new, ImmutableSet.of(ModBlocks.TANK_MEDIUM.get()), null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Large>> TANK_LARGE
            = register("large_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Large::new, ImmutableSet.of(ModBlocks.TANK_LARGE.get()), null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Huge>> TANK_HUGE
            = register("huge_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Huge::new, ImmutableSet.of(ModBlocks.TANK_HUGE.get()), null));
    public static final RegistryObject<BlockEntityType<ReinforcedChestBlockEntity>> REINFORCED_CHEST
            = register("reinforced_chest", () -> new BlockEntityType<>(ReinforcedChestBlockEntity::new, ImmutableSet.of(ModBlocks.REINFORCED_CHEST.get()),null));
    public static final RegistryObject<BlockEntityType<SmartChestBlockEntity>> SMART_CHEST
            = register("smart_chest", () -> new BlockEntityType<>(SmartChestBlockEntity::new, ImmutableSet.of(ModBlocks.SMART_CHEST.get()), null));
    public static final RegistryObject<BlockEntityType<TagWorkbenchBlockEntity>> TAG_WORKBENCH
            = register("tag_workbench", () -> new BlockEntityType<>(TagWorkbenchBlockEntity::new, ImmutableSet.of(ModBlocks.TAG_WORKBENCH.get()), null));
    public static final RegistryObject<BlockEntityType<DisplayTableBlockEntity>> DISPLAY_TABLE
            = register("display_table", () -> new BlockEntityType<>(DisplayTableBlockEntity::new, ImmutableSet.of(ModBlocks.DISPLAY_TABLE.get(), ModBlocks.DISPLAY_SHELF.get()),
            null));
    public static final RegistryObject<BlockEntityType<DroneInterfaceBlockEntity>> DRONE_INTERFACE
            = register("drone_interface", () -> new BlockEntityType<>(DroneInterfaceBlockEntity::new, ImmutableSet.of(ModBlocks.DRONE_INTERFACE.get()),
        null));
    public static final RegistryObject<BlockEntityType<FluidMixerBlockEntity>> FLUID_MIXER
            = register("fluid_mixer", () -> new BlockEntityType<>(FluidMixerBlockEntity::new, ImmutableSet.of(ModBlocks.FLUID_MIXER.get()), null));
    public static final RegistryObject<BlockEntityType<VacuumTrapBlockEntity>> VACUUM_TRAP
            = register("vacuum_trap", () -> new BlockEntityType<>(VacuumTrapBlockEntity::new, ImmutableSet.of(ModBlocks.VACUUM_TRAP.get()), null));
    public static final RegistryObject<BlockEntityType<SpawnerExtractorBlockEntity>> SPAWNER_EXTRACTOR
            = register("spawner_extractor", () -> new BlockEntityType<>(SpawnerExtractorBlockEntity::new, ImmutableSet.of(ModBlocks.SPAWNER_EXTRACTOR.get()), null));
    public static final RegistryObject<BlockEntityType<PressurizedSpawnerBlockEntity>> PRESSURIZED_SPAWNER
            = register("pressurized_spawner", () -> new BlockEntityType<>(PressurizedSpawnerBlockEntity::new, ImmutableSet.of(ModBlocks.PRESSURIZED_SPAWNER.get()), null));
    public static final RegistryObject<BlockEntityType<CreativeCompressedIronBlockBlockEntity>> CREATIVE_COMPRESSED_IRON_BLOCK
            = register("creative_compressed_iron_block", () -> new BlockEntityType<>(CreativeCompressedIronBlockBlockEntity::new, ImmutableSet.of(ModBlocks.CREATIVE_COMPRESSED_IRON_BLOCK.get()), null));
    public static final RegistryObject<BlockEntityType<ReinforcedPressureTubeBlockEntity>> REINFORCED_PRESSURE_TUBE
            = register("reinforced_pressure_tube", () -> new BlockEntityType<>(ReinforcedPressureTubeBlockEntity::new, ImmutableSet.of(ModBlocks.REINFORCED_PRESSURE_TUBE.get()), null));
    public static final RegistryObject<BlockEntityType<TubeJunctionBlockEntity>> TUBE_JUNCTION
            = register("tube_junction", () -> new BlockEntityType<>(TubeJunctionBlockEntity::new, ImmutableSet.of(ModBlocks.TUBE_JUNCTION.get()), null));
    public static final RegistryObject<BlockEntityType<SolarCompressorBlockEntity>> SOLAR_COMPRESSOR =
            register("solar_compressor", () -> new BlockEntityType<>(SolarCompressorBlockEntity::new, ImmutableSet.of(ModBlocks.SOLAR_COMPRESSOR.get()), null));

    private static <T extends BlockEntityType<?>> RegistryObject<T> register(String name, Supplier<T> sup) {
        return BLOCK_ENTITIES.register(name, sup);
    }
}
