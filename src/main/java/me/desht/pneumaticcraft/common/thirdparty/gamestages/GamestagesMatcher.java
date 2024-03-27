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

package me.desht.pneumaticcraft.common.thirdparty.gamestages;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record GamestagesMatcher(List<String> stages, boolean matchAll) implements IPlayerMatcher {
    private static final Function<String, DataResult<String>> STAGE_CHECKER = s ->
            GameStageHelper.isStageKnown(s) ? DataResult.success(s) : DataResult.error(() -> "unknown stage: " + s);
    private static final Codec<String> STAGE_CODEC = Codec.STRING.flatXmap(STAGE_CHECKER, STAGE_CHECKER);

    public static final Codec<GamestagesMatcher> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            STAGE_CODEC.listOf().fieldOf("stages").forGetter(GamestagesMatcher::stages),
            Codec.BOOL.optionalFieldOf("match_all", false).forGetter(GamestagesMatcher::matchAll)
    ).apply(builder, GamestagesMatcher::new));

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeCollection(stages, FriendlyByteBuf::writeUtf);
        buffer.writeBoolean(matchAll);
    }

    @Override
    public MatcherType<?> getType() {
        return GamestagesMatcherType.INSTANCE;
    }

    @Override
    public void addDescription(Player player, List<Component> tooltip) {
        if (!stages.isEmpty()) {
            Component header = xlate("pneumaticcraft.playerFilter.gamestages")
                    .append(" (")
                    .append(xlate("pneumaticcraft.gui.misc." + (matchAll ? "all" : "any")))
                    .append(")");
            List<Component> items = stages.stream().map(Component::literal).collect(Collectors.toList());
            standardTooltip(player, tooltip, header, items);
        }
    }

    @Override
    public boolean test(Player playerEntity) {
        return matchAll ? GameStageHelper.hasAllOf(playerEntity, stages) : GameStageHelper.hasAnyOf(playerEntity, stages);
    }

    public enum GamestagesMatcherType implements IPlayerMatcher.MatcherType<GamestagesMatcher> {
        INSTANCE;

        private static final ResourceLocation ID = RL("gamestages");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public GamestagesMatcher fromNetwork(FriendlyByteBuf buf) {
            return new GamestagesMatcher(buf.readList(FriendlyByteBuf::readUtf), buf.readBoolean());
        }

        @Override
        public Codec<GamestagesMatcher> codec() {
            return CODEC;
        }
    }
}
