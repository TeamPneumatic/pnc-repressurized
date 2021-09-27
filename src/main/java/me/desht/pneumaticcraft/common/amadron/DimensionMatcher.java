package me.desht.pneumaticcraft.common.amadron;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class DimensionMatcher implements AmadronPlayerFilter.Matcher {
    private final Set<ResourceLocation> dimensionIds;

    public DimensionMatcher(Set<ResourceLocation> dimensionIds) {
        this.dimensionIds = ImmutableSet.copyOf(dimensionIds);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
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
    public void addDescription(List<ITextComponent> tooltip) {
        tooltip.add(xlate("pneumaticcraft.gui.amadron.location.dimensions").withStyle(TextFormatting.GOLD));
        dimensionIds.forEach(dimId -> tooltip.add(new StringTextComponent("  ")
                .append(GuiConstants.bullet().append(dimId.toString()).withStyle(TextFormatting.GOLD))));
    }

    @Override
    public boolean test(PlayerEntity playerEntity) {
        return dimensionIds.isEmpty() || dimensionIds.contains(playerEntity.level.dimension().location());
    }

    static class Factory implements AmadronPlayerFilter.MatcherFactory<DimensionMatcher> {
        @Override
        public DimensionMatcher fromJson(JsonElement json) {
            Set<ResourceLocation> dimensionIds = new ObjectOpenHashSet<>();
            json.getAsJsonArray().forEach(el -> dimensionIds.add(new ResourceLocation(el.getAsString())));
            return new DimensionMatcher(dimensionIds);
        }

        @Override
        public DimensionMatcher fromBytes(PacketBuffer buffer) {
            int n = buffer.readVarInt();
            Set<ResourceLocation> dimensionIds = new ObjectOpenHashSet<>();
            for (int i = 0; i < n; i++) {
                dimensionIds.add(buffer.readResourceLocation());
            }
            return new DimensionMatcher(dimensionIds);
        }
    }
}
