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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import me.desht.pneumaticcraft.common.amadron.BiomeMatcher;
import me.desht.pneumaticcraft.common.amadron.DimensionMatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * A player filter is a collection of individual matcher objects with either match-any or match-all behaviour.
 * Custom matcher objects can be registered and have support for reading/writing to JSON and packet buffers, so
 * are suitable for use in recipes, for example.
 */
public class PlayerFilter implements Predicate<Player> {
    public static final PlayerFilter YES = new PlayerFilter(Op.YES, Collections.emptyMap());
    public static final PlayerFilter NO = new PlayerFilter(Op.NO, Collections.emptyMap());

    private enum Op {
        YES, NO, AND, OR;

        public boolean isFake() {
            return this == YES || this == NO;
        }
    }

    private static final Map<ResourceLocation, IPlayerMatcher.MatcherFactory<?>> matcherFactories = new ConcurrentHashMap<>();

    private final Map<ResourceLocation, IPlayerMatcher> matchers;
    private final Op op;

    private PlayerFilter(Op op, @Nonnull Map<ResourceLocation, IPlayerMatcher> matchers) {
        Validate.isTrue(op.isFake() || !matchers.isEmpty(), "received empty matcher list!");
        this.op = op;
        this.matchers = ImmutableMap.copyOf(matchers);
    }

    public static PlayerFilter fromJson(JsonObject json) {
        for (String opStr : new String[] { "or", "and" }) {
            Map<ResourceLocation, IPlayerMatcher> matchers = new HashMap<>();
            if (json.has(opStr)) {
                Op op = Op.valueOf(opStr.toUpperCase(Locale.ROOT));
                JsonObject jsonSub = json.getAsJsonObject(opStr);

                for (Map.Entry<String, JsonElement> entry : jsonSub.entrySet()) {
                    ResourceLocation id = getId(entry.getKey());
                    if (matcherFactories.containsKey(id)) {
                        matchers.put(id, matcherFactories.get(id).fromJson(entry.getValue()));
                    } else {
                        throw new JsonSyntaxException("unknown matcher: " + id);
                    }
                }

                return new PlayerFilter(op, matchers);
            }
        }
        throw new JsonSyntaxException("must provide one of 'and' or 'or'!");
    }

    public static PlayerFilter fromBytes(FriendlyByteBuf buffer) {
        Op op = buffer.readEnum(Op.class);
        int nMatchers = buffer.readVarInt();

        Map<ResourceLocation, IPlayerMatcher> map = new HashMap<>();
        for (int i = 0; i < nMatchers; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            map.put(id, matcherFactories.get(id).fromBytes(buffer));
        }

        return new PlayerFilter(op, map);
    }

    public static void registerDefaultMatchers() {
        registerMatcher("dimensions", new DimensionMatcher.Factory());
        registerMatcher("biome_tags", new BiomeMatcher.Factory());
    }

    public static void registerMatcher(String id, IPlayerMatcher.MatcherFactory<?> matcher) {
        matcherFactories.put(getId(id), matcher);
    }

    public boolean isReal() {
        return !op.isFake();
    }

    public boolean matchAll() {
        return op == Op.AND;
    }

    private static ResourceLocation getId(String key) {
        return key.contains(":") ? new ResourceLocation(key) : RL(key);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(op);
        buffer.writeVarInt(matchers.size());
        matchers.forEach((id, matcher) -> {
            buffer.writeResourceLocation(id);
            matcher.toBytes(buffer);
        });
    }

    public JsonObject toJson() {
        JsonObject sub = new JsonObject();
        matchers.forEach((id, matcher) -> sub.add(id.toString(), matcher.toJson()));
        JsonObject res = new JsonObject();
        res.add(op.name(), sub);
        return res;
    }

    @Override
    public boolean test(Player player) {
        return switch (op) {
            case YES -> true;
            case NO -> false;
            case OR -> matchers.values().stream().anyMatch(matcher -> matcher.test(player));
            case AND -> matchers.values().stream().allMatch(matcher -> matcher.test(player));
        };
    }

    public void getDescription(Player player, List<Component> tooltip) {
        if (isReal()) {
            matchers.values().forEach(matcher -> matcher.addDescription(player, tooltip));
        }
    }

    @Override
    public String toString() {
        String delimiter = " " + op.toString() + " ";
        return "[" + matchers.values().stream().map(Object::toString).collect(Collectors.joining(delimiter)) + "]";
    }
}
