package me.desht.pneumaticcraft.api.remote;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class RemoteWidgetType<R extends IRemoteWidget> {
    private final MapCodec<R> codec;
    private final StreamCodec<RegistryFriendlyByteBuf,R> streamCodec;

    public RemoteWidgetType(MapCodec<R> codec, StreamCodec<RegistryFriendlyByteBuf, R> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public MapCodec<R> codec() {
        return codec;
    }

    public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
        return streamCodec;
    }
}
