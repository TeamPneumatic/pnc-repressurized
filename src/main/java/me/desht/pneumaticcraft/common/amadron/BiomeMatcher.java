package me.desht.pneumaticcraft.common.amadron;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    public void addDescription(List<ITextComponent> tooltip) {
        if (!categories.isEmpty()) {
            tooltip.add(xlate("pneumaticcraft.gui.amadron.location.biomes").withStyle(TextFormatting.GOLD));
            categories.forEach(cat -> tooltip.add(new StringTextComponent("  ")
                    .append(GuiConstants.bullet().append(cat.getName()).withStyle(TextFormatting.GOLD))));
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
