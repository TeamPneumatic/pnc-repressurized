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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BiomeMatcher implements IPlayerMatcher {
    private final Set<Biome.Category> categories;

    public BiomeMatcher(Set<Biome.Category> categories) {
        this.categories = ImmutableSet.copyOf(categories);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
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
    public void addDescription(PlayerEntity player, List<ITextComponent> tooltip) {
        if (!categories.isEmpty()) {
            List<ITextComponent> items = categories.stream().map(cat -> new StringTextComponent(cat.getName())).collect(Collectors.toList());
            standardTooltip(player, tooltip, xlate("pneumaticcraft.playerFilter.biomes"), items);
        }
    }

    @Override
    public boolean test(PlayerEntity playerEntity) {
        return categories.isEmpty() || categories.contains(playerEntity.level.getBiome(playerEntity.blockPosition()).getBiomeCategory());
    }

    public static class Factory implements MatcherFactory<BiomeMatcher> {
        @Override
        public BiomeMatcher fromJson(JsonElement json) {
            Set<Biome.Category> categories = EnumSet.noneOf(Biome.Category.class);
            json.getAsJsonArray().forEach(element -> {
                Biome.Category cat = Biome.Category.byName(element.getAsString());
                //noinspection ConstantConditions
                if (cat == null) {  // yes, the category can be null here... shut up Intellij
                    throw new JsonSyntaxException("unknown biome category: " + element.getAsString());
                }
                categories.add(cat);
            });
            return new BiomeMatcher(categories);
        }

        @Override
        public BiomeMatcher fromBytes(PacketBuffer buffer) {
            Set<Biome.Category> categories = EnumSet.noneOf(Biome.Category.class);
            int nCats = buffer.readVarInt();
            for (int i = 0; i < nCats; i++) {
                categories.add(buffer.readEnum(Biome.Category.class));
            }
            return new BiomeMatcher(categories);
        }
    }
}
