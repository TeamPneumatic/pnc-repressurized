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
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BiomeMatcher implements IPlayerMatcher {
    private final Set<TagKey<Biome>> tags;

    public BiomeMatcher(Set<TagKey<Biome>> tags) {
        this.tags = ImmutableSet.copyOf(tags);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(tags.size());
        tags.forEach(key -> buffer.writeResourceLocation(key.location()));
    }

    @Override
    public JsonElement toJson() {
        JsonArray tags = new JsonArray();
        this.tags.forEach(tag -> tags.add(tag.location().toString()));
        return tags;
    }

    @Override
    public void addDescription(Player player, List<Component> tooltip) {
        if (!tags.isEmpty()) {
            List<Component> items = tags.stream().map(tag -> Component.literal(tag.location().toString())).collect(Collectors.toList());
            standardTooltip(player, tooltip, xlate("pneumaticcraft.playerFilter.biomes"), items);
        }
    }

    @Override
    public boolean test(Player playerEntity) {
        return tags.isEmpty() || playerEntity.level().getBiome(playerEntity.blockPosition()).tags().anyMatch(tags::contains);
    }

    public static class Factory implements MatcherFactory<BiomeMatcher> {
        @Override
        public BiomeMatcher fromJson(JsonElement json) {
            Set<TagKey<Biome>> tags = new HashSet<>();
            json.getAsJsonArray().forEach(element -> {
                try {
                    TagKey<Biome> cat = TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), new ResourceLocation(element.getAsString()));
                    tags.add(cat);
                } catch (ResourceLocationException e) {
                    Log.error("invalid biome tag resource location: %s", element);
                }
            });
            return new BiomeMatcher(tags);
        }

        @Override
        public BiomeMatcher fromBytes(FriendlyByteBuf buffer) {
            Set<TagKey<Biome>> tags = new HashSet<>();
            int nTags = buffer.readVarInt();
            for (int i = 0; i < nTags; i++) {
                ResourceLocation rl = buffer.readResourceLocation();
                tags.add(TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), rl));
            }
            return new BiomeMatcher(tags);
        }
    }
}
