package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder(Names.MOD_ID)
public class Blockss {
    @GameRegistry.ObjectHolder("pressure_tube")
    public static final Block PRESSURE_TUBE = null;
    @GameRegistry.ObjectHolder("air_compressor")
    public static final Block AIR_COMPRESSOR = null;
    @GameRegistry.ObjectHolder("air_cannon")
    public static final Block AIR_CANNON = null;
    @GameRegistry.ObjectHolder("pressure_chamber_wall")
    public static final Block PRESSURE_CHAMBER_WALL = null;
    @GameRegistry.ObjectHolder("pressure_chamber_glass")
    public static final Block PRESSURE_CHAMBER_GLASS = null;
    @GameRegistry.ObjectHolder("pressure_chamber_valve")
    public static final Block PRESSURE_CHAMBER_VALVE = null;
    @GameRegistry.ObjectHolder("pressure_chamber_interface")
    public static final Block PRESSURE_CHAMBER_INTERFACE = null;
    @GameRegistry.ObjectHolder("charging_station")
    public static final Block CHARGING_STATION = null;
    @GameRegistry.ObjectHolder("elevator_base")
    public static final Block ELEVATOR_BASE = null;
    @GameRegistry.ObjectHolder("elevator_frame")
    public static final Block ELEVATOR_FRAME = null;
    @GameRegistry.ObjectHolder("vacuum_pump")
    public static final Block VACUUM_PUMP = null;
    @GameRegistry.ObjectHolder("pneumatic_door_base")
    public static final Block PNEUMATIC_DOOR_BASE = null;
    @GameRegistry.ObjectHolder("pneumatic_door")
    public static final Block PNEUMATIC_DOOR = null;
    @GameRegistry.ObjectHolder("assembly_platform")
    public static final Block ASSEMBLY_PLATFORM = null;
    @GameRegistry.ObjectHolder("assembly_io_unit")
    public static final Block ASSEMBLY_IO_UNIT = null;
    @GameRegistry.ObjectHolder("assembly_drill")
    public static final Block ASSEMBLY_DRILL = null;
    @GameRegistry.ObjectHolder("assembly_laser")
    public static final Block ASSEMBLY_LASER = null;
    @GameRegistry.ObjectHolder("assembly_controller")
    public static final Block ASSEMBLY_CONTROLLER = null;
    @GameRegistry.ObjectHolder("advanced_pressure_tube")
    public static final Block ADVANCED_PRESSURE_TUBE = null;
    @GameRegistry.ObjectHolder("compressed_iron_block")
    public static final Block COMPRESSED_IRON = null;
    @GameRegistry.ObjectHolder("uv_light_box")
    public static final Block UV_LIGHT_BOX = null;
    @GameRegistry.ObjectHolder("security_station")
    public static final Block SECURITY_STATION = null;
    @GameRegistry.ObjectHolder("universal_sensor")
    public static final Block UNIVERSAL_SENSOR = null;
    @GameRegistry.ObjectHolder("universal_actuator")
    public static final Block UNIVERSAL_ACTUATOR = null;
    @GameRegistry.ObjectHolder("aerial_interface")
    public static final Block AERIAL_INTERFACE = null;
    @GameRegistry.ObjectHolder("electrostatic_compressor")
    public static final Block ELECTROSTATIC_COMPRESSOR = null;
    @GameRegistry.ObjectHolder("aphorism_tile")
    public static final Block APHORISM_TILE = null;
    @GameRegistry.ObjectHolder("omnidirectional_hopper")
    public static final Block OMNIDIRECTIONAL_HOPPER = null;
    @GameRegistry.ObjectHolder("elevator_caller")
    public static final Block ELEVATOR_CALLER = null;
    @GameRegistry.ObjectHolder("programmer")
    public static final Block PROGRAMMER = null;
    @GameRegistry.ObjectHolder("creative_compressor")
    public static final Block CREATIVE_COMPRESSOR = null;
    @GameRegistry.ObjectHolder("plastic_mixer")
    public static final Block PLASTIC_MIXER = null;
    @GameRegistry.ObjectHolder("liquid_compressor")
    public static final Block LIQUID_COMPRESSOR = null;
    @GameRegistry.ObjectHolder("advanced_liquid_compressor")
    public static final Block ADVANCED_LIQUID_COMPRESSOR = null;
    @GameRegistry.ObjectHolder("advanced_air_compressor")
    public static final Block ADVANCED_AIR_COMPRESSOR = null;
    @GameRegistry.ObjectHolder("liquid_hopper")
    public static final Block LIQUID_HOPPER = null;
    @GameRegistry.ObjectHolder("drone_redstone_emitter")
    public static final Block DRONE_REDSTONE_EMITTER = null;
    @GameRegistry.ObjectHolder("heat_sink")
    public static final Block HEAT_SINK = null;
    @GameRegistry.ObjectHolder("vortex_tube")
    public static final Block VORTEX_TUBE = null;
    @GameRegistry.ObjectHolder("programmable_controller")
    public static final Block PROGRAMMABLE_CONTROLLER = null;
    @GameRegistry.ObjectHolder("gas_lift")
    public static final Block GAS_LIFT = null;
    @GameRegistry.ObjectHolder("refinery")
    public static final Block REFINERY = null;
    @GameRegistry.ObjectHolder("thermopneumatic_processing_plant")
    public static final Block THERMOPNEUMATIC_PROCESSING_PLANT = null;
    @GameRegistry.ObjectHolder("kerosene_lamp")
    public static final Block KEROSENE_LAMP = null;
    @GameRegistry.ObjectHolder("kerosene_lamp_light")
    public static final Block KEROSENE_LAMP_LIGHT = null;
    @GameRegistry.ObjectHolder("sentry_turret")
    public static final Block SENTRY_TURRET = null;

    public static List<Block> blocks = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        registerBlock(registry, new BlockPressureTube("pressure_tube", PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE));
        registerBlock(registry, new BlockPressureTube("advanced_pressure_tube", PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.VOLUME_ADVANCED_PRESSURE_TUBE));
        registerBlock(registry, new BlockAirCompressor());
        registerBlock(registry, new BlockAdvancedAirCompressor());
        registerBlock(registry, new BlockAirCannon());
        registerBlock(registry, new BlockPressureChamberWall());
        registerBlock(registry, new BlockPressureChamberGlass());
        registerBlock(registry, new BlockPressureChamberValve());
        registerBlock(registry, new BlockChargingStation());
        registerBlock(registry, new BlockElevatorBase());
        registerBlock(registry, new BlockElevatorFrame());
        registerBlock(registry, new BlockPressureChamberInterface());
        registerBlock(registry, new BlockVacuumPump());
        registerBlock(registry, new BlockPneumaticDoorBase());
        registerBlock(registry, new BlockPneumaticDoor());
        registerBlock(registry, new BlockAssemblyIOUnit());
        registerBlock(registry, new BlockAssemblyPlatform());
        registerBlock(registry, new BlockAssemblyDrill());
        registerBlock(registry, new BlockAssemblyLaser());
        registerBlock(registry, new BlockAssemblyController());
        registerBlock(registry, new BlockCompressedIron());
        registerBlock(registry, new BlockUVLightBox());
        registerBlock(registry, new BlockSecurityStation());
        registerBlock(registry, new BlockUniversalSensor());
//        registerBlock(registry, new BlockUniversalActuator());
        registerBlock(registry, new BlockAerialInterface());
        registerBlock(registry, new BlockElectrostaticCompressor());
        registerBlock(registry, new BlockAphorismTile());
        registerBlock(registry, new BlockOmnidirectionalHopper());
        registerBlock(registry, new BlockLiquidHopper());
        registerBlock(registry, new BlockElevatorCaller());
        registerBlock(registry, new BlockProgrammer());
        registerBlock(registry, new BlockCreativeCompressor());
        registerBlock(registry, new BlockPlasticMixer());
        registerBlock(registry, new BlockLiquidCompressor());
        registerBlock(registry, new BlockAdvancedLiquidCompressor());
        registerBlock(registry, new BlockDroneRedstoneEmitter());
        registerBlock(registry, new BlockHeatSink());
        registerBlock(registry, new BlockVortexTube());
        registerBlock(registry, new BlockProgrammableController());
        registerBlock(registry, new BlockGasLift());
        registerBlock(registry, new BlockRefinery());
        registerBlock(registry, new BlockThermopneumaticProcessingPlant());
        registerBlock(registry, new BlockKeroseneLamp());
        if (!ConfigHandler.advanced.disableKeroseneLampFakeAirBlock) registerBlock(registry, new BlockKeroseneLampLight());
        registerBlock(registry, new BlockSentryTurret());

    }

    public static void registerBlock(IForgeRegistry<Block> registry, Block block) {
        registry.register(block);
        ThirdPartyManager.instance().onBlockRegistry(block);
        blocks.add(block);
    }
}
