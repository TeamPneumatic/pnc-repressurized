package me.desht.pneumaticcraft.common.thirdparty.gamestages;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GamestagesMatcher implements IPlayerMatcher {
    private final Set<String> stages;
    private final boolean matchAll;

    public GamestagesMatcher(Collection<String> stages, boolean matchAll) {
        stages.forEach(s -> Validate.isTrue(GameStageHelper.isStageKnown(s), "unknown gamestage '" + s + "'!"));
        this.stages = ImmutableSet.copyOf(stages);
        this.matchAll = matchAll;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(matchAll);
        buffer.writeVarInt(stages.size());
        stages.forEach(buffer::writeUtf);
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        stages.forEach(array::add);
        json.add(matchAll ? "all" : "any", array);
        return json;
    }

    @Override
    public void addDescription(PlayerEntity player, List<ITextComponent> tooltip) {
        if (!stages.isEmpty()) {
            ITextComponent header = xlate("pneumaticcraft.playerFilter.gamestages")
                    .append(" (")
                    .append(xlate("pneumaticcraft.gui.misc." + (matchAll ? "all" : "any")))
                    .append(")");
            List<ITextComponent> items = stages.stream().map(StringTextComponent::new).collect(Collectors.toList());
            standardTooltip(player, tooltip, header, items);
        }
    }

    @Override
    public boolean test(PlayerEntity playerEntity) {
        return matchAll ? GameStageHelper.hasAllOf(playerEntity, stages) : GameStageHelper.hasAnyOf(playerEntity, stages);
    }

    public static class Factory implements IPlayerMatcher.MatcherFactory<GamestagesMatcher> {
        @Override
        public GamestagesMatcher fromJson(JsonElement json) {
            if (!json.isJsonObject()) throw new JsonSyntaxException("expected JSON object here!");
            JsonObject o = json.getAsJsonObject();
            if (o.has("any")) {
                return new GamestagesMatcher(getStrings(o, "any"), false);
            } else if (o.has("all")) {
                return new GamestagesMatcher(getStrings(o, "all"), true);
            } else {
                throw new JsonSyntaxException("element must contain one of 'all' or 'any'");
            }
        }

        private Collection<String> getStrings(JsonObject o, String fieldName) {
            //noinspection UnstableApiUsage
            return Streams.stream(JSONUtils.getAsJsonArray(o, fieldName).iterator())
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toList());
        }

        @Override
        public GamestagesMatcher fromBytes(PacketBuffer buffer) {
            boolean matchAll = buffer.readBoolean();
            int n = buffer.readVarInt();
            List<String> l = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                l.add(buffer.readUtf(64));  // gamestages defines max length of a stage flag as 64
            }
            return new GamestagesMatcher(l, matchAll);
        }
    }
}
