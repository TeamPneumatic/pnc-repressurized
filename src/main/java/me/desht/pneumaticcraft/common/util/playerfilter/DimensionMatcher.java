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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record DimensionMatcher(List<ResourceLocation> dimensionIds) implements IPlayerMatcher {
    public static final MapCodec<DimensionMatcher> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("dimensions").forGetter(DimensionMatcher::dimensionIds)
    ).apply(inst, DimensionMatcher::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionMatcher> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), DimensionMatcher::dimensionIds,
            DimensionMatcher::new
    );

    public static final MatcherType<DimensionMatcher> TYPE = new MatcherType<>(CODEC, STREAM_CODEC);

    @Override
    public MatcherType<? extends IPlayerMatcher> type() {
        return TYPE;
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
}
