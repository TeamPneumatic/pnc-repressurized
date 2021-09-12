package me.desht.pneumaticcraft.common.util;

import java.util.stream.Stream;

public class StreamUtils {
    /**
     * Filter by the requested type and cast the remaining items.
     * @param type the class to filter
     * @param stream the incoming stream
     * @return a new filtered stream
     */
    public static <T> Stream<T> ofType(Class<T> type, Stream<? super T> stream){
        return stream.filter(el -> el != null && type.isAssignableFrom(el.getClass()))
                .map(type::cast);
    }
}
