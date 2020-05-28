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
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraft.world.storage.loot.functions.CopyName;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetNBT;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

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
                        && b.hasTileEntity(b.getDefaultState())
                        && ForgeRegistries.ITEMS.containsKey(b.getRegistryName())) {
                    addStandardSerializedDrop(b);
                } else if (b == ModBlocks.REINFORCED_BRICK_SLAB.get() || b == ModBlocks.REINFORCED_STONE_SLAB.get()) {
                    registerLootTable(b, BlockLootTables::droppingSlab);
                } else if (b.asItem() != Items.AIR) {
                    registerDropSelfLootTable(b);
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
            LootPool.Builder builder = LootPool.builder()
                    .name(block.getRegistryName().getPath())
                    .acceptCondition(SurvivesExplosion.builder())
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(block)
                            .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                            .acceptFunction(TileEntitySerializerFunction.builder()));
            registerLootTable(block, LootTable.builder().addLootPool(builder));
        }

    }

    @Override
    public String getName() {
        return "PneumaticCraft Loot Tables";
    }

    private static class ChestLootTablePNC extends ChestLootTables {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            LootPool.Builder lootPool = LootPool.builder();
            lootPool.rolls(new ConstantRange(4))
                    .addEntry(createEntry(ModItems.COMPRESSED_IRON_INGOT.get(), 10, 4, 12))
                    .addEntry(createEntry(ModItems.AMADRON_TABLET.get(), 2, 1, 1))
                    .addEntry(createEntry(ModItems.AIR_CANISTER.get(), 10, 1, 5))
                    .addEntry(createEntry(ModItems.PNEUMATIC_CYLINDER.get(), 5, 2, 4))
                    .addEntry(createEntry(ModItems.LOGISTICS_CORE.get(), 8, 4, 8))
                    .addEntry(createEntry(ModItems.CAPACITOR.get(), 4, 4, 8))
                    .addEntry(createEntry(ModItems.TRANSISTOR.get(), 4, 4, 8))
                    .addEntry(createEntry(ModItems.TURBINE_ROTOR.get(), 5, 2, 4))
                    .addEntry(createEntry(ModBlocks.COMPRESSED_IRON_BLOCK.get(), 2, 1, 2))
                    .addEntry(createEntry(ModBlocks.VORTEX_TUBE.get(), 5, 1, 1))
                    .addEntry(createEntry(ModBlocks.PRESSURE_TUBE.get(), 10, 3, 8))
                    .addEntry(createEntry(ModBlocks.ADVANCED_PRESSURE_TUBE.get(), 4, 3, 8))
                    .addEntry(createEntry(ModBlocks.HEAT_PIPE.get(), 8, 3, 8))
                    .addEntry(createEntry(ModBlocks.APHORISM_TILE.get(), 5, 2, 3));

            LootTable.Builder lootTable = LootTable.builder();
            lootTable.addLootPool(lootPool);
            consumer.accept(RL("chests/mechanic_house"), lootTable);
        }

        private LootEntry.Builder<?> createEntry(IItemProvider item, int weight, int min, int max)
        {
            return createEntry(new ItemStack(item), weight)
                    .acceptFunction(SetCount.builder(new RandomValueRange(min, max)));
        }

        private StandaloneLootEntry.Builder<?> createEntry(ItemStack item, int weight)
        {
            StandaloneLootEntry.Builder<?> ret = ItemLootEntry.builder(item.getItem()).weight(weight);
            if(item.hasTag())
                ret.acceptFunction(SetNBT.builder(item.getOrCreateTag()));
            return ret;
        }
    }
}
