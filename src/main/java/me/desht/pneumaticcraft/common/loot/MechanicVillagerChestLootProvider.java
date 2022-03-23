package me.desht.pneumaticcraft.common.loot;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Added to the Mechanic Villager house chest
 */
public class MechanicVillagerChestLootProvider extends ChestLoot {
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

    private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item, int weight, int min, int max) {
        return createEntry(new ItemStack(item), weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private LootPoolSingletonContainer.Builder<?> createEntry(ItemStack item, int weight) {
        LootPoolSingletonContainer.Builder<?> ret = LootItem.lootTableItem(item.getItem()).setWeight(weight);
        if (item.hasTag())
            ret.apply(SetNbtFunction.setTag(item.getOrCreateTag()));
        return ret;
    }
}
