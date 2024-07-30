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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;

public class FluidTagPresentCondition implements ICondition {
    public static final MapCodec<FluidTagPresentCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(FluidTagPresentCondition::location)
            ).apply(builder, FluidTagPresentCondition::new)
    );

    private final TagKey<Fluid> tagKey;

    public FluidTagPresentCondition(ResourceLocation tagName) {
        this.tagKey = TagKey.create(Registries.FLUID, tagName);
    }

    public FluidTagPresentCondition(String tagName) {
        this(ResourceLocation.parse(tagName));
    }

    private ResourceLocation location() {
        return tagKey.location();
    }

    @Override
    public boolean test(IContext context) {
        return !context.getTag(tagKey).isEmpty();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
