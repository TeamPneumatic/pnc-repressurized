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
