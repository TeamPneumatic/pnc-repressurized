package me.desht.pneumaticcraft.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.function.BiFunction;

public class CodecUtil {
    public static <A> Codec<List<A>> listWithSizeBound(Codec<List<A>> codec, int min, int max) {
        return listWithSizeBound(codec, min, max, (a, b) -> "List must be " + min + "-" + max + " elements in length!");
    }

    private static <A> Codec<List<A>> listWithSizeBound(Codec<List<A>> codec, int min, int max, BiFunction<Integer,Integer,String> errFunc) {
        return ExtraCodecs.validate(codec, l -> l.size() >= min && l.size() <= max ?
                DataResult.success(l) :
                DataResult.error(() -> errFunc.apply(min, max)));
    }
}
