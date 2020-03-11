package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.block.*;
import me.desht.pneumaticcraft.lib.Names;
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
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Names.MOD_ID);
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
        return () -> new BlockItem(block.get(), new Item.Properties().group(ModItems.ItemGroups.PNC_CREATIVE_TAB).setTEISR(renderMethod));
    }

    private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block, final ItemGroup itemGroup) {
        return () -> new BlockItem(block.get(), new Item.Properties().group(itemGroup));
    }

    public static Block.Properties defaultProps() {
        return Block.Properties.create(Material.IRON)
                .hardnessAndResistance(3f, 10f)
                .sound(SoundType.METAL);
    }

    public static Block.Properties reinforcedStoneProps() {
        return Block.Properties.create(Material.ROCK, MaterialColor.GRAY)
                .hardnessAndResistance(5f, 1200f)
                .sound(SoundType.STONE);
    }

    private static Block.Properties fluidProps() {
        return Block.Properties.create(Material.WATER).doesNotBlockMovement().noDrops();
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
            BlockChargingStation::new);
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
            BlockKeroseneLamp::new);
    public static final RegistryObject<BlockKeroseneLampLight> KEROSENE_LAMP_LIGHT = registerNoItem("kerosene_lamp_light",
            BlockKeroseneLampLight::new);
    public static final RegistryObject<BlockSentryTurret> SENTRY_TURRET = register("sentry_turret",
            BlockSentryTurret::new);
    public static final RegistryObject<BlockFluxCompressor> FLUX_COMPRESSOR = register("flux_compressor",
            BlockFluxCompressor::new);
    public static final RegistryObject<BlockPneumaticDynamo> PNEUMATIC_DYNAMO = register("pneumatic_dynamo",
            BlockPneumaticDynamo::new);
    public static final RegistryObject<BlockFakeIce> FAKE_ICE = registerNoItem("fake_ice",
            BlockFakeIce::new);
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
    public static final RegistryObject<BlockReinforcedChest> REINFORCED_CHEST = register("reinforced_chest",
            BlockReinforcedChest::new, block -> () -> new BlockReinforcedChest.ItemBlockReinforcedChest(block.get()));
    public static final RegistryObject<BlockSmartChest> SMART_CHEST = register("smart_chest",
            BlockSmartChest::new, block -> () -> new BlockSmartChest.ItemBlockBlockSmartChest(block.get()));

    public static final List<RegistryObject<BlockPlasticBrick>> PLASTIC_BRICKS = new ArrayList<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            PLASTIC_BRICKS.add(register("plastic_brick_" + color.getTranslationKey(), () -> new BlockPlasticBrick(color),
                    block -> () -> new BlockPlasticBrick.ItemPlasticBrick(block.get())));
        }
    }
    public static RegistryObject<BlockPlasticBrick> plasticBrick(DyeColor color) {
        return PLASTIC_BRICKS.get(color.getId());
    }

    public static final RegistryObject<Block> REINFORCED_STONE = register("reinforced_stone",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICKS = register("reinforced_bricks",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_TILE = register("reinforced_brick_tile",
            () -> new Block(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_STAIRS = register("reinforced_brick_stairs",
            () -> new StairsBlock(() -> REINFORCED_BRICKS.get().getDefaultState(),
                    reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_SLAB = register("reinforced_brick_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_STONE_SLAB = register("reinforced_stone_slab",
            () -> new SlabBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_PILLAR = register("reinforced_brick_pillar",
            () -> new RotatedPillarBlock(reinforcedStoneProps()));
    public static final RegistryObject<Block> REINFORCED_BRICK_WALL = register("reinforced_brick_wall",
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
}
