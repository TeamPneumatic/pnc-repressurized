package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.*;
import me.desht.pneumaticcraft.common.block.entity.AdvancedPressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ReinforcedPressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.DroneInterfaceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
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

    private static Supplier<BlockItem> itemDefault(final RegistryObject<? extends Block> blockSupplier) {
        return item(blockSupplier);
    }

    private static Supplier<BlockItem> item(final RegistryObject<? extends Block> blockSupplier) {
        return () -> new BlockItem(blockSupplier.get(), ModItems.defaultProps());
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

    public static final RegistryObject<PressureTubeBlock> PRESSURE_TUBE = register("pressure_tube",
            () -> new PressureTubeBlock(PressureTubeBlockEntity::new));
    public static final RegistryObject<PressureTubeBlock> REINFORCED_PRESSURE_TUBE = register("reinforced_pressure_tube",
            () -> new PressureTubeBlock(ReinforcedPressureTubeBlockEntity::new));
    public static final RegistryObject<PressureTubeBlock> ADVANCED_PRESSURE_TUBE = register("advanced_pressure_tube",
            () -> new PressureTubeBlock(AdvancedPressureTubeBlockEntity::new));
    public static final RegistryObject<AirCannonBlock> AIR_CANNON = register("air_cannon",
            AirCannonBlock::new);
    public static final RegistryObject<PressureChamberWallBlock> PRESSURE_CHAMBER_WALL = register("pressure_chamber_wall",
            PressureChamberWallBlock::new);
    public static final RegistryObject<PressureChamberGlassBlock> PRESSURE_CHAMBER_GLASS = register("pressure_chamber_glass",
            PressureChamberGlassBlock::new);
    public static final RegistryObject<PressureChamberValveBlock> PRESSURE_CHAMBER_VALVE = register("pressure_chamber_valve",
            PressureChamberValveBlock::new);
    public static final RegistryObject<PressureChamberInterfaceBlock> PRESSURE_CHAMBER_INTERFACE = register("pressure_chamber_interface",
            PressureChamberInterfaceBlock::new);
    public static final RegistryObject<ChargingStationBlock> CHARGING_STATION = register("charging_station",
            ChargingStationBlock::new, block -> () -> new ChargingStationBlock.ItemBlockChargingStation(block.get()));
    public static final RegistryObject<DrillPipeBlock> DRILL_PIPE = register("drill_pipe",
            DrillPipeBlock::new);
    public static final RegistryObject<ElevatorBaseBlock> ELEVATOR_BASE = register("elevator_base",
            ElevatorBaseBlock::new);
    public static final RegistryObject<ElevatorFrameBlock> ELEVATOR_FRAME = register("elevator_frame",
            ElevatorFrameBlock::new);
    public static final RegistryObject<VacuumPumpBlock> VACUUM_PUMP = register("vacuum_pump",
            VacuumPumpBlock::new);
    public static final RegistryObject<PneumaticDoorBaseBlock> PNEUMATIC_DOOR_BASE = register("pneumatic_door_base",
            PneumaticDoorBaseBlock::new);
    public static final RegistryObject<PneumaticDoorBlock> PNEUMATIC_DOOR = register("pneumatic_door",
            PneumaticDoorBlock::new, block -> () -> new PneumaticDoorBlock.ItemBlockPneumaticDoor(block.get()));
    public static final RegistryObject<AssemblyPlatformBlock> ASSEMBLY_PLATFORM = register("assembly_platform",
            AssemblyPlatformBlock::new);
    public static final RegistryObject<AbstractAssemblyIOUnitBlock> ASSEMBLY_IO_UNIT_IMPORT = register("assembly_io_unit_import",
            () -> new AbstractAssemblyIOUnitBlock.Import(defaultProps()));
    public static final RegistryObject<AbstractAssemblyIOUnitBlock> ASSEMBLY_IO_UNIT_EXPORT = register("assembly_io_unit_export",
            () -> new AbstractAssemblyIOUnitBlock.Export(defaultProps()));
    public static final RegistryObject<AssemblyDrillBlock> ASSEMBLY_DRILL = register("assembly_drill",
            AssemblyDrillBlock::new);
    public static final RegistryObject<AssemblyLaserBlock> ASSEMBLY_LASER = register("assembly_laser",
            AssemblyLaserBlock::new);
    public static final RegistryObject<AssemblyControllerBlock> ASSEMBLY_CONTROLLER = register("assembly_controller",
            AssemblyControllerBlock::new);
    public static final RegistryObject<CompressedIronBlock> COMPRESSED_IRON_BLOCK = register("compressed_iron_block",
            CompressedIronBlock::new);
    public static final RegistryObject<UVLightBoxBlock> UV_LIGHT_BOX = register("uv_light_box",
            UVLightBoxBlock::new);
    public static final RegistryObject<SecurityStationBlock> SECURITY_STATION = register("security_station",
            SecurityStationBlock::new);
    public static final RegistryObject<UniversalSensorBlock> UNIVERSAL_SENSOR = register("universal_sensor",
            UniversalSensorBlock::new);
    public static final RegistryObject<AerialInterfaceBlock> AERIAL_INTERFACE = register("aerial_interface",
            AerialInterfaceBlock::new);
    public static final RegistryObject<ElectrostaticCompressorBlock> ELECTROSTATIC_COMPRESSOR = register("electrostatic_compressor",
            ElectrostaticCompressorBlock::new);
    public static final RegistryObject<AphorismTileBlock> APHORISM_TILE = register("aphorism_tile",
            AphorismTileBlock::new, block -> () -> new AphorismTileBlock.ItemBlockAphorismTile(block.get()));
    public static final RegistryObject<OmnidirectionalHopperBlock> OMNIDIRECTIONAL_HOPPER = register("omnidirectional_hopper",
            OmnidirectionalHopperBlock::new, block -> () -> new OmnidirectionalHopperBlock.ItemBlockOmnidirectionalHopper(block.get()));
    public static final RegistryObject<ElevatorCallerBlock> ELEVATOR_CALLER = register("elevator_caller",
            ElevatorCallerBlock::new);
    public static final RegistryObject<ProgrammerBlock> PROGRAMMER = register("programmer",
            ProgrammerBlock::new);
    public static final RegistryObject<CreativeCompressorBlock> CREATIVE_COMPRESSOR = register("creative_compressor",
            CreativeCompressorBlock::new, block -> () -> new CreativeCompressorBlock.ItemBlockCreativeCompressor(block.get()));
    public static final RegistryObject<CreativeCompressedIronBlock> CREATIVE_COMPRESSED_IRON_BLOCK = register("creative_compressed_iron_block",
            CreativeCompressedIronBlock::new, block -> () -> new CreativeCompressedIronBlock.ItemBlockCreativeCompressedIron(block.get()));
    public static final RegistryObject<LiquidCompressorBlock> LIQUID_COMPRESSOR = register("liquid_compressor",
            LiquidCompressorBlock::new);
    public static final RegistryObject<AdvancedLiquidCompressorBlock> ADVANCED_LIQUID_COMPRESSOR = register("advanced_liquid_compressor",
            AdvancedLiquidCompressorBlock::new);
    public static final RegistryObject<AirCompressorBlock> AIR_COMPRESSOR = register("air_compressor",
            AirCompressorBlock::new);
    public static final RegistryObject<AdvancedAirCompressorBlock> ADVANCED_AIR_COMPRESSOR = register("advanced_air_compressor",
            AdvancedAirCompressorBlock::new);
    public static final RegistryObject<SolarCompressorBlock> SOLAR_COMPRESSOR = register("solar_compressor",
            SolarCompressorBlock::new);
    public static final RegistryObject<LiquidHopperBlock> LIQUID_HOPPER = register("liquid_hopper",
            LiquidHopperBlock::new, block -> () -> new LiquidHopperBlock.ItemBlockLiquidHopper(block.get()));
    public static final RegistryObject<DroneRedstoneEmitterBlock> DRONE_REDSTONE_EMITTER = registerNoItem("drone_redstone_emitter",
            DroneRedstoneEmitterBlock::new);
    public static final RegistryObject<HeatSinkBlock> HEAT_SINK = register("heat_sink",
            HeatSinkBlock::new);
    public static final RegistryObject<VortexTubeBlock> VORTEX_TUBE = register("vortex_tube",
            VortexTubeBlock::new);
    public static final RegistryObject<ProgrammableControllerBlock> PROGRAMMABLE_CONTROLLER = register("programmable_controller",
            ProgrammableControllerBlock::new);
    public static final RegistryObject<GasLiftBlock> GAS_LIFT = register("gas_lift",
            GasLiftBlock::new);
    public static final RegistryObject<RefineryControllerBlock> REFINERY = register("refinery",
            RefineryControllerBlock::new);
    public static final RegistryObject<RefineryOutputBlock> REFINERY_OUTPUT = register("refinery_output",
            RefineryOutputBlock::new);
    public static final RegistryObject<ThermopneumaticProcessingPlantBlock> THERMOPNEUMATIC_PROCESSING_PLANT = register("thermopneumatic_processing_plant",
            ThermopneumaticProcessingPlantBlock::new);
    public static final RegistryObject<KeroseneLampBlock> KEROSENE_LAMP = register("kerosene_lamp",
            KeroseneLampBlock::new, block -> () -> new KeroseneLampBlock.ItemBlockKeroseneLamp(block.get()));
    public static final RegistryObject<KeroseneLampLightBlock> KEROSENE_LAMP_LIGHT = registerNoItem("kerosene_lamp_light",
            KeroseneLampLightBlock::new);
    public static final RegistryObject<SentryTurretBlock> SENTRY_TURRET = register("sentry_turret",
            SentryTurretBlock::new);
    public static final RegistryObject<FluxCompressorBlock> FLUX_COMPRESSOR = register("flux_compressor",
            FluxCompressorBlock::new);
    public static final RegistryObject<PneumaticDynamoBlock> PNEUMATIC_DYNAMO = register("pneumatic_dynamo",
            PneumaticDynamoBlock::new);
    public static final RegistryObject<ThermalCompressorBlock> THERMAL_COMPRESSOR = register("thermal_compressor",
            ThermalCompressorBlock::new);
    public static final RegistryObject<HeatPipeBlock> HEAT_PIPE = register("heat_pipe",
            HeatPipeBlock::new);
    public static final RegistryObject<EtchingTankBlock> ETCHING_TANK = register("etching_tank",
            EtchingTankBlock::new);
    public static final RegistryObject<FluidTankBlock> TANK_SMALL = register("small_tank",
            () -> new FluidTankBlock(FluidTankBlock.Size.SMALL), block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<FluidTankBlock> TANK_MEDIUM = register("medium_tank",
            () -> new FluidTankBlock(FluidTankBlock.Size.MEDIUM), block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<FluidTankBlock> TANK_LARGE = register("large_tank",
            () -> new FluidTankBlock(FluidTankBlock.Size.LARGE), block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<FluidTankBlock> TANK_HUGE = register("huge_tank",
            () -> new FluidTankBlock(FluidTankBlock.Size.HUGE), block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final RegistryObject<ReinforcedChestBlock> REINFORCED_CHEST = register("reinforced_chest",
            ReinforcedChestBlock::new, block -> () -> new ReinforcedChestBlock.ItemBlockReinforcedChest(block.get()));
    public static final RegistryObject<SmartChestBlock> SMART_CHEST = register("smart_chest",
            SmartChestBlock::new, block -> () -> new SmartChestBlock.ItemBlockBlockSmartChest(block.get()));
    public static final RegistryObject<TagWorkbenchBlock> TAG_WORKBENCH = register("tag_workbench",
            TagWorkbenchBlock::new);
    public static final RegistryObject<DisplayTableBlock> DISPLAY_TABLE = register("display_table",
            DisplayTableBlock::new);
    public static final RegistryObject<DisplayTableBlock.Shelf> DISPLAY_SHELF = register("display_shelf",
            DisplayTableBlock.Shelf::new);
    public static final RegistryObject<DroneInterfaceBlock> DRONE_INTERFACE = register("drone_interface",
            DroneInterfaceBlock::new);
    public static final RegistryObject<ThermalLaggingBlock> THERMAL_LAGGING = register("thermal_lagging",
            ThermalLaggingBlock::new);
    public static final RegistryObject<FluidMixerBlock> FLUID_MIXER = register("fluid_mixer",
            FluidMixerBlock::new);
    public static final RegistryObject<VacuumTrapBlock> VACUUM_TRAP = register("vacuum_trap",
            VacuumTrapBlock::new, block -> () -> new VacuumTrapBlock.ItemBlockVacuumTrap(block.get()));
    public static final RegistryObject<SpawnerExtractorBlock> SPAWNER_EXTRACTOR = register("spawner_extractor",
            SpawnerExtractorBlock::new);
    public static final RegistryObject<EmptySpawnerBlock> EMPTY_SPAWNER = register("empty_spawner",
            EmptySpawnerBlock::new);
    public static final RegistryObject<PressurizedSpawnerBlock> PRESSURIZED_SPAWNER = register("pressurized_spawner",
            PressurizedSpawnerBlock::new);
    public static final RegistryObject<TubeJunctionBlock> TUBE_JUNCTION = register("tube_junction",
            TubeJunctionBlock::new);

    public static final List<RegistryObject<PlasticBrickBlock>> PLASTIC_BRICKS = new ArrayList<>();
    public static final List<RegistryObject<SmoothPlasticBrickBlock>> SMOOTH_PLASTIC_BRICKS = new ArrayList<>();
    public static final List<RegistryObject<WallLampBlock>> WALL_LAMPS = new ArrayList<>();
    public static final List<RegistryObject<WallLampBlock>> WALL_LAMPS_INVERTED = new ArrayList<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            PLASTIC_BRICKS.add(register("plastic_brick_" + color.getName(), () -> new PlasticBrickBlock(color),
                    block -> () -> new PlasticBrickBlock.ItemPlasticBrick(block.get())));
            SMOOTH_PLASTIC_BRICKS.add(register("smooth_plastic_brick_" + color.getName(), () -> new SmoothPlasticBrickBlock(color),
                    block -> () -> new SmoothPlasticBrickBlock.SmoothPlasticBrickItem(block.get())));
            WALL_LAMPS.add(register("wall_lamp_" + color.getName(), () -> new WallLampBlock(color, false),
                    block -> () -> new WallLampBlock.ItemWallLamp(block.get())));
            WALL_LAMPS_INVERTED.add(register("wall_lamp_inverted_" + color.getName(), () -> new WallLampBlock(color, true),
                    block -> () -> new WallLampBlock.ItemWallLamp(block.get())));
        }
    }

    public static final RegistryObject<Block> REINFORCED_STONE = register("reinforced_stone",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICKS = register("reinforced_bricks",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_TILE = register("reinforced_brick_tile",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_STAIRS = register("reinforced_brick_stairs",
            () -> new StairBlock(() -> REINFORCED_BRICKS.get().defaultBlockState(),
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
            () -> new StairBlock(() -> COMPRESSED_BRICKS.get().defaultBlockState(),
                    reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_SLAB = register("compressed_brick_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_STONE_SLAB = register("compressed_stone_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_PILLAR = register("compressed_brick_pillar",
            () -> new RotatedPillarBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> COMPRESSED_BRICK_WALL = register("compressed_brick_wall",
            () -> new WallBlock(reinforcedStoneProps()));

    public static final RegistryObject<FluidEtchingAcidBlock> ETCHING_ACID = registerNoItem("etching_acid",
            () -> new FluidEtchingAcidBlock(fluidProps()));
    public static final RegistryObject<LiquidBlock> PLASTIC = registerNoItem("plastic",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.PLASTIC.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> LUBRICANT = registerNoItem("lubricant",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.LUBRICANT.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> OIL = registerNoItem("oil",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.OIL.get(),  fluidProps()));
    public static final RegistryObject<LiquidBlock> DIESEL = registerNoItem("diesel",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.DIESEL.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> KEROSENE = registerNoItem("kerosene",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.KEROSENE.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> GASOLINE = registerNoItem("gasoline",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.GASOLINE.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> LPG = registerNoItem("lpg",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.LPG.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> MEMORY_ESSENCE = registerNoItem("memory_essence",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.MEMORY_ESSENCE.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> YEAST_CULTURE = registerNoItem("yeast_culture",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.YEAST_CULTURE.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> ETHANOL = registerNoItem("ethanol",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.ETHANOL.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> VEGETABLE_OIL = registerNoItem("vegetable_oil",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.VEGETABLE_OIL.get(), fluidProps()));
    public static final RegistryObject<LiquidBlock> BIODIESEL = registerNoItem("biodiesel",
            () -> new LiquidBlock(() -> (FlowingFluid) ModFluids.BIODIESEL.get(), fluidProps()));

    public static RegistryObject<PlasticBrickBlock> plasticBrick(DyeColor color) {
        return PLASTIC_BRICKS.get(color.getId());
    }

    public static RegistryObject<SmoothPlasticBrickBlock> smoothPlasticBrick(DyeColor color) {
        return SMOOTH_PLASTIC_BRICKS.get(color.getId());
    }

    public static RegistryObject<WallLampBlock> wallLamp(DyeColor color, boolean inverted) {
        return inverted ? WALL_LAMPS_INVERTED.get(color.getId()) : WALL_LAMPS.get(color.getId());
    }
}
