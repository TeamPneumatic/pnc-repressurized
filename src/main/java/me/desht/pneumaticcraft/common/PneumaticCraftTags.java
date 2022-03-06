/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class PneumaticCraftTags {
    public static class Blocks extends PneumaticCraftTags {
        public static final Tag.Named<Block> SLABS = modTag("slabs");
        public static final Tag.Named<Block> STAIRS = modTag("stairs");
        public static final Tag.Named<Block> DOORS = modTag("doors");
        public static final Tag.Named<Block> WALLS = modTag("walls");
        public static final Tag.Named<Block> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final Tag.Named<Block> SMOOTH_PLASTIC_BRICKS = modTag("smooth_plastic_bricks");
        public static final Tag.Named<Block> FLUID_TANKS = modTag("fluid_tanks");
        public static final Tag.Named<Block> CHESTS = modTag("chests");
        public static final Tag.Named<Block> REINFORCED_STONE = modTag("reinforced_stone");
        public static final Tag.Named<Block> REINFORCED_STONE_BRICKS = modTag("reinforced_stone_bricks");
        public static final Tag.Named<Block> COMPRESSED_STONE = modTag("compressed_stone");
        public static final Tag.Named<Block> COMPRESSED_STONE_BRICKS = modTag("compressed_stone_bricks");
        public static final Tag.Named<Block> WALL_LAMPS = modTag("wall_lamps");
        public static final Tag.Named<Block> WALL_LAMPS_INVERTED = modTag("wall_lamps_inverted");
        public static final Tag.Named<Block> BLOCK_TRACKER_MISC = modTag("block_tracker_misc_blocks");
        public static final Tag.Named<Block> PROBE_TARGET = modTag("probe_target");
        public static final Tag.Named<Block> JACKHAMMER_ORES = modTag("jackhammer_ores");
        public static final Tag.Named<Block> ELECTROSTATIC_GRID = modTag("electrostatic_grid");

        public static final Tag.Named<Block> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");

        static Tag.Named<Block> tag(String modid, String name) {
            return BlockTags.bind(new ResourceLocation(modid, name).toString());
        }

        static Tag.Named<Block> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static Tag.Named<Block> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Items extends PneumaticCraftTags {
        public static final Tag.Named<Item> SLABS = modTag("slabs");
        public static final Tag.Named<Item> STAIRS = modTag("stairs");
        public static final Tag.Named<Item> DOORS = modTag("doors");
        public static final Tag.Named<Item> WALLS = modTag("walls");
        public static final Tag.Named<Item> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final Tag.Named<Item> SMOOTH_PLASTIC_BRICKS = modTag("smooth_plastic_bricks");
        public static final Tag.Named<Item> WALL_LAMPS = modTag("wall_lamps");
        public static final Tag.Named<Item> WALL_LAMPS_INVERTED = modTag("wall_lamps_inverted");
        public static final Tag.Named<Item> FLUID_TANKS = modTag("fluid_tanks");
        public static final Tag.Named<Item> CHESTS = modTag("chests");
        public static final Tag.Named<Item> REINFORCED_STONE = modTag("reinforced_stone");
        public static final Tag.Named<Item> REINFORCED_STONE_BRICKS = modTag("reinforced_stone_bricks");
        public static final Tag.Named<Item> COMPRESSED_STONE = modTag("compressed_stone");
        public static final Tag.Named<Item> COMPRESSED_STONE_BRICKS = modTag("compressed_stone_bricks");

        public static final Tag.Named<Item> UPGRADE_COMPONENTS = modTag("upgrade_components");
        public static final Tag.Named<Item> BASIC_DRONES = modTag("basic_drones");
        public static final Tag.Named<Item> PLASTIC_SHEETS = modTag("plastic_sheets");
        public static final Tag.Named<Item> FLOUR = forgeTag("dusts/flour");

        public static final Tag.Named<Item> INGOTS_COMPRESSED_IRON = forgeTag("ingots/compressed_iron");
        public static final Tag.Named<Item> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");

        public static final Tag.Named<Item> CURIO = tag("curios", "curio");

        public static final Tag.Named<Item> BREAD = forgeTag("bread");

        public static final Tag.Named<Item> WRENCHES = forgeTag("tools/wrench");

        static Tag.Named<Item> tag(String modid, String name) {
            return ItemTags.bind(new ResourceLocation(modid, name).toString());
        }

        static Tag.Named<Item> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static Tag.Named<Item> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Fluids extends PneumaticCraftTags {
        public static final Tag.Named<Fluid> CRUDE_OIL = forgeTag("crude_oil");
        public static final Tag.Named<Fluid> LUBRICANT = forgeTag("lubricant");
        public static final Tag.Named<Fluid> ETHANOL = forgeTag("ethanol");
        public static final Tag.Named<Fluid> DIESEL = forgeTag("diesel");
        public static final Tag.Named<Fluid> KEROSENE = forgeTag("kerosene");
        public static final Tag.Named<Fluid> GASOLINE = forgeTag("gasoline");
        public static final Tag.Named<Fluid> LPG = forgeTag("lpg");
        public static final Tag.Named<Fluid> BIODIESEL = forgeTag("biodiesel");
        public static final Tag.Named<Fluid> PLANT_OIL = forgeTag("plantoil");
        public static final Tag.Named<Fluid> EXPERIENCE = forgeTag("experience");

        public static final Tag.Named<Fluid> ETCHING_ACID = modTag("etching_acid");
        public static final Tag.Named<Fluid> PLASTIC = modTag("plastic");
        public static final Tag.Named<Fluid> YEAST_CULTURE = modTag("yeast_culture");

        public static final Tag.Named<Fluid> SEISMIC = modTag("seismic_sensor_interesting");

        static Tag.Named<Fluid> tag(String modid, String name) {
            return FluidTags.bind(new ResourceLocation(modid, name).toString());
        }

        static Tag.Named<Fluid> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        public static Tag.Named<Fluid> forgeTag(String name) {
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
