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

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.function.Supplier;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generatorIn, BlockTagsProvider blockTagsProvider, ExistingFileHelper existingFileHelper) {
        super(generatorIn, blockTagsProvider, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        copy(PneumaticCraftTags.Blocks.SLABS, PneumaticCraftTags.Items.SLABS);
        copy(PneumaticCraftTags.Blocks.STAIRS, PneumaticCraftTags.Items.STAIRS);
        copy(PneumaticCraftTags.Blocks.WALLS, PneumaticCraftTags.Items.WALLS);
        copy(PneumaticCraftTags.Blocks.DOORS, PneumaticCraftTags.Items.DOORS);
        copy(PneumaticCraftTags.Blocks.STORAGE_BLOCKS_COMPRESSED_IRON, PneumaticCraftTags.Items.STORAGE_BLOCKS_COMPRESSED_IRON);
        copy(PneumaticCraftTags.Blocks.PLASTIC_BRICKS, PneumaticCraftTags.Items.PLASTIC_BRICKS);
        copy(PneumaticCraftTags.Blocks.WALL_LAMPS, PneumaticCraftTags.Items.WALL_LAMPS);
        copy(PneumaticCraftTags.Blocks.WALL_LAMPS_INVERTED, PneumaticCraftTags.Items.WALL_LAMPS_INVERTED);
        copy(PneumaticCraftTags.Blocks.FLUID_TANKS, PneumaticCraftTags.Items.FLUID_TANKS);
        copy(PneumaticCraftTags.Blocks.CHESTS, PneumaticCraftTags.Items.CHESTS);
        copy(PneumaticCraftTags.Blocks.REINFORCED_STONE, PneumaticCraftTags.Items.REINFORCED_STONE);
        copy(PneumaticCraftTags.Blocks.REINFORCED_STONE_BRICKS, PneumaticCraftTags.Items.REINFORCED_STONE_BRICKS);
        copy(PneumaticCraftTags.Blocks.COMPRESSED_STONE, PneumaticCraftTags.Items.COMPRESSED_STONE);
        copy(PneumaticCraftTags.Blocks.COMPRESSED_STONE_BRICKS, PneumaticCraftTags.Items.COMPRESSED_STONE_BRICKS);

        appendToTag(ItemTags.SLABS, PneumaticCraftTags.Items.SLABS);
        appendToTag(ItemTags.STAIRS, PneumaticCraftTags.Items.STAIRS);
        appendToTag(ItemTags.WALLS, PneumaticCraftTags.Items.WALLS);
        appendToTag(ItemTags.DOORS, PneumaticCraftTags.Items.DOORS);
        appendToTag(Tags.Items.STORAGE_BLOCKS, PneumaticCraftTags.Items.STORAGE_BLOCKS_COMPRESSED_IRON);
        appendToTag(Tags.Items.CHESTS, PneumaticCraftTags.Items.CHESTS);
        appendToTag(Tags.Items.STONE, PneumaticCraftTags.Items.REINFORCED_STONE);
        appendToTag(Tags.Items.STONE, PneumaticCraftTags.Items.COMPRESSED_STONE);
        appendToTag(ItemTags.STONE_BRICKS, PneumaticCraftTags.Items.REINFORCED_STONE_BRICKS);
        appendToTag(ItemTags.STONE_BRICKS, PneumaticCraftTags.Items.COMPRESSED_STONE_BRICKS);

        addItemsToTag(PneumaticCraftTags.Items.PLASTIC_SHEETS, ModItems.PLASTIC);
        addItemsToTag(PneumaticCraftTags.Items.FLOUR, ModItems.WHEAT_FLOUR);

        addItemsToTag(PneumaticCraftTags.Items.UPGRADE_COMPONENTS, ModItems.UPGRADE_MATRIX, () -> Items.LAPIS_LAZULI);

        addItemsToTag(PneumaticCraftTags.Items.BASIC_DRONES,
                ModItems.LOGISTICS_DRONE, ModItems.GUARD_DRONE, ModItems.HARVESTING_DRONE, ModItems.COLLECTOR_DRONE
        );

        addItemsToTag(PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON, ModItems.COMPRESSED_IRON_INGOT);

        appendToTag(Tags.Items.INGOTS, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON);

        addItemsToTag(PneumaticCraftTags.Items.CURIO, ModItems.MEMORY_STICK);

        addItemsToTag(PneumaticCraftTags.Items.BREAD, ModItems.SOURDOUGH_BREAD);

        addItemsToTag(PneumaticCraftTags.Items.WRENCHES, ModItems.PNEUMATIC_WRENCH);
    }

    @SafeVarargs
    private final void addItemsToTag(ITag.INamedTag<Item> tag, Supplier<? extends IItemProvider>... items) {
        tag(tag).add(Arrays.stream(items).map(Supplier::get).map(IItemProvider::asItem).toArray(Item[]::new));
    }

    @SafeVarargs
    private final void appendToTag(ITag.INamedTag<Item> tag, ITag.INamedTag<Item>... toAppend) {
        tag(tag).addTags(toAppend);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Item Tags";
    }
}
