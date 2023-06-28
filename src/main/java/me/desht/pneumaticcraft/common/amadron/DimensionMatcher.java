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

package me.desht.pneumaticcraft.common.amadron;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class DimensionMatcher implements IPlayerMatcher {
    private final Set<ResourceLocation> dimensionIds;

    public DimensionMatcher(Set<ResourceLocation> dimensionIds) {
        this.dimensionIds = ImmutableSet.copyOf(dimensionIds);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(dimensionIds.size());
        dimensionIds.forEach(buffer::writeResourceLocation);
    }

    @Override
    public JsonElement toJson() {
        JsonArray res = new JsonArray();
        dimensionIds.forEach(id -> res.add(id.toString()));
        return res;
    }

    @Override
    public void addDescription(Player player, List<Component> tooltip) {
        if (!dimensionIds.isEmpty()) {
            List<Component> items = dimensionIds.stream().map(id -> Component.literal(id.toString())).collect(Collectors.toList());
            standardTooltip(player, tooltip, xlate("pneumaticcraft.playerFilter.dimensions"), items);
        }
    }

    @Override
    public boolean test(Player playerEntity) {
        return dimensionIds.isEmpty() || dimensionIds.contains(playerEntity.level().dimension().location());
    }

    public static class Factory implements MatcherFactory<DimensionMatcher> {
        @Override
        public DimensionMatcher fromJson(JsonElement json) {
            Set<ResourceLocation> dimensionIds = new ObjectOpenHashSet<>();
            json.getAsJsonArray().forEach(el -> dimensionIds.add(new ResourceLocation(el.getAsString())));
            return new DimensionMatcher(dimensionIds);
        }

        @Override
        public DimensionMatcher fromBytes(FriendlyByteBuf buffer) {
            int n = buffer.readVarInt();
            Set<ResourceLocation> dimensionIds = new ObjectOpenHashSet<>();
            for (int i = 0; i < n; i++) {
                dimensionIds.add(buffer.readResourceLocation());
            }
            return new DimensionMatcher(dimensionIds);
        }
    }
}
