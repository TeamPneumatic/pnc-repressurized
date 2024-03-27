package me.desht.pneumaticcraft.common.util.playerfilter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.modDefaultedRL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.modDefaultedString;

public class PlayerMatcherTypes {
    static final BiMap<ResourceLocation, IPlayerMatcher.MatcherType<?>> matcherTypes = Maps.synchronizedBiMap(HashBiMap.create());

    public static final Codec<IPlayerMatcher.MatcherType<?>> CODEC = Codec.STRING.flatXmap(
            key -> Optional.ofNullable(matcherTypes.get(modDefaultedRL(key)))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown matcher type: " + key)),
            matcherType -> Optional.ofNullable(matcherTypes.inverse().get(matcherType))
                    .map(resLoc -> DataResult.success(modDefaultedString(resLoc)))
                    .orElseGet(() -> DataResult.error(() -> "unknown registry element: " + matcherType))
    );

    public static void registerDefaultMatchers() {
        registerMatcher(DimensionMatcher.DimensionMatcherType.INSTANCE);
        registerMatcher(BiomeMatcher.BiomeMatcherType.INSTANCE);
    }

    public static void registerMatcher(IPlayerMatcher.MatcherType<?> matcher) {
        matcherTypes.put(matcher.getId(), matcher);
    }

    public static IPlayerMatcher.MatcherType<?> getMatcher(ResourceLocation id) {
        return matcherTypes.get(id);
    }
}
