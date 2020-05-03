package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class PneumaticCraftTags {
    public static class Blocks extends PneumaticCraftTags {
        public static final Tag<Block> SLABS = modTag("slabs");
        public static final Tag<Block> STAIRS = modTag("stairs");
        public static final Tag<Block> DOORS = modTag("doors");
        public static final Tag<Block> WALLS = modTag("walls");
        public static final Tag<Block> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final Tag<Block> FLUID_TANKS = modTag("fluid_tanks");
        public static final Tag<Block> CHESTS = modTag("chests");

        public static final Tag<Block> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");

        static Tag<Block> tag(String modid, String name) {
            return tag(BlockTags.Wrapper::new, modid, name);
        }

        static Tag<Block> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static Tag<Block> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Items extends PneumaticCraftTags {
        public static final Tag<Item> SLABS = modTag("slabs");
        public static final Tag<Item> STAIRS = modTag("stairs");
        public static final Tag<Item> DOORS = modTag("doors");
        public static final Tag<Item> WALLS = modTag("walls");
        public static final Tag<Item> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final Tag<Item> FLUID_TANKS = modTag("fluid_tanks");
        public static final Tag<Item> CHESTS = modTag("chests");

        public static final Tag<Item> UPGRADE_COMPONENTS = modTag("upgrade_components");
        public static final Tag<Item> BASIC_DRONES = modTag("basic_drones");

        public static final Tag<Item> INGOTS_COMPRESSED_IRON = forgeTag("ingots/compressed_iron");
        public static final Tag<Item> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");

        static Tag<Item> tag(String modid, String name) {
            return tag(ItemTags.Wrapper::new, modid, name);
        }

        static Tag<Item> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static Tag<Item> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Fluids extends PneumaticCraftTags {
        public static final Tag<Fluid> OIL = modTag("oil");
        public static final Tag<Fluid> ETCHING_ACID = modTag("etching_acid");

        static Tag<Fluid> tag(String modid, String name) {
            return tag(FluidTags.Wrapper::new, modid, name);
        }

        static Tag<Fluid> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static Tag<Fluid> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    static <T extends Tag<?>> T tag(Function<ResourceLocation, T> creator, String modid, String name) {
        return creator.apply(new ResourceLocation(modid, name));
    }

    static <T extends Tag<?>> T modTag(Function<ResourceLocation, T> creator, String name) {
        return tag(creator, Names.MOD_ID, name);
    }

    static <T extends Tag<?>> T forgeTag(Function<ResourceLocation, T> creator, String name) {
        return tag(creator, "forge", name);
    }
}
