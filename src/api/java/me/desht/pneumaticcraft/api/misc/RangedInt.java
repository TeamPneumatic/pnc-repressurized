package me.desht.pneumaticcraft.api.misc;

import java.util.function.IntPredicate;

public record RangedInt(int min, int max) implements IntPredicate {
    public static RangedInt of(int min, int max) {
        return new RangedInt(min, max);
    }

    @Override
    public boolean test(int n) {
        return n >= min && n <= max;
    }
}
