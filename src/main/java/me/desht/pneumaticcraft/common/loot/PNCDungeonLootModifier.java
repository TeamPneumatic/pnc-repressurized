package me.desht.pneumaticcraft.common.loot;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PNCDungeonLootModifier extends LootModifier {
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

    public static class Serializer extends GlobalLootModifierSerializer<PNCDungeonLootModifier> {
        @Override
        public PNCDungeonLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] lootConditions) {
            return new PNCDungeonLootModifier(lootConditions);
        }

        @Override
        public JsonObject write(PNCDungeonLootModifier instance) {
            return this.makeConditions(instance.conditions);
        }
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
            return LootPool.lootPool()
                    .add(LootTableReference.lootTableReference(RL("custom/" + name + "_dungeon_loot")).setWeight(1))
                    .name("pneumaticcraft_custom_" + name)
                    .build();
        }
    }
}
