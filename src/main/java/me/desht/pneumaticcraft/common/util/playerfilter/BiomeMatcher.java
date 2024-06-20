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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record BiomeMatcher(List<TagKey<Biome>> tags) implements IPlayerMatcher {
    private static final MapCodec<BiomeMatcher> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TagKey.codec(Registries.BIOME).listOf().fieldOf("biomes").forGetter(BiomeMatcher::tags)
    ).apply(builder, BiomeMatcher::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, BiomeMatcher> STREAM_CODEC = StreamCodec.of(
            (buf, matcher) -> buf.writeCollection(matcher.tags, (b, key) -> b.writeResourceLocation(key.location())),
            buf -> new BiomeMatcher(buf.readList(b -> TagKey.create(Registries.BIOME, b.readResourceLocation())))
    );

    public static final MatcherType<BiomeMatcher> TYPE = new MatcherType<>(CODEC, STREAM_CODEC);

    @Override
    public MatcherType<?> type() {
        return TYPE;
    }

    @Override
    public void addDescription(Player player, List<Component> tooltip) {
        if (!tags.isEmpty()) {
            List<Component> items = tags.stream().map(tag -> Component.literal(tag.location().toString())).collect(Collectors.toList());
            standardTooltip(player, tooltip, xlate("pneumaticcraft.playerFilter.biomes"), items);
        }
    }

    @Override
    public boolean test(Player playerEntity) {
        return tags.isEmpty() || playerEntity.level().getBiome(playerEntity.blockPosition()).tags().anyMatch(tags::contains);
    }
}
