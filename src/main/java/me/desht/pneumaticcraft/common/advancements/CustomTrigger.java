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
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CustomTrigger extends SimpleCriterionTrigger<CustomTrigger.Instance> {
    private final ResourceLocation triggerID;

    public CustomTrigger(String parString) {
        this(RL(parString));
    }

    public CustomTrigger(ResourceLocation parRL) {
        super();
        triggerID = parRL;
    }

    public void trigger(ServerPlayer parPlayer) {
        this.trigger(parPlayer, Instance::test);
    }

    @Override
    public ResourceLocation getId() {
        return triggerID;
    }

    @Override
    protected Instance createInstance(JsonObject jsonIn, ContextAwarePredicate predicate, DeserializationContext context) {
        return new CustomTrigger.Instance(this.getId());
    }

    public Instance getInstance() {
        return new CustomTrigger.Instance(this.getId());
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ResourceLocation parID) {
            super(parID, ContextAwarePredicate.ANY);
        }

        public boolean test() {
            return true;
        }
    }
}