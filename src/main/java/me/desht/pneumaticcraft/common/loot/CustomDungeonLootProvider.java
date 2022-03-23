package me.desht.pneumaticcraft.common.loot;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CustomDungeonLootProvider extends ChestLoot {
    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        LootPool.Builder commonPool = LootPool.lootPool();
        commonPool.setRolls(ConstantValue.exactly(3))
                .add(createEntry(ModItems.COMPRESSED_IRON_INGOT.get(), 10,1, 3))
                .add(createEntry(ModBlocks.COMPRESSED_STONE.get(), 10,5, 10))
                .add(createEntry(ModItems.LOGISTICS_CORE.get(), 3,1, 1))
                .add(createEntry(ModBlocks.PRESSURE_TUBE.get(), 3,8, 8))
                .add(EmptyLootItem.emptyItem().setWeight(20));
        LootTable.Builder commonTable = LootTable.lootTable();
        commonTable.withPool(commonPool);
        consumer.accept(RL("custom/common_dungeon_loot"), commonTable);

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
        consumer.accept(RL("custom/uncommon_dungeon_loot"), uncommonTable);

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
        consumer.accept(RL("custom/rare_dungeon_loot"), rareTable);
    }

    private LootPoolEntryContainer.Builder<?> ammo(ItemLike item) {
        return createEntry(new ItemStack(item), 1)
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                .apply(EnchantRandomlyFunction.randomApplicableEnchantment());
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
