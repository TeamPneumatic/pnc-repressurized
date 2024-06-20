package me.desht.pneumaticcraft.api.drone.area;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class AreaTypeSerializer<A extends AreaType> {
    private final Supplier<A> defaultSupplier;
    private final MapCodec<A> codec;
    private final StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec;

    public AreaTypeSerializer(Supplier<A> defaultSupplier, MapCodec<A> codec, StreamCodec<? super RegistryFriendlyByteBuf,A> streamCodec) {
        this.defaultSupplier = defaultSupplier;
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public static <A extends AreaType> AreaTypeSerializer<A> createType(Supplier<A> defaultSupplier, MapCodec<A> codec, StreamCodec<? super RegistryFriendlyByteBuf,A> streamCodec) {
        return new AreaTypeSerializer<>(defaultSupplier, codec, streamCodec);
    }

    public A createDefaultInstance() {
        return defaultSupplier.get();
    }

    public MapCodec<A> codec() {
        return codec;
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec() {
        return streamCodec;
    }
}
