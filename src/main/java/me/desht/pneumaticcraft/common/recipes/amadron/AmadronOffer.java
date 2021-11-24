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

package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.util.PlayerFilter;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AmadronOffer extends AmadronRecipe {
    @Nonnull
    protected final AmadronTradeResource input;
    @Nonnull
    protected final AmadronTradeResource output;
    protected final PlayerFilter whitelist;
    protected final PlayerFilter blacklist;
    private final boolean isStaticOffer;
    private final int tradeLevel;  // determines rarity of periodic offers (1 = common, 5 = very rare)
    private final int maxStock; // max number of trades available; negative number indicates unlimited trades (or a player trade)

    protected int inStock; // current number of trades available; gets reset to max when offer is shuffled in (except for player trades)
    private boolean isVillagerTrade = false;

    public AmadronOffer(ResourceLocation id, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output, boolean isStaticOffer,
                        int tradeLevel, int maxStock, int inStock, PlayerFilter whitelist, PlayerFilter blacklist) {
        super(id);
        this.input = Objects.requireNonNull(input).validate();
        this.output = Objects.requireNonNull(output).validate();
        this.isStaticOffer = isStaticOffer;
        this.tradeLevel = tradeLevel;
        this.maxStock = maxStock;
        this.inStock = inStock;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public AmadronOffer(ResourceLocation id, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output, boolean isStaticOffer, int tradeLevel, int maxStock) {
        this(id, input, output, isStaticOffer, tradeLevel, maxStock, maxStock, PlayerFilter.YES, PlayerFilter.NO);
    }

    @Override
    @Nonnull
    public AmadronTradeResource getInput() {
        return input;
    }

    @Override
    @Nonnull
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
    public ITextComponent getVendorName() {
        return isVillagerTrade ? xlate("pneumaticcraft.gui.amadron.villager") : xlate("pneumaticcraft.gui.amadron");
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
        whitelist.toBytes(buf);
        blacklist.toBytes(buf);
    }

    public static AmadronRecipe offerFromBuf(ResourceLocation id, PacketBuffer buf) {
        return ModRecipes.AMADRON_OFFERS.get().fromNetwork(id, buf);
    }

    public JsonObject toJson(JsonObject json) {
        json.addProperty("id", getId().toString());
        json.add("input", input.toJson());
        json.add("output", output.toJson());
        json.addProperty("static", isStaticOffer);
        json.addProperty("level", tradeLevel);
        if (maxStock > 0) json.addProperty("maxStock", maxStock);
        if (whitelist.isReal()) json.add("whitelist", whitelist.toJson());
        if (blacklist.isReal()) json.add("blacklist", blacklist.toJson());

        return json;
    }

    @Override
    public String toString() {
        return String.format("[id = %s, in = %s, out = %s, level = %d, maxStock = %d]", getId().toString(), input, output, tradeLevel, maxStock);
    }

    /**
     * Get a player-friendly description of the offer
     * @return a description string
     */
    public ITextComponent getDescription() {
        return new StringTextComponent(String.format("[%s -> %s]", input, output));
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

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return whitelist.test(player) && !blacklist.test(player);
    }

    @Override
    public void addAvailabilityData(PlayerEntity player, List<ITextComponent> curTip) {
        if (whitelist.isReal()) {
            ITextComponent suffix = xlate("pneumaticcraft.gui.misc." + (whitelist.matchAll() ? "all" : "any"));
            curTip.add(xlate("pneumaticcraft.playerFilter.whitelist").append(" (").append(suffix).append(")").withStyle(TextFormatting.GOLD));
            whitelist.getDescription(player, curTip);
        }
        if (blacklist.isReal()) {
            ITextComponent suffix = xlate("pneumaticcraft.gui.misc." + (blacklist.matchAll() ? "all" : "any"));
            curTip.add(xlate("pneumaticcraft.playerFilter.blacklist").append(" (").append(suffix).append(")").withStyle(TextFormatting.GOLD));
            blacklist.getDescription(player, curTip);
        }
    }

    @Override
    public boolean isLocationLimited() {
        return whitelist.isReal() || blacklist.isReal();
    }

    public static class Serializer<T extends AmadronRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            try {
                int maxStock = JSONUtils.getAsInt(json, "maxStock", -1);
                return factory.create(recipeId,
                        AmadronTradeResource.fromJson(json.getAsJsonObject("input")),
                        AmadronTradeResource.fromJson(json.getAsJsonObject("output")),
                        JSONUtils.getAsBoolean(json, "static", true),
                        JSONUtils.getAsInt(json, "level", 1),
                        maxStock, maxStock,
                        json.has("whitelist") ? PlayerFilter.fromJson(json.getAsJsonObject("whitelist")) : PlayerFilter.YES,
                        json.has("blacklist") ? PlayerFilter.fromJson(json.getAsJsonObject("blacklist")) : PlayerFilter.NO
                );
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(e.getMessage());
            }
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            return factory.create(recipeId,
                    AmadronTradeResource.fromPacketBuf(buffer),
                    AmadronTradeResource.fromPacketBuf(buffer),
                    buffer.readBoolean(),
                    buffer.readByte(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    PlayerFilter.fromBytes(buffer),
                    PlayerFilter.fromBytes(buffer)
            );
        }

        @Override
        public void toNetwork(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends AmadronRecipe> {
            T create(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, boolean isStaticOffer, int level, int maxStock, int inStock, PlayerFilter whitelist, PlayerFilter blacklist);
        }
    }
}
