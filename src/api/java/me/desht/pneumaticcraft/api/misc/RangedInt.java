package me.desht.pneumaticcraft.api.misc;

import java.util.function.Predicate;

public record RangedInt(int min, int max) implements Predicate<Integer> {
    @Override
    public boolean test(Integer n) {
        return n >= min && n <= max;
    }
}
