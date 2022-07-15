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

package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class FluidTagPresentCondition implements ICondition {
    private static final ResourceLocation NAME = RL("fluid_tag_present");

    private final TagKey<Fluid> tagKey;

    public FluidTagPresentCondition(ResourceLocation tagName) {
        this.tagKey = TagKey.create(Registry.FLUID_REGISTRY, tagName);
    }

    public FluidTagPresentCondition(String tagName) {
        this(new ResourceLocation(tagName));
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext context) {
        return !context.getTag(tagKey).isEmpty();
    }

    public static class Serializer implements IConditionSerializer<FluidTagPresentCondition> {
        public static final FluidTagPresentCondition.Serializer INSTANCE = new FluidTagPresentCondition.Serializer();

        @Override
        public void write(JsonObject json, FluidTagPresentCondition value) {
            json.addProperty("tag", value.tagKey.location().toString());
        }

        @Override
        public FluidTagPresentCondition read(JsonObject json) {
            return new FluidTagPresentCondition(new ResourceLocation(GsonHelper.getAsString(json, "tag")));
        }

        @Override
        public ResourceLocation getID() {
            return FluidTagPresentCondition.NAME;
        }
    }
}
