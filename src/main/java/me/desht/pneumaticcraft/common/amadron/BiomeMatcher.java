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
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BiomeMatcher implements IPlayerMatcher {
    private final Set<Biome.BiomeCategory> categories;

    public BiomeMatcher(Set<Biome.BiomeCategory> categories) {
        this.categories = ImmutableSet.copyOf(categories);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(categories.size());
        categories.forEach(buffer::writeEnum);
    }

    @Override
    public JsonElement toJson() {
        JsonArray cats = new JsonArray();
        categories.forEach(cat -> cats.add(cat.name()));
        return cats;
    }

    @Override
    public void addDescription(Player player, List<Component> tooltip) {
        if (!categories.isEmpty()) {
            List<Component> items = categories.stream().map(cat -> new TextComponent(cat.getName())).collect(Collectors.toList());
            standardTooltip(player, tooltip, xlate("pneumaticcraft.playerFilter.biomes"), items);
        }
    }

    @Override
    public boolean test(Player playerEntity) {
        return categories.isEmpty() || categories.contains(playerEntity.level.getBiome(playerEntity.blockPosition()).getBiomeCategory());
    }

    public static class Factory implements MatcherFactory<BiomeMatcher> {
        @Override
        public BiomeMatcher fromJson(JsonElement json) {
            Set<Biome.BiomeCategory> categories = EnumSet.noneOf(Biome.BiomeCategory.class);
            json.getAsJsonArray().forEach(element -> {
                Biome.BiomeCategory cat = Biome.BiomeCategory.byName(element.getAsString());
                //noinspection ConstantConditions
                if (cat == null) {  // yes, the category can be null here... shut up Intellij
                    throw new JsonSyntaxException("unknown biome category: " + element.getAsString());
                }
                categories.add(cat);
            });
            return new BiomeMatcher(categories);
        }

        @Override
        public BiomeMatcher fromBytes(FriendlyByteBuf buffer) {
            Set<Biome.BiomeCategory> categories = EnumSet.noneOf(Biome.BiomeCategory.class);
            int nCats = buffer.readVarInt();
            for (int i = 0; i < nCats; i++) {
                categories.add(buffer.readEnum(Biome.BiomeCategory.class));
            }
            return new BiomeMatcher(categories);
        }
    }
}
