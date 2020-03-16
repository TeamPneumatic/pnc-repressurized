package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerTags() {
        createAndAppend(PneumaticCraftTags.Blocks.WALLS, BlockTags.WALLS,
                ModBlocks.REINFORCED_BRICK_WALL);
        createAndAppend(PneumaticCraftTags.Blocks.SLABS, BlockTags.SLABS,
                ModBlocks.REINFORCED_BRICK_SLAB, ModBlocks.REINFORCED_STONE_SLAB);
        createAndAppend(PneumaticCraftTags.Blocks.STAIRS, BlockTags.STAIRS,
                ModBlocks.REINFORCED_BRICK_STAIRS);
        createAndAppend(PneumaticCraftTags.Blocks.DOORS, BlockTags.DOORS,
                ModBlocks.PNEUMATIC_DOOR);
        createAndAppend(PneumaticCraftTags.Blocks.CHESTS, Tags.Blocks.CHESTS,
                ModBlocks.SMART_CHEST, ModBlocks.REINFORCED_CHEST);
        createAndAppend(PneumaticCraftTags.Blocks.STORAGE_BLOCKS_COMPRESSED_IRON, Tags.Blocks.STORAGE_BLOCKS,
                ModBlocks.COMPRESSED_IRON_BLOCK);

        createTag(PneumaticCraftTags.Blocks.PLASTIC_BRICKS, ModBlocks.PLASTIC_BRICKS.toArray(new Supplier[0]));
        createTag(PneumaticCraftTags.Blocks.FLUID_TANKS, ModBlocks.TANK_SMALL, ModBlocks.TANK_MEDIUM, ModBlocks.TANK_LARGE);
    }

    // with thanks to Tropicraft for these helper methods

    @SafeVarargs
    private final <T> T[] resolveAll(IntFunction<T[]> creator, Supplier<? extends T>... suppliers) {
        return Arrays.stream(suppliers).map(Supplier::get).toArray(creator);
    }

    @SafeVarargs
    private final void createTag(Tag<Block> tag, Supplier<? extends Block>... blocks) {
        getBuilder(tag).add(resolveAll(Block[]::new, blocks));
    }

    @SafeVarargs
    private final void appendToTag(Tag<Block> tag, Tag<Block>... toAppend) {
        getBuilder(tag).add(toAppend);
    }

    @SafeVarargs
    private final void createAndAppend(Tag<Block> tag, Tag<Block> to, Supplier<? extends Block>... blocks) {
        createTag(tag, blocks);
        appendToTag(to, tag);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Block Tags";
    }
}
