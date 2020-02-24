package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;

import java.util.Arrays;
import java.util.function.Supplier;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerTags() {
        copy(PneumaticCraftTags.Blocks.SLABS, PneumaticCraftTags.Items.SLABS);
        copy(PneumaticCraftTags.Blocks.STAIRS, PneumaticCraftTags.Items.STAIRS);
        copy(PneumaticCraftTags.Blocks.WALLS, PneumaticCraftTags.Items.WALLS);
        copy(PneumaticCraftTags.Blocks.DOORS, PneumaticCraftTags.Items.DOORS);
        copy(PneumaticCraftTags.Blocks.STORAGE_BLOCKS_COMPRESSED_IRON, PneumaticCraftTags.Items.STORAGE_BLOCKS_COMPRESSED_IRON);
        copy(PneumaticCraftTags.Blocks.PLASTIC_BRICKS, PneumaticCraftTags.Items.PLASTIC_BRICKS);

        addItemsToTag(PneumaticCraftTags.Items.UPGRADE_COMPONENT, ModItems.UPGRADE_MATRIX, () -> Items.LAPIS_LAZULI);

        addItemsToTag(PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON, ModItems.COMPRESSED_IRON_INGOT);
        appendToTag(Tags.Items.INGOTS, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON);
    }

    @SafeVarargs
    private final void addItemsToTag(Tag<Item> tag, Supplier<? extends IItemProvider>... items) {
        getBuilder(tag).add(Arrays.stream(items).map(Supplier::get).map(IItemProvider::asItem).toArray(Item[]::new));
    }

    @SafeVarargs
    private final void appendToTag(Tag<Item> tag, Tag<Item>... toAppend) {
        getBuilder(tag).add(toAppend);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Item Tags";
    }
}
