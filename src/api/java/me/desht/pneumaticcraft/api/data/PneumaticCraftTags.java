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
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

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
        public static final TagKey<Block> CROP_SUPPORT_GROWABLE = modTag("crop_support_growable");

        public static final TagKey<Block> STORAGE_BLOCKS_COMPRESSED_IRON = commonTag("storage_blocks/compressed_iron");

        static TagKey<Block> tag(String modid, String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(modid, name));
        }

        static TagKey<Block> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static TagKey<Block> neoforgeTag(String name) {
            return tag("neoforge", name);
        }

        static TagKey<Block> commonTag(String name) {
            return tag("c", name);
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
        public static final TagKey<Item> REINFORCED_CHEST_DISALLOWED = modTag("reinforced_chest_disallowed");

        public static final TagKey<Item> WIRING = modTag("wiring");
        public static final TagKey<Item> UPGRADE_COMPONENTS = modTag("upgrade_components");
        public static final TagKey<Item> BASIC_DRONES = modTag("basic_drones");
        public static final TagKey<Item> PLASTIC_SHEETS = modTag("plastic_sheets");
        public static final TagKey<Item> FLOUR = commonTag("dusts/flour");
        public static final TagKey<Item> LOGISTIC_FRAMES = modTag("logistics_frames");
        public static final TagKey<Item> UPGRADES = modTag("upgrades");

        public static final TagKey<Item> INGOTS_COMPRESSED_IRON = commonTag("ingots/compressed_iron");
        public static final TagKey<Item> STORAGE_BLOCKS_COMPRESSED_IRON = commonTag("storage_blocks/compressed_iron");
        public static final TagKey<Item> GEARS = commonTag("gears");
        public static final TagKey<Item> GEARS_COMPRESSED_IRON = commonTag("gears/compressed_iron");
        public static final TagKey<Item> NUGGETS_COPPER = commonTag("nuggets/copper");

        public static final TagKey<Item> CURIO = tag("curios", "curio");

        public static final TagKey<Item> WRENCHES = commonTag("tools/wrench");

        public static final TagKey<Item> COMPRESSED_IRON_ARMOR = modTag("armors/compressed_iron");
        public static final TagKey<Item> PNEUMATIC_ARMOR = modTag("armors/pneumatic");

        static TagKey<Item> tag(String modid, String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(modid, name));
        }

        static TagKey<Item> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        static TagKey<Item> neoforgeTag(String name) {
            return tag("neoforge", name);
        }

        static TagKey<Item> commonTag(String name) {
            return tag("c", name);
        }
    }

    public static class Fluids extends PneumaticCraftTags {
        public static final TagKey<Fluid> CRUDE_OIL = commonTag("fuels/crude_oil");
        public static final TagKey<Fluid> ETHANOL = commonTag("fuels/ethanol");
        public static final TagKey<Fluid> DIESEL = commonTag("fuels/diesel");
        public static final TagKey<Fluid> KEROSENE = commonTag("fuels/kerosene");
        public static final TagKey<Fluid> GASOLINE = commonTag("fuels/gasoline");
        public static final TagKey<Fluid> LPG = commonTag("fuels/lpg");
        public static final TagKey<Fluid> BIODIESEL = commonTag("fuels/biodiesel");

        public static final TagKey<Fluid> LUBRICANT = commonTag("lubricant");
        public static final TagKey<Fluid> PLANT_OIL = commonTag("plantoil");
        public static final TagKey<Fluid> EXPERIENCE = commonTag("experience");

        public static final TagKey<Fluid> ETCHING_ACID = modTag("etching_acid");
        public static final TagKey<Fluid> PLASTIC = modTag("plastic");
        public static final TagKey<Fluid> YEAST_CULTURE = modTag("yeast_culture");

        public static final TagKey<Fluid> SEISMIC = modTag("seismic_sensor_interesting");

        static TagKey<Fluid> tag(String modid, String name) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(modid, name));
        }

        static TagKey<Fluid> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }

        public static TagKey<Fluid> commonTag(String name) {
            return tag("c", name);
        }
    }

    public static class EntityTypes extends PneumaticCraftTags {
        public static final TagKey<EntityType<?>> VACUUM_TRAP_BLACKLISTED = modTag("vacuum_trap_blacklisted");
        public static final TagKey<EntityType<?>> VACUUM_TRAP_WHITELISTED = modTag("vacuum_trap_whitelisted");
        public static final TagKey<EntityType<?>> OMNIHOPPER_BLACKLISTED = modTag("omnihopper_blacklisted");

        static TagKey<EntityType<?>> tag(String modid, String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(modid, name));
        }

        static TagKey<EntityType<?>> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }
    }

    public static class Biomes extends PneumaticCraftTags {
        public static final TagKey<Biome> OIL_LAKES_SURFACE = modTag("has_surface_oil_lakes");
        public static final TagKey<Biome> OIL_LAKES_UNDERGROUND = modTag("has_underground_oil_lakes");

        static TagKey<Biome> tag(String modid, String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(modid, name));
        }

        static TagKey<Biome> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }
    }

    public static class Structures extends PneumaticCraftTags {
        public static final TagKey<Structure> NO_OIL_LAKES = modTag("no_oil_lakes");

        static TagKey<Structure> tag(String modid, String name) {
            return TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(modid, name));
        }

        static TagKey<Structure> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }
    }

    public static class DamageTypes extends PneumaticCraftTags {
        public static final TagKey<DamageType> ACID = modTag("etching_acid");
        public static final TagKey<DamageType> PRESSURE = modTag("pressure");
        public static final TagKey<DamageType> PLASTIC_BLOCK = modTag("plastic_block");
        public static final TagKey<DamageType> SECURITY_STATION = modTag("security_station");
        public static final TagKey<DamageType> MINIGUN = modTag("minigun");

        static TagKey<DamageType> tag(String modid, String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(modid, name));
        }

        static TagKey<DamageType> modTag(String name) {
            return tag(Names.MOD_ID, name);
        }
    }
}
