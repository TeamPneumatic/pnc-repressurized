package me.desht.pneumaticcraft.common.amadron;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class AmadronPlayerFilter implements Predicate<PlayerEntity> {
    public static final AmadronPlayerFilter YES = new AmadronPlayerFilter(Op.YES, Collections.emptyMap());
    public static final AmadronPlayerFilter NO = new AmadronPlayerFilter(Op.NO, Collections.emptyMap());

    private enum Op {
        YES, NO, AND, OR;

        public boolean isFake() {
            return this == YES || this == NO;
        }
    }

    private static final Map<ResourceLocation, MatcherFactory<?>> matcherFactories = new ConcurrentHashMap<>();

    private final Map<ResourceLocation,Matcher> matchers;
    private final Op op;

    private AmadronPlayerFilter(Op op, @Nonnull Map<ResourceLocation,Matcher> matchers) {
        Validate.isTrue(op.isFake() || !matchers.isEmpty(), "received empty matcher list!");
        this.op = op;
        this.matchers = ImmutableMap.copyOf(matchers);
    }

    public static void addDefaultMatchers() {
        registerMatcher("dimensions", new DimensionMatcher.Factory());
        registerMatcher("biome_categories", new BiomeMatcher.Factory());
    }

    public static void registerMatcher(String id, MatcherFactory<?> matcher) {
        matcherFactories.put(getId(id), matcher);
    }

    public boolean isReal() {
        return !op.isFake();
    }

    private static ResourceLocation getId(String key) {
        return key.contains(":") ? new ResourceLocation(key) : RL(key);
    }

    public static AmadronPlayerFilter fromJson(JsonObject json) {
        for (String opStr : new String[] { "or", "and" }) {
            Map<ResourceLocation,Matcher> matchers = new HashMap<>();
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

                return new AmadronPlayerFilter(op, matchers);
            }
        }
        throw new JsonSyntaxException("must provide one of 'and' or 'or'!");
    }

    public static AmadronPlayerFilter fromBytes(PacketBuffer buffer) {
        Op op = buffer.readEnum(Op.class);
        int nMatchers = buffer.readVarInt();

        Map<ResourceLocation,Matcher> map = new HashMap<>();
        for (int i = 0; i < nMatchers; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            map.put(id, matcherFactories.get(id).fromBytes(buffer));
        }

        return new AmadronPlayerFilter(op, map);
    }

    public void toBytes(PacketBuffer buffer) {
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
    public boolean test(PlayerEntity player) {
        switch (op) {
            case YES: return true;
            case NO: return false;
            case OR: return matchers.values().stream().anyMatch(matcher -> matcher.test(player));
            case AND: return matchers.values().stream().allMatch(matcher -> matcher.test(player));
        }
        return false;
    }

    public void getDescription(List<ITextComponent> tooltip) {
        if (isReal()) {
            matchers.values().forEach(matcher -> {
                matcher.addDescription(tooltip);
                tooltip.add(new StringTextComponent("  -- " + op + " --").withStyle(TextFormatting.GOLD));
            });
            if (tooltip.size() > 1) tooltip.remove(tooltip.size() - 1);
        }
    }

    @Override
    public String toString() {
        String delimiter = " " + op.toString() + " ";
        return "[" + matchers.values().stream().map(Object::toString).collect(Collectors.joining(delimiter)) + "]";
    }

    public interface Matcher extends Predicate<PlayerEntity> {
        void toBytes(PacketBuffer buffer);
        JsonElement toJson();
        void addDescription(List<ITextComponent> tooltip);
    }

    public interface MatcherFactory<T extends Matcher> {
        T fromJson(JsonElement json);
        T fromBytes(PacketBuffer buffer);
    }
}
