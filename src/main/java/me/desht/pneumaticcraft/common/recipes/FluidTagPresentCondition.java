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
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class FluidTagPresentCondition implements ICondition {
    private static final ResourceLocation NAME = RL("fluid_tag_present");

    private final ResourceLocation tagName;

    public FluidTagPresentCondition(ResourceLocation tagName) {
        this.tagName = tagName;
    }

    public FluidTagPresentCondition(String tagName) {
        this(new ResourceLocation(tagName));
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test() {
        ITag<Fluid> tag = TagCollectionManager.getInstance().getFluids().getTag(tagName);
        return tag != null && !tag.getValues().isEmpty();
    }

    public static class Serializer implements IConditionSerializer<FluidTagPresentCondition> {
        public static final FluidTagPresentCondition.Serializer INSTANCE = new FluidTagPresentCondition.Serializer();

        @Override
        public void write(JsonObject json, FluidTagPresentCondition value) {
            json.addProperty("tag", value.tagName.toString());
        }

        @Override
        public FluidTagPresentCondition read(JsonObject json) {
            return new FluidTagPresentCondition(new ResourceLocation(JSONUtils.getAsString(json, "tag")));
        }

        @Override
        public ResourceLocation getID() {
            return FluidTagPresentCondition.NAME;
        }
    }
}
