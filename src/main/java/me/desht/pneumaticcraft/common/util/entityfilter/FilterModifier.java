package me.desht.pneumaticcraft.common.util.entityfilter;

import com.mojang.datafixers.util.Either;
import joptsimple.internal.Strings;
import net.minecraft.world.entity.Entity;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class FilterModifier implements BiPredicate<Entity, String> {
    private final Either<Set<String>,Predicate<String>> validation;
    private final String valText;
    private final BiPredicate<Entity, String> testPredicate;

    private FilterModifier(Either<Set<String>,Predicate<String>> validation, String valText, BiPredicate<Entity, String> testPredicate) {
        this.validation = validation;
        this.valText = valText;
        this.testPredicate = testPredicate;
    }

    FilterModifier(Set<String> validationSet, BiPredicate<Entity, String> testPredicate) {
        this(Either.left(validationSet), "", testPredicate);
    }

    FilterModifier(Predicate<String> validationPredicate, String valText, BiPredicate<Entity, String> testPredicate) {
        this(Either.right(validationPredicate), valText, testPredicate);
    }

    boolean isValid(String s) {
        return validation.map(set -> set.contains(s), pred -> pred.test(s));
    }

    @Override
    public boolean test(Entity entity, String val) {
        return testPredicate.test(entity, val);
    }

    public String displayValidOptions() {
        return validation.map(set -> Strings.join(set, ","), pred -> valText);
    }
}
