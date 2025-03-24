package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.*;
import me.desht.pneumaticcraft.common.block.entity.tube.AdvancedPressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.tube.ReinforcedPressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.DroneInterfaceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Names.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = ModItems.ITEMS;

    private static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, ? extends T> sup, BlockBehaviour.Properties props) {
        return register(name, sup, props, ModBlocks::itemDefault);
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, ? extends T> sup, BlockBehaviour.Properties props, Function<DeferredBlock<T>, Supplier<? extends Item>> itemCreator) {
        DeferredBlock<T> ret = registerNoItem(name, sup, props);
        ITEMS.register(name, itemCreator.apply(ret));
        return ret;
    }

    private static <T extends Block> DeferredBlock<T> registerNoItem(String name, Function<BlockBehaviour.Properties, ? extends T> sup, BlockBehaviour.Properties props) {
        return BLOCKS.registerBlock(name, sup, props);
    }

    private static Supplier<BlockItem> itemDefault(final DeferredBlock<? extends Block> blockSupplier) {
        return item(blockSupplier);
    }

    private static Supplier<BlockItem> item(final DeferredBlock<? extends Block> blockSupplier) {
        return () -> new BlockItem(blockSupplier.get(), ModItems.defaultProps());
    }

    public static Block.Properties defaultProps() {
        return Block.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3f, 10f)
                .sound(SoundType.METAL);
    }

    public static BlockBehaviour.Properties camoProps() {
        return defaultProps().noOcclusion();
    }

    public static Block.Properties reinforcedStoneProps() {
        return Block.Properties.of()
                .mapColor(MapColor.STONE)
                .requiresCorrectToolForDrops()
                .strength(3f, 1200f)
                .sound(SoundType.STONE);
    }

    private static Block.Properties fluidProps(MapColor color) {
        return Block.Properties.of()
                .mapColor(color)
                .replaceable()
                .noCollission()
                .strength(100f)
                .noLootTable()
                .liquid()
                .pushReaction(PushReaction.DESTROY);
    }

    public static final DeferredBlock<PressureTubeBlock> PRESSURE_TUBE = register("pressure_tube",
            props -> new PressureTubeBlock(props, PressureTubeBlockEntity::new), camoProps());
    public static final DeferredBlock<PressureTubeBlock> REINFORCED_PRESSURE_TUBE = register("reinforced_pressure_tube",
            props -> new PressureTubeBlock(props, ReinforcedPressureTubeBlockEntity::new), camoProps());
    public static final DeferredBlock<PressureTubeBlock> ADVANCED_PRESSURE_TUBE = register("advanced_pressure_tube",
            props -> new PressureTubeBlock(props, AdvancedPressureTubeBlockEntity::new), camoProps());
    public static final DeferredBlock<AirCannonBlock> AIR_CANNON = register("air_cannon",
            AirCannonBlock::new, defaultProps());
    public static final DeferredBlock<PressureChamberWallBlock> PRESSURE_CHAMBER_WALL = register("pressure_chamber_wall",
            PressureChamberWallBlock::new, IBlockPressureChamber.pressureChamberBlockProps());
    public static final DeferredBlock<PressureChamberGlassBlock> PRESSURE_CHAMBER_GLASS = register("pressure_chamber_glass",
            PressureChamberGlassBlock::new, IBlockPressureChamber.pressureChamberBlockProps().noOcclusion());
    public static final DeferredBlock<PressureChamberValveBlock> PRESSURE_CHAMBER_VALVE = register("pressure_chamber_valve",
            PressureChamberValveBlock::new, defaultProps());
    public static final DeferredBlock<PressureChamberInterfaceBlock> PRESSURE_CHAMBER_INTERFACE = register("pressure_chamber_interface",
            PressureChamberInterfaceBlock::new, defaultProps());
    public static final DeferredBlock<ChargingStationBlock> CHARGING_STATION = register("charging_station",
            ChargingStationBlock::new,
            defaultProps(),
            block -> () -> new ChargingStationBlock.ItemBlockChargingStation(block.get()));
    public static final DeferredBlock<DrillPipeBlock> DRILL_PIPE = register("drill_pipe",
            DrillPipeBlock::new, defaultProps());
    public static final DeferredBlock<ElevatorBaseBlock> ELEVATOR_BASE = register("elevator_base",
            ElevatorBaseBlock::new, camoProps());
    public static final DeferredBlock<ElevatorFrameBlock> ELEVATOR_FRAME = register("elevator_frame",
            ElevatorFrameBlock::new, defaultProps());
    public static final DeferredBlock<VacuumPumpBlock> VACUUM_PUMP = register("vacuum_pump",
            VacuumPumpBlock::new, defaultProps());
    public static final DeferredBlock<PneumaticDoorBaseBlock> PNEUMATIC_DOOR_BASE = register("pneumatic_door_base",
            PneumaticDoorBaseBlock::new, camoProps());
    public static final DeferredBlock<PneumaticDoorBlock> PNEUMATIC_DOOR = register("pneumatic_door",
            PneumaticDoorBlock::new, defaultProps(), block -> () -> new PneumaticDoorBlock.ItemBlockPneumaticDoor(block.get()));
    public static final DeferredBlock<AssemblyPlatformBlock> ASSEMBLY_PLATFORM = register("assembly_platform",
            AssemblyPlatformBlock::new, defaultProps());
    public static final DeferredBlock<AbstractAssemblyIOUnitBlock> ASSEMBLY_IO_UNIT_IMPORT = register("assembly_io_unit_import",
            AbstractAssemblyIOUnitBlock.Import::new, defaultProps());
    public static final DeferredBlock<AbstractAssemblyIOUnitBlock> ASSEMBLY_IO_UNIT_EXPORT = register("assembly_io_unit_export",
            AbstractAssemblyIOUnitBlock.Export::new, defaultProps());
    public static final DeferredBlock<AssemblyDrillBlock> ASSEMBLY_DRILL = register("assembly_drill",
            AssemblyDrillBlock::new, defaultProps());
    public static final DeferredBlock<AssemblyLaserBlock> ASSEMBLY_LASER = register("assembly_laser",
            AssemblyLaserBlock::new, defaultProps());
    public static final DeferredBlock<AssemblyControllerBlock> ASSEMBLY_CONTROLLER = register("assembly_controller",
            AssemblyControllerBlock::new, defaultProps());
    public static final DeferredBlock<CompressedIronBlock> COMPRESSED_IRON_BLOCK = register("compressed_iron_block",
            CompressedIronBlock::new, defaultProps());
    public static final DeferredBlock<UVLightBoxBlock> UV_LIGHT_BOX = register("uv_light_box",
            UVLightBoxBlock::new, defaultProps().lightLevel(state -> state.getValue(UVLightBoxBlock.LIT) ? 15 : 0));
    public static final DeferredBlock<SecurityStationBlock> SECURITY_STATION = register("security_station",
            SecurityStationBlock::new, defaultProps());
    public static final DeferredBlock<UniversalSensorBlock> UNIVERSAL_SENSOR = register("universal_sensor",
            UniversalSensorBlock::new, defaultProps());
    public static final DeferredBlock<AerialInterfaceBlock> AERIAL_INTERFACE = register("aerial_interface",
            AerialInterfaceBlock::new, defaultProps());
    public static final DeferredBlock<ElectrostaticCompressorBlock> ELECTROSTATIC_COMPRESSOR = register("electrostatic_compressor",
            ElectrostaticCompressorBlock::new, defaultProps());
    public static final DeferredBlock<AphorismTileBlock> APHORISM_TILE = register("aphorism_tile",
            AphorismTileBlock::new,
            defaultProps().mapColor(MapColor.QUARTZ).strength(1.5f, 4.0f).noCollission(),
            block -> () -> new AphorismTileBlock.ItemBlockAphorismTile(block.get()));
    public static final DeferredBlock<OmnidirectionalHopperBlock> OMNIDIRECTIONAL_HOPPER = register("omnidirectional_hopper",
            OmnidirectionalHopperBlock::new,
            defaultProps(),
            block -> () -> new OmnidirectionalHopperBlock.ItemBlockOmnidirectionalHopper(block.get()));
    public static final DeferredBlock<ElevatorCallerBlock> ELEVATOR_CALLER = register("elevator_caller",
            ElevatorCallerBlock::new, camoProps());
    public static final DeferredBlock<ProgrammerBlock> PROGRAMMER = register("programmer",
            ProgrammerBlock::new, defaultProps());
    public static final DeferredBlock<CreativeCompressorBlock> CREATIVE_COMPRESSOR = register("creative_compressor",
            CreativeCompressorBlock::new,
            defaultProps(),
            block -> () -> new CreativeCompressorBlock.ItemBlockCreativeCompressor(block.get()));
    public static final DeferredBlock<CreativeCompressedIronBlock> CREATIVE_COMPRESSED_IRON_BLOCK = register("creative_compressed_iron_block",
            CreativeCompressedIronBlock::new,
            defaultProps(),
            block -> () -> new CreativeCompressedIronBlock.ItemBlockCreativeCompressedIron(block.get()));
    public static final DeferredBlock<LiquidCompressorBlock> LIQUID_COMPRESSOR = register("liquid_compressor",
            LiquidCompressorBlock::new, defaultProps().noOcclusion());
    public static final DeferredBlock<AdvancedLiquidCompressorBlock> ADVANCED_LIQUID_COMPRESSOR = register("advanced_liquid_compressor",
            AdvancedLiquidCompressorBlock::new, defaultProps().noOcclusion());
    public static final DeferredBlock<AirCompressorBlock> AIR_COMPRESSOR = register("air_compressor",
            AirCompressorBlock::new, defaultProps());
    public static final DeferredBlock<AdvancedAirCompressorBlock> ADVANCED_AIR_COMPRESSOR = register("advanced_air_compressor",
            AdvancedAirCompressorBlock::new, defaultProps());
    public static final DeferredBlock<SolarCompressorBlock> SOLAR_COMPRESSOR = register("solar_compressor",
            SolarCompressorBlock::new, defaultProps());
    public static final DeferredBlock<LiquidHopperBlock> LIQUID_HOPPER = register("liquid_hopper",
            LiquidHopperBlock::new,
            defaultProps(),
            block -> () -> new LiquidHopperBlock.ItemBlockLiquidHopper(block.get()));
    public static final DeferredBlock<ManualCompressorBlock> MANUAL_COMPRESSOR = register("manual_compressor",
            ManualCompressorBlock::new, defaultProps());
    public static final DeferredBlock<DroneRedstoneEmitterBlock> DRONE_REDSTONE_EMITTER = registerNoItem("drone_redstone_emitter",
            DroneRedstoneEmitterBlock::new, Block.Properties.ofFullCopy(Blocks.AIR));
    public static final DeferredBlock<HeatSinkBlock> HEAT_SINK = register("heat_sink",
            HeatSinkBlock::new, defaultProps());
    public static final DeferredBlock<VortexTubeBlock> VORTEX_TUBE = register("vortex_tube",
            VortexTubeBlock::new, defaultProps());
    public static final DeferredBlock<ProgrammableControllerBlock> PROGRAMMABLE_CONTROLLER = register("programmable_controller",
            ProgrammableControllerBlock::new, defaultProps());
    public static final DeferredBlock<GasLiftBlock> GAS_LIFT = register("gas_lift",
            GasLiftBlock::new, defaultProps());
    public static final DeferredBlock<RefineryControllerBlock> REFINERY = register("refinery",
            RefineryControllerBlock::new, defaultProps());
    public static final DeferredBlock<RefineryOutputBlock> REFINERY_OUTPUT = register("refinery_output",
            RefineryOutputBlock::new, defaultProps());
    public static final DeferredBlock<ThermopneumaticProcessingPlantBlock> THERMOPNEUMATIC_PROCESSING_PLANT = register("thermopneumatic_processing_plant",
            ThermopneumaticProcessingPlantBlock::new, defaultProps().noOcclusion());
    public static final DeferredBlock<KeroseneLampBlock> KEROSENE_LAMP = register("kerosene_lamp",
            KeroseneLampBlock::new,
            defaultProps().lightLevel(state -> state.getValue(LIT) ? 15 : 0),
            block -> () -> new KeroseneLampBlock.ItemBlockKeroseneLamp(block.get()));
    public static final DeferredBlock<KeroseneLampLightBlock> KEROSENE_LAMP_LIGHT = registerNoItem("kerosene_lamp_light",
            KeroseneLampLightBlock::new, Block.Properties.ofFullCopy(Blocks.AIR).lightLevel(blockstate -> 15));
    public static final DeferredBlock<SentryTurretBlock> SENTRY_TURRET = register("sentry_turret",
            SentryTurretBlock::new, defaultProps());
    public static final DeferredBlock<FluxCompressorBlock> FLUX_COMPRESSOR = register("flux_compressor",
            FluxCompressorBlock::new, defaultProps());
    public static final DeferredBlock<PneumaticDynamoBlock> PNEUMATIC_DYNAMO = register("pneumatic_dynamo",
            PneumaticDynamoBlock::new, defaultProps());
    public static final DeferredBlock<ThermalCompressorBlock> THERMAL_COMPRESSOR = register("thermal_compressor",
            ThermalCompressorBlock::new, defaultProps());
    public static final DeferredBlock<HeatPipeBlock> HEAT_PIPE = register("heat_pipe",
            HeatPipeBlock::new, defaultProps().noOcclusion().forceSolidOn());
    public static final DeferredBlock<EtchingTankBlock> ETCHING_TANK = register("etching_tank",
            EtchingTankBlock::new, defaultProps());
    public static final DeferredBlock<FluidTankBlock> TANK_SMALL = register("small_tank",
            props -> new FluidTankBlock(props, FluidTankBlock.Size.SMALL),
            defaultProps(),
            block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final DeferredBlock<FluidTankBlock> TANK_MEDIUM = register("medium_tank",
            props -> new FluidTankBlock(props, FluidTankBlock.Size.MEDIUM),
            defaultProps(),
            block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final DeferredBlock<FluidTankBlock> TANK_LARGE = register("large_tank",
            props -> new FluidTankBlock(props, FluidTankBlock.Size.LARGE),
            defaultProps(),
            block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final DeferredBlock<FluidTankBlock> TANK_HUGE = register("huge_tank",
            props -> new FluidTankBlock(props, FluidTankBlock.Size.HUGE),
            defaultProps(),
            block -> () -> new FluidTankBlock.ItemBlockFluidTank(block.get()));
    public static final DeferredBlock<ReinforcedChestBlock> REINFORCED_CHEST = register("reinforced_chest",
            ReinforcedChestBlock::new,
            reinforcedStoneProps(),
            block -> () -> new ReinforcedChestBlock.ItemBlockReinforcedChest(block.get()));
    public static final DeferredBlock<SmartChestBlock> SMART_CHEST = register("smart_chest",
            SmartChestBlock::new,
            reinforcedStoneProps(),
            block -> () -> new SmartChestBlock.ItemBlockBlockSmartChest(block.get()));
    public static final DeferredBlock<TagWorkbenchBlock> TAG_WORKBENCH = register("tag_workbench",
            TagWorkbenchBlock::new, defaultProps());
    public static final DeferredBlock<DisplayTableBlock> DISPLAY_TABLE = register("display_table",
            DisplayTableBlock::new, defaultProps());
    public static final DeferredBlock<DisplayTableBlock.Shelf> DISPLAY_SHELF = register("display_shelf",
            DisplayTableBlock.Shelf::new, defaultProps());
    public static final DeferredBlock<DroneInterfaceBlock> DRONE_INTERFACE = register("drone_interface",
            DroneInterfaceBlock::new, defaultProps());
    public static final DeferredBlock<ThermalLaggingBlock> THERMAL_LAGGING = register("thermal_lagging",
            ThermalLaggingBlock::new, defaultProps().noOcclusion().noCollission());
    public static final DeferredBlock<FluidMixerBlock> FLUID_MIXER = register("fluid_mixer",
            FluidMixerBlock::new, defaultProps());
    public static final DeferredBlock<VacuumTrapBlock> VACUUM_TRAP = register("vacuum_trap",
            VacuumTrapBlock::new,
            defaultProps(),
            block -> () -> new VacuumTrapBlock.ItemBlockVacuumTrap(block.get()));
    public static final DeferredBlock<SpawnerExtractorBlock> SPAWNER_EXTRACTOR = register("spawner_extractor",
            SpawnerExtractorBlock::new, defaultProps());
    public static final DeferredBlock<EmptySpawnerBlock> EMPTY_SPAWNER = register("empty_spawner",
            EmptySpawnerBlock::new, defaultProps().noOcclusion());
    public static final DeferredBlock<PressurizedSpawnerBlock> PRESSURIZED_SPAWNER = register("pressurized_spawner",
            PressurizedSpawnerBlock::new, defaultProps().noOcclusion());
    public static final DeferredBlock<TubeJunctionBlock> TUBE_JUNCTION = register("tube_junction",
            TubeJunctionBlock::new, defaultProps());

    public static final List<DeferredBlock<PlasticBrickBlock>> PLASTIC_BRICKS = new ArrayList<>();
    public static final List<DeferredBlock<SmoothPlasticBrickBlock>> SMOOTH_PLASTIC_BRICKS = new ArrayList<>();
    public static final List<DeferredBlock<WallLampBlock>> WALL_LAMPS = new ArrayList<>();
    public static final List<DeferredBlock<WallLampBlock>> WALL_LAMPS_INVERTED = new ArrayList<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            PLASTIC_BRICKS.add(register("plastic_brick_" + color.getName(),
                    props -> new PlasticBrickBlock(props, color),
                    defaultProps().sound(SoundType.WOOD).strength(2f),
                    block -> () -> new PlasticBrickBlock.ItemPlasticBrick(block.get())));
            SMOOTH_PLASTIC_BRICKS.add(register("smooth_plastic_brick_" + color.getName(),
                    props -> new SmoothPlasticBrickBlock(props, color),
                    defaultProps().sound(SoundType.WOOD).strength(2f).speedFactor(1.35f),
                    block -> () -> new SmoothPlasticBrickBlock.SmoothPlasticBrickItem(block.get())));
            WALL_LAMPS.add(register("wall_lamp_" + color.getName(), props -> new WallLampBlock(props, color, false),
                    WallLampBlock.wallLampProperties(),
                    block -> () -> new WallLampBlock.ItemWallLamp(block.get())));
            WALL_LAMPS_INVERTED.add(register("wall_lamp_inverted_" + color.getName(),
                    props -> new WallLampBlock(props, color, true),
                    WallLampBlock.wallLampProperties(),
                    block -> () -> new WallLampBlock.ItemWallLamp(block.get())));
        }
    }

    public static final DeferredBlock<Block> REINFORCED_STONE = register("reinforced_stone",
            Block::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> REINFORCED_BRICKS = register("reinforced_bricks",
            Block::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> REINFORCED_BRICK_TILE = register("reinforced_brick_tile",
            Block::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> REINFORCED_BRICK_STAIRS = register("reinforced_brick_stairs",
            props -> new StairBlock(REINFORCED_BRICKS.get().defaultBlockState(), props),
            reinforcedStoneProps());
    public static final DeferredBlock<Block> REINFORCED_BRICK_SLAB = register("reinforced_brick_slab",
            SlabBlock::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> REINFORCED_STONE_SLAB = register("reinforced_stone_slab",
            SlabBlock::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> REINFORCED_BRICK_PILLAR = register("reinforced_brick_pillar",
            RotatedPillarBlock::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> REINFORCED_BRICK_WALL = register("reinforced_brick_wall",
            WallBlock::new, reinforcedStoneProps());

    public static final DeferredBlock<Block> COMPRESSED_STONE = register("compressed_stone",
            Block::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> COMPRESSED_BRICKS = register("compressed_bricks",
            Block::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> COMPRESSED_BRICK_TILE = register("compressed_brick_tile",
            Block::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> COMPRESSED_BRICK_STAIRS = register("compressed_brick_stairs",
            props -> new StairBlock(COMPRESSED_BRICKS.get().defaultBlockState(), props),
            reinforcedStoneProps());
    public static final DeferredBlock<Block> COMPRESSED_BRICK_SLAB = register("compressed_brick_slab",
            SlabBlock::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> COMPRESSED_STONE_SLAB = register("compressed_stone_slab",
            SlabBlock::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> COMPRESSED_BRICK_PILLAR = register("compressed_brick_pillar",
            RotatedPillarBlock::new, reinforcedStoneProps());
    public static final DeferredBlock<Block> COMPRESSED_BRICK_WALL = register("compressed_brick_wall",
            WallBlock::new, reinforcedStoneProps());

    public static final DeferredBlock<FluidEtchingAcidBlock> ETCHING_ACID = registerNoItem("etching_acid",
            FluidEtchingAcidBlock::new, fluidProps(MapColor.EMERALD));
    public static final DeferredBlock<LiquidBlock> PLASTIC = registerNoItem("plastic",
            props -> new LiquidBlock(ModFluids.PLASTIC.get(), props), fluidProps(MapColor.COLOR_GRAY));
    public static final DeferredBlock<LiquidBlock> LUBRICANT = registerNoItem("lubricant",
            props -> new LiquidBlock(ModFluids.LUBRICANT.get(), props), fluidProps(MapColor.COLOR_ORANGE));
    public static final DeferredBlock<LiquidBlock> OIL = registerNoItem("oil",
            props -> new LiquidBlock(ModFluids.OIL.get(),  props.mapColor(DyeColor.BLACK)), fluidProps(MapColor.COLOR_BLACK));
    public static final DeferredBlock<LiquidBlock> DIESEL = registerNoItem("diesel",
            props -> new LiquidBlock(ModFluids.DIESEL.get(), props), fluidProps(MapColor.COLOR_BROWN));
    public static final DeferredBlock<LiquidBlock> KEROSENE = registerNoItem("kerosene",
            props -> new LiquidBlock(ModFluids.KEROSENE.get(), props), fluidProps(MapColor.COLOR_CYAN));
    public static final DeferredBlock<LiquidBlock> GASOLINE = registerNoItem("gasoline",
            props -> new LiquidBlock(ModFluids.GASOLINE.get(), props), fluidProps(MapColor.COLOR_YELLOW));
    public static final DeferredBlock<LiquidBlock> LPG = registerNoItem("lpg",
            props -> new LiquidBlock(ModFluids.LPG.get(), props), fluidProps(MapColor.TERRACOTTA_YELLOW));
    public static final DeferredBlock<LiquidBlock> MEMORY_ESSENCE = registerNoItem("memory_essence",
            props -> new LiquidBlock(ModFluids.MEMORY_ESSENCE.get(), props), fluidProps(MapColor.COLOR_GREEN));
    public static final DeferredBlock<LiquidBlock> YEAST_CULTURE = registerNoItem("yeast_culture",
            props -> new LiquidBlock(ModFluids.YEAST_CULTURE.get(), props), fluidProps(MapColor.SAND));
    public static final DeferredBlock<LiquidBlock> ETHANOL = registerNoItem("ethanol",
            props -> new LiquidBlock(ModFluids.ETHANOL.get(), props), fluidProps(MapColor.COLOR_LIGHT_GRAY));
    public static final DeferredBlock<LiquidBlock> VEGETABLE_OIL = registerNoItem("vegetable_oil",
            props -> new LiquidBlock(ModFluids.VEGETABLE_OIL.get(), props), fluidProps(MapColor.COLOR_YELLOW));
    public static final DeferredBlock<LiquidBlock> BIODIESEL = registerNoItem("biodiesel",
            props -> new LiquidBlock(ModFluids.BIODIESEL.get(), props), fluidProps(MapColor.TERRACOTTA_GREEN));

    public static DeferredBlock<PlasticBrickBlock> plasticBrick(DyeColor color) {
        return PLASTIC_BRICKS.get(color.getId());
    }

    public static DeferredBlock<SmoothPlasticBrickBlock> smoothPlasticBrick(DyeColor color) {
        return SMOOTH_PLASTIC_BRICKS.get(color.getId());
    }

    public static DeferredBlock<WallLampBlock> wallLamp(DyeColor color, boolean inverted) {
        return inverted ? WALL_LAMPS_INVERTED.get(color.getId()) : WALL_LAMPS.get(color.getId());
    }
}
