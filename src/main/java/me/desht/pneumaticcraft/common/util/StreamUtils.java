/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

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
