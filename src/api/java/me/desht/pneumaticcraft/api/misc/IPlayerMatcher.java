package me.desht.pneumaticcraft.api.misc;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.function.Predicate;

/**
 * A player matcher predicate with potential multiple uses - currently used by the Amadron tablet to check if a
 * particular offer is usable by a player.
 * <p>
 * This matcher should be able to run on both client and server.
 */
public interface IPlayerMatcher extends Predicate<PlayerEntity> {
    /**
     * Serialize this matcher object to a packet buffer, for sync'ing to clients
     * @param buffer a packet buffer
     */
    void toBytes(PacketBuffer buffer);

    /**
     * Serialize this matcher object to JSON, for data generation.
     * @return a JSON element
     */
    JsonElement toJson();

    /**
     * Add this matcher's information to a tooltip.  This is used for example by the Amadron Tablet GUI to show
     * information about this matcher.
     * @param player the relevant player
     * @param tooltip a tooltip list
     */
    void addDescription(PlayerEntity player, List<ITextComponent> tooltip);

    /**
     * Utility method to add a standardised tooltip for a matcher. Don't override this - it is strongly recommended to
     * call it from {@link #addDescription(PlayerEntity, List)} for a consistent tooltip for all matchers.
     *
     * @param player the player, as received by {@link #addDescription(PlayerEntity, List)}
     * @param tooltip tooltip to add to, as received by {@link #addDescription(PlayerEntity, List)}
     * @param header the header text line
     * @param itemList a list of items that this matcher matches against
     */
    default void standardTooltip(PlayerEntity player, List<ITextComponent> tooltip, ITextComponent header, List<ITextComponent> itemList) {
        ITextComponent avail = new StringTextComponent(" ")
            .append(test(player) ?
                    new StringTextComponent(Symbols.TICK_MARK).withStyle(TextFormatting.GREEN) :
                    new StringTextComponent(Symbols.X_MARK).withStyle(TextFormatting.RED)
            );
        tooltip.add(new StringTextComponent(Symbols.TRIANGLE_RIGHT + " ")
                .append(header)
                .withStyle(TextFormatting.GRAY)
                .append(avail)
        );
        itemList.forEach(item -> tooltip.add(new StringTextComponent("  ")
                .append(Symbols.bullet())
                .append(item)
                .withStyle(TextFormatting.GRAY)
        ));
    }

    /**
     * Implement this and register it via
     * {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#registerPlayerMatcher(ResourceLocation, me.desht.pneumaticcraft.api.misc.IPlayerMatcher.MatcherFactory)}.
     * <p>
     * This factory creates instances of a player matcher from JSON and packet buffer data, matching data written by
     * {@link me.desht.pneumaticcraft.api.misc.IPlayerMatcher#toJson()} and
     * {@link me.desht.pneumaticcraft.api.misc.IPlayerMatcher#toBytes(PacketBuffer)}.
     * @param <T> the matcher type
     */
    interface MatcherFactory<T extends IPlayerMatcher> {
        T fromJson(JsonElement json);
        T fromBytes(PacketBuffer buffer);
    }
}
