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
import me.desht.pneumaticcraft.common.thirdparty.computer_common.TileEntityDroneInterface;
import me.desht.pneumaticcraft.common.tileentity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Names.MOD_ID);

    public static final RegistryObject<BlockEntityType<TileEntityPressureTube>> PRESSURE_TUBE
            = register("pressure_tube", () -> new BlockEntityType<>(TileEntityPressureTube::new, ImmutableSet.of(ModBlocks.PRESSURE_TUBE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAdvancedPressureTube>> ADVANCED_PRESSURE_TUBE
            = register("advanced_pressure_tube", () -> new BlockEntityType<>(TileEntityAdvancedPressureTube::new, ImmutableSet.of(ModBlocks.ADVANCED_PRESSURE_TUBE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAirCompressor>> AIR_COMPRESSOR
            = register("air_compressor", () -> new BlockEntityType<>(TileEntityAirCompressor::new, ImmutableSet.of(ModBlocks.AIR_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAdvancedAirCompressor>> ADVANCED_AIR_COMPRESSOR
            = register("advanced_air_compressor", () -> new BlockEntityType<>(TileEntityAdvancedAirCompressor::new, ImmutableSet.of(ModBlocks.ADVANCED_AIR_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAirCannon>> AIR_CANNON
            = register("air_cannon", () -> new BlockEntityType<>(TileEntityAirCannon::new, ImmutableSet.of(ModBlocks.AIR_CANNON.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPressureChamberWall>> PRESSURE_CHAMBER_WALL
            = register("pressure_chamber_wall", () -> new BlockEntityType<>(TileEntityPressureChamberWall::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_WALL.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPressureChamberGlass>> PRESSURE_CHAMBER_GLASS
            = register("pressure_chamber_glass", () -> new BlockEntityType<>(TileEntityPressureChamberGlass::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_GLASS.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPressureChamberValve>> PRESSURE_CHAMBER_VALVE
            = register("pressure_chamber_valve", () -> new BlockEntityType<>(TileEntityPressureChamberValve::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_VALVE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityChargingStation>> CHARGING_STATION
            = register("charging_station", () -> new BlockEntityType<>(TileEntityChargingStation::new, ImmutableSet.of(ModBlocks.CHARGING_STATION.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityElevatorBase>> ELEVATOR_BASE
            = register("elevator_base", () -> new BlockEntityType<>(TileEntityElevatorBase::new, ImmutableSet.of(ModBlocks.ELEVATOR_BASE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityElevatorFrame>> ELEVATOR_FRAME
            = register("elevator_frame", () -> new BlockEntityType<>(TileEntityElevatorFrame::new, ImmutableSet.of(ModBlocks.ELEVATOR_FRAME.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPressureChamberInterface>> PRESSURE_CHAMBER_INTERFACE
            = register("pressure_chamber_interface", () -> new BlockEntityType<>(TileEntityPressureChamberInterface::new, ImmutableSet.of(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityVacuumPump>> VACUUM_PUMP
            = register("vacuum_pump", () -> new BlockEntityType<>(TileEntityVacuumPump::new, ImmutableSet.of(ModBlocks.VACUUM_PUMP.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPneumaticDoorBase>> PNEUMATIC_DOOR_BASE
            = register("pneumatic_door_base", () -> new BlockEntityType<>(TileEntityPneumaticDoorBase::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DOOR_BASE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPneumaticDoor>> PNEUMATIC_DOOR
            = register("pneumatic_door", () -> new BlockEntityType<>(TileEntityPneumaticDoor::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DOOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAssemblyIOUnit>> ASSEMBLY_IO_UNIT
            = register("assembly_io_unit", () -> new BlockEntityType<>(TileEntityAssemblyIOUnit::new, ImmutableSet.of(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get(), ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAssemblyPlatform>> ASSEMBLY_PLATFORM
            = register("assembly_platform", () -> new BlockEntityType<>(TileEntityAssemblyPlatform::new, ImmutableSet.of(ModBlocks.ASSEMBLY_PLATFORM.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAssemblyDrill>> ASSEMBLY_DRILL
            = register("assembly_drill", () -> new BlockEntityType<>(TileEntityAssemblyDrill::new, ImmutableSet.of(ModBlocks.ASSEMBLY_DRILL.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAssemblyLaser>> ASSEMBLY_LASER
            = register("assembly_laser", () -> new BlockEntityType<>(TileEntityAssemblyLaser::new, ImmutableSet.of(ModBlocks.ASSEMBLY_LASER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAssemblyController>> ASSEMBLY_CONTROLLER
            = register("assembly_controller", () -> new BlockEntityType<>(TileEntityAssemblyController::new, ImmutableSet.of(ModBlocks.ASSEMBLY_CONTROLLER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityUVLightBox>> UV_LIGHT_BOX
            = register("uv_light_box", () -> new BlockEntityType<>(TileEntityUVLightBox::new, ImmutableSet.of(ModBlocks.UV_LIGHT_BOX.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntitySecurityStation>> SECURITY_STATION
            = register("security_station", () -> new BlockEntityType<>(TileEntitySecurityStation::new, ImmutableSet.of(ModBlocks.SECURITY_STATION.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityUniversalSensor>> UNIVERSAL_SENSOR
            = register("universal_sensor", () -> new BlockEntityType<>(TileEntityUniversalSensor::new, ImmutableSet.of(ModBlocks.UNIVERSAL_SENSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAerialInterface>> AERIAL_INTERFACE
            = register("aerial_interface", () -> new BlockEntityType<>(TileEntityAerialInterface::new, ImmutableSet.of(ModBlocks.AERIAL_INTERFACE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityElectrostaticCompressor>> ELECTROSTATIC_COMPRESSOR
            = register("electrostatic_compressor", () -> new BlockEntityType<>(TileEntityElectrostaticCompressor::new, ImmutableSet.of(ModBlocks.ELECTROSTATIC_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAphorismTile>> APHORISM_TILE
            = register("aphorism_tile", () -> new BlockEntityType<>(TileEntityAphorismTile::new, ImmutableSet.of(ModBlocks.APHORISM_TILE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityOmnidirectionalHopper>> OMNIDIRECTIONAL_HOPPER
            = register("omnidirectional_hopper", () -> new BlockEntityType<>(TileEntityOmnidirectionalHopper::new, ImmutableSet.of(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityLiquidHopper>> LIQUID_HOPPER
            = register("liquid_hopper", () -> new BlockEntityType<>(TileEntityLiquidHopper::new, ImmutableSet.of(ModBlocks.LIQUID_HOPPER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityElevatorCaller>> ELEVATOR_CALLER
            = register("elevator_caller", () -> new BlockEntityType<>(TileEntityElevatorCaller::new, ImmutableSet.of(ModBlocks.ELEVATOR_CALLER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityProgrammer>> PROGRAMMER
            = register("programmer", () -> new BlockEntityType<>(TileEntityProgrammer::new, ImmutableSet.of(ModBlocks.PROGRAMMER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityCreativeCompressor>> CREATIVE_COMPRESSOR
            = register("creative_compressor", () -> new BlockEntityType<>(TileEntityCreativeCompressor::new, ImmutableSet.of(ModBlocks.CREATIVE_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityLiquidCompressor>> LIQUID_COMPRESSOR
            = register("liquid_compressor", () -> new BlockEntityType<>(TileEntityLiquidCompressor::new, ImmutableSet.of(ModBlocks.LIQUID_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityAdvancedLiquidCompressor>> ADVANCED_LIQUID_COMPRESSOR
            = register("advanced_liquid_compressor", () -> new BlockEntityType<>(TileEntityAdvancedLiquidCompressor::new, ImmutableSet.of(ModBlocks.ADVANCED_LIQUID_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityDroneRedstoneEmitter>> DRONE_REDSTONE_EMITTER
            = register("drone_redstone_emitter", () -> new BlockEntityType<>(TileEntityDroneRedstoneEmitter::new, ImmutableSet.of(ModBlocks.DRONE_REDSTONE_EMITTER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityCompressedIronBlock>> COMPRESSED_IRON_BLOCK
            = register("compressed_iron_block", () -> new BlockEntityType<>(TileEntityCompressedIronBlock::new, ImmutableSet.of(ModBlocks.COMPRESSED_IRON_BLOCK.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityHeatSink>> HEAT_SINK
            = register("heat_sink", () -> new BlockEntityType<>(TileEntityHeatSink::new, ImmutableSet.of(ModBlocks.HEAT_SINK.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityVortexTube>> VORTEX_TUBE
            = register("vortex_tube", () -> new BlockEntityType<>(TileEntityVortexTube::new, ImmutableSet.of(ModBlocks.VORTEX_TUBE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityProgrammableController>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", () -> new BlockEntityType<>(TileEntityProgrammableController::new, ImmutableSet.of(ModBlocks.PROGRAMMABLE_CONTROLLER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityGasLift>> GAS_LIFT
            = register("gas_lift", () -> new BlockEntityType<>(TileEntityGasLift::new, ImmutableSet.of(ModBlocks.GAS_LIFT.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityRefineryController>> REFINERY
            = register("refinery", () -> new BlockEntityType<>(TileEntityRefineryController::new, ImmutableSet.of(ModBlocks.REFINERY.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityRefineryOutput>> REFINERY_OUTPUT
            = register("refinery_output", () -> new BlockEntityType<>(TileEntityRefineryOutput::new, ImmutableSet.of(ModBlocks.REFINERY_OUTPUT.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityThermopneumaticProcessingPlant>> THERMOPNEUMATIC_PROCESSING_PLANT
            = register("thermopneumatic_processing_plant", () -> new BlockEntityType<>(TileEntityThermopneumaticProcessingPlant::new, ImmutableSet.of(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityKeroseneLamp>> KEROSENE_LAMP
            = register("kerosene_lamp", () -> new BlockEntityType<>(TileEntityKeroseneLamp::new, ImmutableSet.of(ModBlocks.KEROSENE_LAMP.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntitySentryTurret>> SENTRY_TURRET
            = register("sentry_turret", () -> new BlockEntityType<>(TileEntitySentryTurret::new, ImmutableSet.of(ModBlocks.SENTRY_TURRET.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityFluxCompressor>> FLUX_COMPRESSOR
            = register("flux_compressor", () -> new BlockEntityType<>(TileEntityFluxCompressor::new, ImmutableSet.of(ModBlocks.FLUX_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPneumaticDynamo>> PNEUMATIC_DYNAMO
            = register("pneumatic_dynamo", () -> new BlockEntityType<>(TileEntityPneumaticDynamo::new, ImmutableSet.of(ModBlocks.PNEUMATIC_DYNAMO.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityThermalCompressor>> THERMAL_COMPRESSOR
            = register("thermal_compressor", () -> new BlockEntityType<>(TileEntityThermalCompressor::new, ImmutableSet.of(ModBlocks.THERMAL_COMPRESSOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityHeatPipe>> HEAT_PIPE
            = register("heat_pipe", () -> new BlockEntityType<>(TileEntityHeatPipe::new, ImmutableSet.of(ModBlocks.HEAT_PIPE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityEtchingTank>> ETCHING_TANK
            = register("etching_tank", () -> new BlockEntityType<>(TileEntityEtchingTank::new, ImmutableSet.of(ModBlocks.ETCHING_TANK.get()),
    null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Small>> TANK_SMALL
            = register("small_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Small::new, ImmutableSet.of(ModBlocks.TANK_SMALL.get()), null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Medium>> TANK_MEDIUM
            = register("medium_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Medium::new, ImmutableSet.of(ModBlocks.TANK_MEDIUM.get()), null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Large>> TANK_LARGE
            = register("large_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Large::new, ImmutableSet.of(ModBlocks.TANK_LARGE.get()), null));
    public static final RegistryObject<BlockEntityType<AbstractFluidTankBlockEntity.Huge>> TANK_HUGE
            = register("huge_tank", () -> new BlockEntityType<>(AbstractFluidTankBlockEntity.Huge::new, ImmutableSet.of(ModBlocks.TANK_HUGE.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityReinforcedChest>> REINFORCED_CHEST
            = register("reinforced_chest", () -> new BlockEntityType<>(TileEntityReinforcedChest::new, ImmutableSet.of(ModBlocks.REINFORCED_CHEST.get()),null));
    public static final RegistryObject<BlockEntityType<TileEntitySmartChest>> SMART_CHEST
            = register("smart_chest", () -> new BlockEntityType<>(TileEntitySmartChest::new, ImmutableSet.of(ModBlocks.SMART_CHEST.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityTagWorkbench>> TAG_WORKBENCH
            = register("tag_workbench", () -> new BlockEntityType<>(TileEntityTagWorkbench::new, ImmutableSet.of(ModBlocks.TAG_WORKBENCH.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityDisplayTable>> DISPLAY_TABLE
            = register("display_table", () -> new BlockEntityType<>(TileEntityDisplayTable::new, ImmutableSet.of(ModBlocks.DISPLAY_TABLE.get(), ModBlocks.DISPLAY_SHELF.get()),
            null));
    public static final RegistryObject<BlockEntityType<TileEntityDroneInterface>> DRONE_INTERFACE
            = register("drone_interface", () -> new BlockEntityType<>(TileEntityDroneInterface::new, ImmutableSet.of(ModBlocks.DRONE_INTERFACE.get()),
        null));
    public static final RegistryObject<BlockEntityType<TileEntityFluidMixer>> FLUID_MIXER
            = register("fluid_mixer", () -> new BlockEntityType<>(TileEntityFluidMixer::new, ImmutableSet.of(ModBlocks.FLUID_MIXER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityVacuumTrap>> VACUUM_TRAP
            = register("vacuum_trap", () -> new BlockEntityType<>(TileEntityVacuumTrap::new, ImmutableSet.of(ModBlocks.VACUUM_TRAP.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntitySpawnerExtractor>> SPAWNER_EXTRACTOR
            = register("spawner_extractor", () -> new BlockEntityType<>(TileEntitySpawnerExtractor::new, ImmutableSet.of(ModBlocks.SPAWNER_EXTRACTOR.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityPressurizedSpawner>> PRESSURIZED_SPAWNER
            = register("pressurized_spawner", () -> new BlockEntityType<>(TileEntityPressurizedSpawner::new, ImmutableSet.of(ModBlocks.PRESSURIZED_SPAWNER.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityCreativeCompressedIronBlock>> CREATIVE_COMPRESSED_IRON_BLOCK
            = register("creative_compressed_iron_block", () -> new BlockEntityType<>(TileEntityCreativeCompressedIronBlock::new, ImmutableSet.of(ModBlocks.CREATIVE_COMPRESSED_IRON_BLOCK.get()), null));
    public static final RegistryObject<BlockEntityType<TileEntityReinforcedPressureTube>> REINFORCED_PRESSURE_TUBE
            = register("reinforced_pressure_tube", () -> new BlockEntityType<>(TileEntityReinforcedPressureTube::new, ImmutableSet.of(ModBlocks.REINFORCED_PRESSURE_TUBE.get()), null));

    private static <T extends BlockEntityType<?>> RegistryObject<T> register(String name, Supplier<T> sup) {
        return BLOCK_ENTITIES.register(name, sup);
    }
}
