package me.desht.pneumaticcraft.datagen;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.recipes.FluidTagPresentCondition;
import me.desht.pneumaticcraft.common.recipes.machine.PressureDisenchantingRecipe;
import me.desht.pneumaticcraft.common.recipes.machine.PressureEnchantingRecipe;
import me.desht.pneumaticcraft.common.recipes.special.*;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.playerfilter.PlayerFilter;
import me.desht.pneumaticcraft.datagen.recipe.*;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.crafting.NBTIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn.getPackOutput());
    }

    @Override
    protected void buildRecipes(RecipeOutput consumer) {
        shaped(ModItems.AIR_CANISTER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " T /IRI/IRI",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'R', Tags.Items.DUSTS_REDSTONE
        ).save(consumer);

        shapedCompressorUpgrade(ModBlocks.ADVANCED_AIR_COMPRESSOR.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "III/I T/ICI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'C', ModBlocks.AIR_COMPRESSOR.get()
        ).save(consumer);

        shapedCompressorUpgrade(ModBlocks.ADVANCED_LIQUID_COMPRESSOR.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "III/ITT/ICI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'C', ModBlocks.LIQUID_COMPRESSOR.get()
        ).save(consumer);

        shaped(ModItems.MODULE_EXPANSION_CARD.get(), 4, ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "RPR/PCP/RPR",
                'R', Tags.Items.DUSTS_REDSTONE,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'C', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).save(consumer);

        shaped(ModBlocks.AERIAL_INTERFACE.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "WHW/ESE/WTW",
                'W', ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                'H', ModBlocks.OMNIDIRECTIONAL_HOPPER.get(),
                'S', Items.NETHER_STAR,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'E', Items.ENDER_PEARL
        ).save(consumer);

        shaped(ModItems.AIR_CANISTER.get(), ModBlocks.PRESSURE_TUBE.get(),
                " T /IRI/IRI",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'R', Tags.Items.DUSTS_REDSTONE);

        shaped(ModBlocks.AIR_CANNON.get(), ModBlocks.PRESSURE_TUBE.get(),
                " B / ST/HHH",
                'B', ModItems.CANNON_BARREL.get(),
                'S', ModItems.STONE_BASE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'H', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).save(consumer);

        shaped(ModBlocks.AIR_COMPRESSOR.get(), ModBlocks.PRESSURE_TUBE.get(),
                "III/I T/IFI",
                'I', PneumaticCraftTags.Items.REINFORCED_STONE_BRICKS,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'F', Blocks.FURNACE
        ).save(consumer);

        shaped(ModItems.AIR_GRATE_MODULE.get(), ModBlocks.PRESSURE_TUBE.get(),
                " B /BTB/ B ",
                'B', Blocks.IRON_BARS,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shapedPressure(ModItems.AMADRON_TABLET.get(), ModItems.PLASTIC.get(),
                "PPP/PGP/PCP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'G', ModItems.GPS_TOOL.get(),
                'C', ModItems.AIR_CANISTER.get()
        ).save(consumer);

        shapeless(ModBlocks.APHORISM_TILE.get(), ModBlocks.APHORISM_TILE.get(),
                ModBlocks.APHORISM_TILE.get()
        ).save(consumer, RL("aphorism_tile_reset"));

        shaped(ModBlocks.ASSEMBLY_CONTROLLER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " B /TBB/III",
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.ASSEMBLY_DRILL.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "DCC/  C/IBI",
                'D', Tags.Items.GEMS_DIAMOND,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.ASSEMBLY_LASER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "DCC/  C/IBI",
                'D', Tags.Items.GLASS_RED,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "HCC/  C/IBI",
                'H', Blocks.HOPPER,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "HCC/  C/IBI",
                'H', Blocks.DROPPER,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shapeless(ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get()
        ).save(consumer, RL("assembly_io_unit_export_from_import"));

        shapeless(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get()
        ).save(consumer, RL("assembly_io_unit_import_from_export"));

        shaped(ModBlocks.ASSEMBLY_PLATFORM.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "C C/PPP/IBI",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shapeless(ModItems.ASSEMBLY_PROGRAM_DRILL_LASER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                ModItems.ASSEMBLY_PROGRAM_LASER.get(),
                ModItems.ASSEMBLY_PROGRAM_DRILL.get()
        ).save(consumer);

        shaped(ModItems.BANDAGE.get(), ModItems.GLYCEROL.get(),
                " G /GCG/ G ",
                'G', ModItems.GLYCEROL.get(),
                'C', ItemTags.WOOL_CARPETS
        ).save(consumer);

        shaped(ModItems.CANNON_BARREL.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/I I/IPI",
                'I', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModItems.CHARGING_MODULE.get(), ModBlocks.CHARGING_STATION.get(),
                " C /CPC/ C ",
                'C', ModBlocks.CHARGING_STATION.get(),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.CHARGING_STATION.get(), ModBlocks.PRESSURE_TUBE.get(),
                "  T/PPP/SSS",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'P', Items.BRICK,
                'S', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).save(consumer);

        shapeless(ModItems.CLASSIFY_FILTER.get(), ModItems.LOGISTICS_CORE.get(),
                ModItems.LOGISTICS_CORE.get(), ModItems.PLASTIC.get()
        ).save(consumer);

        shaped(ModItems.COLLECTOR_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " R /RSR/ R ",
                'S', Items.HOPPER,
                'R', ModItems.TURBINE_ROTOR.get()
        ).save(consumer);

        shaped(ModBlocks.COMPRESSED_IRON_BLOCK.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/III/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer, RL("compressed_iron_block_from_ingot"));

        shaped(ModItems.COMPRESSED_IRON_BOOTS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "ILI/I I",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'L', Items.LEATHER_BOOTS
        ).save(consumer);
        shaped(ModItems.COMPRESSED_IRON_CHESTPLATE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "ILI/III/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'L', Items.LEATHER_CHESTPLATE
        ).save(consumer);
        shaped(ModItems.COMPRESSED_IRON_HELMET.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/ILI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'L', Items.LEATHER_HELMET
        ).save(consumer);
        shaped(ModItems.COMPRESSED_IRON_LEGGINGS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/ILI/I I",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'L', Items.LEATHER_LEGGINGS
        ).save(consumer);

        shaped(ModItems.COMPRESSED_IRON_GEAR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " C /CIC/ C ",
                'C', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'I', Tags.Items.INGOTS_IRON
        ).save(consumer);

        shaped(ModItems.CROP_SUPPORT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/I I",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shapeless(ModItems.COPPER_NUGGET.get(), 9, Items.COPPER_INGOT,
                Items.COPPER_INGOT
        ).save(consumer);

        shaped(Items.COPPER_INGOT, Items.COPPER_INGOT,
                "NNN/NNN/NNN",
                'N', ModItems.COPPER_NUGGET.get()
        ).save(consumer, RL("copper_ingot_from_nugget"));

        shaped(ModBlocks.DISPLAY_TABLE.get(), ModBlocks.REINFORCED_STONE.get(),
                "SSS/I I",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shapeless(ModBlocks.DISPLAY_TABLE.get(), ModBlocks.REINFORCED_STONE.get(),
                ModBlocks.DISPLAY_SHELF.get()
        ).save(consumer, RL("display_table_from_shelf"));

        shapeless(ModBlocks.DISPLAY_SHELF.get(), ModBlocks.REINFORCED_STONE.get(),
                ModBlocks.DISPLAY_TABLE.get()
        ).save(consumer);

        shaped(ModBlocks.DRILL_PIPE.get(), 3, ModBlocks.GAS_LIFT.get(),
                "T/T/T",
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModItems.DRONE.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " B /BPB/ B ",
                'B', ModItems.TURBINE_ROTOR.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).save(consumer);

        Item ccModem = BuiltInRegistries.ITEM.get(new ResourceLocation("computercraft:wireless_modem_normal"));
        shaped(ModBlocks.DRONE_INTERFACE.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " U /MP /III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'U', ModUpgrades.RANGE.get().getItem(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'M', ccModem
        ).save(consumer.withConditions(new ModLoadedCondition(ModIds.COMPUTERCRAFT)), RL("drone_interface"));

        shaped(ModBlocks.ELECTROSTATIC_COMPRESSOR.get(), ModItems.TURBINE_ROTOR.get(),
                "BPB/PRP/BCB",
                'B', Blocks.IRON_BARS,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'R', ModItems.TURBINE_ROTOR.get(),
                'C', ModBlocks.ADVANCED_AIR_COMPRESSOR.get()
        ).save(consumer);

        shaped(ModBlocks.ELEVATOR_BASE.get(), ModItems.PLASTIC.get(),
                "CP/PC",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS
        ).save(consumer, RL("elevator_base_1"));
        shaped(ModBlocks.ELEVATOR_BASE.get(), ModItems.PLASTIC.get(),
                "PC/CP",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS
        ).save(consumer, RL("elevator_base_2"));

        shaped(ModBlocks.ELEVATOR_CALLER.get(), ModItems.PLASTIC.get(),
                "BPB/PRP/BPB",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', Blocks.STONE_BUTTON,
                'R', Tags.Blocks.STONE
        ).save(consumer);

        shaped(ModBlocks.ELEVATOR_FRAME.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/I I/I I",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.ETCHING_TANK.get(), ModItems.ETCHING_ACID_BUCKET.get(),
                "OGO/WTW/SSS",
                'O', Tags.Blocks.OBSIDIAN,
                'W', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'T', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get())),
                'S', ModBlocks.REINFORCED_BRICK_SLAB.get(),
                'G', Tags.Items.GLASS_PANES
        ).save(consumer);

        shapeless(ModItems.COD_N_CHIPS.get(), ModItems.CHIPS.get(),
                ModItems.CHIPS.get(), Items.COOKED_COD, Items.PAPER
        ).save(consumer);

        shaped(ModItems.FLOW_DETECTOR_MODULE.get(), ModItems.TURBINE_BLADE.get(),
                "B B/ T /B B",
                'B', ModItems.TURBINE_BLADE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.FLUX_COMPRESSOR.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "GCP/FRT/GQP",
                'G', Tags.Items.DUSTS_REDSTONE,
                'C', ModItems.COMPRESSED_IRON_GEAR.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'F', Tags.Items.STORAGE_BLOCKS_REDSTONE,
                'R', ModItems.TURBINE_ROTOR.get(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'Q', Blocks.BLAST_FURNACE
        ).save(consumer);

        shaped(ModBlocks.FLUID_MIXER.get(), ModBlocks.TANK_SMALL.get(),
                " T /WRW/TPT",
                'T', ModBlocks.TANK_SMALL.get(),
                'W', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'R', ModItems.TURBINE_ROTOR.get(),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.GAS_LIFT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " T /TGT/SSS",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'G', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get())),
                'S', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).save(consumer);

        shaped(ModItems.GPS_TOOL.get(), ModItems.PLASTIC.get(),
                " R /PGP/PDP",
                'R', Blocks.REDSTONE_TORCH,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'G', Tags.Items.GLASS_PANES,
                'D', Tags.Items.GEMS_DIAMOND
        ).save(consumer);

        shapeless(ModItems.GPS_TOOL.get(), 2, ModItems.PLASTIC.get(),
                ModItems.GPS_AREA_TOOL.get()).save(consumer, RL("gps_tool_from_gps_area_tool"));

        shapeless(ModItems.GPS_AREA_TOOL.get(), ModItems.PLASTIC.get(),
                ModItems.GPS_TOOL.get(), ModItems.GPS_TOOL.get()).save(consumer);

        shaped(ModItems.GUARD_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " R /RSR/ R ",
                'S', Items.IRON_SWORD,
                'R', ModItems.TURBINE_ROTOR.get()
        ).save(consumer);

        shapeless(ModItems.GUN_AMMO.get(), ModItems.MINIGUN.get(),
                Tags.Items.GUNPOWDER, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON, Tags.Items.INGOTS_COPPER
        ).save(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_AP.get(), Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_DIAMOND).save(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_EXPLOSIVE.get(), Blocks.TNT, Blocks.TNT).save(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_FREEZING.get(), Blocks.ICE, Blocks.ICE).save(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_INCENDIARY.get(), Tags.Items.RODS_BLAZE, Tags.Items.RODS_BLAZE).save(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_WEIGHTED.get(), Tags.Items.STORAGE_BLOCKS_GOLD, Tags.Items.OBSIDIAN).save(consumer);

        shaped(ModItems.HARVESTING_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " R /RSR/ R ",
                'S', Tags.Items.CROPS,
                'R', ModItems.TURBINE_ROTOR.get()
        ).save(consumer);

        shaped(ModItems.HEAT_FRAME.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/IFI/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'F', Blocks.FURNACE
        ).save(consumer);

        shaped(ModBlocks.HEAT_PIPE.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "WWW/BBB/WWW",
                'W', ModBlocks.THERMAL_LAGGING.get(),
                'B', PneumaticCraftTags.Items.STORAGE_BLOCKS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.HEAT_SINK.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/IGI",
                'B', Blocks.IRON_BARS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Tags.Items.INGOTS_COPPER
        ).save(consumer);

        shapeless(ModItems.COMPRESSED_IRON_INGOT.get(), 9, ModBlocks.COMPRESSED_IRON_BLOCK.get(),
                ModBlocks.COMPRESSED_IRON_BLOCK.get()
        ).save(consumer, RL("compressed_iron_ingot_from_block"));

        shaped(ModItems.JACKHAMMER.get(), ModItems.PLASTIC.get(),
                "PBP/ITI/DCD",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'B', PneumaticCraftTags.Blocks.STORAGE_BLOCKS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'P', ModItems.PLASTIC.get(),
                'D', Tags.Items.GEMS_DIAMOND
        ).save(consumer);

        shaped(ModBlocks.KEROSENE_LAMP.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " I /G G/IBI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Tags.Items.GLASS_PANES,
                'B', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get()))
        ).save(consumer);

        shaped(ModBlocks.LIQUID_COMPRESSOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "PBP/LCL",
                'P', ModBlocks.PRESSURE_TUBE.get(),
                'B', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get())),
                'L', Tags.Items.LEATHER,
                'C', ModBlocks.AIR_COMPRESSOR.get()
        ).save(consumer);

        shaped(ModBlocks.LIQUID_HOPPER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "T/H",
                'T', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get())),
                'H', Blocks.HOPPER
        ).save(consumer);

        shaped(ModItems.LOGISTICS_CORE.get(), 2, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/BRB/BBB",
                'B', ModBlocks.REINFORCED_BRICK_TILE.get(),
                'R', Tags.Items.DUSTS_REDSTONE
        ).save(consumer);

        shaped(ModItems.LOGISTICS_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " B /BCB/ B ",
                'B', ModItems.TURBINE_ROTOR.get(),
                'C', ModItems.LOGISTICS_CORE.get()
        ).save(consumer);

        shaped(ModItems.LOGISTICS_MODULE.get(), ModItems.LOGISTICS_CORE.get(),
                " R /RCR/TRT",
                'R', Tags.Items.DUSTS_REDSTONE,
                'C', ModItems.LOGISTICS_CORE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shapedPressure(ModItems.MANOMETER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "G/C",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'C', ModItems.AIR_CANISTER.get()
        ).save(consumer);

        shaped(ModBlocks.MANUAL_COMPRESSOR.get(), ModBlocks.PRESSURE_TUBE.get(),
                "RIR/ T /GBG",
                'R', Items.RED_DYE,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'G', PneumaticCraftTags.Items.GEARS_COMPRESSED_IRON,
                'B', ModItems.STONE_BASE.get()
        ).save(consumer);

        shaped(ModItems.MEMORY_STICK.get(), ModItems.PLASTIC.get(),
                "DED/PSP/G G",
                'D', Tags.Items.GEMS_DIAMOND,
                'E', Tags.Items.GEMS_EMERALD,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'S', Blocks.SOUL_SAND,
                'G', Tags.Items.INGOTS_GOLD
        ).save(consumer);

        shaped(ModItems.MICROMISSILES.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " T /WPW/WFW",
                'W', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'T', Blocks.TNT,
                'F', Items.FIRE_CHARGE
        ).save(consumer);

        shapedPressure(ModItems.MINIGUN.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "A  /CIB/GL ",
                'A', ModItems.AIR_CANISTER.get(),
                'C', Tags.Items.CHESTS_WOODEN,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'B', ModItems.CANNON_BARREL.get(),
                'G', Tags.Items.INGOTS_COPPER,
                'L', Blocks.LEVER
        ).save(consumer);

        shaped(ModBlocks.OMNIDIRECTIONAL_HOPPER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/ICI/ I ",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'C', Tags.Items.CHESTS_WOODEN
        ).save(consumer);

        shapedPressure(ModItems.PNEUMATIC_BOOTS.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', ModItems.COMPRESSED_IRON_BOOTS.get()
        ).save(consumer);
        shapedPressure(ModItems.PNEUMATIC_CHESTPLATE.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC/CCC",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', ModItems.COMPRESSED_IRON_CHESTPLATE.get()
        ).save(consumer);
        shapedPressure(ModItems.PNEUMATIC_HELMET.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC/CCC",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', ModItems.COMPRESSED_IRON_HELMET.get()
        ).save(consumer);
        shapedPressure(ModItems.PNEUMATIC_LEGGINGS.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC/I I",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', ModItems.COMPRESSED_IRON_LEGGINGS.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModItems.PNEUMATIC_CYLINDER.get(), 2, ModItems.PLASTIC.get(),
                "PIP/PIP/PBP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', ModItems.CANNON_BARREL.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.PNEUMATIC_DOOR_BASE.get(), ModItems.PLASTIC.get(),
                " CI/IIT/III",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.PNEUMATIC_DOOR.get(), ModItems.PLASTIC.get(),
                "II/II/II",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.PNEUMATIC_DYNAMO.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " T /GIG/IPI",
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'G', ModItems.COMPRESSED_IRON_GEAR.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).save(consumer);

        shaped(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), 16, ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/IGI/III",
                'I', ModBlocks.REINFORCED_BRICKS.get(),
                'G', Tags.Items.GLASS
        ).save(consumer);
        shapeless(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                Tags.Items.GLASS, ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).save(consumer, RL("pressure_chamber_glass_x1"));
        shapeless(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                Tags.Items.GLASS,
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).save(consumer, RL("pressure_chamber_glass_x4"));

        shapeless(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get(), 2, ModItems.COMPRESSED_IRON_INGOT.get(),
                Blocks.HOPPER, ModBlocks.PRESSURE_CHAMBER_WALL.get(), ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).save(consumer);

        shaped(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), 16, ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/ITI/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);
        shapeless(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                ModBlocks.PRESSURE_TUBE.get(), ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).save(consumer, RL("pressure_chamber_valve_x1"));
        shapeless(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                ModBlocks.PRESSURE_TUBE.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).save(consumer, RL("pressure_chamber_valve_x4"));

        shaped(ModBlocks.PRESSURE_CHAMBER_WALL.get(), 16, ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/I I/III",
                'I', ModBlocks.REINFORCED_BRICKS.get()
        ).save(consumer);

        shaped(ModItems.PRESSURE_GAUGE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " G /GIG/ G ",
                'G', Tags.Items.INGOTS_COPPER,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModItems.PRESSURE_GAUGE_MODULE.get(), ModItems.PRESSURE_GAUGE.get(),
                " G /RTR",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModItems.THERMOSTAT_MODULE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " M / H /TDT",
                'M', ModItems.MANOMETER.get(),
                'H', ModBlocks.HEAT_PIPE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'D', Tags.Items.DUSTS_REDSTONE
        ).save(consumer);

        shaped(ModBlocks.PRESSURE_TUBE.get(), 8, ModItems.COMPRESSED_IRON_INGOT.get(),
                "IGI",
                'G', Tags.Items.GLASS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.PRESSURIZED_SPAWNER.get(), ModBlocks.EMPTY_SPAWNER.get(),
                "ITI/TET/ITI",
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'E', ModBlocks.EMPTY_SPAWNER.get()
        ).save(consumer);

        shaped(ModItems.PRINTED_CIRCUIT_BOARD.get(), ModItems.PLASTIC.get(),
                " T /CUC/ T ",
                'T', ModItems.TRANSISTOR.get(),
                'C', ModItems.CAPACITOR.get(),
                'U', ModItems.UNASSEMBLED_PCB.get()
        ).save(consumer);

        shaped(ModBlocks.PROGRAMMABLE_CONTROLLER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "IRI/CDP/INI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'R', ModItems.REMOTE.get(),
                'C', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'P', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'D', ModItems.DRONE.get(),
                'N', ModItems.NETWORK_REGISTRY.get()
        ).save(consumer);

        shaped(ModBlocks.PROGRAMMER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "RGR/TBT/P P",
                'R', Tags.Items.DYES_RED,
                'G', Tags.Items.GLASS_PANES_BLACK,
                'T', ModItems.TURBINE_ROTOR.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS
        ).save(consumer);

        shaped(ModItems.PROGRAMMING_PUZZLE.get(),8, ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "PPP/PCP/PPP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'C', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).save(consumer);

        shapeless(ModItems.RAW_SALMON_TEMPURA.get(), ModItems.SOURDOUGH.get(),
                Items.SALMON, ModItems.SOURDOUGH.get()
        ).save(consumer);

        shaped(ModItems.REDSTONE_MODULE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " R /TDT",
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'D', Blocks.REPEATER
        ).save(consumer);

        shaped(ModBlocks.REFINERY.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "SSS/RTR/SSS",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'T', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get())),
                'R', Tags.Items.DUSTS_REDSTONE
        ).save(consumer);

        shaped(ModBlocks.REFINERY_OUTPUT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "SSS/GDG/SSS",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'G', Tags.Items.GLASS,
                'D', Tags.Items.GEMS_DIAMOND
        ).save(consumer);

        shaped(ModItems.REGULATOR_TUBE_MODULE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "STS",
                'S', ModItems.SAFETY_TUBE_MODULE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModItems.REINFORCED_AIR_CANISTER.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                " T /ICI/III",
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'C', ModItems.AIR_CANISTER.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.REINFORCED_CHEST.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "IGI/WCW/IOI",
                'G', Tags.Items.NUGGETS_GOLD,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'W', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'O', Tags.Blocks.OBSIDIAN,
                'C', Tags.Items.CHESTS_WOODEN
        ).save(consumer);

        shaped(ModItems.REINFORCED_CHEST_KIT.get(), ModBlocks.REINFORCED_CHEST.get(),
                " C/S ",
                'C', NBTIngredient.of(true, null, ModBlocks.REINFORCED_CHEST.get()),
                'S', Items.STICK
        ).save(consumer);

        shaped(ModItems.REMOTE.get(), ModItems.TRANSISTOR.get(),
                " I /TGT/TDT",
                'I', ModItems.NETWORK_IO_PORT.get(),
                'D', ModItems.NETWORK_DATA_STORAGE.get(),
                'G', ModItems.GPS_TOOL.get(),
                'T', ModItems.TRANSISTOR.get()
        ).save(consumer);

        shaped(ModItems.SAFETY_TUBE_MODULE.get(), ModItems.PRESSURE_GAUGE.get(),
                " G /LTL",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'L', Blocks.LEVER,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.SECURITY_STATION.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "DBD/TPT/G G",
                'D', Tags.Items.DYES_GRAY,
                'G', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', Tags.Items.GLASS_PANES_BLACK,
                'T', ModItems.TURBINE_ROTOR.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).save(consumer);

        shaped(ModItems.SEISMIC_SENSOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " T /GRG/GCG",
                'T', Blocks.REDSTONE_TORCH,
                'G', Tags.Items.GLASS,
                'R', Blocks.REPEATER,
                'C', Blocks.NOTE_BLOCK
        ).save(consumer);

        shaped(ModBlocks.SENTRY_TURRET.get(), ModItems.PLASTIC.get(),
                " M /PIP/I I",
                'M', ModItems.MINIGUN.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModItems.SMART_CHEST_KIT.get(), ModBlocks.SMART_CHEST.get(),
                " C/S ",
                'C', NBTIngredient.of(true, null, ModBlocks.SMART_CHEST.get()),
                'S', Items.STICK
        ).save(consumer);

        shaped(ModItems.SOURDOUGH.get(), 8, ModItems.WHEAT_FLOUR.get(),
                "FFF/FYF/FFF",
                'F', PneumaticCraftTags.Items.FLOUR,
                'Y', FluidIngredient.of(1000, ModFluids.YEAST_CULTURE.get())
        ).save(consumer);

        shaped(ModBlocks.SMART_CHEST.get(), ModBlocks.REINFORCED_CHEST.get(),
                "DPD/CHC",
                'D', Tags.Items.GEMS_DIAMOND,
                'H', ModBlocks.OMNIDIRECTIONAL_HOPPER.get(),
                'C', ModBlocks.REINFORCED_CHEST.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).save(consumer);

        shaped(ModBlocks.SOLAR_COMPRESSOR.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "WWW/PGP/TBT",
                'W', ModItems.SOLAR_CELL.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'G', ModItems.COMPRESSED_IRON_GEAR.get(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'B', ModBlocks.COMPRESSED_IRON_BLOCK.get()
        ).save(consumer);

        shaped(ModItems.SPAWNER_AGITATOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/IGI/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Items.GHAST_TEAR
        ).save(consumer);

        shaped(ModItems.SPAWNER_CORE_SHELL.get(), ModBlocks.PRESSURE_CHAMBER_GLASS.get(),
                "IGI/GEG/IGI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', ModBlocks.PRESSURE_CHAMBER_GLASS.get(),
                'E', Tags.Items.GEMS_EMERALD
        ).save(consumer);

        shaped(ModBlocks.SPAWNER_EXTRACTOR.get(), ModItems.SPAWNER_AGITATOR.get(),
                "ITI/WAW/OPO",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'W', ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                'A', ModItems.SPAWNER_AGITATOR.get(),
                'O', Tags.Blocks.OBSIDIAN,
                'P', ModBlocks.DRILL_PIPE.get()
        ).save(consumer);

        shaped(ModItems.STONE_BASE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "S S/STS",
                'S', Tags.Items.STONE,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.TAG_WORKBENCH.get(), ModBlocks.DISPLAY_TABLE.get(),
                "B/D",
                'B', Items.WRITABLE_BOOK,
                'D', ModBlocks.DISPLAY_TABLE.get()
        ).save(consumer);

        shaped(ModBlocks.TANK_SMALL.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "BIB/IGI/BIB",
                'B', Blocks.IRON_BARS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Tags.Items.GLASS
        ).save(consumer);

        shaped(ModBlocks.TANK_MEDIUM.get(), ModItems.PLASTIC.get(),
                "PSP/ITI/PSP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'S', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get())),
                'I', Tags.Items.INGOTS_GOLD,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.TANK_LARGE.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "PMP/DTD/PMP",
                'M', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_MEDIUM.get())),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'D', Tags.Items.GEMS_DIAMOND,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.TANK_HUGE.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "NTN/TRT/NTN",
                'T', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_LARGE.get())),
                'R', ModItems.REINFORCED_AIR_CANISTER.get(),
                'N', Tags.Items.INGOTS_NETHERITE
        ).save(consumer);

        shaped(ModBlocks.THERMAL_COMPRESSOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "ITI/PAP/ITI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'A', ModBlocks.AIR_COMPRESSOR.get(),
                'P', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
        ).save(consumer);

        shaped(ModBlocks.THERMAL_LAGGING.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "WGW/GWG/WGW",
                'W', ItemTags.WOOL,
                'G', Tags.Items.GLASS
        ).save(consumer);

        shaped(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "SSS/TPT/SSS",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'T', NBTIngredient.of(true, new ItemStack(ModBlocks.TANK_SMALL.get())),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shapeless(ModItems.TRANSFER_GADGET.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                Blocks.HOPPER, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shaped(ModBlocks.TUBE_JUNCTION.get(), ModBlocks.PRESSURE_TUBE.get(),
                "IPI/PPP/IPI",
                'I', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModItems.TURBINE_ROTOR.get(), ModItems.TURBINE_BLADE.get(),
                " B / I /B B",
                'B', ModItems.TURBINE_BLADE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);

        shapeless(ModItems.UNASSEMBLED_NETHERITE_DRILL_BIT.get(), ModItems.DIAMOND_DRILL_BIT.get(),
                ModItems.DIAMOND_DRILL_BIT.get(), Tags.Items.INGOTS_NETHERITE
        ).save(consumer);

        shaped(ModBlocks.UNIVERSAL_SENSOR.get(), ModItems.PLASTIC.get(),
                " S /PRP/PCP",
                'S', ModItems.SEISMIC_SENSOR.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'R', Blocks.REPEATER,
                'C', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.UV_LIGHT_BOX.get(), ModItems.PCB_BLUEPRINT.get(),
                "LLL/IBT/III",
                'L', Blocks.REDSTONE_LAMP,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'B', ModItems.PCB_BLUEPRINT.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModItems.VACUUM_MODULE.get(), ModBlocks.VACUUM_PUMP.get(),
                "TVT",
                'V', ModBlocks.VACUUM_PUMP.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModBlocks.VACUUM_PUMP.get(), ModItems.TURBINE_ROTOR.get(),
                "GRG/TRT/SSS",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'R', ModItems.TURBINE_ROTOR.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'S', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).save(consumer);

        shaped(ModBlocks.VACUUM_TRAP.get(), ModItems.TURBINE_ROTOR.get(),
                "ITI/WEP/SSS",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', Blocks.IRON_TRAPDOOR,
                'W', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'P', ModBlocks.PRESSURE_TUBE.get(),
                'E', Items.ENDER_EYE,
                'S', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).save(consumer);

        shaped(ModBlocks.VORTEX_TUBE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "ITI/GTG/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'G', Tags.Items.INGOTS_COPPER
        ).save(consumer);

        shapeless(Items.PAPER, Items.PAPER, ModItems.TAG_FILTER.get()).save(consumer, RL("paper_from_tag_filter"));

        // network components
        networkComponent(ModItems.DIAGNOSTIC_SUBROUTINE.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_RED).save(consumer);
        networkComponent(ModItems.NETWORK_API.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_BLUE).save(consumer);
        networkComponent(ModItems.NETWORK_DATA_STORAGE.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_GRAY).save(consumer);
        networkComponent(ModItems.NETWORK_IO_PORT.get(), 1, ModItems.CAPACITOR.get(), Tags.Items.DYES_CYAN).save(consumer);
        networkComponent(ModItems.NETWORK_REGISTRY.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_LIME).save(consumer);
        networkComponent(ModItems.NETWORK_NODE.get(), 16, ModItems.TRANSISTOR.get(), Tags.Items.DYES_PURPLE).save(consumer);

        // logistics frames
        logisticsFrame(ModItems.LOGISTICS_FRAME_ACTIVE_PROVIDER.get(), Tags.Items.DYES_PURPLE).save(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), Tags.Items.DYES_RED).save(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_REQUESTER.get(), Tags.Items.DYES_BLUE).save(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_STORAGE.get(), Tags.Items.DYES_YELLOW).save(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), Tags.Items.DYES_GREEN).save(consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_ACTIVE_PROVIDER.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_REQUESTER.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_STORAGE.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), consumer);

        // pressurizable tools
        pneumaticTool(ModItems.CAMO_APPLICATOR.get(), Tags.Items.DYES_BLUE).save(consumer);
        pneumaticTool(ModItems.PNEUMATIC_WRENCH.get(), Tags.Items.DYES_ORANGE).save(consumer);
        pneumaticTool(ModItems.LOGISTICS_CONFIGURATOR.get(), Tags.Items.DYES_RED).save(consumer);
        pneumaticTool(ModItems.VORTEX_CANNON.get(), Tags.Items.DYES_YELLOW).save(consumer);

        // standard upgrade patterns (4 x lapis, 4 x edge item, 1 x center item)
        standardUpgrade(ModUpgrades.ARMOR.get(), PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON, Tags.Items.GEMS_DIAMOND).save(consumer);
        standardUpgrade(ModUpgrades.BLOCK_TRACKER.get(), Items.FERMENTED_SPIDER_EYE, ModBlocks.PRESSURE_CHAMBER_WALL.get()).save(consumer);
        standardUpgrade(ModUpgrades.CHARGING.get(), ModItems.CHARGING_MODULE.get(), ModBlocks.PRESSURE_TUBE.get()).save(consumer);
        standardUpgrade(ModUpgrades.COORDINATE_TRACKER.get(), ModItems.GPS_TOOL.get(), Tags.Items.DUSTS_REDSTONE).save(consumer);
        standardUpgrade(ModUpgrades.DISPENSER.get(), Blocks.DISPENSER, Tags.Items.GEMS_QUARTZ).save(consumer);
        standardUpgrade(ModUpgrades.ENDER_VISOR.get(), Blocks.CARVED_PUMPKIN, Blocks.CHORUS_FLOWER).save(consumer);
        standardUpgrade(ModUpgrades.ELYTRA.get(), Items.ELYTRA, Blocks.END_ROD).save(consumer);
        standardUpgrade(ModUpgrades.ENTITY_TRACKER.get(), Items.FERMENTED_SPIDER_EYE, Tags.Items.BONES).save(consumer);
        standardUpgrade(ModUpgrades.FLIPPERS.get(), Items.BLACK_WOOL, PneumaticCraftTags.Items.PLASTIC_SHEETS).save(consumer);
        standardUpgrade(ModUpgrades.GILDED.get(), Blocks.GILDED_BLACKSTONE, Tags.Items.INGOTS_GOLD).save(consumer);
        standardUpgrade(ModUpgrades.INVENTORY.get(), Tags.Items.CHESTS_WOODEN, ItemTags.PLANKS).save(consumer);
        standardUpgrade(ModUpgrades.ITEM_LIFE.get(), Items.CLOCK, Items.APPLE).save(consumer);
        standardUpgrade(ModUpgrades.MAGNET.get(), PneumaticCraftTags.Items.PLASTIC_SHEETS, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON).save(consumer);
        standardUpgrade(ModUpgrades.MINIGUN.get(), ModItems.MINIGUN.get(), Tags.Items.GUNPOWDER).save(consumer);
        standardUpgrade(ModUpgrades.RANGE.get(), Items.BOW, ItemTags.ARROWS).save(consumer);
        standardUpgrade(ModUpgrades.SEARCH.get(), Items.GOLDEN_CARROT, Items.ENDER_EYE).save(consumer);
        standardUpgrade(ModUpgrades.SECURITY.get(), ModItems.SAFETY_TUBE_MODULE.get(), Tags.Items.OBSIDIAN).save(consumer);
        standardUpgrade(ModUpgrades.SPEED.get(), FluidIngredient.of(1000, PneumaticCraftTags.Fluids.LUBRICANT), Items.SUGAR).save(consumer);
        standardUpgrade(ModUpgrades.SPEED.get(), 2, FluidIngredient.of(1000, PneumaticCraftTags.Fluids.LUBRICANT), ModItems.GLYCEROL.get()).save(consumer, RL("speed_upgrade_from_glycerol"));
        standardUpgrade(ModUpgrades.STANDBY.get(), ItemTags.BEDS, Items.REDSTONE_TORCH).save(consumer);
        standardUpgrade(ModUpgrades.STOMP.get(), Blocks.TNT, Blocks.PISTON).save(consumer);
        standardUpgrade(ModUpgrades.VOLUME.get(), ModItems.AIR_CANISTER.get(), PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON).save(consumer);

        // non-standard upgrade patterns
        ItemStack nightVisionPotion = new ItemStack(Items.POTION);
        PotionUtils.setPotion(nightVisionPotion, Potions.LONG_NIGHT_VISION);
        shaped(ModUpgrades.NIGHT_VISION.get().getItem(), ModItems.PNEUMATIC_HELMET.get(),
                "LNL/GNG/LNL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'G', ModBlocks.PRESSURE_CHAMBER_GLASS.get(),
                'N', NBTIngredient.of(true, nightVisionPotion)
        ).save(consumer);

        shaped(ModUpgrades.SCUBA.get().getItem(), ModItems.PNEUMATIC_HELMET.get(),
                "LTL/PRP/LPL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'R', ModItems.REGULATOR_TUBE_MODULE.get(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get()
        ).save(consumer);

        shaped(ModUpgrades.JET_BOOTS.get().getItem(1), ModItems.PNEUMATIC_BOOTS.get(),
                "LTL/VCV/LTL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'V', ModItems.VORTEX_CANNON.get(),
                'C', ModBlocks.ADVANCED_AIR_COMPRESSOR.get(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get()
        ).save(consumer);
        shaped(ModUpgrades.JET_BOOTS.get().getItem(2), ModItems.PNEUMATIC_BOOTS.get(),
                "FFF/VUV/CFC",
                'F', Items.FEATHER,
                'V', ModItems.VORTEX_CANNON.get(),
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'U', ModUpgrades.JET_BOOTS.get().getItem(1)
        ).save(consumer);
        shaped(ModUpgrades.JET_BOOTS.get().getItem(3), ModItems.PNEUMATIC_BOOTS.get(),
                "TBT/VUV/TBT",
                'T', Items.GHAST_TEAR,
                'B', Items.BLAZE_ROD,
                'V', ModItems.VORTEX_CANNON.get(),
                'U', ModUpgrades.JET_BOOTS.get().getItem(2)
        ).save(consumer);
        ItemStack slowFallPotion = new ItemStack(Items.POTION);
        PotionUtils.setPotion(slowFallPotion, Potions.LONG_SLOW_FALLING);
        shaped(ModUpgrades.JET_BOOTS.get().getItem(4), ModItems.PNEUMATIC_BOOTS.get(),
                "MNM/VUV/P P",
                'N', Items.NETHER_STAR,
                'M', Items.PHANTOM_MEMBRANE,
                'V', ModItems.VORTEX_CANNON.get(),
                'P', NBTIngredient.of(true, slowFallPotion),
                'U', ModUpgrades.JET_BOOTS.get().getItem(3)
        ).save(consumer);
        shaped(ModUpgrades.JET_BOOTS.get().getItem(5), ModItems.PNEUMATIC_BOOTS.get(),
                "RER/VUV/RDR",
                'R', Items.END_ROD,
                'E', Items.ELYTRA,
                'V', ModItems.VORTEX_CANNON.get(),
                'D', Items.DRAGON_BREATH,
                'U', ModUpgrades.JET_BOOTS.get().getItem(4)
        ).save(consumer);

        shaped(ModUpgrades.JUMPING.get().getItem(1), ModItems.PNEUMATIC_LEGGINGS.get(),
                "LCL/VTV/LPL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'P', Blocks.PISTON,
                'V', ModItems.VORTEX_CANNON.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).save(consumer);
        shaped(ModUpgrades.JUMPING.get().getItem(2), ModItems.PNEUMATIC_LEGGINGS.get(),
                "PCP/SUS",
                'U', ModUpgrades.JUMPING.get().getItem(1),
                'S', Blocks.SLIME_BLOCK,
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).save(consumer);
        ItemStack jumpBoostPotion1 = new ItemStack(Items.POTION);
        PotionUtils.setPotion(jumpBoostPotion1, Potions.LEAPING);
        shaped(ModUpgrades.JUMPING.get().getItem(3), ModItems.PNEUMATIC_LEGGINGS.get(),
                "PCP/JUJ/ J ",
                'U', ModUpgrades.JUMPING.get().getItem(2),
                'J', NBTIngredient.of(true, jumpBoostPotion1),
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).save(consumer);
        ItemStack jumpBoostPotion2 = new ItemStack(Items.POTION);
        PotionUtils.setPotion(jumpBoostPotion2, Potions.STRONG_LEAPING);
        shaped(ModUpgrades.JUMPING.get().getItem(4), ModItems.PNEUMATIC_LEGGINGS.get(),
                "PCP/JUJ/ J ",
                'U', ModUpgrades.JUMPING.get().getItem(3),
                'J', NBTIngredient.of(true, jumpBoostPotion2),
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).save(consumer);

        Item mekRadShield = BuiltInRegistries.ITEM.get(new ResourceLocation("mekanism:module_radiation_shielding_unit"));
        shaped(ModUpgrades.RADIATION_SHIELDING.get().getItem(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "LIL/IRI/LIL",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'R', mekRadShield
        ).save(consumer.withConditions(new ModLoadedCondition(ModIds.MEKANISM)));

        // bricks etc.
        shaped(ModBlocks.REINFORCED_STONE.get(), 8, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/BIB/BBB",
                'B', Blocks.STONE,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).save(consumer);
        shaped(ModBlocks.REINFORCED_STONE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B",
                'B', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).save(consumer, RL("reinforced_stone_from_slab"));
        shaped(ModBlocks.REINFORCED_BRICKS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "SS/SS",
                'S', ModBlocks.REINFORCED_STONE.get()
        ).save(consumer);
        shapeless(ModBlocks.REINFORCED_BRICKS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                ModBlocks.REINFORCED_BRICK_TILE.get()
        ).save(consumer, RL("reinforced_bricks_from_tile"));
        shaped(ModBlocks.REINFORCED_BRICKS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B",
                'B', ModBlocks.REINFORCED_BRICK_SLAB.get()
        ).save(consumer, RL("reinforced_brick_from_slab"));
        shaped(ModBlocks.REINFORCED_BRICK_SLAB.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.REINFORCED_STONE_SLAB.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB",
                'B', ModBlocks.REINFORCED_STONE.get()
        ).save(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_STAIRS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "B  /BB /BBB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_PILLAR.get(), 3, ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B/B",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_TILE.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BB/BB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_WALL.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/BBB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).save(consumer);

        shaped(ModBlocks.COMPRESSED_STONE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B",
                'B', ModBlocks.COMPRESSED_STONE_SLAB.get()
        ).save(consumer, RL("compressed_stone_from_slab"));
        shaped(ModBlocks.COMPRESSED_BRICKS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "SS/SS",
                'S', ModBlocks.COMPRESSED_STONE.get()
        ).save(consumer);
        shapeless(ModBlocks.COMPRESSED_BRICKS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                ModBlocks.COMPRESSED_BRICK_TILE.get()
        ).save(consumer, RL("compressed_bricks_from_tile"));
        shaped(ModBlocks.COMPRESSED_BRICKS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B",
                'B', ModBlocks.COMPRESSED_BRICK_SLAB.get()
        ).save(consumer, RL("compressed_brick_from_slab"));
        shaped(ModBlocks.COMPRESSED_BRICK_SLAB.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB",
                'B', ModBlocks.COMPRESSED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.COMPRESSED_STONE_SLAB.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB",
                'B', ModBlocks.COMPRESSED_STONE.get()
        ).save(consumer);
        shaped(ModBlocks.COMPRESSED_BRICK_STAIRS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "B  /BB /BBB",
                'B', ModBlocks.COMPRESSED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.COMPRESSED_BRICK_PILLAR.get(), 3, ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B/B",
                'B', ModBlocks.COMPRESSED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.COMPRESSED_BRICK_TILE.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BB/BB",
                'B', ModBlocks.COMPRESSED_BRICKS.get()
        ).save(consumer);
        shaped(ModBlocks.COMPRESSED_BRICK_WALL.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/BBB",
                'B', ModBlocks.COMPRESSED_BRICKS.get()
        ).save(consumer);

        // vanilla stonecutter for bricks etc.
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_STONE.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_STONE_SLAB.get(), 2)
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_stone_slab_from_stone_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_STONE.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_BRICKS.get())
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_bricks_from_stone_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_BRICK_SLAB.get(), 2)
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_brick_slab_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_BRICK_TILE.get())
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_brick_tile_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_BRICK_STAIRS.get())
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_brick_stairs_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_BRICK_WALL.get())
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_brick_wall_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_BRICK_PILLAR.get())
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_brick_pillar_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.REINFORCED_BRICK_TILE.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_BRICKS.get())
                .unlockedBy("has_reinforced_stone", has(ModBlocks.REINFORCED_STONE.get())
                ).save(consumer, RL("reinforced_bricks_from_tile_stonecutting"));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_STONE.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_STONE_SLAB.get(), 2)
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_stone_slab_from_stone_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_STONE.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_BRICKS.get())
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_bricks_from_stone_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_BRICK_SLAB.get(), 2)
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_brick_slab_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_BRICK_TILE.get())
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_brick_tile_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_BRICK_STAIRS.get())
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_brick_stairs_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_BRICK_WALL.get())
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_brick_wall_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_BRICK_PILLAR.get())
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_brick_pillar_from_bricks_stonecutting"));
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ModBlocks.COMPRESSED_BRICK_TILE.get()), RecipeCategory.BUILDING_BLOCKS, ModBlocks.COMPRESSED_BRICKS.get())
                .unlockedBy("has_compressed_stone", has(ModBlocks.COMPRESSED_STONE.get())
                ).save(consumer, RL("compressed_bricks_from_tile_stonecutting"));

        // plastic bricks & wall lamps
        for (DyeColor dye : DyeColor.values()) {
            plasticBrick(dye, dye.getTag()).save(consumer);
            smoothPlasticBrick(dye).save(consumer);
            wallLamp(dye, false, dye.getTag()).save(consumer);
            wallLamp(dye, true, dye.getTag()).save(consumer);
        }

        // specials
        SpecialRecipeBuilder.special(DroneColorCrafting::new)
                .save(consumer, getId("drone_color"));
        SpecialRecipeBuilder.special(DroneUpgradeCrafting::new)
                .save(consumer, getId("drone_upgrade"));
        SpecialRecipeBuilder.special(GunAmmoPotionCrafting::new)
                .save(consumer, getId("gun_ammo_potion_crafting"));
        SpecialRecipeBuilder.special(PatchouliBookCrafting::new)
                .save(consumer.withConditions(new ModLoadedCondition(ModIds.PATCHOULI)), getId("patchouli_book_crafting"));
        SpecialRecipeBuilder.special(OneProbeCrafting::new)
                .save(consumer.withConditions(new ModLoadedCondition(ModIds.THE_ONE_PROBE)), getId("one_probe_crafting"));

        SpecialRecipeBuilder.special(PressureEnchantingRecipe::new)
                .save(consumer, getId("pressure_chamber/pressure_chamber_enchanting"));
        SpecialRecipeBuilder.special(PressureDisenchantingRecipe::new)
                .save(consumer, getId("pressure_chamber/pressure_chamber_disenchanting"));

        // smelting
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModItems.FAILED_PCB.get()), RecipeCategory.MISC, ModItems.EMPTY_PCB.get(),
                        0.25f, 100)
                .unlockedBy("has_empty_pcb", has(ModItems.FAILED_PCB.get()))
                .save(consumer, RL("empty_pcb_from_failed_pcb"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(PneumaticCraftTags.Items.PLASTIC_BRICKS), RecipeCategory.MISC, ModItems.PLASTIC.get(),
                        0f, 100)
                .unlockedBy("has_plastic", has(ModItems.PLASTIC.get()))
                .save(consumer, RL("plastic_sheet_from_brick"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(PneumaticCraftTags.Items.SMOOTH_PLASTIC_BRICKS), RecipeCategory.BUILDING_BLOCKS, ModItems.PLASTIC.get(),
                        0f, 100)
                .unlockedBy("has_plastic", has(ModItems.PLASTIC.get()))
                .save(consumer, RL("plastic_sheet_from_smooth_brick"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.SOURDOUGH.get()), RecipeCategory.FOOD, ModItems.SOURDOUGH_BREAD.get(),
                        0.35f, 100)
                .unlockedBy("has_dough", has(ModItems.SOURDOUGH.get()))
                .save(consumer, RL("sourdough_bread"));

        // pressure chamber
        pressureChamber(ImmutableList.of(StackedIngredient.fromItems(4, Items.SNOW_BLOCK)),
                1.5f, new ItemStack(Items.ICE))
                .save(consumer, RL("pressure_chamber/ice"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromItems(4, Items.ICE)),
                2.0f, new ItemStack(Items.PACKED_ICE))
                .save(consumer, RL("pressure_chamber/packed_ice"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromItems(4, Items.PACKED_ICE)),
                2.5f, new ItemStack(Items.BLUE_ICE))
                .save(consumer, RL("pressure_chamber/blue_ice"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromTag(1, Tags.Items.INGOTS_IRON)),
                2.0f, new ItemStack(ModItems.COMPRESSED_IRON_INGOT.get()))
                .save(consumer, RL("pressure_chamber/compressed_iron_ingot"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromTag(1, Tags.Items.STORAGE_BLOCKS_IRON)),
                2.0f, new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK.get()))
                .save(consumer, RL("pressure_chamber/compressed_iron_block"));
        pressureChamber(ImmutableList.of(
                        StackedIngredient.fromItems(2, Items.REDSTONE_TORCH),
                        StackedIngredient.fromTag(3, PneumaticCraftTags.Items.WIRING),
                        StackedIngredient.fromItems(1, ModItems.PLASTIC.get())),
                1.5f, new ItemStack(ModItems.EMPTY_PCB.get(), 3))
                .save(consumer, RL("pressure_chamber/empty_pcb"));
        pressureChamber(ImmutableList.of(
                        StackedIngredient.fromTag(2, PneumaticCraftTags.Items.WIRING),
                        StackedIngredient.fromTag(1, Tags.Items.SLIMEBALLS),
                        StackedIngredient.fromItems(1, ModItems.PLASTIC.get())),
                1f, new ItemStack(ModItems.CAPACITOR.get()))
                .save(consumer, RL("pressure_chamber/capacitor"));
        pressureChamber(ImmutableList.of(
                        StackedIngredient.fromTag(3, PneumaticCraftTags.Items.WIRING),
                        StackedIngredient.fromTag(1, Tags.Items.DUSTS_REDSTONE),
                        StackedIngredient.fromItems(1, ModItems.PLASTIC.get())),
                1f, new ItemStack(ModItems.TRANSISTOR.get()))
                .save(consumer, RL("pressure_chamber/transistor"));
        pressureChamber(ImmutableList.of(
                        StackedIngredient.fromItems(1, ModItems.PLASTIC_BUCKET.get()),
                        StackedIngredient.fromItems(2, Items.ROTTEN_FLESH),
                        StackedIngredient.fromItems(2, Items.SPIDER_EYE),
                        StackedIngredient.fromTag(2, Tags.Items.GUNPOWDER)),
                1f, new ItemStack(ModItems.ETCHING_ACID_BUCKET.get()))
                .save(consumer, RL("pressure_chamber/etching_acid"));
        pressureChamber(ImmutableList.of(
                        StackedIngredient.fromItems(1, Items.MILK_BUCKET),
                        StackedIngredient.fromTag(4, Tags.Items.DYES_GREEN)),
                1f, new ItemStack(Items.SLIME_BALL, 4), new ItemStack(Items.BUCKET))
                .save(consumer, RL("pressure_chamber/milk_to_slime_balls"));
        pressureChamber(ImmutableList.of(
                        StackedIngredient.fromTag(1, Tags.Items.INGOTS_GOLD),
                        StackedIngredient.fromTag(2, Tags.Items.DUSTS_REDSTONE)),
                1f, new ItemStack(ModItems.TURBINE_BLADE.get()))
                .save(consumer, RL("pressure_chamber/turbine_blade"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromTag(8, Tags.Items.STORAGE_BLOCKS_COAL)),
                4f, new ItemStack(Items.DIAMOND))
                .save(consumer, RL("pressure_chamber/coal_to_diamond"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromTag(1, Tags.Items.CROPS_WHEAT)),
                1.5f, new ItemStack(ModItems.WHEAT_FLOUR.get(), 3))
                .save(consumer, RL("pressure_chamber/wheat_flour"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromTag(1, Tags.Items.STONE)),
                1f, new ItemStack(ModBlocks.COMPRESSED_STONE.get()))
                .save(consumer, RL("pressure_chamber/compressed_stone"));
        pressureChamber(ImmutableList.of(
                        StackedIngredient.fromItems(1, ModItems.UPGRADE_MATRIX.get()),
                        StackedIngredient.fromItems(1, Items.AMETHYST_SHARD)),
                2.5f, new ItemStack(ModItems.SOLAR_WAFER.get()))
                .save(consumer, RL("pressure_chamber/solar_wafer"));

        // explosion crafting
        explosionCrafting(Ingredient.of(Tags.Items.INGOTS_IRON), 20, new ItemStack(ModItems.COMPRESSED_IRON_INGOT.get()))
                .save(consumer, RL("explosion_crafting/compressed_iron_ingot"));
        explosionCrafting(Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON), 20, new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK.get()))
                .save(consumer, RL("explosion_crafting/compressed_iron_block"));
        explosionCrafting(Ingredient.of(Tags.Items.CROPS_WHEAT), 50, new ItemStack(ModItems.WHEAT_FLOUR.get()))
                .save(consumer, RL("explosion_crafting/wheat_flour"));

        // heat frame cooling
        heatFrameCooling(FluidIngredient.of(1000, FluidTags.WATER), 273,
                new ItemStack(Blocks.ICE))
                .save(consumer, RL("heat_frame_cooling/ice"));
        heatFrameCooling(FluidIngredient.of(1000, FluidTags.LAVA), 273,
                new ItemStack(Blocks.OBSIDIAN), 0.025f, 0.5f)
                .save(consumer, RL("heat_frame_cooling/obsidian"));
        heatFrameCooling(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.PLASTIC), 273,
                new ItemStack(ModItems.PLASTIC.get()), 0.01f, 0.75f)
                .save(consumer, RL("heat_frame_cooling/plastic"));

        // refinery
        refinery(FluidIngredient.of(10, PneumaticCraftTags.Fluids.CRUDE_OIL),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL.get(), 4),
                new FluidStack(ModFluids.LPG.get(), 2)
        ).save(consumer, RL("refinery/oil_2"));
        refinery(FluidIngredient.of(10, PneumaticCraftTags.Fluids.CRUDE_OIL),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL.get(), 2),
                new FluidStack(ModFluids.KEROSENE.get(), 3),
                new FluidStack(ModFluids.LPG.get(), 2)
        ).save(consumer, RL("refinery/oil_3"));
        refinery(FluidIngredient.of(10, PneumaticCraftTags.Fluids.CRUDE_OIL),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL.get(), 2),
                new FluidStack(ModFluids.KEROSENE.get(), 3),
                new FluidStack(ModFluids.GASOLINE.get(), 3),
                new FluidStack(ModFluids.LPG.get(), 2)
        ).save(consumer, RL("refinery/oil_4"));

        // thermopneumatic processing plant
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.DIESEL), Ingredient.EMPTY,
                new FluidStack(ModFluids.KEROSENE.get(), 80), ItemStack.EMPTY,
                TemperatureRange.min(573), 2.0f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/kerosene"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.KEROSENE), Ingredient.EMPTY,
                new FluidStack(ModFluids.GASOLINE.get(), 80), ItemStack.EMPTY,
                TemperatureRange.min(573), 2.0f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/gasoline"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.GASOLINE), Ingredient.EMPTY,
                new FluidStack(ModFluids.LPG.get(), 80), ItemStack.EMPTY,
                TemperatureRange.min(573), 2.0f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/lpg"));
        thermoPlant(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.DIESEL), Ingredient.of(Tags.Items.DUSTS_REDSTONE),
                new FluidStack(ModFluids.LUBRICANT.get(), 1000), ItemStack.EMPTY,
                TemperatureRange.min(373), 0f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/lubricant_from_diesel"));
        thermoPlant(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.BIODIESEL), Ingredient.of(Tags.Items.DUSTS_REDSTONE),
                new FluidStack(ModFluids.LUBRICANT.get(), 1000), ItemStack.EMPTY,
                TemperatureRange.min(373), 0f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/lubricant_from_biodiesel"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.LPG), Ingredient.of(Items.COAL),
                new FluidStack(ModFluids.PLASTIC.get(), 1000), ItemStack.EMPTY,
                TemperatureRange.min(373), 0f,1.0f, 1.0f,  false
        ).save(consumer, RL("thermo_plant/plastic_from_lpg"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.BIODIESEL), Ingredient.of(Items.CHARCOAL),
                new FluidStack(ModFluids.PLASTIC.get(), 1000), ItemStack.EMPTY,
                TemperatureRange.min(373), 0f,1.0f, 1.0f,  false
        ).save(consumer, RL("thermo_plant/plastic_from_biodiesel"));
        thermoPlant(FluidIngredient.of(1000, Fluids.WATER), Ingredient.of(Items.LAPIS_LAZULI),
                FluidStack.EMPTY, new ItemStack(ModItems.UPGRADE_MATRIX.get(), 4),
                TemperatureRange.min(273), 2f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/upgrade_matrix"));
        thermoPlant(FluidIngredient.of(1000, Fluids.WATER), Ingredient.of(Tags.Items.MUSHROOMS),
                new FluidStack(ModFluids.YEAST_CULTURE.get(), 250), ItemStack.EMPTY,
                TemperatureRange.of(303, 333), 0f, 0.1f, 1.0f, false
        ).save(consumer, RL("thermo_plant/yeast_culture"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.YEAST_CULTURE), Ingredient.of(Items.SUGAR),
                new FluidStack(ModFluids.ETHANOL.get(), 50), ItemStack.EMPTY,
                TemperatureRange.of(303, 333), 0f, 0.5f, 1.0f, true
        ).save(consumer, RL("thermo_plant/ethanol_from_sugar"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.YEAST_CULTURE), Ingredient.of(Tags.Items.CROPS_POTATO),
                new FluidStack(ModFluids.ETHANOL.get(), 25), ItemStack.EMPTY,
                TemperatureRange.of(303, 333), 0f, 0.25f, 1.0f, true
        ).save(consumer, RL("thermo_plant/ethanol_from_potato"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.YEAST_CULTURE), Ingredient.of(Items.POISONOUS_POTATO),
                new FluidStack(ModFluids.ETHANOL.get(), 50), ItemStack.EMPTY,
                TemperatureRange.of(303, 333), 0f, 0.25f, 1.0f, true
        ).save(consumer, RL("thermo_plant/ethanol_from_poisonous_potato"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.YEAST_CULTURE), Ingredient.of(Items.APPLE),
                new FluidStack(ModFluids.ETHANOL.get(), 50), ItemStack.EMPTY,
                TemperatureRange.of(303, 333), 0f, 0.25f, 1.0f, true
        ).save(consumer, RL("thermo_plant/ethanol_from_apple"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.YEAST_CULTURE), Ingredient.of(Items.MELON_SLICE),
                new FluidStack(ModFluids.ETHANOL.get(), 10), ItemStack.EMPTY,
                TemperatureRange.of(303, 333), 0f, 0.4f, 1.0f, true
        ).save(consumer, RL("thermo_plant/ethanol_from_melon"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.YEAST_CULTURE), Ingredient.of(Items.SWEET_BERRIES),
                new FluidStack(ModFluids.ETHANOL.get(), 20), ItemStack.EMPTY,
                TemperatureRange.of(303, 333), 0f, 0.4f, 1.0f, true
        ).save(consumer, RL("thermo_plant/ethanol_from_sweet_berries"));
        thermoPlant(FluidIngredient.EMPTY, Ingredient.of(Tags.Items.SEEDS),
                new FluidStack(ModFluids.VEGETABLE_OIL.get(), 50), ItemStack.EMPTY,
                TemperatureRange.any(), 2f, 0.5f, 1.0f, false
        ).save(consumer, RL("thermo_plant/vegetable_oil_from_seeds"));
        thermoPlant(FluidIngredient.EMPTY, Ingredient.of(Tags.Items.CROPS),
                new FluidStack(ModFluids.VEGETABLE_OIL.get(), 20), ItemStack.EMPTY,
                TemperatureRange.any(), 2f, 0.5f, 1.0f, false
        ).save(consumer, RL("thermo_plant/vegetable_oil_from_crops"));
        thermoPlant(FluidIngredient.of(2000, PneumaticCraftTags.Fluids.LUBRICANT), Ingredient.of(Tags.Items.INGOTS_IRON),
                FluidStack.EMPTY, new ItemStack(ModItems.IRON_DRILL_BIT.get()),
                TemperatureRange.any(), 3f, 0.5f, 2.0f, false
        ).save(consumer, RL("thermo_plant/iron_drill_bit"));
        thermoPlant(FluidIngredient.of(4000, PneumaticCraftTags.Fluids.LUBRICANT), Ingredient.of(PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON),
                FluidStack.EMPTY, new ItemStack(ModItems.COMPRESSED_IRON_DRILL_BIT.get()),
                TemperatureRange.min(573), 4f, 0.25f, 5.0f, false
        ).save(consumer, RL("thermo_plant/compressed_iron_drill_bit"));
        thermoPlant(FluidIngredient.of(8000, PneumaticCraftTags.Fluids.LUBRICANT), Ingredient.of(Tags.Items.STORAGE_BLOCKS_DIAMOND),
                FluidStack.EMPTY, new ItemStack(ModItems.DIAMOND_DRILL_BIT.get()),
                TemperatureRange.min(773), 7.5f, 0.1f, 10.0f, false
        ).save(consumer, RL("thermo_plant/diamond_drill_bit"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.PLANT_OIL), Ingredient.of(Tags.Items.CROPS_POTATO),
                FluidStack.EMPTY, new ItemStack(ModItems.CHIPS.get(), 4),
                TemperatureRange.min(423), 0f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/chips"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.PLANT_OIL), Ingredient.of(ModItems.RAW_SALMON_TEMPURA.get()),
                FluidStack.EMPTY, new ItemStack(ModItems.SALMON_TEMPURA.get(), 1),
                TemperatureRange.min(423), 0f, 1.0f, 1.0f, false
        ).save(consumer, RL("thermo_plant/salmon_tempura"));
        thermoPlant(FluidIngredient.of(4000, PneumaticCraftTags.Fluids.EXPERIENCE), Ingredient.of(ModItems.SPAWNER_CORE_SHELL.get()),
                FluidStack.EMPTY, new ItemStack(ModItems.SPAWNER_CORE.get()),
                TemperatureRange.any(), 3f, 0.5f, 3.0f, false
        ).save(consumer, RL("thermo_plant/spawner_core"));
        thermoPlant(FluidIngredient.of(50, PneumaticCraftTags.Fluids.PLASTIC), Ingredient.of(ModBlocks.PRESSURE_TUBE.get()),
                FluidStack.EMPTY, new ItemStack(ModBlocks.REINFORCED_PRESSURE_TUBE.get()),
                TemperatureRange.any(), 1.5f, 1f, 1.0f, false
        ).save(consumer, RL("thermo_plant/reinforced_pressure_tube"));

        // assembly system
        assembly(Ingredient.of(ModItems.EMPTY_PCB.get()), new ItemStack(ModItems.UNASSEMBLED_PCB.get()),
                AssemblyProgramType.LASER)
                .save(consumer, RL("assembly/unassembled_pcb"));
        assembly(Ingredient.of(Tags.Items.DUSTS_REDSTONE), new ItemStack(Items.RED_DYE, 2),
                AssemblyProgramType.DRILL)
                .save(consumer, RL("assembly/red_dye"));
        assembly(Ingredient.of(ModBlocks.COMPRESSED_IRON_BLOCK.get()), new ItemStack(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), 20),
                AssemblyProgramType.DRILL)
                .save(consumer, RL("assembly/pressure_chamber_valve"));
        assembly(StackedIngredient.fromItems(20, ModBlocks.PRESSURE_CHAMBER_VALVE.get()), new ItemStack(ModBlocks.ADVANCED_PRESSURE_TUBE.get(), 8),
                AssemblyProgramType.LASER)
                .save(consumer, RL("assembly/advanced_pressure_tube"));
        assembly(Ingredient.of(Tags.Items.STORAGE_BLOCKS_QUARTZ), new ItemStack(ModBlocks.APHORISM_TILE.get(), 4),
                AssemblyProgramType.LASER)
                .save(consumer, RL("assembly/aphorism_tile"));
        assembly(Ingredient.of(ModItems.UNASSEMBLED_NETHERITE_DRILL_BIT.get()), new ItemStack(ModItems.NETHERITE_DRILL_BIT.get()),
                AssemblyProgramType.DRILL)
                .save(consumer, RL("assembly/netherite_drill_bit"));
        assembly(Ingredient.of(ModItems.SOLAR_WAFER.get()), new ItemStack(ModItems.SOLAR_CELL.get()),
                AssemblyProgramType.DRILL)
                .save(consumer, RL("assembly/solar_cell"));

        // amadron (core static offers only)
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 8)),
                AmadronTradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_DRILL.get()))
        ).save(consumer, RL("amadron/assembly_program_drill"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 8)),
                AmadronTradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_LASER.get()))
        ).save(consumer, RL("amadron/assembly_program_laser"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 14)),
                AmadronTradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_DRILL_LASER.get()))
        ).save(consumer, RL("amadron/assembly_program_drill_laser"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 8)),
                AmadronTradeResource.of(new ItemStack(ModItems.PCB_BLUEPRINT.get()))
        ).save(consumer, RL("amadron/pcb_blueprint"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.OIL.get(), 5000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).save(consumer, RL("amadron/oil_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.DIESEL.get(), 4000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).save(consumer, RL("amadron/diesel_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.KEROSENE.get(), 3000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).save(consumer, RL("amadron/kerosene_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.GASOLINE.get(), 2000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).save(consumer, RL("amadron/gasoline_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.LPG.get(), 1000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).save(consumer, RL("amadron/lpg_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.LUBRICANT.get(), 2500)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).save(consumer, RL("amadron/lubricant_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 5)),
                AmadronTradeResource.of(new FluidStack(ModFluids.LUBRICANT.get(), 1000))
        ).save(consumer, RL("amadron/emerald_to_lubricant"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 1)),
                AmadronTradeResource.of(new FluidStack(ModFluids.OIL.get(), 1000))
        ).save(consumer, RL("amadron/emerald_to_oil"));

        // fluid mixer
        fluidMixer(
                FluidIngredient.of(25, PneumaticCraftTags.Fluids.PLANT_OIL),
                FluidIngredient.of(25, PneumaticCraftTags.Fluids.ETHANOL),
                new FluidStack(ModFluids.BIODIESEL.get(), 50), new ItemStack(ModItems.GLYCEROL.get()),
                2.0f, 300
        ).save(consumer, RL("fluid_mixer/biodiesel"));
        fluidMixer(
                FluidIngredient.of(1000, Fluids.WATER),
                FluidIngredient.of(1000, Fluids.LAVA),
                FluidStack.EMPTY, new ItemStack(Items.OBSIDIAN),
                0.5f, 40
        ).save(consumer, RL("fluid_mixer/mix_obsidian"));

        // fuels
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.CRUDE_OIL), 200_000, 0.25f)
                .save(consumer, RL("pneumaticcraft_fuels/crude_oil"));
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.DIESEL), 1_000_000, 0.8f)
                .save(consumer, RL("pneumaticcraft_fuels/diesel"));
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.BIODIESEL), 1_000_000, 0.8f)
                .save(consumer, RL("pneumaticcraft_fuels/biodiesel"));
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.KEROSENE), 1_100_000, 1f)
                .save(consumer, RL("pneumaticcraft_fuels/kerosene"));
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.GASOLINE), 1_500_000, 1.5f)
                .save(consumer, RL("pneumaticcraft_fuels/gasoline"));
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.LPG), 1_800_000, 1.25f)
                .save(consumer, RL("pneumaticcraft_fuels/lpg"));
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.ETHANOL), 400_000, 1f)
                .save(consumer, RL("pneumaticcraft_fuels/ethanol"));

        // non-pneumaticcraft fuel compat
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.forgeTag("ethene")), 1_800_800, 1.25f)
                .save(consumer.withConditions(new FluidTagPresentCondition("forge:ethene")), RL("pneumaticcraft_fuels/ethylene"));
        fuelQuality(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.forgeTag("hydrogen")), 300_000, 1.5f)
                .save(consumer.withConditions(new FluidTagPresentCondition("forge:hydrogen")), RL("pneumaticcraft_fuels/hydrogen"));

//        ModLoadedCondition thermalLoaded = new ModLoadedCondition("thermal");
//        conditionalFuelQuality(consumer, RL("pneumaticcraft_fuels/cofh_biofuel"), thermalLoaded,
//                FluidIngredient.of(1000, new ResourceLocation("thermal:refined_biofuel")), 1_000_000, 0.8f);
//        conditionalFuelQuality(consumer, RL("pneumaticcraft_fuels/cofh_creosote"), thermalLoaded,
//                FluidIngredient.of(1000, new ResourceLocation("thermal:creosote")), 50_000, 0.25f);
//        conditionalFuelQuality(consumer, RL("pneumaticcraft_fuels/cofh_refined_fuel"), thermalLoaded,
//                FluidIngredient.of(1000, new ResourceLocation("thermal:refined_fuel")), 1_500_000, 1.5f);
//        conditionalFuelQuality(consumer, RL("pneumaticcraft_fuels/cofh_tree_oil"), thermalLoaded,
//                FluidIngredient.of(1000, new ResourceLocation("thermal:tree_oil")), 400_000, 1.0f);
    }

    private <T extends ItemLike> ShapelessRecipeBuilder shapeless(T result, T required, Object... ingredients) {
        return shapeless(result, 1, required, ingredients);
    }

    private <T extends ItemLike> ShapelessRecipeBuilder shapeless(T result, int count, T required, Object... ingredients) {
        ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, count);
        for (Object v : ingredients) {
            if (v instanceof TagKey<?>) {
                //noinspection unchecked
                b.requires((TagKey<Item>) v);
            } else if (v instanceof ItemLike) {
                b.requires((ItemLike) v);
            } else if (v instanceof Ingredient) {
                b.requires((Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.unlockedBy("has_" + safeName(required), has(required));
        return b;
    }

    private void buildLogisticsFrameSelfCraft(Item frame, RecipeOutput consumer) {
        shapeless(frame, frame, frame).save(consumer, PneumaticCraftUtils.getRegistryName(frame).orElseThrow() + "_self");
    }

    private ShapedRecipeBuilder logisticsFrame(Item result, TagKey<Item> dye) {
        return shaped(result, 8, ModItems.LOGISTICS_CORE.get(),
                "PPP/PDP/PCP",
                'P', Items.STICK,
                'C', ModItems.LOGISTICS_CORE.get(),
                'D', dye);
    }

    private ShapedRecipeBuilder networkComponent(Item result, int count, TagKey<Item> edge, TagKey<Item> dyeCorner) {
        return shaped(result, count, ModItems.CAPACITOR.get(), "CEC/EXE/CEC", 'C', dyeCorner, 'E', edge, 'X', Tags.Items.CHESTS_WOODEN);
    }

    private ShapedRecipeBuilder networkComponent(Item result, int count, Item edge, TagKey<Item> dyeCorner) {
        return shaped(result, count, ModItems.CAPACITOR.get(), "CEC/EXE/CEC", 'C', dyeCorner, 'E', edge, 'X', Tags.Items.CHESTS_WOODEN);
    }

    private <T extends ItemLike> ShapedPressurizableRecipeBuilder pneumaticTool(T result, Object dye) {
        return shapedPressure(result, ModItems.COMPRESSED_IRON_INGOT.get(),
                "IDI/C  /ILI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'D', dye,
                'C', ModItems.AIR_CANISTER.get(),
                'L', Blocks.LEVER
        );
    }

    private <T extends ItemLike, U extends ShapedRecipeBuilder> U genericShaped(U builder, T result, T required, String pattern, Object... keys) {
        Arrays.stream(pattern.split("/")).forEach(builder::pattern);
        for (int i = 0; i < keys.length; i += 2) {
            Object v = keys[i + 1];
            if (v instanceof TagKey<?>) {
                //noinspection unchecked
                builder.define((Character) keys[i], (TagKey<Item>) v);
            } else if (v instanceof ItemLike) {
                builder.define((Character) keys[i], (ItemLike) v);
            } else if (v instanceof Ingredient) {
                builder.define((Character) keys[i], (Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        builder.unlockedBy("has_" + safeName(required), has(required));
        return builder;
    }

    private <T extends ItemLike> CompressorUpgradeRecipeBuilder shapedCompressorUpgrade(T result, T required, String pattern, Object... keys) {
        return genericShaped(CompressorUpgradeRecipeBuilder.shapedRecipe(result), result, required, pattern, keys);
    };

    private <T extends ItemLike> ShapedPressurizableRecipeBuilder shapedPressure(T result, T required, String pattern, Object... keys) {
        return genericShaped(ShapedPressurizableRecipeBuilder.shapedRecipe(result), result, required, pattern, keys);
    }

    private <T extends ItemLike> ShapedRecipeBuilder shaped(T result, int count, T required, String pattern, Object... keys) {
        return genericShaped(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, count), result, required, pattern, keys);
    }

    private <T extends ItemLike> ShapedRecipeBuilder shaped(T result, T required, String pattern, Object... keys) {
        return shaped(result, 1, required, pattern, keys);
    }

    private ShapedRecipeBuilder plasticBrick(DyeColor color, TagKey<Item> dyeIngredient) {
        Item brick = ModBlocks.plasticBrick(color).get().asItem();
        return shaped(brick, 8, ModItems.PLASTIC.get(),
                "PPP/PDP/PPP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'D', dyeIngredient);
    }

    private ShapelessRecipeBuilder smoothPlasticBrick(DyeColor color) {
        Item smoothBrick = ModBlocks.smoothPlasticBrick(color).get().asItem();
        Item brick = ModBlocks.plasticBrick(color).get().asItem();
        return shapeless(smoothBrick, ModItems.PLASTIC.get(), brick);
    }

    private ShapedRecipeBuilder wallLamp(DyeColor color, boolean inverted, TagKey<Item> dyeIngredient) {
        Item lamp = ModBlocks.wallLamp(color, inverted).get().asItem();
        return shaped(lamp, 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                " R /IGI/ D ",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Items.GLOWSTONE,
                'R', inverted ? Items.REDSTONE_TORCH : Tags.Items.DUSTS_REDSTONE,
                'D', dyeIngredient);
    }

    private ShapedRecipeBuilder miniGunAmmo(Item result, Object item1, Object item2) {
        return shaped(result, ModItems.GUN_AMMO.get(),
                " A /C1C/C2C",
                'A', ModItems.GUN_AMMO.get(),
                'C', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                '1', item1,
                '2', item2);
    }

    private ShapedRecipeBuilder standardUpgrade(PNCUpgrade what, Object center, Object edge) {
        return standardUpgrade(what, 1, center, edge);
    }

    private ShapedRecipeBuilder standardUpgrade(PNCUpgrade what, int count, Object center, Object edge) {
        return shaped(what.getItem(), count, Items.LAPIS_LAZULI,
                "LXL/XCX/LXL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'X', edge,
                'C', center);
    }

    private RecipeBuilder pressureChamber(List<StackedIngredient> in, float pressure, ItemStack... out) {
        return new PressureChamberRecipeBuilder(in, pressure, out)
                .unlockedBy(getHasName(ModBlocks.PRESSURE_CHAMBER_VALVE.get()), has(ModBlocks.PRESSURE_CHAMBER_VALVE.get()));
    }

    private RecipeBuilder explosionCrafting(Ingredient ingredient, int lossRate, ItemStack result) {
        return new ExplosionCraftingRecipeBuilder(ingredient, lossRate, result)
                .unlockedBy(getHasName(Blocks.TNT), has(Blocks.TNT));
    }

    private RecipeBuilder heatFrameCooling(Ingredient ingredient, int maxTemp, ItemStack result) {
        return heatFrameCooling(ingredient, maxTemp, result, 0f, 0f);
    }

    private RecipeBuilder heatFrameCooling(Ingredient ingredient, int maxTemp, ItemStack result, float bonusMult, float bonusLimit) {
        return new HeatFrameCoolingRecipeBuilder(ingredient, maxTemp, result, bonusMult, bonusLimit)
                .unlockedBy(getHasName(ModItems.HEAT_FRAME.get()), has(ModItems.HEAT_FRAME.get()));
    }

    private RecipeBuilder refinery(FluidIngredient ingredient, TemperatureRange operatingTemp, FluidStack... outputs) {
        return new RefineryRecipeBuilder(ingredient, operatingTemp, outputs)
                .unlockedBy(getHasName(ModBlocks.REFINERY.get()), has(ModBlocks.REFINERY.get()));
    }

    private RecipeBuilder thermoPlant(FluidIngredient inputFluid, @Nullable Ingredient inputItem,
                                                 FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                                                 float speed, float airUseMuliplier, boolean exothermic) {
        return new ThermoPlantRecipeBuilder(inputFluid, inputItem, outputFluid, outputItem, operatingTemperature, requiredPressure, speed, airUseMuliplier, exothermic)
                .unlockedBy(getHasName(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()), has(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()));
    }

    private RecipeBuilder assembly(Ingredient input, ItemStack output, AssemblyProgramType programType) {
        return new AssemblyRecipeBuilder(input, output, programType)
                .unlockedBy(getHasName(ModBlocks.ASSEMBLY_CONTROLLER.get()), has(ModBlocks.ASSEMBLY_CONTROLLER.get()));
    }

    private RecipeBuilder amadronStatic(AmadronTradeResource in, AmadronTradeResource out) {
        return new AmadronRecipeBuilder(in, out, true, 0)
                .unlockedBy(getHasName(ModItems.AMADRON_TABLET.get()), has(ModItems.AMADRON_TABLET.get()));
    }

    private RecipeBuilder amadronPeriodic(AmadronTradeResource in, AmadronTradeResource out, int tradeLevel, int maxStock) {
        return new AmadronRecipeBuilder(in, out, false, false, tradeLevel, maxStock, PlayerFilter.YES, PlayerFilter.NO)
                .unlockedBy(getHasName(ModItems.AMADRON_TABLET.get()), has(ModItems.AMADRON_TABLET.get()));
    }

    private RecipeBuilder fluidMixer(FluidIngredient input1, FluidIngredient input2, FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime) {
        return new FluidMixerRecipeBuilder(input1, input2, outputFluid, outputItem, pressure, processingTime)
                .unlockedBy(getHasName(ModBlocks.FLUID_MIXER.get()), has(ModBlocks.FLUID_MIXER.get()));
    }

    private RecipeBuilder fuelQuality(FluidIngredient fuel, int airPerBucket, float burnRate) {
        return new FuelQualityBuilder(fuel, airPerBucket, burnRate)
                .unlockedBy(getHasName(ModBlocks.LIQUID_COMPRESSOR.get()), has(ModBlocks.LIQUID_COMPRESSOR.get()));
    }

    private String getId(String s) {
        return RL(s).toString();
    }

    private <T extends ItemLike> String safeName(T required) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(required.asItem());
        return key.getPath().replace('/', '_');
    }

//    @Override
//    public String getName() {
//        return "PneumaticCraft Recipes";
//    }
}
