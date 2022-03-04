package me.desht.pneumaticcraft.common.loot;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PNCDungeonLootModifier extends LootModifier {
    public double commonChance;
    public double uncommonChance;
    public double rareChance;

    public int commonRolls;
    public int uncommonRolls;
    public int rareRolls;

    public PNCDungeonLootModifier(LootItemCondition[] conditions) {
        super(conditions);

        this.commonChance = 0.3;
        this.uncommonChance = 0.2;
        this.rareChance =  0.1;

        this.commonRolls = 3;
        this.uncommonRolls = 2;
        this.rareRolls = 1;
    }

    public PNCDungeonLootModifier(LootItemCondition[] conditions, double commonChance, double uncommonChance, double rareChance, int commonRolls, int uncommonRolls, int rareRolls) {
        super(conditions);

        this.commonChance = commonChance;
        this.uncommonChance = uncommonChance;
        this.rareChance = rareChance;

        this.commonRolls = commonRolls;
        this.uncommonRolls = uncommonRolls;
        this.rareRolls = rareRolls;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.addAll(DungeonLootTable.getRandomRoll(this));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<PNCDungeonLootModifier> {
        @Override
        public PNCDungeonLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] lootConditions) {
            return new PNCDungeonLootModifier(lootConditions,
                    object.get("common_chance").getAsDouble(),
                    object.get("uncommon_chance").getAsDouble(),
                    object.get("rare_chance").getAsDouble(),
                    object.get("common_rolls").getAsInt(),
                    object.get("uncommon_rolls").getAsInt(),
                    object.get("rare_rolls").getAsInt()
            );
        }

        @Override
        public JsonObject write(PNCDungeonLootModifier instance) {
            JsonObject obj = this.makeConditions(instance.conditions);

            obj.addProperty("common_chance", instance.commonChance);
            obj.addProperty("uncommon_chance", instance.uncommonChance);
            obj.addProperty("rare_chance", instance.rareChance);
            obj.addProperty("common_rolls", instance.commonRolls);
            obj.addProperty("uncommon_rolls", instance.uncommonRolls);
            obj.addProperty("rare_rolls", instance.rareRolls);

            return obj;
        }
    }
}
