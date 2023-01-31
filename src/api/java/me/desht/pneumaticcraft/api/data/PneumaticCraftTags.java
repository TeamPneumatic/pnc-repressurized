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

package me.desht.pneumaticcraft.api.data;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class PneumaticCraftTags {
    public static class Blocks extends PneumaticCraftTags {
        public static final TagKey<Block> SLABS = modTag("slabs");
        public static final TagKey<Block> STAIRS = modTag("stairs");
        public static final TagKey<Block> DOORS = modTag("doors");
        public static final TagKey<Block> WALLS = modTag("walls");
        public static final TagKey<Block> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final TagKey<Block> SMOOTH_PLASTIC_BRICKS = modTag("smooth_plastic_bricks");
        public static final TagKey<Block> FLUID_TANKS = modTag("fluid_tanks");
        public static final TagKey<Block> CHESTS = modTag("chests");
        public static final TagKey<Block> REINFORCED_STONE = modTag("reinforced_stone");
        public static final TagKey<Block> REINFORCED_STONE_BRICKS = modTag("reinforced_stone_bricks");
        public static final TagKey<Block> COMPRESSED_STONE = modTag("compressed_stone");
        public static final TagKey<Block> COMPRESSED_STONE_BRICKS = modTag("compressed_stone_bricks");
        public static final TagKey<Block> WALL_LAMPS = modTag("wall_lamps");
        public static final TagKey<Block> WALL_LAMPS_INVERTED = modTag("wall_lamps_inverted");
        public static final TagKey<Block> BLOCK_TRACKER_MISC = modTag("block_tracker_misc_blocks");
        public static final TagKey<Block> PROBE_TARGET = modTag("probe_target");
        public static final TagKey<Block> JACKHAMMER_ORES = modTag("jackhammer_ores");
        public static final TagKey<Block> ELECTROSTATIC_GRID = modTag("electrostatic_grid");

        public static final TagKey<Block> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");

        static TagKey<Block> tag(String modid, String name) {
            return ForgeRegistries.BLOCKS.tags().createTagKey(new ResourceLocation(modid, name));
        }

        static TagKey<Block> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static TagKey<Block> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Items extends PneumaticCraftTags {
        public static final TagKey<Item> SLABS = modTag("slabs");
        public static final TagKey<Item> STAIRS = modTag("stairs");
        public static final TagKey<Item> DOORS = modTag("doors");
        public static final TagKey<Item> WALLS = modTag("walls");
        public static final TagKey<Item> PLASTIC_BRICKS = modTag("plastic_bricks");
        public static final TagKey<Item> SMOOTH_PLASTIC_BRICKS = modTag("smooth_plastic_bricks");
        public static final TagKey<Item> WALL_LAMPS = modTag("wall_lamps");
        public static final TagKey<Item> WALL_LAMPS_INVERTED = modTag("wall_lamps_inverted");
        public static final TagKey<Item> FLUID_TANKS = modTag("fluid_tanks");
        public static final TagKey<Item> CHESTS = modTag("chests");
        public static final TagKey<Item> REINFORCED_STONE = modTag("reinforced_stone");
        public static final TagKey<Item> REINFORCED_STONE_BRICKS = modTag("reinforced_stone_bricks");
        public static final TagKey<Item> COMPRESSED_STONE = modTag("compressed_stone");
        public static final TagKey<Item> COMPRESSED_STONE_BRICKS = modTag("compressed_stone_bricks");

        public static final TagKey<Item> WIRING = modTag("wiring");
        public static final TagKey<Item> UPGRADE_COMPONENTS = modTag("upgrade_components");
        public static final TagKey<Item> BASIC_DRONES = modTag("basic_drones");
        public static final TagKey<Item> PLASTIC_SHEETS = modTag("plastic_sheets");
        public static final TagKey<Item> FLOUR = forgeTag("dusts/flour");

        public static final TagKey<Item> INGOTS_COMPRESSED_IRON = forgeTag("ingots/compressed_iron");
        public static final TagKey<Item> STORAGE_BLOCKS_COMPRESSED_IRON = forgeTag("storage_blocks/compressed_iron");
        public static final TagKey<Item> GEARS = forgeTag("gears");
        public static final TagKey<Item> GEARS_COMPRESSED_IRON = forgeTag("gears/compressed_iron");
        public static final TagKey<Item> NUGGETS_COPPER = forgeTag("nuggets/copper");

        public static final TagKey<Item> CURIO = tag("curios", "curio");

        public static final TagKey<Item> BREAD = forgeTag("bread");

        public static final TagKey<Item> WRENCHES = forgeTag("tools/wrench");

        static TagKey<Item> tag(String modid, String name) {
            return Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(new ResourceLocation(modid, name));
        }

        static TagKey<Item> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static TagKey<Item> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class Fluids extends PneumaticCraftTags {
        public static final TagKey<Fluid> CRUDE_OIL = forgeTag("crude_oil");
        public static final TagKey<Fluid> LUBRICANT = forgeTag("lubricant");
        public static final TagKey<Fluid> ETHANOL = forgeTag("ethanol");
        public static final TagKey<Fluid> DIESEL = forgeTag("diesel");
        public static final TagKey<Fluid> KEROSENE = forgeTag("kerosene");
        public static final TagKey<Fluid> GASOLINE = forgeTag("gasoline");
        public static final TagKey<Fluid> LPG = forgeTag("lpg");
        public static final TagKey<Fluid> BIODIESEL = forgeTag("biodiesel");
        public static final TagKey<Fluid> PLANT_OIL = forgeTag("plantoil");
        public static final TagKey<Fluid> EXPERIENCE = forgeTag("experience");

        public static final TagKey<Fluid> ETCHING_ACID = modTag("etching_acid");
        public static final TagKey<Fluid> PLASTIC = modTag("plastic");
        public static final TagKey<Fluid> YEAST_CULTURE = modTag("yeast_culture");

        public static final TagKey<Fluid> SEISMIC = modTag("seismic_sensor_interesting");

        static TagKey<Fluid> tag(String modid, String name) {
            return Objects.requireNonNull(ForgeRegistries.FLUIDS.tags()).createTagKey(new ResourceLocation(modid, name));
        }

        static TagKey<Fluid> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        public static TagKey<Fluid> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    public static class EntityTypes extends PneumaticCraftTags {
        public static final TagKey<EntityType<?>> VACUUM_TRAP_BLACKLISTED = modTag("vacuum_trap_blacklisted");
        public static final TagKey<EntityType<?>> VACUUM_TRAP_WHITELISTED = modTag("vacuum_trap_whitelisted");
        public static final TagKey<EntityType<?>> OMNIHOPPER_BLACKLISTED = modTag("omnihopper_blacklisted");

        static TagKey<EntityType<?>> tag(String modid, String name) {
            return Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags()).createTagKey(new ResourceLocation(modid, name));
        }

        static TagKey<EntityType<?>> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }
    }

    public static class Biomes extends PneumaticCraftTags {
        public static final TagKey<Biome> OIL_LAKES_SURFACE = modTag("has_surface_oil_lakes");
        public static final TagKey<Biome> OIL_LAKES_UNDERGROUND = modTag("has_underground_oil_lakes");

        static TagKey<Biome> tag(String modid, String name) {
            return TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), new ResourceLocation(modid, name));
        }

        static TagKey<Biome> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }
    }

    public static class Structures extends PneumaticCraftTags {
        public static final TagKey<Structure> NO_OIL_LAKES = modTag("no_oil_lakes");

        static TagKey<Structure> tag(String modid, String name) {
            return TagKey.create(Registries.STRUCTURE, new ResourceLocation(modid, name));
        }

        static TagKey<Structure> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }
    }
}
