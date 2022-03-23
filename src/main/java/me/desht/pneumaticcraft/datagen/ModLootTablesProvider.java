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
import me.desht.pneumaticcraft.common.block.PneumaticCraftEntityBlock;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.loot.CustomDungeonLootProvider;
import me.desht.pneumaticcraft.common.loot.MechanicVillagerChestLootProvider;
import me.desht.pneumaticcraft.common.loot.ModLootFunctions;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModLootTablesProvider extends LootTableProvider {

    public ModLootTablesProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(
                Pair.of(BlockLootTablePNC::new, LootContextParamSets.BLOCK),
                Pair.of(MechanicVillagerChestLootProvider::new, LootContextParamSets.CHEST),
                Pair.of(CustomDungeonLootProvider::new, LootContextParamSets.CHEST)
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
                if (b instanceof PneumaticCraftEntityBlock && ForgeRegistries.ITEMS.containsKey(b.getRegistryName())) {
                    addStandardSerializedDrop(b);
                } else if (b instanceof SlabBlock) {
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
                            .apply(ModLootFunctions.BlockEntitySerializerFunction.builder()));
            add(block, LootTable.lootTable().withPool(builder));
        }

    }

    @Override
    public String getName() {
        return "PneumaticCraft Loot Tables";
    }

}
