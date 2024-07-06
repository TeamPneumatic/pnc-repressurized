package me.desht.pneumaticcraft.common.amadron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.HashMap;
import java.util.Map;

public class ImmutableBasket extends ShoppingBasket {
    private static final Codec<Map<ResourceLocation,Integer>> BASKET_CODEC
            = Codec.unboundedMap(ResourceLocation.CODEC, ExtraCodecs.NON_NEGATIVE_INT).xmap(HashMap::new, Map::copyOf);
    public static final Codec<ImmutableBasket> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BASKET_CODEC.fieldOf("basket").forGetter(b -> b.basket)
    ).apply(builder, ImmutableBasket::new));
    public static StreamCodec<FriendlyByteBuf, ImmutableBasket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), b -> b.basket,
            ImmutableBasket::new
    );

    static final ImmutableBasket EMPTY = new ImmutableBasket();

    ImmutableBasket(Map<ResourceLocation, Integer> basket) {
        super(Map.copyOf(basket));
    }

    ImmutableBasket() {
        super(Map.of());
    }

    @Override
    public ImmutableBasket toImmutable() {
        return this;
    }

    public ShoppingBasket toMutable() {
        return new MutableBasket(basket);
    }

}
