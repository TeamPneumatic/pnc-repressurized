package me.desht.pneumaticcraft.common.util;

import java.util.stream.Stream;

public class StreamUtils{
    /**
     * Filter by the requested type and cast the remaining items.
     * @param type
     * @param stream
     * @return
     */
    public static <T> Stream<T> ofType(Class<T> type, Stream<? super T> stream){
        return stream.filter(el -> el != null && type.isAssignableFrom(el.getClass()))
                     .map(type::cast);
    }
}
