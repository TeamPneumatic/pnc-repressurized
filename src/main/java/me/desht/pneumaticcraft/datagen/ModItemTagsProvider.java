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
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagsProvider, ExistingFileHelper existingFileHelper) {
        super(generatorIn.getPackOutput(), lookupProvider, blockTagsProvider, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        copy(PneumaticCraftTags.Blocks.SLABS, PneumaticCraftTags.Items.SLABS);
        copy(PneumaticCraftTags.Blocks.STAIRS, PneumaticCraftTags.Items.STAIRS);
        copy(PneumaticCraftTags.Blocks.WALLS, PneumaticCraftTags.Items.WALLS);
        copy(PneumaticCraftTags.Blocks.DOORS, PneumaticCraftTags.Items.DOORS);
        copy(PneumaticCraftTags.Blocks.STORAGE_BLOCKS_COMPRESSED_IRON, PneumaticCraftTags.Items.STORAGE_BLOCKS_COMPRESSED_IRON);
        copy(PneumaticCraftTags.Blocks.PLASTIC_BRICKS, PneumaticCraftTags.Items.PLASTIC_BRICKS);
        copy(PneumaticCraftTags.Blocks.SMOOTH_PLASTIC_BRICKS, PneumaticCraftTags.Items.SMOOTH_PLASTIC_BRICKS);
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
        appendToTag(Tags.Items.STONES, PneumaticCraftTags.Items.REINFORCED_STONE, PneumaticCraftTags.Items.COMPRESSED_STONE);
        appendToTag(ItemTags.STONE_BRICKS, PneumaticCraftTags.Items.REINFORCED_STONE_BRICKS, PneumaticCraftTags.Items.COMPRESSED_STONE_BRICKS);

        addItemsToTag(PneumaticCraftTags.Items.PLASTIC_SHEETS, ModItems.PLASTIC);
        addItemsToTag(PneumaticCraftTags.Items.FLOUR, ModItems.WHEAT_FLOUR);
        addItemsToTag(Tags.Items.NUGGETS, ModItems.COPPER_NUGGET);
        addItemsToTag(PneumaticCraftTags.Items.NUGGETS_COPPER, ModItems.COPPER_NUGGET);
        addItemsToTag(ItemTags.DYEABLE,
                ModItems.PNEUMATIC_HELMET, ModItems.PNEUMATIC_CHESTPLATE, ModItems.PNEUMATIC_LEGGINGS, ModItems.PNEUMATIC_BOOTS
        );

        addItemsToTag(ItemTags.HEAD_ARMOR, ModItems.COMPRESSED_IRON_HELMET, ModItems.PNEUMATIC_HELMET);
        addItemsToTag(ItemTags.CHEST_ARMOR, ModItems.COMPRESSED_IRON_CHESTPLATE, ModItems.PNEUMATIC_CHESTPLATE);
        addItemsToTag(ItemTags.LEG_ARMOR, ModItems.COMPRESSED_IRON_LEGGINGS, ModItems.PNEUMATIC_LEGGINGS);
        addItemsToTag(ItemTags.FOOT_ARMOR, ModItems.COMPRESSED_IRON_BOOTS, ModItems.PNEUMATIC_BOOTS);

        appendToTag(PneumaticCraftTags.Items.WIRING, Tags.Items.NUGGETS_GOLD);
        appendToTag(PneumaticCraftTags.Items.WIRING, PneumaticCraftTags.Items.NUGGETS_COPPER);

        addItemsToTag(PneumaticCraftTags.Items.UPGRADE_COMPONENTS, ModItems.UPGRADE_MATRIX, () -> Items.LAPIS_LAZULI);

        addItemsToTag(PneumaticCraftTags.Items.BASIC_DRONES,
                ModItems.LOGISTICS_DRONE, ModItems.GUARD_DRONE, ModItems.HARVESTING_DRONE, ModItems.COLLECTOR_DRONE
        );

        addItemsToTag(PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON, ModItems.COMPRESSED_IRON_INGOT);

        appendToTag(Tags.Items.INGOTS, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON);

        addItemsToTag(PneumaticCraftTags.Items.CURIO, ModItems.MEMORY_STICK);

        addItemsToTag(Tags.Items.FOODS_BREAD, ModItems.SOURDOUGH_BREAD);

        addItemsToTag(PneumaticCraftTags.Items.WRENCHES, ModItems.PNEUMATIC_WRENCH);

        addItemsToTag(PneumaticCraftTags.Items.GEARS, ModItems.COMPRESSED_IRON_GEAR);
        addItemsToTag(PneumaticCraftTags.Items.GEARS_COMPRESSED_IRON, ModItems.COMPRESSED_IRON_GEAR);

        addItemsToTag(ItemTags.FREEZE_IMMUNE_WEARABLES,
                ModItems.COMPRESSED_IRON_HELMET, ModItems.COMPRESSED_IRON_CHESTPLATE,
                ModItems.COMPRESSED_IRON_LEGGINGS, ModItems.COMPRESSED_IRON_BOOTS,
                ModItems.PNEUMATIC_HELMET, ModItems.PNEUMATIC_CHESTPLATE,
                ModItems.PNEUMATIC_LEGGINGS, ModItems.PNEUMATIC_BOOTS
        );
    }

    @SafeVarargs
    private void addItemsToTag(TagKey<Item> tag, Supplier<? extends ItemLike>... items) {
        tag(tag).add(Arrays.stream(items).map(Supplier::get).map(ItemLike::asItem).toArray(Item[]::new));
    }

    @SafeVarargs
    private void appendToTag(TagKey<Item> tag, TagKey<Item>... toAppend) {
        tag(tag).addTags(toAppend);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Item Tags";
    }

}
