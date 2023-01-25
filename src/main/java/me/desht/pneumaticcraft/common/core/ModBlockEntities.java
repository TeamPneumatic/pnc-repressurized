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
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.DroneInterfaceBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Names.MOD_ID);

    public static final RegistryObject<BlockEntityType<PressureTubeBlockEntity>> PRESSURE_TUBE
            = register("pressure_tube", PressureTubeBlockEntity::new, ModBlocks.PRESSURE_TUBE);
    public static final RegistryObject<BlockEntityType<AdvancedPressureTubeBlockEntity>> ADVANCED_PRESSURE_TUBE
            = register("advanced_pressure_tube", AdvancedPressureTubeBlockEntity::new, ModBlocks.ADVANCED_PRESSURE_TUBE);
    public static final RegistryObject<BlockEntityType<AirCompressorBlockEntity>> AIR_COMPRESSOR
            = register("air_compressor", AirCompressorBlockEntity::new, ModBlocks.AIR_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<AdvancedAirCompressorBlockEntity>> ADVANCED_AIR_COMPRESSOR
            = register("advanced_air_compressor", AdvancedAirCompressorBlockEntity::new, ModBlocks.ADVANCED_AIR_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<AirCannonBlockEntity>> AIR_CANNON
            = register("air_cannon", AirCannonBlockEntity::new, ModBlocks.AIR_CANNON);
    public static final RegistryObject<BlockEntityType<PressureChamberWallBlockEntity>> PRESSURE_CHAMBER_WALL
            = register("pressure_chamber_wall", PressureChamberWallBlockEntity::new, ModBlocks.PRESSURE_CHAMBER_WALL, ModBlocks.PRESSURE_CHAMBER_GLASS);
    public static final RegistryObject<BlockEntityType<PressureChamberValveBlockEntity>> PRESSURE_CHAMBER_VALVE
            = register("pressure_chamber_valve", PressureChamberValveBlockEntity::new, ModBlocks.PRESSURE_CHAMBER_VALVE);
    public static final RegistryObject<BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION
            = register("charging_station", ChargingStationBlockEntity::new, ModBlocks.CHARGING_STATION);
    public static final RegistryObject<BlockEntityType<ElevatorBaseBlockEntity>> ELEVATOR_BASE
            = register("elevator_base", ElevatorBaseBlockEntity::new, ModBlocks.ELEVATOR_BASE);
    public static final RegistryObject<BlockEntityType<ElevatorFrameBlockEntity>> ELEVATOR_FRAME
            = register("elevator_frame", ElevatorFrameBlockEntity::new, ModBlocks.ELEVATOR_FRAME);
    public static final RegistryObject<BlockEntityType<PressureChamberInterfaceBlockEntity>> PRESSURE_CHAMBER_INTERFACE
            = register("pressure_chamber_interface", PressureChamberInterfaceBlockEntity::new, ModBlocks.PRESSURE_CHAMBER_INTERFACE);
    public static final RegistryObject<BlockEntityType<VacuumPumpBlockEntity>> VACUUM_PUMP
            = register("vacuum_pump", VacuumPumpBlockEntity::new, ModBlocks.VACUUM_PUMP);
    public static final RegistryObject<BlockEntityType<PneumaticDoorBaseBlockEntity>> PNEUMATIC_DOOR_BASE
            = register("pneumatic_door_base", PneumaticDoorBaseBlockEntity::new, ModBlocks.PNEUMATIC_DOOR_BASE);
    public static final RegistryObject<BlockEntityType<PneumaticDoorBlockEntity>> PNEUMATIC_DOOR
            = register("pneumatic_door", PneumaticDoorBlockEntity::new, ModBlocks.PNEUMATIC_DOOR);
    public static final RegistryObject<BlockEntityType<AssemblyIOUnitBlockEntity>> ASSEMBLY_IO_UNIT
            = register("assembly_io_unit", AssemblyIOUnitBlockEntity::new, ModBlocks.ASSEMBLY_IO_UNIT_IMPORT, ModBlocks.ASSEMBLY_IO_UNIT_EXPORT);
    public static final RegistryObject<BlockEntityType<AssemblyPlatformBlockEntity>> ASSEMBLY_PLATFORM
            = register("assembly_platform", AssemblyPlatformBlockEntity::new, ModBlocks.ASSEMBLY_PLATFORM);
    public static final RegistryObject<BlockEntityType<AssemblyDrillBlockEntity>> ASSEMBLY_DRILL
            = register("assembly_drill", AssemblyDrillBlockEntity::new, ModBlocks.ASSEMBLY_DRILL);
    public static final RegistryObject<BlockEntityType<AssemblyLaserBlockEntity>> ASSEMBLY_LASER
            = register("assembly_laser", AssemblyLaserBlockEntity::new, ModBlocks.ASSEMBLY_LASER);
    public static final RegistryObject<BlockEntityType<AssemblyControllerBlockEntity>> ASSEMBLY_CONTROLLER
            = register("assembly_controller", AssemblyControllerBlockEntity::new, ModBlocks.ASSEMBLY_CONTROLLER);
    public static final RegistryObject<BlockEntityType<UVLightBoxBlockEntity>> UV_LIGHT_BOX
            = register("uv_light_box", UVLightBoxBlockEntity::new, ModBlocks.UV_LIGHT_BOX);
    public static final RegistryObject<BlockEntityType<SecurityStationBlockEntity>> SECURITY_STATION
            = register("security_station", SecurityStationBlockEntity::new, ModBlocks.SECURITY_STATION);
    public static final RegistryObject<BlockEntityType<UniversalSensorBlockEntity>> UNIVERSAL_SENSOR
            = register("universal_sensor", UniversalSensorBlockEntity::new, ModBlocks.UNIVERSAL_SENSOR);
    public static final RegistryObject<BlockEntityType<AerialInterfaceBlockEntity>> AERIAL_INTERFACE
            = register("aerial_interface", AerialInterfaceBlockEntity::new, ModBlocks.AERIAL_INTERFACE);
    public static final RegistryObject<BlockEntityType<ElectrostaticCompressorBlockEntity>> ELECTROSTATIC_COMPRESSOR
            = register("electrostatic_compressor", ElectrostaticCompressorBlockEntity::new, ModBlocks.ELECTROSTATIC_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<AphorismTileBlockEntity>> APHORISM_TILE
            = register("aphorism_tile", AphorismTileBlockEntity::new, ModBlocks.APHORISM_TILE);
    public static final RegistryObject<BlockEntityType<OmnidirectionalHopperBlockEntity>> OMNIDIRECTIONAL_HOPPER
            = register("omnidirectional_hopper", OmnidirectionalHopperBlockEntity::new, ModBlocks.OMNIDIRECTIONAL_HOPPER);
    public static final RegistryObject<BlockEntityType<LiquidHopperBlockEntity>> LIQUID_HOPPER
            = register("liquid_hopper", LiquidHopperBlockEntity::new, ModBlocks.LIQUID_HOPPER);
    public static final RegistryObject<BlockEntityType<ElevatorCallerBlockEntity>> ELEVATOR_CALLER
            = register("elevator_caller", ElevatorCallerBlockEntity::new, ModBlocks.ELEVATOR_CALLER);
    public static final RegistryObject<BlockEntityType<ProgrammerBlockEntity>> PROGRAMMER
            = register("programmer", ProgrammerBlockEntity::new, ModBlocks.PROGRAMMER);
    public static final RegistryObject<BlockEntityType<CreativeCompressorBlockEntity>> CREATIVE_COMPRESSOR
            = register("creative_compressor", CreativeCompressorBlockEntity::new, ModBlocks.CREATIVE_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<LiquidCompressorBlockEntity>> LIQUID_COMPRESSOR
            = register("liquid_compressor", LiquidCompressorBlockEntity::new, ModBlocks.LIQUID_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<AdvancedLiquidCompressorBlockEntity>> ADVANCED_LIQUID_COMPRESSOR
            = register("advanced_liquid_compressor", AdvancedLiquidCompressorBlockEntity::new, ModBlocks.ADVANCED_LIQUID_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<DroneRedstoneEmitterBlockEntity>> DRONE_REDSTONE_EMITTER
            = register("drone_redstone_emitter", DroneRedstoneEmitterBlockEntity::new, ModBlocks.DRONE_REDSTONE_EMITTER);
    public static final RegistryObject<BlockEntityType<CompressedIronBlockBlockEntity>> COMPRESSED_IRON_BLOCK
            = register("compressed_iron_block", CompressedIronBlockBlockEntity::new, ModBlocks.COMPRESSED_IRON_BLOCK);
    public static final RegistryObject<BlockEntityType<HeatSinkBlockEntity>> HEAT_SINK
            = register("heat_sink", HeatSinkBlockEntity::new, ModBlocks.HEAT_SINK);
    public static final RegistryObject<BlockEntityType<VortexTubeBlockEntity>> VORTEX_TUBE
            = register("vortex_tube", VortexTubeBlockEntity::new, ModBlocks.VORTEX_TUBE);
    public static final RegistryObject<BlockEntityType<ProgrammableControllerBlockEntity>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ProgrammableControllerBlockEntity::new, ModBlocks.PROGRAMMABLE_CONTROLLER);
    public static final RegistryObject<BlockEntityType<GasLiftBlockEntity>> GAS_LIFT
            = register("gas_lift", GasLiftBlockEntity::new, ModBlocks.GAS_LIFT);
    public static final RegistryObject<BlockEntityType<RefineryControllerBlockEntity>> REFINERY
            = register("refinery", RefineryControllerBlockEntity::new, ModBlocks.REFINERY);
    public static final RegistryObject<BlockEntityType<RefineryOutputBlockEntity>> REFINERY_OUTPUT
            = register("refinery_output", RefineryOutputBlockEntity::new, ModBlocks.REFINERY_OUTPUT);
    public static final RegistryObject<BlockEntityType<ThermopneumaticProcessingPlantBlockEntity>> THERMOPNEUMATIC_PROCESSING_PLANT
            = register("thermopneumatic_processing_plant", ThermopneumaticProcessingPlantBlockEntity::new, ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT);
    public static final RegistryObject<BlockEntityType<KeroseneLampBlockEntity>> KEROSENE_LAMP
            = register("kerosene_lamp", KeroseneLampBlockEntity::new, ModBlocks.KEROSENE_LAMP);
    public static final RegistryObject<BlockEntityType<SentryTurretBlockEntity>> SENTRY_TURRET
            = register("sentry_turret", SentryTurretBlockEntity::new, ModBlocks.SENTRY_TURRET);
    public static final RegistryObject<BlockEntityType<FluxCompressorBlockEntity>> FLUX_COMPRESSOR
            = register("flux_compressor", FluxCompressorBlockEntity::new, ModBlocks.FLUX_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<PneumaticDynamoBlockEntity>> PNEUMATIC_DYNAMO
            = register("pneumatic_dynamo", PneumaticDynamoBlockEntity::new, ModBlocks.PNEUMATIC_DYNAMO);
    public static final RegistryObject<BlockEntityType<ThermalCompressorBlockEntity>> THERMAL_COMPRESSOR
            = register("thermal_compressor", ThermalCompressorBlockEntity::new, ModBlocks.THERMAL_COMPRESSOR);
    public static final RegistryObject<BlockEntityType<HeatPipeBlockEntity>> HEAT_PIPE
            = register("heat_pipe", HeatPipeBlockEntity::new, ModBlocks.HEAT_PIPE);
    public static final RegistryObject<BlockEntityType<EtchingTankBlockEntity>> ETCHING_TANK
            = register("etching_tank", EtchingTankBlockEntity::new, ModBlocks.ETCHING_TANK);
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Small>> TANK_SMALL
            = register("small_tank", AbstractFluidTankBlockEntity.Small::new, ModBlocks.TANK_SMALL);
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Medium>> TANK_MEDIUM
            = register("medium_tank", AbstractFluidTankBlockEntity.Medium::new, ModBlocks.TANK_MEDIUM);
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Large>> TANK_LARGE
            = register("large_tank", AbstractFluidTankBlockEntity.Large::new, ModBlocks.TANK_LARGE);
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Huge>> TANK_HUGE
            = register("huge_tank", AbstractFluidTankBlockEntity.Huge::new, ModBlocks.TANK_HUGE);
    public static final RegistryObject<BlockEntityType<ReinforcedChestBlockEntity>> REINFORCED_CHEST
            = register("reinforced_chest", ReinforcedChestBlockEntity::new, ModBlocks.REINFORCED_CHEST);
    public static final RegistryObject<BlockEntityType<SmartChestBlockEntity>> SMART_CHEST
            = register("smart_chest", SmartChestBlockEntity::new, ModBlocks.SMART_CHEST);
    public static final RegistryObject<BlockEntityType<TagWorkbenchBlockEntity>> TAG_WORKBENCH
            = register("tag_workbench", TagWorkbenchBlockEntity::new, ModBlocks.TAG_WORKBENCH);
    public static final RegistryObject<BlockEntityType<DisplayTableBlockEntity>> DISPLAY_TABLE
            = register("display_table", DisplayTableBlockEntity::new, ModBlocks.DISPLAY_TABLE, ModBlocks.DISPLAY_SHELF);
    public static final RegistryObject<BlockEntityType<DroneInterfaceBlockEntity>> DRONE_INTERFACE
            = register("drone_interface", DroneInterfaceBlockEntity::new, ModBlocks.DRONE_INTERFACE);
    public static final RegistryObject<BlockEntityType<FluidMixerBlockEntity>> FLUID_MIXER
            = register("fluid_mixer", FluidMixerBlockEntity::new, ModBlocks.FLUID_MIXER);
    public static final RegistryObject<BlockEntityType<VacuumTrapBlockEntity>> VACUUM_TRAP
            = register("vacuum_trap", VacuumTrapBlockEntity::new, ModBlocks.VACUUM_TRAP);
    public static final RegistryObject<BlockEntityType<SpawnerExtractorBlockEntity>> SPAWNER_EXTRACTOR
            = register("spawner_extractor", SpawnerExtractorBlockEntity::new, ModBlocks.SPAWNER_EXTRACTOR);
    public static final RegistryObject<BlockEntityType<PressurizedSpawnerBlockEntity>> PRESSURIZED_SPAWNER
            = register("pressurized_spawner", PressurizedSpawnerBlockEntity::new, ModBlocks.PRESSURIZED_SPAWNER);
    public static final RegistryObject<BlockEntityType<CreativeCompressedIronBlockBlockEntity>> CREATIVE_COMPRESSED_IRON_BLOCK
            = register("creative_compressed_iron_block", CreativeCompressedIronBlockBlockEntity::new, ModBlocks.CREATIVE_COMPRESSED_IRON_BLOCK);
    public static final RegistryObject<BlockEntityType<ReinforcedPressureTubeBlockEntity>> REINFORCED_PRESSURE_TUBE
            = register("reinforced_pressure_tube", ReinforcedPressureTubeBlockEntity::new, ModBlocks.REINFORCED_PRESSURE_TUBE);
    public static final RegistryObject<BlockEntityType<TubeJunctionBlockEntity>> TUBE_JUNCTION
            = register("tube_junction", TubeJunctionBlockEntity::new, ModBlocks.TUBE_JUNCTION);
    public static final RegistryObject<BlockEntityType<SolarCompressorBlockEntity>> SOLAR_COMPRESSOR
            = register("solar_compressor", SolarCompressorBlockEntity::new, ModBlocks.SOLAR_COMPRESSOR);

    @SafeVarargs
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> supplier, RegistryObject<? extends Block>... blocks) {
        //noinspection ConstantConditions
        return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(supplier, Arrays.stream(blocks).map(RegistryObject::get).collect(Collectors.toSet()), null));
    }

}
