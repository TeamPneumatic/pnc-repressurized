package me.desht.pneumaticcraft.api.misc;

import java.util.function.IntPredicate;

public record RangedInt(int min, int max) implements IntPredicate {
    @Override
    public boolean test(int n) {
        return n >= min && n <= max;
    }
}
