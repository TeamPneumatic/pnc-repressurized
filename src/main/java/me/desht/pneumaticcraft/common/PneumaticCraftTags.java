package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class PneumaticCraftTags {
    public static class Blocks extends PneumaticCraftTags {
        public static final ITag.INamedTag<Block> SLABS = modTag("slabs");
        public static final ITag.INamedTag<Block> STAIRS = modTag("stairs");
        public static final ITag.INamedTag<Block> DOORS = modTag("doors");
        public static final ITag.INamedTag<Block> WALLS = modTag("walls");
        public static final ITag.INamedTag<Block> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final ITag.INamedTag<Block> FLUID_TANKS = modTag("fluid_tanks");
        public static final ITag.INamedTag<Block> CHESTS = modTag("chests");
        public static final ITag.INamedTag<Block> REINFORCED_STONE = modTag("reinforced_stone");
        public static final ITag.INamedTag<Block> REINFORCED_STONE_BRICKS = modTag("reinforced_stone_bricks");
        public static final ITag.INamedTag<Block> WALL_LAMPS = modTag("wall_lamps");
        public static final ITag.INamedTag<Block> WALL_LAMPS_INVERTED = modTag("wall_lamps_inverted");
        public static final ITag.INamedTag<Block> BLOCK_TRACKER_MISC = modTag("block_tracker_misc_blocks");
        public static final ITag.INamedTag<Block> PROBE_TARGET = modTag("probe_target");

        public static final ITag.INamedTag<Block> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");

        public static final ITag.INamedTag<Block> JACKHAMMER_ORES = modTag("jackhammer_ores");

        static ITag.INamedTag<Block> tag(String modid, String name) {
            return BlockTags.bind(new ResourceLocation(modid, name).toString());
        }

        static ITag.INamedTag<Block> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static ITag.INamedTag<Block> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Items extends PneumaticCraftTags {
        public static final ITag.INamedTag<Item> SLABS = modTag("slabs");
        public static final ITag.INamedTag<Item> STAIRS = modTag("stairs");
        public static final ITag.INamedTag<Item> DOORS = modTag("doors");
        public static final ITag.INamedTag<Item> WALLS = modTag("walls");
        public static final ITag.INamedTag<Item> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final ITag.INamedTag<Item> WALL_LAMPS = modTag("wall_lamps");
        public static final ITag.INamedTag<Item> WALL_LAMPS_INVERTED = modTag("wall_lamps_inverted");
        public static final ITag.INamedTag<Item> FLUID_TANKS = modTag("fluid_tanks");
        public static final ITag.INamedTag<Item> CHESTS = modTag("chests");
        public static final ITag.INamedTag<Item> REINFORCED_STONE = modTag("reinforced_stone");
        public static final ITag.INamedTag<Item> REINFORCED_STONE_BRICKS = modTag("reinforced_stone_bricks");

        public static final ITag.INamedTag<Item> UPGRADE_COMPONENTS = modTag("upgrade_components");
        public static final ITag.INamedTag<Item> BASIC_DRONES = modTag("basic_drones");
        public static final ITag.INamedTag<Item> PLASTIC_SHEETS = modTag("plastic_sheets");
        public static final ITag.INamedTag<Item> FLOUR = forgeTag("dusts/flour");

        public static final ITag.INamedTag<Item> INGOTS_COMPRESSED_IRON = forgeTag("ingots/compressed_iron");
        public static final ITag.INamedTag<Item> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");

        public static final ITag.INamedTag<Item> CURIO = tag("curios", "curio");

        public static final ITag.INamedTag<Item> BREAD = forgeTag("bread");

        public static final Tags.IOptionalNamedTag<Item> WRENCHES = ItemTags.createOptional(new ResourceLocation("forge:tools/wrench"));

        static ITag.INamedTag<Item> tag(String modid, String name) {
            return ItemTags.bind(new ResourceLocation(modid, name).toString());
        }

        static ITag.INamedTag<Item> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static ITag.INamedTag<Item> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Fluids extends PneumaticCraftTags {
        public static final ITag.INamedTag<Fluid> CRUDE_OIL = forgeTag("crude_oil");
        public static final ITag.INamedTag<Fluid> LUBRICANT = forgeTag("lubricant");
        public static final ITag.INamedTag<Fluid> ETHANOL = forgeTag("ethanol");
        public static final ITag.INamedTag<Fluid> DIESEL = forgeTag("diesel");
        public static final ITag.INamedTag<Fluid> KEROSENE = forgeTag("kerosene");
        public static final ITag.INamedTag<Fluid> GASOLINE = forgeTag("gasoline");
        public static final ITag.INamedTag<Fluid> LPG = forgeTag("lpg");
        public static final ITag.INamedTag<Fluid> BIODIESEL = forgeTag("biodiesel");
        public static final ITag.INamedTag<Fluid> PLANT_OIL = forgeTag("plantoil");
        public static final ITag.INamedTag<Fluid> EXPERIENCE = forgeTag("experience");

        public static final ITag.INamedTag<Fluid> ETCHING_ACID = modTag("etching_acid");
        public static final ITag.INamedTag<Fluid> PLASTIC = modTag("plastic");
        public static final ITag.INamedTag<Fluid> YEAST_CULTURE = modTag("yeast_culture");

        static ITag.INamedTag<Fluid> tag(String modid, String name) {
            return FluidTags.bind(new ResourceLocation(modid, name).toString());
        }

        static ITag.INamedTag<Fluid> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        public static ITag.INamedTag<Fluid> forgeTag(String name) {
            return tag("forge", name);
        }
    }

//    static <T extends Tag<?>> T tag(Function<ResourceLocation, T> creator, String modid, String name) {
//        return creator.apply(new ResourceLocation(modid, name));
//    }
//
//    static <T extends Tag<?>> T modTag(Function<ResourceLocation, T> creator, String name) {
//        return tag(creator, Names.MOD_ID, name);
//    }
//
//    static <T extends Tag<?>> T forgeTag(Function<ResourceLocation, T> creator, String name) {
//        return tag(creator, "forge", name);
//    }
}
