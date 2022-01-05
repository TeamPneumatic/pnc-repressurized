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
import me.desht.pneumaticcraft.common.block.*;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.BlockDroneInterface;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Names.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = ModItems.ITEMS;

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        return register(name, sup, ModBlocks::itemDefault);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup, Function<RegistryObject<T>, Supplier<? extends Item>> itemCreator) {
        RegistryObject<T> ret = registerNoItem(name, sup);
        ITEMS.register(name, itemCreator.apply(ret));
        return ret;
    }

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<? extends T> sup) {
        return BLOCKS.register(name, sup);
    }

    private static Supplier<BlockItem> itemDefault(final RegistryObject<? extends Block> block) {
        return item(block, ModItems.ItemGroups.PNC_CREATIVE_TAB);
    }

    private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block, final Supplier<Callable<ItemStackTileEntityRenderer>> renderMethod) {
        return () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.ItemGroups.PNC_CREATIVE_TAB).setISTER(renderMethod));
    }

    private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block, final ItemGroup itemGroup) {
        return () -> new BlockItem(block.get(), new Item.Properties().tab(itemGroup));
    }

    public static Block.Properties defaultProps() {
        return Block.Properties.of(Material.METAL)
                .strength(3f, 10f)
                .sound(SoundType.METAL);
    }

    public static Block.Properties reinforcedStoneProps() {
        return Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY)
                .requiresCorrectToolForDrops()
                .strength(3f, 1200f)
                .sound(SoundType.STONE);
    }

    private static Block.Properties fluidProps() {
        return Block.Properties.of(Material.WATER).noCollission().strength(100f).noDrops();
    }

    public static final RegistryObject<BlockPressureTube> PRESSURE_TUBE = register("pressure_tube",
            () -> new BlockPressureTube(BlockPressureTube.Tier.ONE));
    public static final RegistryObject<BlockPressureTube> ADVANCED_PRESSURE_TUBE = register("advanced_pressure_tube",
            () -> new BlockPressureTube(BlockPressureTube.Tier.TWO));
    public static final RegistryObject<BlockAirCannon> AIR_CANNON = register("air_cannon",
            BlockAirCannon::new);
    public static final RegistryObject<BlockPressureChamberWall> PRESSURE_CHAMBER_WALL = register("pressure_chamber_wall",
            BlockPressureChamberWall::new);
    public static final RegistryObject<BlockPressureChamberGlass> PRESSURE_CHAMBER_GLASS = register("pressure_chamber_glass",
            BlockPressureChamberGlass::new);
    public static final RegistryObject<BlockPressureChamberValve> PRESSURE_CHAMBER_VALVE = register("pressure_chamber_valve",
            BlockPressureChamberValve::new);
    public static final RegistryObject<BlockPressureChamberInterface> PRESSURE_CHAMBER_INTERFACE = register("pressure_chamber_interface",
            BlockPressureChamberInterface::new);
    public static final RegistryObject<BlockChargingStation> CHARGING_STATION = register("charging_station",
            BlockChargingStation::new, block -> () -> new BlockChargingStation.ItemBlockChargingStation(block.get()));
    public static final RegistryObject<BlockDrillPipe> DRILL_PIPE = register("drill_pipe",
            BlockDrillPipe::new);
    public static final RegistryObject<BlockElevatorBase> ELEVATOR_BASE = register("elevator_base",
            BlockElevatorBase::new);
    public static final RegistryObject<BlockElevatorFrame> ELEVATOR_FRAME = register("elevator_frame",
            BlockElevatorFrame::new);
    public static final RegistryObject<BlockVacuumPump> VACUUM_PUMP = register("vacuum_pump",
            BlockVacuumPump::new);
    public static final RegistryObject<BlockPneumaticDoorBase> PNEUMATIC_DOOR_BASE = register("pneumatic_door_base",
            BlockPneumaticDoorBase::new);
    public static final RegistryObject<BlockPneumaticDoor> PNEUMATIC_DOOR = register("pneumatic_door",
            BlockPneumaticDoor::new, block -> () -> new BlockPneumaticDoor.ItemBlockPneumaticDoor(block.get()));
    public static final RegistryObject<BlockAssemblyPlatform> ASSEMBLY_PLATFORM = register("assembly_platform",
            BlockAssemblyPlatform::new);
    public static final RegistryObject<BlockAssemblyIOUnit> ASSEMBLY_IO_UNIT_IMPORT = register("assembly_io_unit_import",
            () -> new BlockAssemblyIOUnit.Import(defaultProps()));
    public static final RegistryObject<BlockAssemblyIOUnit> ASSEMBLY_IO_UNIT_EXPORT = register("assembly_io_unit_export",
            () -> new BlockAssemblyIOUnit.Export(defaultProps()));
    public static final RegistryObject<BlockAssemblyDrill> ASSEMBLY_DRILL = register("assembly_drill",
            BlockAssemblyDrill::new);
    public static final RegistryObject<BlockAssemblyLaser> ASSEMBLY_LASER = register("assembly_laser",
            BlockAssemblyLaser::new);
    public static final RegistryObject<BlockAssemblyController> ASSEMBLY_CONTROLLER = register("assembly_controller",
            BlockAssemblyController::new);
    public static final RegistryObject<BlockCompressedIron> COMPRESSED_IRON_BLOCK = register("compressed_iron_block",
            BlockCompressedIron::new);
    public static final RegistryObject<BlockUVLightBox> UV_LIGHT_BOX = register("uv_light_box",
            BlockUVLightBox::new);
    public static final RegistryObject<BlockSecurityStation> SECURITY_STATION = register("security_station",
            BlockSecurityStation::new);
    public static final RegistryObject<BlockUniversalSensor> UNIVERSAL_SENSOR = register("universal_sensor",
            BlockUniversalSensor::new);
    public static final RegistryObject<BlockAerialInterface> AERIAL_INTERFACE = register("aerial_interface",
            BlockAerialInterface::new);
    public static final RegistryObject<BlockElectrostaticCompressor> ELECTROSTATIC_COMPRESSOR = register("electrostatic_compressor",
            BlockElectrostaticCompressor::new);
    public static final RegistryObject<BlockAphorismTile> APHORISM_TILE = register("aphorism_tile",
            BlockAphorismTile::new, block -> () -> new BlockAphorismTile.ItemBlockAphorismTile(block.get()));
    public static final RegistryObject<BlockOmnidirectionalHopper> OMNIDIRECTIONAL_HOPPER = register("omnidirectional_hopper",
            BlockOmnidirectionalHopper::new, block -> () -> new BlockOmnidirectionalHopper.ItemBlockOmnidirectionalHopper(block.get()));
    public static final RegistryObject<BlockElevatorCaller> ELEVATOR_CALLER = register("elevator_caller",
            BlockElevatorCaller::new);
    public static final RegistryObject<BlockProgrammer> PROGRAMMER = register("programmer",
            BlockProgrammer::new);
    public static final RegistryObject<BlockCreativeCompressor> CREATIVE_COMPRESSOR = register("creative_compressor",
            BlockCreativeCompressor::new, block -> () -> new BlockCreativeCompressor.ItemBlockCreativeCompressor(block.get()));
    public static final RegistryObject<BlockCreativeCompressedIron> CREATIVE_COMPRESSED_IRON_BLOCK = register("creative_compressed_iron_block",
            BlockCreativeCompressedIron::new, block -> () -> new BlockCreativeCompressedIron.ItemBlockCreativeCompressedIron(block.get()));
    public static final RegistryObject<BlockLiquidCompressor> LIQUID_COMPRESSOR = register("liquid_compressor",
            BlockLiquidCompressor::new);
    public static final RegistryObject<BlockAdvancedLiquidCompressor> ADVANCED_LIQUID_COMPRESSOR = register("advanced_liquid_compressor",
            BlockAdvancedLiquidCompressor::new);
    public static final RegistryObject<BlockAirCompressor> AIR_COMPRESSOR = register("air_compressor",
            BlockAirCompressor::new);
    public static final RegistryObject<BlockAdvancedAirCompressor> ADVANCED_AIR_COMPRESSOR = register("advanced_air_compressor",
            BlockAdvancedAirCompressor::new);
    public static final RegistryObject<BlockLiquidHopper> LIQUID_HOPPER = register("liquid_hopper",
            BlockLiquidHopper::new, block -> () -> new BlockLiquidHopper.ItemBlockLiquidHopper(block.get()));
    public static final RegistryObject<BlockDroneRedstoneEmitter> DRONE_REDSTONE_EMITTER = registerNoItem("drone_redstone_emitter",
            BlockDroneRedstoneEmitter::new);
    public static final RegistryObject<BlockHeatSink> HEAT_SINK = register("heat_sink",
            BlockHeatSink::new);
    public static final RegistryObject<BlockVortexTube> VORTEX_TUBE = register("vortex_tube",
            BlockVortexTube::new);
    public static final RegistryObject<BlockProgrammableController> PROGRAMMABLE_CONTROLLER = register("programmable_controller",
            BlockProgrammableController::new);
    public static final RegistryObject<BlockGasLift> GAS_LIFT = register("gas_lift",
            BlockGasLift::new);
    public static final RegistryObject<BlockRefineryController> REFINERY = register("refinery",
            BlockRefineryController::new);
    public static final RegistryObject<BlockRefineryOutput> REFINERY_OUTPUT = register("refinery_output",
            BlockRefineryOutput::new);
    public static final RegistryObject<BlockThermopneumaticProcessingPlant> THERMOPNEUMATIC_PROCESSING_PLANT = register("thermopneumatic_processing_plant",
            BlockThermopneumaticProcessingPlant::new);
    public static final RegistryObject<BlockKeroseneLamp> KEROSENE_LAMP = register("kerosene_lamp",
            BlockKeroseneLamp::new, block -> () -> new BlockKeroseneLamp.ItemBlockKeroseneLamp(block.get()));
    public static final RegistryObject<BlockKeroseneLampLight> KEROSENE_LAMP_LIGHT = registerNoItem("kerosene_lamp_light",
            BlockKeroseneLampLight::new);
    public static final RegistryObject<BlockSentryTurret> SENTRY_TURRET = register("sentry_turret",
            BlockSentryTurret::new);
    public static final RegistryObject<BlockFluxCompressor> FLUX_COMPRESSOR = register("flux_compressor",
            BlockFluxCompressor::new);
    public static final RegistryObject<BlockPneumaticDynamo> PNEUMATIC_DYNAMO = register("pneumatic_dynamo",
            BlockPneumaticDynamo::new);
    public static final RegistryObject<BlockThermalCompressor> THERMAL_COMPRESSOR = register("thermal_compressor",
            BlockThermalCompressor::new);
    public static final RegistryObject<BlockHeatPipe> HEAT_PIPE = register("heat_pipe",
            BlockHeatPipe::new);
    public static final RegistryObject<BlockEtchingTank> ETCHING_TANK = register("etching_tank",
            BlockEtchingTank::new);
    public static final RegistryObject<BlockFluidTank> TANK_SMALL = register("small_tank",
            () -> new BlockFluidTank(BlockFluidTank.Size.SMALL), block -> () -> new BlockFluidTank.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<BlockFluidTank> TANK_MEDIUM = register("medium_tank",
            () -> new BlockFluidTank(BlockFluidTank.Size.MEDIUM), block -> () -> new BlockFluidTank.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<BlockFluidTank> TANK_LARGE = register("large_tank",
            () -> new BlockFluidTank(BlockFluidTank.Size.LARGE), block -> () -> new BlockFluidTank.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<BlockFluidTank> TANK_HUGE = register("huge_tank",
            () -> new BlockFluidTank(BlockFluidTank.Size.HUGE), block -> () -> new BlockFluidTank.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<BlockReinforcedChest> REINFORCED_CHEST = register("reinforced_chest",
            BlockReinforcedChest::new, block -> () -> new BlockReinforcedChest.ItemBlockReinforcedChest(block.get()));
    public static final RegistryObject<BlockSmartChest> SMART_CHEST = register("smart_chest",
            BlockSmartChest::new, block -> () -> new BlockSmartChest.ItemBlockBlockSmartChest(block.get()));
    public static final RegistryObject<BlockTagWorkbench> TAG_WORKBENCH = register("tag_workbench",
            BlockTagWorkbench::new);
    public static final RegistryObject<BlockDisplayTable> DISPLAY_TABLE = register("display_table",
            BlockDisplayTable::new);
    public static final RegistryObject<BlockDisplayShelf> DISPLAY_SHELF = register("display_shelf",
            BlockDisplayShelf::new);
    public static final RegistryObject<BlockDroneInterface> DRONE_INTERFACE = register("drone_interface",
            BlockDroneInterface::new);
    public static final RegistryObject<BlockThermalLagging> THERMAL_LAGGING = register("thermal_lagging",
            BlockThermalLagging::new);
    public static final RegistryObject<BlockFluidMixer> FLUID_MIXER = register("fluid_mixer",
            BlockFluidMixer::new);
    public static final RegistryObject<BlockVacuumTrap> VACUUM_TRAP = register("vacuum_trap",
            BlockVacuumTrap::new, block -> () -> new BlockVacuumTrap.ItemBlockVacuumTrap(block.get()));
    public static final RegistryObject<BlockSpawnerExtractor> SPAWNER_EXTRACTOR = register("spawner_extractor",
            BlockSpawnerExtractor::new);
    public static final RegistryObject<BlockEmptySpawner> EMPTY_SPAWNER = register("empty_spawner",
            BlockEmptySpawner::new);
    public static final RegistryObject<BlockPressurizedSpawner> PRESSURIZED_SPAWNER = register("pressurized_spawner",
            BlockPressurizedSpawner::new);

    public static final List<RegistryObject<BlockPlasticBrick>> PLASTIC_BRICKS = new ArrayList<>();
    public static final List<RegistryObject<BlockWallLamp>> WALL_LAMPS = new ArrayList<>();
    public static final List<RegistryObject<BlockWallLamp>> WALL_LAMPS_INVERTED = new ArrayList<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            PLASTIC_BRICKS.add(register("plastic_brick_" + color.getName(), () -> new BlockPlasticBrick(color),
                    block -> () -> new BlockPlasticBrick.ItemPlasticBrick(block.get())));
            WALL_LAMPS.add(register("wall_lamp_" + color.getName(), () -> new BlockWallLamp(color, false),
                    block -> () -> new BlockWallLamp.ItemWallLamp(block.get())));
            WALL_LAMPS_INVERTED.add(register("wall_lamp_inverted_" + color.getName(), () -> new BlockWallLamp(color, true),
                    block -> () -> new BlockWallLamp.ItemWallLamp(block.get())));
        }
    }

    public static final RegistryObject<Block> REINFORCED_STONE = register("reinforced_stone",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICKS = register("reinforced_bricks",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_TILE = register("reinforced_brick_tile",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_STAIRS = register("reinforced_brick_stairs",
            () -> new StairsBlock(() -> REINFORCED_BRICKS.get().defaultBlockState(),
                    reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_SLAB = register("reinforced_brick_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_STONE_SLAB = register("reinforced_stone_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_PILLAR = register("reinforced_brick_pillar",
            () -> new RotatedPillarBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_WALL = register("reinforced_brick_wall",
            () -> new WallBlock(reinforcedStoneProps()));

    public static final RegistryObject<Block> COMPRESSED_STONE = register("compressed_stone",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICKS = register("compressed_bricks",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_TILE = register("compressed_brick_tile",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_STAIRS = register("compressed_brick_stairs",
            () -> new StairsBlock(() -> COMPRESSED_BRICKS.get().getDefaultState(),
                    reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_SLAB = register("compressed_brick_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_STONE_SLAB = register("compressed_stone_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_PILLAR = register("compressed_brick_pillar",
            () -> new RotatedPillarBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_WALL = register("compressed_brick_wall",
            () -> new WallBlock(reinforcedStoneProps()));

    public static final RegistryObject<BlockFluidEtchingAcid> ETCHING_ACID = registerNoItem("etching_acid",
            () -> new BlockFluidEtchingAcid(fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> PLASTIC = registerNoItem("plastic",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.PLASTIC.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> LUBRICANT = registerNoItem("lubricant",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.LUBRICANT.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> OIL = registerNoItem("oil",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.OIL.get(),  fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> DIESEL = registerNoItem("diesel",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.DIESEL.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> KEROSENE = registerNoItem("kerosene",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.KEROSENE.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> GASOLINE = registerNoItem("gasoline",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.GASOLINE.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> LPG = registerNoItem("lpg",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.LPG.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> MEMORY_ESSENCE = registerNoItem("memory_essence",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.MEMORY_ESSENCE.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> YEAST_CULTURE = registerNoItem("yeast_culture",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.YEAST_CULTURE.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> ETHANOL = registerNoItem("ethanol",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.ETHANOL.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> VEGETABLE_OIL = registerNoItem("vegetable_oil",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.VEGETABLE_OIL.get(), fluidProps()));
    public static final RegistryObject<FlowingFluidBlock> BIODIESEL = registerNoItem("biodiesel",
            () -> new FlowingFluidBlock(() -> (FlowingFluid) ModFluids.BIODIESEL.get(), fluidProps()));



    public static RegistryObject<BlockPlasticBrick> plasticBrick(DyeColor color) {
        return PLASTIC_BRICKS.get(color.getId());
    }

    public static RegistryObject<BlockWallLamp> wallLamp(DyeColor color, boolean inverted) {
        return inverted ? WALL_LAMPS_INVERTED.get(color.getId()) : WALL_LAMPS.get(color.getId());
    }
}
