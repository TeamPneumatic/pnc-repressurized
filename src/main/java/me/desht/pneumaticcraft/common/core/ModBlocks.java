package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.block.*;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.awt.*;
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
    public static final Block ELEVATOR_BASE = null;
    public static final Block ELEVATOR_FRAME = null;
    public static final Block VACUUM_PUMP = null;
    public static final Block PNEUMATIC_DOOR_BASE = null;
    public static final Block PNEUMATIC_DOOR = null;
    public static final Block ASSEMBLY_PLATFORM = null;
    public static final Block ASSEMBLY_IO_UNIT = null;
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
    public static final Block THERMOPNEUMATIC_PROCESSING_PLANT = null;
    public static final Block KEROSENE_LAMP = null;
    public static final Block KEROSENE_LAMP_LIGHT = null;
    public static final Block SENTRY_TURRET = null;
    public static final Block FLUX_COMPRESSOR = null;
    public static final Block PNEUMATIC_DYNAMO = null;
    public static final Block FAKE_ICE = null;
    public static final Block THERMAL_COMPRESSOR = null;
    public static final FlowingFluidBlock OIL_BLOCK = null;
    public static final FlowingFluidBlock ETCHING_ACID_BLOCK = null;
    public static final FlowingFluidBlock PLASTIC_BLOCK = null;
    public static final FlowingFluidBlock DIESEL_BLOCK = null;
    public static final FlowingFluidBlock KEROSENE_BLOCK = null;
    public static final FlowingFluidBlock GASOLINE_BLOCK = null;
    public static final FlowingFluidBlock LPG_BLOCK = null;
    public static final FlowingFluidBlock LUBRICANT_BLOCK = null;

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
            registerBlock(r, new BlockElevatorBase());
            registerBlock(r, new BlockElevatorFrame());
            registerBlock(r, new BlockPressureChamberInterface());
            registerBlock(r, new BlockVacuumPump());
            registerBlock(r, new BlockPneumaticDoorBase());
            registerBlock(r, new BlockPneumaticDoor());
            registerBlock(r, new BlockAssemblyIOUnit());
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
            registerBlock(r, new BlockRefinery());
            registerBlock(r, new BlockThermopneumaticProcessingPlant());
            registerBlock(r, new BlockKeroseneLamp());
            registerBlock(r, new BlockKeroseneLampLight());
            registerBlock(r, new BlockSentryTurret());
            registerBlock(r, new BlockFluxCompressor());
            registerBlock(r, new BlockPneumaticDynamo());
            registerBlock(r, new BlockFakeIce());
            registerBlock(r, new BlockThermalCompressor());

            Block.Properties fluidProps = Block.Properties.create(Material.WATER).doesNotBlockMovement().noDrops();
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.OIL_SOURCE, fluidProps, "oil_block"));
            registerBlock(r, new BlockFluidEtchingAcid(() -> (FlowingFluid) ModFluids.ETCHING_ACID_SOURCE, fluidProps));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.PLASTIC_SOURCE, fluidProps, "plastic_block"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.DIESEL_SOURCE, fluidProps, "diesel_block"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.KEROSENE_SOURCE, fluidProps, "kerosene_block"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.GASOLINE_SOURCE, fluidProps, "gasoline_block"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.LPG_SOURCE, fluidProps, "lpg_block"));
            registerBlock(r, new BlockFluidPneumaticCraft(() -> (FlowingFluid) ModFluids.LUBRICANT_SOURCE, fluidProps, "lubricant_block"));
        }

        static void registerBlock(IForgeRegistry<Block> registry, Block block) {
            registry.register(block);
            ThirdPartyManager.instance().onBlockRegistry(block);
            ALL_BLOCKS.add(block);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                int heatLevel = te instanceof IHeatTinted ? ((IHeatTinted) te).getHeatLevelForTintIndex(tintIndex) : 10;
                float[] color = HeatUtil.getColorForHeatLevel(heatLevel);
                return 0xFF000000 + ((int) (color[0] * 255) << 16) + ((int) (color[1] * 255) << 8) + (int) (color[2] * 255);
            }
            return 0xFFFFFFFF;
        }, ModBlocks.COMPRESSED_IRON_BLOCK, ModBlocks.HEAT_SINK, ModBlocks.VORTEX_TUBE, ModBlocks.THERMAL_COMPRESSOR);

        event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                if (te instanceof TileEntityUVLightBox) {
                    return ((TileEntityUVLightBox) te).areLightsOn ? 0xFF4000FF : 0xFFAFAFE4;
                }
            }
            return 0xFFAFAFE4;
        }, ModBlocks.UV_LIGHT_BOX);

        event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                if (te instanceof TileEntityOmnidirectionalHopper) {
                    return ((TileEntityOmnidirectionalHopper) te).isCreative ? 0xFFFF60FF : 0xFFFFFFFF;
                }
            }
            return 0xFFFFFFFF;
        }, ModBlocks.OMNIDIRECTIONAL_HOPPER, ModBlocks.LIQUID_HOPPER);

        for (Block b : ModBlocks.Registration.ALL_BLOCKS) {
            if (b instanceof BlockPneumaticCraftCamo) {
                event.getBlockColors().register((state, worldIn, pos, tintIndex) -> {
                    if (pos == null || worldIn == null) return 0xffffff;
                    TileEntity te = worldIn.getTileEntity(pos);
                    if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
                        return Minecraft.getInstance().getBlockColors().getColor(((ICamouflageableTE) te).getCamouflage(), te.getWorld(), pos, tintIndex);
                    } else {
                        return 0xffffff;
                    }
                }, b);
            }
        }

        event.getBlockColors().register((state, worldIn, pos, tintIndex) -> {
            if (worldIn != null && pos != null) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof TileEntityAphorismTile) {
                    int dmg;
                    switch (tintIndex) {
                        case 0: // border
                            dmg = ((TileEntityAphorismTile) te).getBorderColor();
                            return DyeColor.byId(dmg).func_218388_g();
                        case 1: // background
                            dmg = ((TileEntityAphorismTile) te).getBackgroundColor();
                            return desaturate(DyeColor.byId(dmg).func_218388_g());
                        default:
                            return 0xFFFFFF;
                    }
                }
            }
            return 0xFFFFFF;
        }, ModBlocks.APHORISM_TILE);
    }

    static int desaturate(int c) {
        float[] hsb = Color.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        Color color = Color.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getRGB();
    }
}
