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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.misc.IPlayerFilter;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A player filter is a collection of individual matcher objects with either match-any or match-all behaviour.
 * Custom matcher objects can be registered and have support for reading/writing to JSON and packet buffers, so
 * are suitable for use in recipes, for example.
 */
public record PlayerFilter(Op op, List<IPlayerMatcher> matchers) implements IPlayerFilter {
    public static final Codec<IPlayerMatcher> MATCHER_CODEC
            = PlayerMatcherTypes.CODEC.dispatch("type", IPlayerMatcher::getType, IPlayerMatcher.MatcherType::codec);

    public static final Codec<PlayerFilter> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Op.CODEC.fieldOf("op").forGetter(PlayerFilter::op),
            MATCHER_CODEC.listOf().fieldOf("matchers").forGetter(PlayerFilter::matchers)
    ).apply(inst, PlayerFilter::new));

    public static final PlayerFilter YES = new PlayerFilter(Op.YES, List.of());
    public static final PlayerFilter NO = new PlayerFilter(Op.NO, List.of());

    public static PlayerFilter fromNetwork(FriendlyByteBuf buffer) {
        Op op = buffer.readEnum(Op.class);

        int nMatchers = buffer.readVarInt();
        List<IPlayerMatcher> list = new ArrayList<>();
        for (int i = 0; i < nMatchers; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            list.add(PlayerMatcherTypes.matcherTypes.get(id).fromNetwork(buffer));
        }

        return new PlayerFilter(op, list);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeEnum(op);
        buffer.writeVarInt(matchers.size());
        matchers.forEach(matcher -> {
            buffer.writeResourceLocation(matcher.getType().getId());
            matcher.toNetwork(buffer);
        });
    }

    @Override
    public boolean isReal() {
        return !op.isFake();
    }

    @Override
    public boolean matchAll() {
        return op == Op.AND;
    }

//    private static ResourceLocation getId(String key) {
//        return key.contains(":") ? new ResourceLocation(key) : RL(key);
//    }

    @Override
    public boolean test(Player player) {
        return switch (op) {
            case YES -> true;
            case NO -> false;
            case OR -> matchers.stream().anyMatch(matcher -> matcher.test(player));
            case AND -> matchers.stream().allMatch(matcher -> matcher.test(player));
        };
    }

    @Override
    public void getDescription(Player player, List<Component> tooltip) {
        if (isReal()) {
            matchers.forEach(matcher -> matcher.addDescription(player, tooltip));
        }
    }

    @Override
    public String toString() {
        String delimiter = " " + op.toString() + " ";
        return "[" + matchers.stream().map(Object::toString).collect(Collectors.joining(delimiter)) + "]";
    }

    public enum Op {
        YES, NO, AND, OR;

        public static final Codec<Op> CODEC = new PrimitiveCodec<>() {
            @Override
            public <T> DataResult<Op> read(DynamicOps<T> ops, T input) {
                try {
                    return ops.getStringValue(input).map(Op::valueOf);
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "invalid value: " + input);
                }
            }

            @Override
            public <T> T write(DynamicOps<T> ops, Op value) {
                return ops.createString(value.name());
            }
        };

        public boolean isFake() {
            return this == YES || this == NO;
        }
    }
}
