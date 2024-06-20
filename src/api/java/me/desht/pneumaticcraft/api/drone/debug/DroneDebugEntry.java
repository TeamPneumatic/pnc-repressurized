package me.desht.pneumaticcraft.api.drone.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record DroneDebugEntry(int progWidgetIndex, String message, Optional<BlockPos> pos, long receivedTime) {
    public static final StreamCodec<FriendlyByteBuf, DroneDebugEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DroneDebugEntry::progWidgetIndex,
            ByteBufCodecs.STRING_UTF8, DroneDebugEntry::message,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), DroneDebugEntry::pos,
            DroneDebugEntry::createClient
    );

    private static DroneDebugEntry createClient(int progWidgetIndex, String message, Optional<BlockPos> pos) {
        return new DroneDebugEntry(progWidgetIndex, message, pos, System.currentTimeMillis());
    }

    public static DroneDebugEntry create(String message, BlockPos pos, int progWidgetIndex) {
        return new DroneDebugEntry(progWidgetIndex, message, Optional.ofNullable(pos), 0L);
    }

    public boolean hasPos() {
        return pos.isPresent();
    }

    public BlockPos getPos() {
        return pos.orElse(null);
    }
}
