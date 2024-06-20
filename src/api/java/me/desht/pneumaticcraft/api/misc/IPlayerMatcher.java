/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Predicate;

/**
 * A player matcher predicate with potential multiple uses - currently used by the Amadron tablet to check if a
 * particular offer is usable by a player.
 * <p>
 * This matcher should be able to run on both client and server.
 * <p>
 * Player matchers are registry objects, and should be registered via the usual Neoforge deferred registry system.
 * Register an instance of {@link MatcherType} for each player matcher object you wish to register.
 */
public interface IPlayerMatcher extends Predicate<Player> {
    Codec<IPlayerMatcher> CODEC = PNCRegistries.PLAYER_MATCHER_REGISTRY.byNameCodec()
            .dispatch(IPlayerMatcher::type, MatcherType::codec);
    StreamCodec<RegistryFriendlyByteBuf,IPlayerMatcher> STREAM_CODEC = ByteBufCodecs.registry(PNCRegistries.PLAYER_MATCHER_KEY)
            .dispatch(IPlayerMatcher::type, MatcherType::streamCodec);

    /**
     * Get the matcher type, which handles serialization for the matcher.
     * @return the matcher type
     */
    MatcherType<? extends IPlayerMatcher> type();

    /**
     * Add this matcher's information to a tooltip.  This is used for example by the Amadron Tablet GUI to show
     * information about this matcher.
     * @param player the relevant player
     * @param tooltip a tooltip list
     */
    void addDescription(Player player, List<Component> tooltip);

    /**
     * Utility method to add a standardised tooltip for a matcher. Don't override this - it is strongly recommended to
     * call it from {@link #addDescription(Player, List)} for a consistent tooltip for all matchers.
     *
     * @param player the player, as received by {@link #addDescription(Player, List)}
     * @param tooltip tooltip to add to, as received by {@link #addDescription(Player, List)}
     * @param header the header text line
     * @param itemList a list of items that this matcher matches against
     */
    default void standardTooltip(Player player, List<Component> tooltip, Component header, List<Component> itemList) {
        Component avail = Component.literal(" ")
            .append(test(player) ?
                    Component.literal(Symbols.TICK_MARK).withStyle(ChatFormatting.GREEN) :
                    Component.literal(Symbols.X_MARK).withStyle(ChatFormatting.RED)
            );
        tooltip.add(Component.literal(Symbols.TRIANGLE_RIGHT + " ")
                .append(header)
                .withStyle(ChatFormatting.GRAY)
                .append(avail)
        );
        itemList.forEach(item -> tooltip.add(Component.literal("  ")
                .append(Symbols.bullet())
                .append(item)
                .withStyle(ChatFormatting.GRAY)
        ));
    }

    /**
     * The matcher type, which provides a codec and stream codec for serialization
     *
     * @param <P> the parameterised type of the player matcher object
     */
    record MatcherType<P extends IPlayerMatcher>(MapCodec<P> codec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
    }
}
