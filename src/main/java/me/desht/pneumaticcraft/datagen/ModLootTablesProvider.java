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

import me.desht.pneumaticcraft.common.block.PneumaticCraftEntityBlock;
import me.desht.pneumaticcraft.common.block.SerializableComponentsProvider;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModLootTablesProvider extends LootTableProvider {

    public ModLootTablesProvider(DataGenerator dataGeneratorIn, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(dataGeneratorIn.getPackOutput(), Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLootTablePNC::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(MechanicVillagerChestLootProvider::new, LootContextParamSets.CHEST),
                new LootTableProvider.SubProviderEntry(CustomDungeonLootProvider::new, LootContextParamSets.CHEST)
        ), lookupProvider);
    }

    @Override
    protected void validate(WritableRegistry<LootTable> writableregistry, ValidationContext validationcontext, ProblemReporter.Collector problemreporter$collector) {
        // TODO
    }

    private static ResourceKey<LootTable> lootResourceKey(String id) {
        return ResourceKey.create(Registries.LOOT_TABLE, RL(id));
    }

    private static class BlockLootTablePNC extends BlockLootSubProvider {
        public BlockLootTablePNC(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, provider);
        }

        @Override
        protected void generate() {
            for (var holder: ModBlocks.BLOCKS.getEntries()) {
                Block b = holder.get();
                if (b instanceof PneumaticCraftEntityBlock && BuiltInRegistries.ITEM.containsKey(holder.getId())) {
                    addStandardSerializedDrop(b, holder.getId());
                } else if (b instanceof SlabBlock) {
                    add(b, this::createSlabItemTable);
                } else if (b.asItem() != Items.AIR) {
                    dropSelf(b);
                }
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> l = new ArrayList<>();
            for (var holder: ModBlocks.BLOCKS.getEntries()) {
                if (BuiltInRegistries.ITEM.containsKey(holder.getId())) {
                    l.add(holder.get());
                }
            }
            return l;
        }

        private void addStandardSerializedDrop(Block block, ResourceLocation blockId) {
            LootPoolSingletonContainer.Builder<?> lootBuilder = LootItem.lootTableItem(block)
                    .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));

            if (block instanceof SerializableComponentsProvider scp) {
                List<DataComponentType<?>> components = new ArrayList<>();
                scp.addSerializableComponents(components);
                if (!components.isEmpty()) {
                    CopyComponentsFunction.Builder compBuilder = CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY);
                    components.forEach(compBuilder::include);
                    lootBuilder.apply(compBuilder);
                }
            }

            LootPool.Builder builder = LootPool.lootPool()
                    .name(blockId.getPath())
                    .when(ExplosionCondition.survivesExplosion())
                    .setRolls(ConstantValue.exactly(1))
                    .add(lootBuilder);
            add(block, LootTable.lootTable().withPool(builder));
        }

    }

    public record MechanicVillagerChestLootProvider(HolderLookup.Provider provider) implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> builder) {
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
            builder.accept(lootResourceKey("chests/mechanic_house"), lootTable);
        }

        private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item, int weight, int min, int max) {
            return createEntry(new ItemStack(item), weight)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        }

        private LootPoolSingletonContainer.Builder<?> createEntry(ItemStack item, int weight) {
            LootPoolSingletonContainer.Builder<?> ret = LootItem.lootTableItem(item.getItem()).setWeight(weight);

//            if (item.hasTag())
//                ret.apply(SetNbtFunction.setTag(item.getOrCreateTag()));
            return ret;
        }
    }

    private record CustomDungeonLootProvider(HolderLookup.Provider provider) implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> builder) {
            LootPool.Builder commonPool = LootPool.lootPool();
            commonPool.setRolls(ConstantValue.exactly(3))
                    .add(createEntry(ModItems.COMPRESSED_IRON_INGOT.get(), 10,1, 3))
                    .add(createEntry(ModBlocks.COMPRESSED_STONE.get(), 10,5, 10))
                    .add(createEntry(ModItems.LOGISTICS_CORE.get(), 3,1, 1))
                    .add(createEntry(ModBlocks.PRESSURE_TUBE.get(), 3,8, 8))
                    .add(EmptyLootItem.emptyItem().setWeight(20));
            LootTable.Builder commonTable = LootTable.lootTable();
            commonTable.withPool(commonPool);
            builder.accept(lootResourceKey("custom/common_dungeon_loot"), commonTable);

            LootPool.Builder uncommonPool = LootPool.lootPool();
            uncommonPool.setRolls(ConstantValue.exactly(2))
                    .add(createEntry(ModItems.VORTEX_CANNON.get(), 1,1, 1))
                    .add(createEntry(ModItems.SPAWNER_AGITATOR.get(), 1,1, 1))
                    .add(createEntry(ModItems.COMPRESSED_IRON_BOOTS.get(), 1,1, 1))
                    .add(createEntry(ModItems.COMPRESSED_IRON_LEGGINGS.get(), 1,1, 1))
                    .add(createEntry(ModItems.COMPRESSED_IRON_CHESTPLATE.get(), 1,1, 1))
                    .add(createEntry(ModItems.COMPRESSED_IRON_HELMET.get(), 1,1, 1))
                    .add(createEntry(ModItems.TRANSISTOR.get(), 1,1, 4))
                    .add(createEntry(ModItems.CAPACITOR.get(), 1,1, 4))
                    .add(createEntry(ModItems.PNEUMATIC_CYLINDER.get(), 1,2, 3))
                    .add(EmptyLootItem.emptyItem().setWeight(10));
            LootTable.Builder uncommonTable = LootTable.lootTable();
            uncommonTable.withPool(uncommonPool);
            builder.accept(lootResourceKey("custom/uncommon_dungeon_loot"), uncommonTable);

            LootPool.Builder rarePool = LootPool.lootPool();
            rarePool.setRolls(ConstantValue.exactly(1))
                    .add(createEntry(ModItems.STOP_WORM.get(), 1,1, 1))
                    .add(createEntry(ModItems.NUKE_VIRUS.get(), 1,1, 1))
                    .add(createEntry(ModItems.GUN_AMMO_AP.get(), 1,1, 1))
                    .add(ammo(ModItems.GUN_AMMO_FREEZING.get()))
                    .add(ammo(ModItems.GUN_AMMO_WEIGHTED.get()))
                    .add(ammo(ModItems.GUN_AMMO_INCENDIARY.get()))
                    .add(ammo(ModItems.GUN_AMMO_EXPLOSIVE.get()))
                    .add(createEntry(ModItems.PROGRAMMING_PUZZLE.get(), 1,4, 12))
                    .add(createEntry(ModItems.MICROMISSILES.get(), 1,1, 1))
                    .add(EmptyLootItem.emptyItem().setWeight(20));
            LootTable.Builder rareTable = LootTable.lootTable();
            rareTable.withPool(rarePool);
            builder.accept(lootResourceKey("custom/rare_dungeon_loot"), rareTable);
        }

        private LootPoolEntryContainer.Builder<?> ammo(ItemLike item) {
            return createEntry(new ItemStack(item), 1)
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                    .apply(EnchantRandomlyFunction.randomApplicableEnchantment(provider));
        }

        private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item, int weight, int min, int max) {
            return createEntry(new ItemStack(item), weight)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        }

        private LootPoolSingletonContainer.Builder<?> createEntry(ItemStack item, int weight) {
            LootPoolSingletonContainer.Builder<?> ret = LootItem.lootTableItem(item.getItem()).setWeight(weight);
//            if (item.hasTag())
//                ret.apply(SetNbtFunction.setTag(item.getOrCreateTag()));
            return ret;
        }
    }
}
