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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.datagen.loot.TileEntitySerializerFunction;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.data.loot.ChestLootTables;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyName;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.loot.functions.SetNBT;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModLootTablesProvider extends LootTableProvider {

    public ModLootTablesProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(
                Pair.of(BlockLootTablePNC::new, LootParameterSets.BLOCK),
                Pair.of(ChestLootTablePNC::new, LootParameterSets.CHEST)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationresults) {
        // ...
    }

    private static class BlockLootTablePNC extends BlockLootTables {
        @Override
        protected void addTables() {
            for (RegistryObject<Block> ro: ModBlocks.BLOCKS.getEntries()) {
                Block b = ro.get();
                if (b instanceof BlockPneumaticCraft
                        && b.hasTileEntity(b.defaultBlockState())
                        && ForgeRegistries.ITEMS.containsKey(b.getRegistryName())) {
                    addStandardSerializedDrop(b);
                } else if (b == ModBlocks.REINFORCED_BRICK_SLAB.get() || b == ModBlocks.REINFORCED_STONE_SLAB.get() || b == ModBlocks.COMPRESSED_BRICK_SLAB.get() || b == ModBlocks.COMPRESSED_STONE_SLAB.get()) {
                    add(b, BlockLootTables::droppingSlab);
                } else if (b.asItem() != Items.AIR) {
                    dropSelf(b);
                }
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> l = new ArrayList<>();
            for (RegistryObject<Block> ro: ModBlocks.BLOCKS.getEntries()) {
                if (ForgeRegistries.ITEMS.containsKey(ro.get().getRegistryName())) {
                    l.add(ro.get());
                }
            }
            return l;
        }

        private void addStandardSerializedDrop(Block block) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(block.getRegistryName().getPath())
                    .when(SurvivesExplosion.survivesExplosion())
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(block)
                            .apply(CopyName.copyName(CopyName.Source.BLOCK_ENTITY))
                            .apply(TileEntitySerializerFunction.builder()));
            add(block, LootTable.lootTable().withPool(builder));
        }

    }

    @Override
    public String getName() {
        return "PneumaticCraft Loot Tables";
    }

    private static class ChestLootTablePNC extends ChestLootTables {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            LootPool.Builder lootPool = LootPool.lootPool();
            lootPool.setRolls(new ConstantRange(4))
                    .add(createEntry(ModItems.COMPRESSED_IRON_INGOT.get(), 10, 4, 12))
                    .add(createEntry(ModItems.AMADRON_TABLET.get(), 2, 1, 1))
                    .add(createEntry(ModItems.AIR_CANISTER.get(), 10, 1, 5))
                    .add(createEntry(ModItems.PNEUMATIC_CYLINDER.get(), 5, 2, 4))
                    .add(createEntry(ModItems.LOGISTICS_CORE.get(), 8, 4, 8))
                    .add(createEntry(ModItems.CAPACITOR.get(), 4, 4, 8))
                    .add(createEntry(ModItems.TRANSISTOR.get(), 4, 4, 8))
                    .add(createEntry(ModItems.TURBINE_ROTOR.get(), 5, 2, 4))
                    .add(createEntry(ModBlocks.COMPRESSED_IRON_BLOCK.get(), 2, 1, 2))
                    .add(createEntry(ModBlocks.VORTEX_TUBE.get(), 5, 1, 1))
                    .add(createEntry(ModBlocks.PRESSURE_TUBE.get(), 10, 3, 8))
                    .add(createEntry(ModBlocks.ADVANCED_PRESSURE_TUBE.get(), 4, 3, 8))
                    .add(createEntry(ModBlocks.HEAT_PIPE.get(), 8, 3, 8))
                    .add(createEntry(ModBlocks.APHORISM_TILE.get(), 5, 2, 3));

            LootTable.Builder lootTable = LootTable.lootTable();
            lootTable.withPool(lootPool);
            consumer.accept(RL("chests/mechanic_house"), lootTable);
        }

        private LootEntry.Builder<?> createEntry(IItemProvider item, int weight, int min, int max)
        {
            return createEntry(new ItemStack(item), weight)
                    .apply(SetCount.setCount(new RandomValueRange(min, max)));
        }

        private StandaloneLootEntry.Builder<?> createEntry(ItemStack item, int weight)
        {
            StandaloneLootEntry.Builder<?> ret = ItemLootEntry.lootTableItem(item.getItem()).setWeight(weight);
            if(item.hasTag())
                ret.apply(SetNBT.setTag(item.getOrCreateTag()));
            return ret;
        }
    }
}
