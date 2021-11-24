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