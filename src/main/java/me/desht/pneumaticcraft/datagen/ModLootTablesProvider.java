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
import me.desht.pneumaticcraft.common.block.EntityBlockPneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.datagen.loot.TileEntitySerializerFunction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(
                Pair.of(BlockLootTablePNC::new, LootContextParamSets.BLOCK),
                Pair.of(ChestLootTablePNC::new, LootContextParamSets.CHEST)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationresults) {
        // ...
    }

    private static class BlockLootTablePNC extends BlockLoot {
        @Override
        protected void addTables() {
            for (RegistryObject<Block> ro: ModBlocks.BLOCKS.getEntries()) {
                Block b = ro.get();
                if (b instanceof EntityBlockPneumaticCraft
//                        && b.hasTileEntity(b.defaultBlockState())
                        && ForgeRegistries.ITEMS.containsKey(b.getRegistryName())) {
                    addStandardSerializedDrop(b);
                } else if (b == ModBlocks.REINFORCED_BRICK_SLAB.get() || b == ModBlocks.REINFORCED_STONE_SLAB.get() || b == ModBlocks.COMPRESSED_BRICK_SLAB.get() || b == ModBlocks.COMPRESSED_STONE_SLAB.get()) {
                    add(b, BlockLoot::createSlabItemTable);
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
                    .when(ExplosionCondition.survivesExplosion())
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(block)
                            .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                            .apply(TileEntitySerializerFunction.builder()));
            add(block, LootTable.lootTable().withPool(builder));
        }

    }

    @Override
    public String getName() {
        return "PneumaticCraft Loot Tables";
    }

    private static class ChestLootTablePNC extends ChestLoot {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            LootPool.Builder lootPool = LootPool.lootPool();
            lootPool.setRolls(ConstantValue.exactly(4))
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

        private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item, int weight, int min, int max)
        {
            return createEntry(new ItemStack(item), weight)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        }

        private LootPoolSingletonContainer.Builder<?> createEntry(ItemStack item, int weight)
        {
            LootPoolSingletonContainer.Builder<?> ret = LootItem.lootTableItem(item.getItem()).setWeight(weight);
            if(item.hasTag())
                ret.apply(SetNbtFunction.setTag(item.getOrCreateTag()));
            return ret;
        }
    }
}
