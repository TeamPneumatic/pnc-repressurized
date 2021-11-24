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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.function.Supplier;

public class AcceptabilityCache<T> {
    public enum Acceptability {
        ACCEPTED(true),
        REJECTED(false),
        UNKNOWN(false);

        private final boolean result;

        public static Acceptability of(boolean b) {
            return b ? ACCEPTED : REJECTED;
        }

        Acceptability(boolean result) {
            this.result = result;
        }
    }

    private final Map<T,Acceptability> theCache = new Object2ObjectOpenHashMap<>();

    public void clear() {
        theCache.clear();
    }

    public boolean isAcceptable(T itemToTest, Supplier<Boolean> tester) {
        Acceptability a = theCache.getOrDefault(itemToTest, Acceptability.UNKNOWN);
        if (a != Acceptability.UNKNOWN) return a.result;
        boolean result = tester.get();
        theCache.put(itemToTest, Acceptability.of(result));
        return result;
    }
}
