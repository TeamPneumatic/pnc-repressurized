package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.tubes.*;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.datagen.recipe.ShapedPressurizableRecipeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Arrays;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ModRecipeProvider extends RecipeProvider {
    @ObjectHolder("patchouli:book")
    private static Item PATCHOULI_BOOK = null;

    public ModRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        shaped(ModItems.AIR_CANISTER, ModItems.INGOT_IRON_COMPRESSED,
                " T /IRI/IRI",
                'T', ModBlocks.PRESSURE_TUBE,
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'R', Tags.Items.DUSTS_REDSTONE
        ).build(consumer);

        shaped(ModBlocks.ADVANCED_AIR_COMPRESSOR, ModBlocks.ADVANCED_PRESSURE_TUBE,
                "III/I T/ICI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE,
                'C', ModBlocks.AIR_COMPRESSOR
        ).build(consumer);

        shaped(ModBlocks.ADVANCED_LIQUID_COMPRESSOR, ModBlocks.ADVANCED_PRESSURE_TUBE,
                "III/IBT/ICI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'B', Items.BUCKET,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE,
                'C', ModBlocks.AIR_COMPRESSOR
        ).build(consumer);

        shaped(ModItems.ADVANCED_PCB, ModItems.PRINTED_CIRCUIT_BOARD,
                "RPR/PCP/RPR",
                'R', Tags.Items.DUSTS_REDSTONE,
                'P', ModItems.PLASTIC,
                'C', ModItems.PRINTED_CIRCUIT_BOARD
        ).build(consumer);

        shaped(ModBlocks.AERIAL_INTERFACE, ModBlocks.ADVANCED_PRESSURE_TUBE,
                "WHW/ESE/WTW",
                'W', ModBlocks.PRESSURE_CHAMBER_WALL,
                'H', ModBlocks.OMNIDIRECTIONAL_HOPPER,
                'S', Items.NETHER_STAR,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE,
                'E', Items.ENDER_PEARL
        ).build(consumer);

        shaped(ModItems.AIR_CANISTER, ModBlocks.PRESSURE_TUBE,
                " T /IRI/IRI",
                'T', ModBlocks.PRESSURE_TUBE,
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'R', Tags.Items.DUSTS_REDSTONE);

        shaped(ModBlocks.AIR_CANNON, ModBlocks.PRESSURE_TUBE,
                " B / ST/HHH",
                'B', ModItems.CANNON_BARREL,
                'S', ModItems.STONE_BASE,
                'T', ModBlocks.PRESSURE_TUBE,
                'H', Blocks.COBBLESTONE_SLAB
        ).build(consumer);

        shaped(ModBlocks.AIR_COMPRESSOR, ModBlocks.PRESSURE_TUBE,
                "III/I T/IFI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'T', ModBlocks.PRESSURE_TUBE,
                'F', Blocks.FURNACE
        ).build(consumer);

        shaped(new ModuleAirGrate().getItem(), ModBlocks.PRESSURE_TUBE,
                " B /BTB/ B ",
                'B', Blocks.IRON_BARS,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shapedPressure(ModItems.AMADRON_TABLET, ModItems.PLASTIC,
                "PPP/PGP/PCP",
                'P', ModItems.PLASTIC,
                'G', ModItems.GPS_TOOL,
                'C', ModItems.AIR_CANISTER
        ).build(consumer);

        shapeless(ModBlocks.APHORISM_TILE, ModBlocks.APHORISM_TILE,
                ModBlocks.APHORISM_TILE
        ).build(consumer, RL("aphorism_tile_reset"));

        shaped(ModBlocks.ASSEMBLY_CONTROLLER, ModItems.PRINTED_CIRCUIT_BOARD,
                " B /TBB/III",
                'B', ModItems.PRINTED_CIRCUIT_BOARD,
                'T', ModBlocks.PRESSURE_TUBE,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_DRILL, ModItems.PRINTED_CIRCUIT_BOARD,
                "DCC/  C/IBI",
                'D', Tags.Items.GEMS_DIAMOND,
                'C', ModItems.PNEUMATIC_CYLINDER,
                'B', ModItems.PRINTED_CIRCUIT_BOARD,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_LASER, ModItems.PRINTED_CIRCUIT_BOARD,
                "DCC/  C/IBI",
                'D', Tags.Items.GLASS_RED,
                'C', ModItems.PNEUMATIC_CYLINDER,
                'B', ModItems.PRINTED_CIRCUIT_BOARD,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT, ModItems.PRINTED_CIRCUIT_BOARD,
                "HCC/  C/IBI",
                'H', Blocks.HOPPER,
                'C', ModItems.PNEUMATIC_CYLINDER,
                'B', ModItems.PRINTED_CIRCUIT_BOARD,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_IO_UNIT_EXPORT, ModItems.PRINTED_CIRCUIT_BOARD,
                "CCH/C  /IBI",
                'H', Blocks.HOPPER,
                'C', ModItems.PNEUMATIC_CYLINDER,
                'B', ModItems.PRINTED_CIRCUIT_BOARD,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shapeless(ModBlocks.ASSEMBLY_IO_UNIT_EXPORT, ModItems.PRINTED_CIRCUIT_BOARD,
                ModBlocks.ASSEMBLY_IO_UNIT_IMPORT
        ).build(consumer, RL("assembly_io_unit_export_from_import"));

        shapeless(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT, ModItems.PRINTED_CIRCUIT_BOARD,
                ModBlocks.ASSEMBLY_IO_UNIT_EXPORT
        ).build(consumer, RL("assembly_io_unit_import_from_export"));

        shaped(ModBlocks.ASSEMBLY_PLATFORM, ModItems.PRINTED_CIRCUIT_BOARD,
                "C C/PPP/IBI",
                'C', ModItems.PNEUMATIC_CYLINDER,
                'P', ModItems.PLASTIC,
                'B', ModItems.PRINTED_CIRCUIT_BOARD,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shapeless(ModItems.ASSEMBLY_PROGRAM_DRILL_LASER, ModItems.PRINTED_CIRCUIT_BOARD,
                ModItems.ASSEMBLY_PROGRAM_LASER,
                ModItems.ASSEMBLY_PROGRAM_DRILL
        ).build(consumer);

        shaped(ModItems.CANNON_BARREL, ModItems.INGOT_IRON_COMPRESSED,
                "I I/I I/PII",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'P', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(new ModuleCharging().getItem(), ModBlocks.CHARGING_STATION,
                " C /CPC/ C ",
                'C', ModBlocks.CHARGING_STATION,
                'P', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.CHARGING_STATION, ModBlocks.PRESSURE_TUBE,
                "  T/PPP/SSS",
                'T', ModBlocks.PRESSURE_TUBE,
                'P', Items.BRICK,
                'S', Blocks.COBBLESTONE_SLAB
        ).build(consumer);

        shaped(ModBlocks.COMPRESSED_IRON_BLOCK, ModItems.INGOT_IRON_COMPRESSED,
                "III/III/III",
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer, RL("compressed_iron_block_from_ingot"));

        shaped(ModItems.COMPRESSED_IRON_GEAR, ModItems.INGOT_IRON_COMPRESSED,
                " C /CIC/ C ",
                'C', ModItems.INGOT_IRON_COMPRESSED,
                'I', Tags.Items.INGOTS_IRON
        ).build(consumer);

        shaped(ModItems.CROP_SUPPORT, ModItems.INGOT_IRON_COMPRESSED,
                "I I/I I",
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.DRILL_PIPE, 3, ModBlocks.GAS_LIFT,
                "T/T/T",
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModItems.DRONE, ModItems.PRINTED_CIRCUIT_BOARD,
                " B /BPB/ B ",
                'B', ModItems.TURBINE_ROTOR,
                'P', ModItems.PRINTED_CIRCUIT_BOARD
        ).build(consumer);

        shaped(ModBlocks.ELECTROSTATIC_COMPRESSOR, ModItems.TURBINE_ROTOR,
                "BPB/PRP/BCB",
                'B', Blocks.IRON_BARS,
                'P', ModItems.PLASTIC,
                'R', ModItems.TURBINE_ROTOR,
                'C', ModBlocks.AIR_COMPRESSOR
        ).build(consumer);

        shaped(ModBlocks.ELEVATOR_BASE, ModItems.PLASTIC,
                "CP/PC",
                'C', ModItems.PNEUMATIC_CYLINDER,
                'P', ModItems.PLASTIC
        ).build(consumer, RL("elevator_base_1"));
        shaped(ModBlocks.ELEVATOR_BASE, ModItems.PLASTIC,
                "PC/CP",
                'C', ModItems.PNEUMATIC_CYLINDER,
                'P', ModItems.PLASTIC
        ).build(consumer, RL("elevator_base_2"));

        shaped(ModBlocks.ELEVATOR_CALLER, ModItems.PLASTIC,
                "BPB/PRP/BPB",
                'P', ModItems.PLASTIC,
                'B', Blocks.STONE_BUTTON,
                'R', Tags.Blocks.STONE
        ).build(consumer);

        shaped(ModBlocks.ELEVATOR_FRAME, 4, ModItems.INGOT_IRON_COMPRESSED,
                "I I/I I/I I",
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(new ModuleFlowDetector().getItem(), ModItems.TURBINE_BLADE,
                "B B/ T /B B",
                'B', ModItems.TURBINE_BLADE,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.FLUX_COMPRESSOR, ModItems.PRINTED_CIRCUIT_BOARD,
                "GCP/FRT/GQP",
                'G', Tags.Items.DUSTS_REDSTONE,
                'C', ModItems.COMPRESSED_IRON_GEAR,
                'P', ModItems.PRINTED_CIRCUIT_BOARD,
                'F', Tags.Items.STORAGE_BLOCKS_REDSTONE,
                'R', ModItems.TURBINE_ROTOR,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE,
                'Q', Blocks.BLAST_FURNACE
        ).build(consumer);

        shaped(ModBlocks.GAS_LIFT, ModItems.INGOT_IRON_COMPRESSED,
                " T /TGT/III",
                'T', ModBlocks.PRESSURE_TUBE,
                'G', Tags.Items.GLASS,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModItems.GPS_TOOL, ModItems.PLASTIC,
                " R /PGP/PDP",
                'R', Blocks.REDSTONE_TORCH,
                'P', ModItems.PLASTIC,
                'G', Tags.Items.GLASS_PANES,
                'D', Tags.Items.GEMS_DIAMOND
        ).build(consumer);

        shapeless(ModItems.GPS_AREA_TOOL, ModItems.PLASTIC,
                ModItems.GPS_TOOL, ModItems.GPS_TOOL).build(consumer);

        shapeless(ModItems.GUN_AMMO, ModItems.MINIGUN,
                Tags.Items.GUNPOWDER, ModItems.INGOT_IRON_COMPRESSED, Tags.Items.INGOTS_GOLD
        ).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_AP, Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_DIAMOND).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_EXPLOSIVE, Blocks.TNT, Blocks.TNT).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_FREEZING, Blocks.ICE, Blocks.ICE).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_INCENDIARY, Tags.Items.RODS_BLAZE, Tags.Items.RODS_BLAZE).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_WEIGHTED, Tags.Items.STORAGE_BLOCKS_GOLD, Tags.Items.OBSIDIAN).build(consumer);

        shaped(ModItems.HARVESTING_DRONE, ModItems.TURBINE_ROTOR,
                " S /SRS/ S ",
                'S', Tags.Items.CROPS,
                'R', ModItems.TURBINE_ROTOR
        ).build(consumer);

        shaped(ModItems.HEAT_FRAME, ModItems.INGOT_IRON_COMPRESSED,
                "III/IFI/III",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'F', Blocks.FURNACE
        ).build(consumer);

        shaped(ModBlocks.HEAT_SINK, ModItems.INGOT_IRON_COMPRESSED,
                "BBB/IGI",
                'B', Blocks.IRON_BARS,
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'G', Tags.Items.INGOTS_GOLD
        ).build(consumer);

        shapeless(ModItems.INGOT_IRON_COMPRESSED, 9, ModBlocks.COMPRESSED_IRON_BLOCK,
                ModBlocks.COMPRESSED_IRON_BLOCK
        ).build(consumer, RL("compressed_iron_ingot_from_block"));

        shaped(ModBlocks.KEROSENE_LAMP, ModItems.INGOT_IRON_COMPRESSED,
                " I /G G/IBI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'G', Tags.Items.GLASS_PANES,
                'B', Items.BUCKET
        ).build(consumer);

        shaped(ModBlocks.LIQUID_COMPRESSOR, ModItems.INGOT_IRON_COMPRESSED,
                "PBP/LCL",
                'P', ModBlocks.PRESSURE_TUBE,
                'B', Items.BUCKET,
                'L', Tags.Items.LEATHER,
                'C', ModBlocks.AIR_COMPRESSOR
        ).build(consumer);

        shaped(ModBlocks.LIQUID_HOPPER, ModItems.INGOT_IRON_COMPRESSED,
                "I I/ICI/ I ",
                'I', Tags.Items.GLASS,
                'C', Blocks.HOPPER
        ).build(consumer);

        shaped(ModItems.LOGISTIC_DRONE, ModItems.TURBINE_ROTOR,
                " B /BCB/ B ",
                'B', ModItems.TURBINE_ROTOR,
                'C', Tags.Items.DUSTS_REDSTONE
        ).build(consumer);

        shaped(new ModuleLogistics().getItem(), ModItems.PLASTIC,
                "PIP/IRI/PIP",
                'P', ModItems.PLASTIC,
                'R', new ModuleRegulatorTube().getItem(),
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer, RL("logistics_module_1"));
        shaped(new ModuleLogistics().getItem(), ModItems.PLASTIC,
                "IPI/PRP/IRI",
                'P', ModItems.PLASTIC,
                'R', new ModuleRegulatorTube().getItem(),
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer, RL("logistics_module_2"));

        shapedPressure(ModItems.MANOMETER, ModItems.INGOT_IRON_COMPRESSED,
                "G/C",
                'G', ModItems.PRESSURE_GAUGE,
                'C', ModItems.AIR_CANISTER
        ).build(consumer);

        shaped(ModItems.MICROMISSILES, ModItems.PRINTED_CIRCUIT_BOARD,
                " T /WPW/WFW",
                'W', ModItems.PLASTIC,
                'P', ModItems.PRINTED_CIRCUIT_BOARD,
                'T', Blocks.TNT,
                'F', Items.FIRE_CHARGE
        ).build(consumer);

        shapedPressure(ModItems.MINIGUN, ModItems.INGOT_IRON_COMPRESSED,
                "A  /CIB/GL ",
                'A', ModItems.AIR_CANISTER,
                'C', Tags.Items.CHESTS,
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'B', ModItems.CANNON_BARREL,
                'G', Tags.Items.INGOTS_GOLD,
                'L', Blocks.LEVER
        ).build(consumer);

        shaped(ModBlocks.OMNIDIRECTIONAL_HOPPER, ModItems.INGOT_IRON_COMPRESSED,
                "I I/ICI/ I ",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'C', Tags.Items.CHESTS
        ).build(consumer);

        shapedPressure(ModItems.PNEUMATIC_BOOTS, ModItems.PRINTED_CIRCUIT_BOARD,
                "CPC/CAC",
                'C', ModItems.AIR_CANISTER,
                'P', ModItems.PRINTED_CIRCUIT_BOARD,
                'A', Items.LEATHER_BOOTS
        ).build(consumer);
        shapedPressure(ModItems.PNEUMATIC_CHESTPLATE, ModItems.PRINTED_CIRCUIT_BOARD,
                "CPC/CAC/CCC",
                'C', ModItems.AIR_CANISTER,
                'P', ModItems.PRINTED_CIRCUIT_BOARD,
                'A', Items.LEATHER_CHESTPLATE
        ).build(consumer);
        shapedPressure(ModItems.PNEUMATIC_HELMET, ModItems.PRINTED_CIRCUIT_BOARD,
                "CPC/CAC/CCC",
                'C', ModItems.AIR_CANISTER,
                'P', ModItems.PRINTED_CIRCUIT_BOARD,
                'A', Items.LEATHER_HELMET
        ).build(consumer);
        shapedPressure(ModItems.PNEUMATIC_LEGGINGS, ModItems.PRINTED_CIRCUIT_BOARD,
                "CPC/CAC/I I",
                'C', ModItems.AIR_CANISTER,
                'P', ModItems.PRINTED_CIRCUIT_BOARD,
                'A', Items.LEATHER_LEGGINGS,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModItems.PNEUMATIC_CYLINDER, 2, ModItems.PLASTIC,
                "PIP/PIP/PBP",
                'P', ModItems.PLASTIC,
                'B', ModItems.CANNON_BARREL,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.PNEUMATIC_DOOR_BASE, ModItems.PLASTIC,
                " CI/IIT/III",
                'C', ModItems.PNEUMATIC_CYLINDER,
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.PNEUMATIC_DOOR, ModItems.PLASTIC,
                "II/II/II",
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.PNEUMATIC_DYNAMO, ModItems.PRINTED_CIRCUIT_BOARD,
                " T /GIG/IPI",
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE,
                'G', ModItems.COMPRESSED_IRON_GEAR,
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'P', ModItems.PRINTED_CIRCUIT_BOARD
        ).build(consumer);

        shaped(ModBlocks.PRESSURE_CHAMBER_GLASS, 16, ModItems.INGOT_IRON_COMPRESSED,
                "III/IGI/III",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'G', Tags.Items.GLASS
        ).build(consumer);
        shapeless(ModBlocks.PRESSURE_CHAMBER_GLASS, ModItems.INGOT_IRON_COMPRESSED,
                Tags.Items.GLASS, ModBlocks.PRESSURE_CHAMBER_WALL
        ).build(consumer, RL("pressure_chamber_glass_x1"));
        shapeless(ModBlocks.PRESSURE_CHAMBER_GLASS, 4, ModItems.INGOT_IRON_COMPRESSED,
                Tags.Items.GLASS,
                ModBlocks.PRESSURE_CHAMBER_WALL,
                ModBlocks.PRESSURE_CHAMBER_WALL,
                ModBlocks.PRESSURE_CHAMBER_WALL,
                ModBlocks.PRESSURE_CHAMBER_WALL
        ).build(consumer, RL("pressure_chamber_glass_x4"));

        shapeless(ModBlocks.PRESSURE_CHAMBER_INTERFACE, 2, ModItems.INGOT_IRON_COMPRESSED,
                Blocks.HOPPER, ModBlocks.PRESSURE_CHAMBER_WALL, Blocks.HOPPER
        ).build(consumer);

        shaped(ModBlocks.PRESSURE_CHAMBER_VALVE, 16, ModItems.INGOT_IRON_COMPRESSED,
                "III/ITI/III",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);
        shapeless(ModBlocks.PRESSURE_CHAMBER_VALVE, ModItems.INGOT_IRON_COMPRESSED,
                ModBlocks.PRESSURE_TUBE, ModBlocks.PRESSURE_CHAMBER_WALL
        ).build(consumer, RL("pressure_chamber_valve_x1"));
        shapeless(ModBlocks.PRESSURE_CHAMBER_VALVE, 4, ModItems.INGOT_IRON_COMPRESSED,
                ModBlocks.PRESSURE_TUBE,
                ModBlocks.PRESSURE_CHAMBER_WALL,
                ModBlocks.PRESSURE_CHAMBER_WALL,
                ModBlocks.PRESSURE_CHAMBER_WALL,
                ModBlocks.PRESSURE_CHAMBER_WALL
        ).build(consumer, RL("pressure_chamber_valve_x4"));

        shaped(ModBlocks.PRESSURE_CHAMBER_WALL, 16, ModItems.INGOT_IRON_COMPRESSED,
                "III/I I/III",
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModItems.PRESSURE_GAUGE, ModItems.INGOT_IRON_COMPRESSED,
                " G /GIG/ G ",
                'G', Tags.Items.INGOTS_GOLD,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(new ModulePressureGauge().getItem(), ModItems.PRESSURE_GAUGE,
                " G /RTR",
                'G', ModItems.PRESSURE_GAUGE,
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.PRESSURE_TUBE, 8, ModItems.INGOT_IRON_COMPRESSED,
                "IGI",
                'G', Tags.Items.GLASS,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shapeless(ModItems.PRINTED_CIRCUIT_BOARD, ModItems.PLASTIC,
                ModItems.UNASSEMBLED_PCB,
                ModItems.TRANSISTOR, ModItems.TRANSISTOR, ModItems.TRANSISTOR,
                ModItems.CAPACITOR, ModItems.CAPACITOR, ModItems.CAPACITOR
        ).build(consumer);

        shaped(ModBlocks.PROGRAMMABLE_CONTROLLER, ModItems.PRINTED_CIRCUIT_BOARD,
                "IRI/CDP/INI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'R', ModItems.REMOTE,
                'C', ModItems.PRINTED_CIRCUIT_BOARD,
                'P', ModBlocks.ADVANCED_PRESSURE_TUBE,
                'D', ModItems.DRONE,
                'N', ModItems.NETWORK_REGISTRY
        ).build(consumer);

        shaped(ModBlocks.PROGRAMMER, ModItems.PRINTED_CIRCUIT_BOARD,
                "RGR/TBT/P P",
                'R', Tags.Items.DYES_RED,
                'G', Tags.Items.GLASS_PANES_BLACK,
                'T', ModItems.TURBINE_ROTOR,
                'B', ModItems.PRINTED_CIRCUIT_BOARD,
                'P', ModItems.PLASTIC
        ).build(consumer);

        shaped(ModItems.PROGRAMMING_PUZZLE,8, ModItems.PRINTED_CIRCUIT_BOARD,
                "PPP/PCP/PPP",
                'P', ModItems.PLASTIC,
                'C', ModItems.PRINTED_CIRCUIT_BOARD
        ).build(consumer);

        shaped(new ModuleRedstone().getItem(), ModItems.INGOT_IRON_COMPRESSED,
                " R /TDT",
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', ModBlocks.PRESSURE_TUBE,
                'D', Blocks.REPEATER
        ).build(consumer);

        shaped(ModBlocks.REFINERY, ModItems.INGOT_IRON_COMPRESSED,
                "III/GBG/III",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'G', Tags.Items.GLASS,
                'B', Items.BUCKET
        ).build(consumer);
        shaped(ModBlocks.REFINERY_OUTPUT, ModItems.INGOT_IRON_COMPRESSED,
                "III/GDG/III",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'G', Tags.Items.GLASS,
                'D', Tags.Items.GEMS_DIAMOND
        ).build(consumer);

        shaped(new ModuleRegulatorTube().getItem(), ModItems.INGOT_IRON_COMPRESSED,
                "STS",
                'S', new ModuleSafetyValve().getItem(),
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModItems.REINFORCED_AIR_CANISTER, ModBlocks.ADVANCED_PRESSURE_TUBE,
                " T /ICI/III",
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE,
                'C', ModItems.AIR_CANISTER,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModItems.REMOTE, ModItems.TRANSISTOR,
                " I /TGT/TDT",
                'I', ModItems.NETWORK_IO_PORT,
                'D', ModItems.NETWORK_DATA_STORAGE,
                'G', ModItems.GPS_TOOL,
                'T', ModItems.TRANSISTOR
        ).build(consumer);

        shaped(new ModuleSafetyValve().getItem(), ModItems.PRESSURE_GAUGE,
                " G /LTL",
                'G', ModItems.PRESSURE_GAUGE,
                'L', Blocks.LEVER,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.SECURITY_STATION, ModItems.PRINTED_CIRCUIT_BOARD,
                "DBD/TPT/G G",
                'D', Tags.Items.DYES_GRAY,
                'G', ModItems.PLASTIC,
                'B', Tags.Items.GLASS_PANES_BLACK,
                'T', ModItems.TURBINE_ROTOR,
                'P', ModItems.PRINTED_CIRCUIT_BOARD
        ).build(consumer);

        shaped(ModItems.SEISMIC_SENSOR, ModItems.INGOT_IRON_COMPRESSED,
                " T /GRG/GCG",
                'T', Blocks.REDSTONE_TORCH,
                'G', Tags.Items.GLASS,
                'R', Blocks.REPEATER,
                'C', Blocks.NOTE_BLOCK
        ).build(consumer);

        shaped(ModBlocks.SENTRY_TURRET, ModItems.PLASTIC,
                " M /PIP/I I",
                'M', ModItems.MINIGUN,
                'P', ModItems.PLASTIC,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModItems.SPAWNER_AGITATOR, ModItems.INGOT_IRON_COMPRESSED,
                "III/IGI/III",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'G', Items.GHAST_TEAR
        ).build(consumer);

        shaped(ModItems.STONE_BASE, ModItems.INGOT_IRON_COMPRESSED,
                "S S/STS",
                'S', Tags.Items.STONE,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.THERMAL_COMPRESSOR, ModItems.INGOT_IRON_COMPRESSED,
                "ITI/PAP/ITI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'T', ModBlocks.PRESSURE_TUBE,
                'A', ModBlocks.AIR_COMPRESSOR,
                'P', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
        ).build(consumer);

        shaped(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT, ModItems.INGOT_IRON_COMPRESSED,
                "IGI/TRI/IGI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'G', Tags.Items.GLASS,
                'T', ModBlocks.PRESSURE_TUBE,
                'R', Tags.Items.DUSTS_REDSTONE
        ).build(consumer);

        shapeless(ModItems.TRANSFER_GADGET, ModItems.INGOT_IRON_COMPRESSED,
                Blocks.HOPPER, ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModItems.TURBINE_ROTOR, ModItems.TURBINE_BLADE,
                " B / I /B B",
                'B', ModItems.TURBINE_BLADE,
                'I', ModItems.INGOT_IRON_COMPRESSED
        ).build(consumer);

        shaped(ModBlocks.UNIVERSAL_SENSOR, ModItems.PLASTIC,
                " S /PRP/PCP",
                'S', ModItems.SEISMIC_SENSOR,
                'P', ModItems.PLASTIC,
                'R', Blocks.REPEATER,
                'C', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.UV_LIGHT_BOX, ModItems.PCB_BLUEPRINT,
                "LLL/IBT/III",
                'L', Blocks.REDSTONE_LAMP,
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'B', ModItems.PCB_BLUEPRINT,
                'T', ModBlocks.PRESSURE_TUBE
        ).build(consumer);

        shaped(ModBlocks.VACUUM_PUMP, ModItems.TURBINE_ROTOR,
                "GRG/TRT/SSS",
                'G', ModItems.PRESSURE_GAUGE,
                'R', ModItems.TURBINE_ROTOR,
                'T', ModBlocks.PRESSURE_TUBE,
                'S', Blocks.STONE_SLAB
        ).build(consumer);

        shaped(ModBlocks.VORTEX_TUBE, ModItems.INGOT_IRON_COMPRESSED,
                "ITI/GTG/III",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'T', ModBlocks.PRESSURE_TUBE,
                'G', Tags.Items.INGOTS_GOLD
        ).build(consumer);

        // network components
        networkComponent(ModItems.DIAGNOSTIC_SUBROUTINE, 1, ModItems.PLASTIC, Tags.Items.DYES_RED).build(consumer);
        networkComponent(ModItems.NETWORK_API, 1, ModItems.PLASTIC, Tags.Items.DYES_BLUE).build(consumer);
        networkComponent(ModItems.NETWORK_DATA_STORAGE, 1, ModItems.PLASTIC, Tags.Items.DYES_GRAY).build(consumer);
        networkComponent(ModItems.NETWORK_IO_PORT, 1, ModItems.CAPACITOR, Tags.Items.DYES_CYAN).build(consumer);
        networkComponent(ModItems.NETWORK_REGISTRY, 1, ModItems.PLASTIC, Tags.Items.DYES_LIME).build(consumer);
        networkComponent(ModItems.NETWORK_NODE, 16, ModItems.TRANSISTOR, Tags.Items.DYES_PURPLE).build(consumer);

        // logistics frames
        logisticsFrame(ModItems.LOGISTICS_FRAME_ACTIVE_PROVIDER, Tags.Items.DYES_PURPLE).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER, Tags.Items.DYES_RED).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_REQUESTER, Tags.Items.DYES_BLUE).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_STORAGE, Tags.Items.DYES_YELLOW).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_DEFAULT_STORAGE, Tags.Items.DYES_GREEN).build(consumer);

        // pressurizable tools
        pneumaticTool(ModItems.CAMO_APPLICATOR, Tags.Items.DYES_BLUE).build(consumer);
        pneumaticTool(ModItems.PNEUMATIC_WRENCH, Tags.Items.DYES_ORANGE).build(consumer);
        pneumaticTool(ModItems.LOGISTICS_CONFIGURATOR, Tags.Items.DYES_RED).build(consumer);
        pneumaticTool(ModItems.VORTEX_CANNON, Tags.Items.DYES_YELLOW).build(consumer);

        // standard upgrade shapes (4 x lapis, 4 x edge item, 1 x center item)
        standardUpgrade(EnumUpgrade.ARMOR, ModItems.INGOT_IRON_COMPRESSED, Items.DIAMOND).build(consumer);
        standardUpgrade(EnumUpgrade.BLOCK_TRACKER, Items.FERMENTED_SPIDER_EYE, ModBlocks.PRESSURE_CHAMBER_WALL).build(consumer);
        standardUpgrade(EnumUpgrade.CHARGING, new ModuleCharging().getItem(), ModBlocks.PRESSURE_TUBE).build(consumer);
        standardUpgrade(EnumUpgrade.COORDINATE_TRACKER, ModItems.GPS_TOOL, Items.REDSTONE).build(consumer);
        standardUpgrade(EnumUpgrade.DISPENSER, Blocks.DISPENSER, Items.QUARTZ).build(consumer);
        standardUpgrade(EnumUpgrade.ENTITY_TRACKER, Items.FERMENTED_SPIDER_EYE, Items.BONE).build(consumer);
        standardUpgrade(EnumUpgrade.ITEM_LIFE, Items.CLOCK, Items.APPLE).build(consumer);
        standardUpgrade(EnumUpgrade.MAGNET, ModItems.PLASTIC, ModItems.INGOT_IRON_COMPRESSED).build(consumer);
        standardUpgrade(EnumUpgrade.RANGE, Items.BOW, Items.ARROW).build(consumer);
        standardUpgrade(EnumUpgrade.SEARCH, Items.GOLDEN_CARROT, Items.ENDER_EYE).build(consumer);
        standardUpgrade(EnumUpgrade.SECURITY, new ModuleSafetyValve().getItem(), Blocks.OBSIDIAN).build(consumer);
        standardUpgrade(EnumUpgrade.VOLUME, ModItems.AIR_CANISTER, ModItems.INGOT_IRON_COMPRESSED).build(consumer);
        standardUpgrade(EnumUpgrade.FLIPPERS, Items.BLACK_WOOL, ModItems.PLASTIC).build(consumer);

        // non-standard upgrades
        ItemStack nightVisionPotion = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(nightVisionPotion, Potions.LONG_NIGHT_VISION);
        shaped(EnumUpgrade.NIGHT_VISION.getItem(), ModItems.PNEUMATIC_HELMET,
                "LNL/GNG/LNL",
                'L', Items.LAPIS_LAZULI,
                'G', ModBlocks.PRESSURE_CHAMBER_GLASS,
                'N', IngredientNBTWrapper.fromItemStack(nightVisionPotion)
        ).build(consumer);

        shaped(EnumUpgrade.INVENTORY.getItem(), Blocks.CHEST,
                "LWL/WCW/LWL",
                'L', Tags.Items.GEMS_LAPIS,
                'W', ItemTags.PLANKS,
                'C', Tags.Items.CHESTS
        ).build(consumer);

        shaped(EnumUpgrade.SCUBA.getItem(), ModItems.PNEUMATIC_HELMET,
                "LTL/PRP/LPL",
                'L', Items.LAPIS_LAZULI,
                'P', ModItems.PLASTIC,
                'R', new ModuleRegulatorTube().getItem(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE
        ).build(consumer);

        shaped(EnumUpgrade.SPEED.getItem(), ModItems.LUBRICANT_BUCKET,
                "LSL/SFS/LSL",
                'L', Items.LAPIS_LAZULI,
                'S', Items.SUGAR,
                'F', new FluidIngredient(ModFluids.LUBRICANT, 1000)
        ).build(consumer);

        shaped(EnumUpgrade.JET_BOOTS.getItem(1), ModItems.PNEUMATIC_BOOTS,
                "LTL/VCV/LTL",
                'L', Items.LAPIS_LAZULI,
                'V', ModItems.VORTEX_CANNON,
                'C', ModBlocks.ADVANCED_AIR_COMPRESSOR,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE
        ).build(consumer);
        shaped(EnumUpgrade.JET_BOOTS.getItem(2), ModItems.PNEUMATIC_BOOTS,
                "FFF/VUV/CFC",
                'F', Items.FEATHER,
                'V', ModItems.VORTEX_CANNON,
                'C', ModItems.PNEUMATIC_CYLINDER,
                'U', EnumUpgrade.JET_BOOTS.getItem(1)
        ).build(consumer);
        shaped(EnumUpgrade.JET_BOOTS.getItem(3), ModItems.PNEUMATIC_BOOTS,
                "TBT/VUV/TBT",
                'T', Items.GHAST_TEAR,
                'B', Items.BLAZE_ROD,
                'V', ModItems.VORTEX_CANNON,
                'U', EnumUpgrade.JET_BOOTS.getItem(2)
        ).build(consumer);
        ItemStack slowFallPotion = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(slowFallPotion, Potions.LONG_SLOW_FALLING);
        shaped(EnumUpgrade.JET_BOOTS.getItem(4), ModItems.PNEUMATIC_BOOTS,
                "MNM/VUV/P P",
                'N', Items.NETHER_STAR,
                'M', Items.PHANTOM_MEMBRANE,
                'V', ModItems.VORTEX_CANNON,
                'P', IngredientNBTWrapper.fromItemStack(slowFallPotion),
                'U', EnumUpgrade.JET_BOOTS.getItem(3)
        ).build(consumer);
        shaped(EnumUpgrade.JET_BOOTS.getItem(5), ModItems.PNEUMATIC_BOOTS,
                "RER/VUV/RDR",
                'R', Items.END_ROD,
                'E', Items.ELYTRA,
                'V', ModItems.VORTEX_CANNON,
                'D', Items.DRAGON_BREATH,
                'U', EnumUpgrade.JET_BOOTS.getItem(4)
        ).build(consumer);

        shaped(EnumUpgrade.JUMPING.getItem(1), ModItems.PNEUMATIC_LEGGINGS,
                "PCP/VTV",
                'P', Blocks.PISTON,
                'V', ModItems.VORTEX_CANNON,
                'T', ModBlocks.PRESSURE_TUBE,
                'C', ModItems.PNEUMATIC_CYLINDER
        ).build(consumer);
        shaped(EnumUpgrade.JUMPING.getItem(2), ModItems.PNEUMATIC_LEGGINGS,
                "PCP/SUS",
                'U', EnumUpgrade.JUMPING.getItem(1),
                'S', Blocks.SLIME_BLOCK,
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER
        ).build(consumer);
        ItemStack jumpBoostPotion1 = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(jumpBoostPotion1, Potions.LEAPING);
        shaped(EnumUpgrade.JUMPING.getItem(3), ModItems.PNEUMATIC_LEGGINGS,
                "PCP/JUJ/ J ",
                'U', EnumUpgrade.JUMPING.getItem(2),
                'J', IngredientNBTWrapper.fromItemStack(jumpBoostPotion1),
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER
        ).build(consumer);
        ItemStack jumpBoostPotion2 = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(jumpBoostPotion2, Potions.STRONG_LEAPING);
        shaped(EnumUpgrade.JUMPING.getItem(4), ModItems.PNEUMATIC_LEGGINGS,
                "PCP/JUJ/ J ",
                'U', EnumUpgrade.JUMPING.getItem(3),
                'J', IngredientNBTWrapper.fromItemStack(jumpBoostPotion2),
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER
        ).build(consumer);

        specialRecipe(ModRecipes.DRONE_COLOR_CRAFTING).build(consumer, getId("color_drone"));
        specialRecipe(ModRecipes.DRONE_UPGRADE_CRAFTING).build(consumer, getId("drone_upgrade"));
        specialRecipe(ModRecipes.GUN_AMMO_POTION_CRAFTING).build(consumer, getId("gun_ammo_potion_crafting"));
        specialRecipe(ModRecipes.ONE_PROBE_HELMET_CRAFTING).build(consumer, getId("one_probe_crafting"));
        specialRecipe(ModRecipes.PATCHOULI_BOOK_CRAFTING).build(consumer, getId("patchouli_book_crafting"));

        CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(ModItems.FAILED_PCB), ModItems.EMPTY_PCB, 0.5f, 100)
                .addCriterion("has_empty_pcb", this.hasItem(ModItems.FAILED_PCB))
                .build(consumer, RL("empty_pcb_from_failed_pcb"));
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder shapeless(T result, T required, Object... ingredients) {
        return shapeless(result, 1, required, ingredients);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder shapeless(T result, int count, T required, Object... ingredients) {
        ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapelessRecipe(result, count);
        for (Object v : ingredients) {
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.addIngredient((Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.addIngredient((IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.addIngredient((Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private ShapedRecipeBuilder logisticsFrame(Item result, Tag<Item> dye) {
        return shaped(result, 4, ModItems.PLASTIC, "PPP/PDP/PPP", 'P', ModItems.PLASTIC, 'D', dye);
    }

    private ShapedRecipeBuilder networkComponent(Item result, int count, Item edge, Tag<Item> dyeCorner) {
        return shaped(result, count, ModItems.CAPACITOR, "CEC/EXE/CEC", 'C', dyeCorner, 'E', edge, 'X', Tags.Items.CHESTS);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedPressurizableRecipeBuilder pneumaticTool(T result, Object dye) {
        return shapedPressure(result, ModItems.INGOT_IRON_COMPRESSED,
                "IDI/C  /ILI",
                'I', ModItems.INGOT_IRON_COMPRESSED,
                'D', dye,
                'C', ModItems.AIR_CANISTER,
                'L', Blocks.LEVER
        );
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedPressurizableRecipeBuilder shapedPressure(T result, T required, String pattern, Object... keys) {
        ShapedPressurizableRecipeBuilder b = ShapedPressurizableRecipeBuilder.shapedRecipe(result);
        Arrays.stream(pattern.split("/")).forEach(b::patternLine);
        for (int i = 0; i < keys.length; i += 2) {
            Object v = keys[i + 1];
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.key((Character) keys[i], (Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.key((Character) keys[i], (IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.key((Character) keys[i], (Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder shaped(T result, T required, String pattern, Object... keys) {
        return shaped(result, 1, required, pattern, keys);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder shaped(T result, int count, T required, String pattern, Object... keys) {
        ShapedRecipeBuilder b = ShapedRecipeBuilder.shapedRecipe(result, count);
        Arrays.stream(pattern.split("/")).forEach(b::patternLine);
        for (int i = 0; i < keys.length; i += 2) {
            Object v = keys[i + 1];
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.key((Character) keys[i], (Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.key((Character) keys[i], (IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.key((Character) keys[i], (Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private ShapedRecipeBuilder miniGunAmmo(Item result, Object item1, Object item2) {
        return shaped(result, ModItems.GUN_AMMO,
                " A /C1C/C2C",
                'A', ModItems.GUN_AMMO,
                'C', ModItems.INGOT_IRON_COMPRESSED,
                '1', item1,
                '2', item2);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder standardUpgrade(EnumUpgrade what, T center, T edge) {
        return ShapedRecipeBuilder.shapedRecipe(what.getItem())
                .patternLine("LXL")
                .patternLine("XCX")
                .patternLine("LXL")
                .key('L', Items.LAPIS_LAZULI)
                .key('X', edge)
                .key('C', center)
                .addCriterion("has_" + safeName(center), this.hasItem(center));
    }

    private CustomRecipeBuilder specialRecipe(SpecialRecipeSerializer<?> recipe) {
        return CustomRecipeBuilder.func_218656_a(recipe);
    }

    private String getId(String s) {
        return RL(s).toString();
    }

    private ResourceLocation safeId(ResourceLocation id) {
        return new ResourceLocation(id.getNamespace(), safeName(id));
    }

    private String safeName(IForgeRegistryEntry<?>  i) {
        return safeName(i.getRegistryName());
    }

    private String safeName(ResourceLocation registryName) {
        return registryName.getPath().replace('/', '_');
    }

    // this wrapper is due to lack of any public constructor method for IngredientNBT
    private static class IngredientNBTWrapper extends IngredientNBT {
        IngredientNBTWrapper(ItemStack stack) {
            super(stack);
        }

        static IngredientNBTWrapper fromItemStack(ItemStack stack) {
            return new IngredientNBTWrapper(stack);
        }
    }
}
