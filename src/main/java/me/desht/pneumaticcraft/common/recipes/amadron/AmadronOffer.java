package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
    private final int maxStock; // max number of trades available; negative number indicates unlimited trades (or a player trade)

    protected int inStock; // current number of trades available; gets reset to max when offer is shuffled in (except for player trades)
    private boolean isVillagerTrade = false;

    public AmadronOffer(ResourceLocation id, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output, boolean isStaticOffer, int tradeLevel, int maxStock, int inStock) {
        super(id);
        this.input = input.validate();
        this.output = output.validate();
        this.isStaticOffer = isStaticOffer;
        this.tradeLevel = tradeLevel;
        this.maxStock = maxStock;
        this.inStock = inStock;
    }

    public AmadronOffer(ResourceLocation id, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output, boolean isStaticOffer, int tradeLevel, int maxStock) {
        this(id, input, output, isStaticOffer, tradeLevel, maxStock, maxStock);
    }

    public AmadronOffer(ResourceLocation id, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output, boolean isStaticOffer, int tradeLevel) {
        this(id, input, output, isStaticOffer, tradeLevel, -1);
    }

    @Override
    public AmadronTradeResource getInput() {
        return input;
    }

    @Override
    public AmadronTradeResource getOutput() {
        return output;
    }

    @Override
    public boolean isStaticOffer() {
        return isStaticOffer;
    }

    @Override
    public int getTradeLevel() {
        return tradeLevel;
    }

    public AmadronOffer setVillagerTrade() {
        isVillagerTrade = true;
        return this;
    }

    public boolean equivalentTo(AmadronPlayerOffer offer) {
        return input.equivalentTo(offer.getInput()) && output.equivalentTo(offer.getOutput());
    }

    @Override
    public String getVendor() {
        return xlate(isVillagerTrade ? "pneumaticcraft.gui.amadron.villager" : "pneumaticcraft.gui.amadron").getString();
    }

    @Override
    public int getStock() {
        return inStock;
    }

    @Override
    public void setStock(int inStock) {
        int max = maxStock > 0 ? maxStock : Integer.MAX_VALUE;
        if (inStock < 0 || inStock > max) {
            Log.warning("Amadron Offer %s: new stock %d out of range (0,%d) - clamped", this, inStock, maxStock);
        }
        this.inStock = MathHelper.clamp(inStock, 0, max);
    }

    public void onTrade(int tradingAmount, String buyingPlayer) {
    }

    @Override
    public void write(PacketBuffer buf) {
        input.writeToBuf(buf);
        output.writeToBuf(buf);
        buf.writeBoolean(isStaticOffer);
        buf.writeByte(tradeLevel);
        buf.writeVarInt(maxStock);
        buf.writeVarInt(inStock);
    }

    public static AmadronOffer offerFromBuf(ResourceLocation id, PacketBuffer buf) {
        return new AmadronOffer(id,
                AmadronTradeResource.fromPacketBuf(buf),
                AmadronTradeResource.fromPacketBuf(buf),
                buf.readBoolean(),
                buf.readByte(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public JsonObject toJson(JsonObject json) {
        json.addProperty("id", getId().toString());
        json.add("input", input.toJson());
        json.add("output", output.toJson());
        json.addProperty("static", isStaticOffer);
        json.addProperty("level", tradeLevel);
        if (maxStock > 0) json.addProperty("maxStock", maxStock);

        return json;
    }

    public static AmadronOffer fromJson(ResourceLocation id, JsonObject json) throws CommandSyntaxException {
        return new AmadronOffer(id,
                AmadronTradeResource.fromJson(json.getAsJsonObject("input")),
                AmadronTradeResource.fromJson(json.getAsJsonObject("output")),
                JSONUtils.getBoolean(json, "static", true),
                JSONUtils.getInt(json, "level", 1),
                JSONUtils.getInt(json, "maxStock", -1)
        );
    }

    @Override
    public String toString() {
        return String.format("[id = %s, in = %s, out = %s, level = %d, maxStock = %d]", getId().toString(), input.toString(), output.toString(), tradeLevel, maxStock);
    }

    /**
     * Get a player-friendly description of the offer
     * @return a description string
     */
    public ITextComponent getDescription() {
        return new StringTextComponent(String.format("[%s -> %s]", input.toString(), output.toString()));
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

    @Override
    public int getMaxStock() {
        return maxStock;
    }

    public static class Serializer<T extends AmadronRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json) {
            try {
                int max = JSONUtils.getInt(json, "maxStock", -1);
                return factory.create(recipeId,
                        AmadronTradeResource.fromJson(json.getAsJsonObject("input")),
                        AmadronTradeResource.fromJson(json.getAsJsonObject("output")),
                        JSONUtils.getBoolean(json, "static", true),
                        JSONUtils.getInt(json, "level", 1),
                        max, max
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
                    buffer.readByte(),
                    buffer.readVarInt(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends AmadronRecipe> {
            T create(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, boolean isStaticOffer, int level, int maxStock, int inStock);
        }
    }
}
