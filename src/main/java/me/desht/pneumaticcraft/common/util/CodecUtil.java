package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.Validate;

import java.util.BitSet;

public class CodecUtil {
    public static Codec<BitSet> bitSetCodec(int nBits) {
        Validate.isTrue(nBits > 0 && nBits <= 8, "only 1..8 bits supported!");
        return Codec.BYTE.xmap(b -> fromByte(b, nBits), CodecUtil::toByte);
    }

    public static StreamCodec<ByteBuf,BitSet> bitSetStreamCodec(int nBits) {
        Validate.isTrue(nBits > 0 && nBits <= 8, "only 1..8 bits supported!");
        return StreamCodec.of(
                (buf, bitSet) -> buf.writeByte(toByte(bitSet)),
                buf -> fromByte(buf.readByte(), nBits)
        );
    }

    private static BitSet fromByte(byte b, int nBits) {
        BitSet res = new BitSet(nBits);
        for (int i = 0; i < nBits; i++) {
            res.set(i, (b & (1 << i)) != 0);
        }
        return res;
    }

    private static byte toByte(BitSet set) {
        byte[] a = set.toByteArray();
        return a.length == 0 ? 0 : a[0];
    }

    public static <K,T> Codec<MapCodec<? extends T>> simpleDispatchCodec(Codec<K> keyCodec, BiMap<K,MapCodec<? extends T>> map) {
        return keyCodec.comapFlatMap(
                key -> {
                    var x = map.get(key);
                    return x == null ? DataResult.error(() -> "unknown type " + key) : DataResult.success(x);
                },
                mapCodec -> map.inverse().get(mapCodec));
    }
}
