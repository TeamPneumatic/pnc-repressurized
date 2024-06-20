package me.desht.pneumaticcraft.common.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PNCDungeonLootModifier extends LootModifier {
    public static final Supplier<MapCodec<PNCDungeonLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, PNCDungeonLootModifier::new))
    );

    public PNCDungeonLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (ConfigHelper.common().general.enableDungeonLoot.get()) {
            generatedLoot.addAll(CustomPools.roll(context));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }

    private static class CustomPools {
        private static LootPool commonPool = null;
        private static LootPool uncommonPool = null;
        private static LootPool rarePool = null;

        private static List<ItemStack> roll(LootContext ctx) {
            if (commonPool == null) {
                commonPool = buildLootPool("common");
                uncommonPool = buildLootPool("uncommon");
                rarePool = buildLootPool("rare");
            }
            List<ItemStack> res = new ArrayList<>();
            commonPool.addRandomItems(res::add, ctx);
            uncommonPool.addRandomItems(res::add, ctx);
            rarePool.addRandomItems(res::add, ctx);
            return res;
        }

        private static LootPool buildLootPool(String name) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, RL("custom/" + name + "_dungeon_loot"));
            return LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(key).setWeight(1))
                    .name("pneumaticcraft_custom_" + name)
                    .build();
        }
    }
}
