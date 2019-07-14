package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Marker interface to indicate this message might have a payload larger than
 * {@link ILargePayload#MAX_PAYLOAD_SIZE} bytes
 */
public interface ILargePayload {
    int MAX_PAYLOAD_SIZE = 32000;

    ByteBuf dumpToBuffer();

    void handleLargePayload(PlayerEntity player);
}
