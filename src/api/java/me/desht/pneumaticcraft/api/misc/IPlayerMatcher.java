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
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Predicate;

/**
 * A player matcher predicate with potential multiple uses - currently used by the Amadron tablet to check if a
 * particular offer is usable by a player.
 * <p>
 * This matcher should be able to run on both client and server.
 */
public interface IPlayerMatcher extends Predicate<Player> {
    /**
     * Serialize this matcher object to a packet buffer, for sync'ing to clients
     * @param buffer a packet buffer
     */
    void toNetwork(FriendlyByteBuf buffer);

    MatcherType<?> getType();

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
     * The type of a matcher object, which supplies a codec for serialization, as well as a factory method to construct
     * a matcher from a byte buffer (which may one day become part of the codec...)
     *
     * @param <P> the matcher type
     */
    interface MatcherType<P extends IPlayerMatcher> {
        ResourceLocation getId();

        P fromNetwork(FriendlyByteBuf buf);

        Codec<P> codec();
    }
}
