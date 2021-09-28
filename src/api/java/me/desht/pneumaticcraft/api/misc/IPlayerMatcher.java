package me.desht.pneumaticcraft.api.misc;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.function.Predicate;

/**
 * A player matcher predicate with potential multiple uses - currently used by the Amadron tablet to check if a
 * particular offer is usable by a player. This matcher should be able to run on both client and server.
 */
public interface IPlayerMatcher extends Predicate<PlayerEntity> {
    /**
     * Serialize this matcher to a packet buffer for sync'ing to clients
     * @param buffer a packet buffer
     */
    void toBytes(PacketBuffer buffer);

    /**
     * Serialize this match to JSON, for data generation.
     * @return a JSON element
     */
    JsonElement toJson();

    /**
     * Add this matcher's information to a tooltip.  This is used for example by the Amadron Tablet GUI to show
     * information about this matcher.
     * @param tooltip a tooltip list
     */
    void addDescription(List<ITextComponent> tooltip);

    /**
     * Implement this to create instances of a matcher from JSON and from a packet buffer.
     * @param <T> the matcher type
     */
    interface MatcherFactory<T extends IPlayerMatcher> {
        T fromJson(JsonElement json);
        T fromBytes(PacketBuffer buffer);
    }
}
