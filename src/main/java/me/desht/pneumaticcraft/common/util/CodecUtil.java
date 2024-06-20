package me.desht.pneumaticcraft.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import net.minecraft.util.ExtraCodecs;
import org.apache.commons.lang3.Validate;

import javax.xml.validation.Validator;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiFunction;

public class CodecUtil {
    public static <A> Codec<List<A>> listWithSizeBound(Codec<List<A>> codec, int min, int max) {
        return listWithSizeBound(codec, min, max, (a, b) -> "List must be " + min + "-" + max + " elements in length!");
    }

    private static <A> Codec<List<A>> listWithSizeBound(Codec<List<A>> codec, int min, int max, BiFunction<Integer,Integer,String> errFunc) {
        return codec.validate(l -> l.size() >= min && l.size() <= max ?
                DataResult.success(l) :
                DataResult.error(() -> errFunc.apply(min, max)));
    }

    public static Codec<BitSet> bitSetCodec(int nBits) {
        Validate.isTrue(nBits > 0 && nBits <= 8, "only 1..8 bits supported!");
        return Codec.BYTE.xmap(b -> fromByte(b, nBits), CodecUtil::toByte);
    }

    private static BitSet fromByte(byte b, int nBits) {
        BitSet res = new BitSet(nBits);
        for (int i = 0; i < nBits; i++) {
            res.set(i, (b & (1 << i)) != 0);
        }
        return res;
    }

    private static byte toByte(BitSet set) {
        return set.toByteArray()[0];
    }

}
