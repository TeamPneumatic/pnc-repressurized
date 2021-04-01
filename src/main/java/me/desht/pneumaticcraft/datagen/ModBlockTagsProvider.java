package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Names.MOD_ID, existingFileHelper);
    }

    @Override
    public void addTags() {

        createAndAppend(PneumaticCraftTags.Blocks.REINFORCED_STONE, Tags.Blocks.STONE,
                ModBlocks.REINFORCED_STONE);
        createAndAppend(PneumaticCraftTags.Blocks.REINFORCED_STONE_BRICKS, BlockTags.STONE_BRICKS,
                ModBlocks.REINFORCED_BRICKS);
        createAndAppend(PneumaticCraftTags.Blocks.WALLS, BlockTags.WALLS,
                ModBlocks.REINFORCED_BRICK_WALL);
        createAndAppend(PneumaticCraftTags.Blocks.SLABS, BlockTags.SLABS,
                ModBlocks.REINFORCED_BRICK_SLAB, ModBlocks.REINFORCED_STONE_SLAB);
        createAndAppend(PneumaticCraftTags.Blocks.STAIRS, BlockTags.STAIRS,
                ModBlocks.REINFORCED_BRICK_STAIRS);

        createAndAppend(PneumaticCraftTags.Blocks.COMPRESSED_STONE, Tags.Blocks.STONE,
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
        createTag(PneumaticCraftTags.Blocks.WALL_LAMPS, ModBlocks.WALL_LAMPS.toArray(new Supplier[0]));
        createTag(PneumaticCraftTags.Blocks.WALL_LAMPS_INVERTED, ModBlocks.WALL_LAMPS_INVERTED.toArray(new Supplier[0]));
        createTag(PneumaticCraftTags.Blocks.FLUID_TANKS, ModBlocks.TANK_SMALL, ModBlocks.TANK_MEDIUM, ModBlocks.TANK_LARGE);

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
        tag(PneumaticCraftTags.Blocks.JACKHAMMER_ORES).addTag(Tags.Blocks.ORES).addTag(BlockTags.LOGS);
    }

// with thanks to Tropicraft for these helper methods

    @SafeVarargs
    private final <T> T[] resolveAll(IntFunction<T[]> creator, Supplier<? extends T>... suppliers) {
        return Arrays.stream(suppliers).map(Supplier::get).toArray(creator);
    }

    @SafeVarargs
    private final void createTag(ITag.INamedTag<Block> tag, Supplier<? extends Block>... blocks) {
        tag(tag).add(resolveAll(Block[]::new, blocks));
    }

    @SafeVarargs
    private final void appendToTag(ITag.INamedTag<Block> tag, ITag.INamedTag<Block>... toAppend) {
        tag(tag).addTags(toAppend);
    }

    @SafeVarargs
    private final void createAndAppend(ITag.INamedTag<Block> tag, ITag.INamedTag<Block> to, Supplier<? extends Block>... blocks) {
        createTag(tag, blocks);
        appendToTag(to, tag);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Block Tags";
    }
}
