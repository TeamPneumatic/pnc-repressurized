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

package me.desht.pneumaticcraft.common.util.playerfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record DimensionMatcher(List<ResourceLocation> dimensionIds) implements IPlayerMatcher {
    public static final Codec<DimensionMatcher> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("dimensions").forGetter(DimensionMatcher::dimensionIds)
    ).apply(inst, DimensionMatcher::new));

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeCollection(dimensionIds, FriendlyByteBuf::writeResourceLocation);    }

    @Override
    public MatcherType<?> getType() {
        return DimensionMatcherType.INSTANCE;
    }

    @Override
    public void addDescription(Player player, List<Component> tooltip) {
        if (!dimensionIds.isEmpty()) {
            List<Component> items = dimensionIds.stream().map(id -> Component.literal(id.toString())).collect(Collectors.toList());
            standardTooltip(player, tooltip, xlate("pneumaticcraft.playerFilter.dimensions"), items);
        }
    }

    @Override
    public boolean test(Player playerEntity) {
        return dimensionIds.isEmpty() || dimensionIds.contains(playerEntity.level().dimension().location());
    }

    public enum DimensionMatcherType implements MatcherType<DimensionMatcher> {
        INSTANCE;

        private static final ResourceLocation ID = RL("dimensions");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public DimensionMatcher fromNetwork(FriendlyByteBuf buf) {
            return new DimensionMatcher(buf.readList(FriendlyByteBuf::readResourceLocation));
        }

        @Override
        public Codec<DimensionMatcher> codec() {
            return CODEC;
        }
    }
}
