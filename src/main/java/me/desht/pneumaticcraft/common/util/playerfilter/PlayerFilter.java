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
import me.desht.pneumaticcraft.api.misc.IPlayerFilter;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.stream.Collectors;

public record PlayerFilter(Op op, List<IPlayerMatcher> matchers) implements IPlayerFilter {
    public static final Codec<PlayerFilter> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            StringRepresentable.fromEnum(Op::values).fieldOf("op").forGetter(PlayerFilter::op),
            IPlayerMatcher.CODEC.listOf().fieldOf("matchers").forGetter(PlayerFilter::matchers)
    ).apply(inst, PlayerFilter::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerFilter> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Op.class), PlayerFilter::op,
            IPlayerMatcher.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerFilter::matchers,
            PlayerFilter::new
    );

    public static final PlayerFilter YES = new PlayerFilter(Op.YES, List.of());
    public static final PlayerFilter NO = new PlayerFilter(Op.NO, List.of());

    public static PlayerFilter fromNetwork(RegistryFriendlyByteBuf buffer) {
        return STREAM_CODEC.decode(buffer);
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public boolean isReal() {
        return op.isReal();
    }

    @Override
    public boolean matchAll() {
        return op == Op.AND;
    }

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

    public enum Op implements StringRepresentable {
        YES("yes"), NO("no"), AND("and"), OR("or");

        private final String name;

        Op(String name) {
            this.name = name;
        }

        public boolean isReal() {
            return this == AND || this == OR;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
