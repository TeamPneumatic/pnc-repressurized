package me.desht.pneumaticcraft.common.amadron;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiPredicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class LocationFilter implements BiPredicate<World, BlockPos> {
    public static final LocationFilter YES = new LocationFilter(Op.YES, Collections.emptySet(), EnumSet.noneOf(Biome.Category.class));
    public static final LocationFilter NO = new LocationFilter(Op.NO, Collections.emptySet(), EnumSet.noneOf(Biome.Category.class));

    private enum Op { YES, NO, AND, OR }

    private final Set<ResourceLocation> dimensionIds;
    private final Set<Biome.Category> categories;
    private final Op op;

    private LocationFilter(Op op, @Nonnull Set<ResourceLocation> dimensionIds, @Nonnull Set<Biome.Category> categories) {
        this.op = op;
        this.dimensionIds = dimensionIds;
        this.categories = categories;
    }

    public boolean isReal() {
        return op != Op.YES && op != Op.NO;
    }

    public static LocationFilter fromJson(JsonObject json) {
        for (String opStr : new String[] { "or", "and" }) {
            if (json.has(opStr)) {
                Op op = Op.valueOf(opStr.toUpperCase(Locale.ROOT));
                JsonObject jsonSub = json.getAsJsonObject(opStr);
                Set<ResourceLocation> dimensionIds = new ObjectOpenHashSet<>();
                if (jsonSub.has("dimensions")) {
                    JSONUtils.getAsJsonArray(jsonSub, "dimensions")
                            .forEach(element -> dimensionIds.add(new ResourceLocation(element.getAsString())));
                }
                Set<Biome.Category> categories = EnumSet.noneOf(Biome.Category.class);
                if (jsonSub.has("biome_categories")) {
                    JSONUtils.getAsJsonArray(jsonSub, "biome_categories")
                            .forEach(element -> {
                                Biome.Category cat = Biome.Category.byName(element.getAsString());
                                //noinspection ConstantConditions
                                if (cat == null) {  // yes, the category can be null here... shut up Intellij
                                    throw new JsonSyntaxException("unknown biome category: " + element.getAsString());
                                }
                                categories.add(cat);
                            });
                }
                if (categories.isEmpty() && dimensionIds.isEmpty()) {
                    throw new JsonSyntaxException("must provide at least one of 'dimensions' or 'biome_categories'!");
                }
                return new LocationFilter(op, dimensionIds, categories);
            }
        }
        throw new JsonSyntaxException("must provide one of 'and' or 'or'!");
    }

    public static LocationFilter fromBytes(PacketBuffer buffer) {
        Op op = buffer.readEnum(Op.class);

        Set<ResourceLocation> dimensionIds = new ObjectOpenHashSet<>();
        int nDims = buffer.readVarInt();
        for (int i = 0; i < nDims; i++) {
            dimensionIds.add(buffer.readResourceLocation());
        }

        Set<Biome.Category> categories = EnumSet.noneOf(Biome.Category.class);
        int nCats = buffer.readVarInt();
        for (int i = 0; i < nCats; i++) {
            categories.add(buffer.readEnum(Biome.Category.class));
        }

        return new LocationFilter(op, dimensionIds, categories);
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeEnum(op);
        buffer.writeVarInt(dimensionIds.size());
        dimensionIds.forEach(buffer::writeResourceLocation);
        buffer.writeVarInt(categories.size());
        categories.forEach(buffer::writeEnum);
    }

    public JsonObject toJson() {

        JsonObject sub = new JsonObject();
        JsonArray dims = new JsonArray();
        dimensionIds.forEach(id -> dims.add(id.toString()));
        sub.add("dimensions", dims);
        JsonArray cats = new JsonArray();
        categories.forEach(cat -> cats.add(cat.name()));
        sub.add("biome_categories", cats);

        JsonObject res = new JsonObject();
        res.add(op.name(), sub);
        return res;
    }

    @Override
    public boolean test(World world, BlockPos blockPos) {
        Biome biome = world.getBiome(blockPos);
        ResourceLocation dimId = world.dimension().location();

        switch (op) {
            case YES: return true;
            case NO: return false;
            case OR: return dimensionIds.contains(dimId) || categories.contains(biome.getBiomeCategory());
            case AND: return dimensionIds.contains(dimId) && categories.contains(biome.getBiomeCategory());
        }
        return false;
    }

    public void getDescription(List<ITextComponent> tooltip) {
        if (isReal()) {
            if (!dimensionIds.isEmpty()) {
                tooltip.add(xlate("pneumaticcraft.gui.amadron.location.dimensions").withStyle(TextFormatting.GOLD));
                dimensionIds.forEach(dimId -> tooltip.add(new StringTextComponent("  ")
                        .append(GuiConstants.bullet().append(dimId.toString()).withStyle(TextFormatting.GOLD))));
            }
            if (!dimensionIds.isEmpty() && !categories.isEmpty()) {
                tooltip.add(new StringTextComponent("-- " + op + " --").withStyle(TextFormatting.GOLD));
            }
            if (!categories.isEmpty()) {
                tooltip.add(xlate("pneumaticcraft.gui.amadron.location.biomes").withStyle(TextFormatting.GOLD));
                categories.forEach(cat -> tooltip.add(new StringTextComponent("  ")
                        .append(GuiConstants.bullet().append(cat.getName()).withStyle(TextFormatting.GOLD))));
            }
        }

    }
}
