package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AmadronOffer extends AmadronRecipe {
    protected final AmadronTradeResource input;
    protected final AmadronTradeResource output;
    private final boolean isStaticOffer;
    private final int tradeLevel;  // determines rarity of periodic offers (1 = common, 5 = very rare)

    public AmadronOffer(ResourceLocation id, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output, boolean isStaticOffer, int tradeLevel) {
        super(id);
        this.input = input.validate();
        this.output = output.validate();
        this.isStaticOffer = isStaticOffer;
        this.tradeLevel = tradeLevel;
    }

    @Override
    public AmadronTradeResource getInput() {
        return input;
    }

    @Override
    public AmadronTradeResource getOutput() {
        return output;
    }

    public boolean isStaticOffer() {
        return isStaticOffer;
    }

    @Override
    public int getTradeLevel() {
        return tradeLevel;
    }

    public boolean equivalentTo(AmadronPlayerOffer offer) {
        return input.equivalentTo(offer.getInput()) && output.equivalentTo(offer.getOutput());
    }

    @Override
    public String getVendor() {
        return xlate("pneumaticcraft.gui.amadron").getFormattedText();
    }

    public int getStock() {
        return -1;
    }

    public boolean passesQuery(String query) {
        String queryLow = query.toLowerCase();
        return getInput().getName().toLowerCase().contains(queryLow)
                || getOutput().getName().toLowerCase().contains(queryLow)
                || getVendor().toLowerCase().contains(queryLow);
    }

    public void onTrade(int tradingAmount, String buyingPlayer) {
    }

    @Override
    public void write(PacketBuffer buf) {
        input.writeToBuf(buf);
        output.writeToBuf(buf);
        buf.writeBoolean(isStaticOffer);
        buf.writeVarInt(tradeLevel);
    }

    public static AmadronOffer offerFromBuf(ResourceLocation id, PacketBuffer buf) {
        return new AmadronOffer(id,
                AmadronTradeResource.fromPacketBuf(buf),
                AmadronTradeResource.fromPacketBuf(buf),
                buf.readBoolean(),
                buf.readVarInt()
        );
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("id", getId().toString());
        object.add("input", input.toJson());
        object.add("output", output.toJson());
        object.addProperty("static", isStaticOffer);
        object.addProperty("level", tradeLevel);

        return object;
    }

    public static AmadronOffer fromJson(ResourceLocation id, JsonObject json) throws CommandSyntaxException {
        return new AmadronOffer(id,
                AmadronTradeResource.fromJson(json.getAsJsonObject("input")),
                AmadronTradeResource.fromJson(json.getAsJsonObject("output")),
                JSONUtils.getBoolean(json, "static", true),
                JSONUtils.getInt(json, "level", 1)
        );
    }

    @Override
    public String toString() {
        return String.format("[id = %s, in = %s, out = %s]", getId().toString(), input.toString(), output.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmadronOffer)) return false;
        AmadronOffer that = (AmadronOffer) o;
        return input.equals(that.input) &&
                output.equals(that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.AMADRON_OFFERS.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return PneumaticCraftRecipeType.AMADRON_OFFERS;
    }

    public static class Serializer<T extends AmadronRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json) {
            try {
                return factory.create(recipeId,
                        AmadronTradeResource.fromJson(json.getAsJsonObject("input")),
                        AmadronTradeResource.fromJson(json.getAsJsonObject("output")),
                        JSONUtils.getBoolean(json, "static", true),
                        JSONUtils.getInt(json, "level", 1)
                );
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(e.getMessage());
            }
        }

        @Nullable
        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
            return factory.create(recipeId,
                    AmadronTradeResource.fromPacketBuf(buffer),
                    AmadronTradeResource.fromPacketBuf(buffer),
                    buffer.readBoolean(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends AmadronRecipe> {
            T create(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, boolean isStaticOffer, int level);
        }
    }
}
