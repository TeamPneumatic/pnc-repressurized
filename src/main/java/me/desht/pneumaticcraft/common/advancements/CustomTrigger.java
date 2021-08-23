package me.desht.pneumaticcraft.common.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CustomTrigger extends AbstractCriterionTrigger<CustomTrigger.Instance> {
    private final ResourceLocation triggerID;

    public CustomTrigger(String parString) {
        this(RL(parString));
    }

    public CustomTrigger(ResourceLocation parRL) {
        super();
        triggerID = parRL;
    }

    public void trigger(ServerPlayerEntity parPlayer) {
        this.trigger(parPlayer, Instance::test);
    }

    @Override
    public ResourceLocation getId() {
        return triggerID;
    }

    @Override
    protected Instance createInstance(JsonObject jsonIn, EntityPredicate.AndPredicate entityPredicateIn, ConditionArrayParser conditionsParserIn) {
        return new CustomTrigger.Instance(this.getId());
    }

    public Instance getInstance() {
        return new CustomTrigger.Instance(this.getId());
    }

    public static class Instance extends CriterionInstance {
        public Instance(ResourceLocation parID) {
            super(parID, EntityPredicate.AndPredicate.ANY);
        }

        public boolean test() {
            return true;
        }
    }
}