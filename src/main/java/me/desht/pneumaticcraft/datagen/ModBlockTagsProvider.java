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

package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), lookupProvider, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        createAndAppend(PneumaticCraftTags.Blocks.REINFORCED_STONE, Tags.Blocks.STONES,
                ModBlocks.REINFORCED_STONE);
        createAndAppend(PneumaticCraftTags.Blocks.REINFORCED_STONE_BRICKS, BlockTags.STONE_BRICKS,
                ModBlocks.REINFORCED_BRICKS);
        createAndAppend(PneumaticCraftTags.Blocks.WALLS, BlockTags.WALLS,
                ModBlocks.REINFORCED_BRICK_WALL);
        createAndAppend(PneumaticCraftTags.Blocks.SLABS, BlockTags.SLABS,
                ModBlocks.REINFORCED_BRICK_SLAB, ModBlocks.REINFORCED_STONE_SLAB);
        createAndAppend(PneumaticCraftTags.Blocks.STAIRS, BlockTags.STAIRS,
                ModBlocks.REINFORCED_BRICK_STAIRS);

        createAndAppend(PneumaticCraftTags.Blocks.COMPRESSED_STONE, Tags.Blocks.STONES,
                ModBlocks.COMPRESSED_STONE);
        createAndAppend(PneumaticCraftTags.Blocks.COMPRESSED_STONE_BRICKS, BlockTags.STONE_BRICKS,
                ModBlocks.COMPRESSED_BRICKS);
        createAndAppend(PneumaticCraftTags.Blocks.WALLS, BlockTags.WALLS,
                ModBlocks.COMPRESSED_BRICK_WALL);
        createAndAppend(PneumaticCraftTags.Blocks.SLABS, BlockTags.SLABS,
                ModBlocks.COMPRESSED_BRICK_SLAB, ModBlocks.COMPRESSED_STONE_SLAB);
        createAndAppend(PneumaticCraftTags.Blocks.STAIRS, BlockTags.STAIRS,
                ModBlocks.COMPRESSED_BRICK_STAIRS);

        createAndAppend(PneumaticCraftTags.Blocks.DOORS, BlockTags.DOORS,
                ModBlocks.PNEUMATIC_DOOR);
        createAndAppend(PneumaticCraftTags.Blocks.CHESTS, Tags.Blocks.CHESTS,
                ModBlocks.SMART_CHEST, ModBlocks.REINFORCED_CHEST);
        createAndAppend(PneumaticCraftTags.Blocks.STORAGE_BLOCKS_COMPRESSED_IRON, Tags.Blocks.STORAGE_BLOCKS,
                ModBlocks.COMPRESSED_IRON_BLOCK);

        createTag(PneumaticCraftTags.Blocks.PLASTIC_BRICKS, ModBlocks.PLASTIC_BRICKS.toArray(new Supplier[0]));
        createTag(PneumaticCraftTags.Blocks.SMOOTH_PLASTIC_BRICKS, ModBlocks.SMOOTH_PLASTIC_BRICKS.toArray(new Supplier[0]));
        createTag(PneumaticCraftTags.Blocks.WALL_LAMPS, ModBlocks.WALL_LAMPS.toArray(new Supplier[0]));
        createTag(PneumaticCraftTags.Blocks.WALL_LAMPS_INVERTED, ModBlocks.WALL_LAMPS_INVERTED.toArray(new Supplier[0]));
        createTag(PneumaticCraftTags.Blocks.FLUID_TANKS, ModBlocks.TANK_SMALL, ModBlocks.TANK_MEDIUM, ModBlocks.TANK_LARGE, ModBlocks.TANK_HUGE);

        createTag(PneumaticCraftTags.Blocks.BLOCK_TRACKER_MISC,
                () -> Blocks.TNT,
                () -> Blocks.TRIPWIRE,
                () -> Blocks.BEE_NEST,
                () -> Blocks.INFESTED_CHISELED_STONE_BRICKS, () -> Blocks.INFESTED_CRACKED_STONE_BRICKS,
                () -> Blocks.INFESTED_COBBLESTONE, () -> Blocks.INFESTED_STONE,
                () -> Blocks.INFESTED_MOSSY_STONE_BRICKS, () -> Blocks.INFESTED_STONE_BRICKS
        );

        createTag(PneumaticCraftTags.Blocks.PROBE_TARGET);

        tag(Tags.Blocks.ORES);
        tag(BlockTags.LOGS);
        tag(PneumaticCraftTags.Blocks.JACKHAMMER_ORES)
                .addTag(Tags.Blocks.ORES)
                .addTag(BlockTags.LOGS);
        tag(PneumaticCraftTags.Blocks.ELECTROSTATIC_GRID)
                .add(Blocks.IRON_BARS);
        tag(PneumaticCraftTags.Blocks.CROP_SUPPORT_GROWABLE)
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.SAPLINGS)
                .addTag(BlockTags.BAMBOO_BLOCKS)
                .add(Blocks.RED_MUSHROOM)
                .add(Blocks.BROWN_MUSHROOM)
                .add(Blocks.NETHER_WART)
                .add(Blocks.SWEET_BERRY_BUSH);

        ModBlocks.BLOCKS.getEntries().forEach(ro -> {
            Block block = ro.get();
            if (!(block instanceof LiquidBlock) && !(block instanceof AirBlock)) {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
                tag(BlockTags.NEEDS_IRON_TOOL).add(block);
            }
        });
    }

// with thanks to Tropicraft for these helper methods

    @SafeVarargs
    private <T> T[] resolveAll(IntFunction<T[]> creator, Supplier<? extends T>... suppliers) {
        return Arrays.stream(suppliers).map(Supplier::get).toArray(creator);
    }

    @SafeVarargs
    private void createTag(TagKey<Block> tag, Supplier<? extends Block>... blocks) {
        tag(tag).add(resolveAll(Block[]::new, blocks));
    }

    @SafeVarargs
    private void appendToTag(TagKey<Block> tag, TagKey<Block>... toAppend) {
        tag(tag).addTags(toAppend);
    }

    @SafeVarargs
    private void createAndAppend(TagKey<Block> tag, TagKey<Block> to, Supplier<? extends Block>... blocks) {
        createTag(tag, blocks);
        appendToTag(to, tag);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Block Tags";
    }

}
