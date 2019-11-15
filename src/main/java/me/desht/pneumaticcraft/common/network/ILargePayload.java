package me.desht.pneumaticcraft.common.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

/**
 * Marker interface to indicate this message might have a payload larger than
 * {@link ILargePayload#MAX_PAYLOAD_SIZE} bytes
 */
public interface ILargePayload {
    int MAX_PAYLOAD_SIZE = 32000;

    PacketBuffer dumpToBuffer();

    void handleLargePayload(PlayerEntity player);
}
