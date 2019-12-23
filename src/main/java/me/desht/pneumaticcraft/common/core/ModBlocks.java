package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.block.*;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.List;

@ObjectHolder(Names.MOD_ID)
public class ModBlocks {
    public static final Block PRESSURE_TUBE = null;
    public static final Block AIR_COMPRESSOR = null;
    public static final Block AIR_CANNON = null;
    public static final Block PRESSURE_CHAMBER_WALL = null;
    public static final Block PRESSURE_CHAMBER_GLASS = null;
    public static final Block PRESSURE_CHAMBER_VALVE = null;
    public static final Block PRESSURE_CHAMBER_INTERFACE = null;
    public static final Block CHARGING_STATION = null;
    public static final Block DRILL_PIPE = null;
    public static final Block ELEVATOR_BASE = null;
    public static final Block ELEVATOR_FRAME = null;
    public static final Block VACUUM_PUMP = null;
    public static final Block PNEUMATIC_DOOR_BASE = null;
    public static final Block PNEUMATIC_DOOR = null;
    public static final Block ASSEMBLY_PLATFORM = null;
    public static final Block ASSEMBLY_IO_UNIT_IMPORT = null;
    public static final Block ASSEMBLY_IO_UNIT_EXPORT = null;
    public static final Block ASSEMBLY_DRILL = null;
    public static final Block ASSEMBLY_LASER = null;
    public static final Block ASSEMBLY_CONTROLLER = null;
    public static final Block ADVANCED_PRESSURE_TUBE = null;
    public static final Block COMPRESSED_IRON_BLOCK = null;
    public static final Block UV_LIGHT_BOX = null;
    public static final Block SECURITY_STATION = null;
    public static final Block UNIVERSAL_SENSOR = null;
    public static final Block AERIAL_INTERFACE = null;
    public static final Block ELECTROSTATIC_COMPRESSOR = null;
    public static final Block APHORISM_TILE = null;
    public static final Block OMNIDIRECTIONAL_HOPPER = null;
    public static final Block ELEVATOR_CALLER = null;
    public static final Block PROGRAMMER = null;
    public static final Block CREATIVE_COMPRESSOR = null;
    public static final Block LIQUID_COMPRESSOR = null;
    public static final Block ADVANCED_LIQUID_COMPRESSOR = null;
    public static final Block ADVANCED_AIR_COMPRESSOR = null;
    public static final Block LIQUID_HOPPER = null;
    public static final Block DRONE_REDSTONE_EMITTER = null;
    public static final Block HEAT_SINK = null;
    public static final Block VORTEX_TUBE = null;
    public static final Block PROGRAMMABLE_CONTROLLER = null;
    public static final Block GAS_LIFT = null;
    public static final Block REFINERY = null;
    public static final Block REFINERY_OUTPUT = null;
    public static final Block THERMOPNEUMATIC_PROCESSING_PLANT = null;
    public static final Block KEROSENE_LAMP = null;
    public static final Block KEROSENE_LAMP_LIGHT = null;
    public static final Block SENTRY_TURRET = null;
    public static final Block FLUX_COMPRESSOR = null;
    public static final Block PNEUMATIC_DYNAMO = null;
    public static final Block FAKE_ICE = null;
    public static final Block THERMAL_COMPRESSOR = null;
    public static final FlowingFluidBlock OIL = null;
    public static final FlowingFluidBlock ETCHING_ACID = null;
    public static final FlowingFluidBlock PLASTIC = null;
    public static final FlowingFluidBlock DIESEL = null;
    public static final FlowingFluidBlock KEROSENE = null;
    public static final FlowingFluidBlock GASOLINE = null;
    public static final FlowingFluidBlock LPG = null;
    public static final FlowingFluidBlock LUBRICANT = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        public static final List<Block> ALL_BLOCKS = new ArrayList<>();

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            IForgeRegistry<Block> r = event.getRegistry();

            registerBlock(r, new BlockPressureTube("pressure_tube", BlockPressureTube.Tier.ONE));
            registerBlock(r, new BlockPressureTube("advanced_pressure_tube", BlockPressureTube.Tier.TWO));
            registerBlock(r, new BlockAirCompressor());
            registerBlock(r, new BlockAdvancedAirCompressor());
            registerBlock(r, new BlockAirCannon());
            registerBlock(r, new BlockPressureChamberWall());
            registerBlock(r, new BlockPressureChamberGlass());
            registerBlock(r, new BlockPressureChamberValve());
            registerBlock(r, new BlockChargingStation());
            registerBlock(r, new BlockDrillPipe());
            registerBlock(r, new BlockElevatorBase());
            registerBlock(r, new BlockElevatorFrame());
            registerBlock(r, new BlockPressureChamberInterface());
            registerBlock(r, new BlockVacuumPump());
            registerBlock(r, new BlockPneumaticDoorBase());
            registerBlock(r, new BlockPneumaticDoor());
            registerBlock(r, new BlockAssemblyIOUnit.Import());
            registerBlock(r, new BlockAssemblyIOUnit.Export());
            registerBlock(r, new BlockAssemblyPlatform());
            registerBlock(r, new BlockAssemblyDrill());
            registerBlock(r, new BlockAssemblyLaser());
            registerBlock(r, new BlockAssemblyController());
            registerBlock(r, new BlockCompressedIron());
            registerBlock(r, new BlockUVLightBox());
            registerBlock(r, new BlockSecurityStation());
            registerBlock(r, new BlockUniversalSensor());
            registerBlock(r, new BlockAerialInterface());
            registerBlock(r, new BlockElectrostaticCompressor());
            registerBlock(r, new BlockAphorismTile());
            registerBlock(r, new BlockOmnidirectionalHopper());
            registerBlock(r, new BlockLiquidHopper());
            registerBlock(r, new BlockElevatorCaller());
            registerBlock(r, new BlockProgrammer());
            registerBlock(r, new BlockCreativeCompressor());
            registerBlock(r, new BlockLiquidCompressor());
            registerBlock(r, new BlockAdvancedLiquidCompressor());
            registerBlock(r, new BlockDroneRedstoneEmitter());
            registerBlock(r, new BlockHeatSink());
            registerBlock(r, new BlockVortexTube());
            registerBlock(r, new BlockProgrammableController());
            registerBlock(r, new BlockGasLift());
            registerBlock(r, new BlockRefineryController());
            registerBlock(r, new BlockRefineryOutput());
            registerBlock(r, new BlockThermopneumaticProcessingPlant());
            registerBlock(r, new BlockKeroseneLamp());
            registerBlock(r, new BlockKeroseneLampLight());
            registerBlock(r, new BlockSentryTurret());
            registerBlock(r, new BlockFluxCompressor());
            registerBlock(r, new BlockPneumaticDynamo());
            registerBlock(r, new BlockFakeIce());
            registerBlock(r, new BlockThermalCompressor());

            Block.Properties fluidProps = Block.Properties.create(Material.WATER).doesNotBlockMovement().noDrops();
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.OIL, fluidProps, "oil"));
            registerBlock(r, new BlockFluidEtchingAcid(() -> (FlowingFluid) ModFluids.ETCHING_ACID, fluidProps));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.PLASTIC, fluidProps, "plastic"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.DIESEL, fluidProps, "diesel"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.KEROSENE, fluidProps, "kerosene"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.GASOLINE, fluidProps, "gasoline"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.LPG, fluidProps, "lpg"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.LUBRICANT, fluidProps, "lubricant"));
        }

        static void registerBlock(IForgeRegistry<Block> registry, Block block) {
            registry.register(block);
            ThirdPartyManager.instance().onBlockRegistry(block);
            ALL_BLOCKS.add(block);
        }
    }
}
