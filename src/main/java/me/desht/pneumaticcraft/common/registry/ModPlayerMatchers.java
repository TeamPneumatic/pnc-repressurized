package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.util.playerfilter.BiomeMatcher;
import me.desht.pneumaticcraft.common.util.playerfilter.DimensionMatcher;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModPlayerMatchers {
    public static final DeferredRegister<IPlayerMatcher.MatcherType<?>> PLAYER_MATCHERS_DEFERRED
            = DeferredRegister.create(PNCRegistries.PLAYER_MATCHER_REGISTRY, Names.MOD_ID);

    public static final Supplier<IPlayerMatcher.MatcherType<BiomeMatcher>> BIOME_MATCHER_TYPE
            = PLAYER_MATCHERS_DEFERRED.register("biomes", () -> BiomeMatcher.TYPE);
    public static final Supplier<IPlayerMatcher.MatcherType<DimensionMatcher>> DIMENSION_MATCHER_TYPE
            = PLAYER_MATCHERS_DEFERRED.register("dimensions", () -> DimensionMatcher.TYPE);
}
