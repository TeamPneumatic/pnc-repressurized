package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.util.DamageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * For checking if a damage source type is radiation in a mod-agnostic way.
 */
public enum RadiationSourceCheck {
    INSTANCE;

    private final List<Predicate<DamageSource>> radiationSources = new ArrayList<>();

    public void registerRadiationSource(Predicate<DamageSource> predicate) {
        radiationSources.add(predicate);
    }

    public boolean isRadiation(DamageSource source) {
        return radiationSources.stream().anyMatch(p -> p.test(source));
    }
}
