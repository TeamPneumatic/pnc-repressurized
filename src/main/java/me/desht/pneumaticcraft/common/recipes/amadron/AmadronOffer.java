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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.playerfilter.PlayerFilter;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AmadronOffer extends AmadronRecipe {
    @Nonnull
    private final ResourceLocation offerId;
    @Nonnull
    protected final AmadronTradeResource input;
    @Nonnull
    protected final AmadronTradeResource output;
    protected final PlayerFilter whitelist;
    protected final PlayerFilter blacklist;
    private final boolean staticOffer;
    private final boolean villagerTrade;
    private final int tradeLevel;  // determines rarity of periodic offers (1 = common, 5 = very rare)
    private final int maxStock; // max number of trades available; negative number indicates unlimited trades (or a player trade)

    protected int inStock; // current number of trades available; gets reset to max when offer is shuffled in (except for player trades)

    public AmadronOffer(@Nonnull ResourceLocation offerId, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output,
                        boolean staticOffer, boolean villagerTrade,
                        int tradeLevel, int maxStock, int inStock, PlayerFilter whitelist, PlayerFilter blacklist) {
        this.offerId = offerId;
        this.input = Objects.requireNonNull(input).validate();
        this.output = Objects.requireNonNull(output).validate();
        this.staticOffer = staticOffer;
        this.villagerTrade = villagerTrade;
        this.tradeLevel = tradeLevel;
        this.maxStock = maxStock;
        this.inStock = inStock;
        this.whitelist = whitelist;
        this.blacklist = blacklist;

    }

    public static AmadronOffer villagerTrade(ResourceLocation offerId, MerchantOffer merchantOffer, int level) {
        return new AmadronOffer(offerId,
                AmadronTradeResource.of(merchantOffer.getBaseCostA()),
                AmadronTradeResource.of(merchantOffer.getResult()),
                false,
                true,
                level,
                merchantOffer.getMaxUses(),
                merchantOffer.getMaxUses(),
                PlayerFilter.YES,
                PlayerFilter.NO
        );
    }

    public ResourceLocation getOfferId() {
        return offerId;
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
        return staticOffer;
    }

    @Override
    public int getTradeLevel() {
        return tradeLevel;
    }

    public boolean equivalentTo(AmadronPlayerOffer offer) {
        return input.equivalentTo(offer.getInput()) && output.equivalentTo(offer.getOutput());
    }

    @Override
    public Component getVendorName() {
        return xlate(villagerTrade ? "pneumaticcraft.gui.amadron.villager" : "pneumaticcraft.gui.amadron");
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
        this.inStock = Mth.clamp(inStock, 0, max);
    }

    public void onTrade(int tradingAmount, String buyingPlayer) {
    }

    public static AmadronOffer offerFromBuf(ResourceLocation id, FriendlyByteBuf buf) {
        return ModRecipeSerializers.AMADRON_OFFERS.get().fromNetwork(buf);
    }

    @Override
    public String toString() {
        return String.format("[in = %s, out = %s, level = %d, maxStock = %d]", input, output, tradeLevel, maxStock);
    }

    /**
     * Get a player-friendly description of the offer
     * @return a description string
     */
    public Component getDescription() {
        return Component.literal(String.format("[%s -> %s]", input, output));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmadronOffer that)) return false;
        return input.equals(that.input) && output.equals(that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.AMADRON_OFFERS.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.AMADRON.get();
    }

    @Override
    public int getMaxStock() {
        return maxStock;
    }

    @Override
    public boolean isVillagerTrade() {
        return villagerTrade;
    }

    @Override
    public PlayerFilter getWhitelist() {
        return whitelist;
    }

    @Override
    public PlayerFilter getBlacklist() {
        return blacklist;
    }

    @Override
    public boolean isUsableByPlayer(Player player) {
        return whitelist.test(player) && !blacklist.test(player);
    }

    @Override
    public void addAvailabilityData(Player player, List<Component> curTip) {
        if (whitelist.isReal()) {
            Component suffix = xlate("pneumaticcraft.gui.misc." + (whitelist.matchAll() ? "all" : "any"));
            curTip.add(xlate("pneumaticcraft.playerFilter.whitelist").append(" (").append(suffix).append(")").withStyle(ChatFormatting.GOLD));
            whitelist.getDescription(player, curTip);
        }
        if (blacklist.isReal()) {
            Component suffix = xlate("pneumaticcraft.gui.misc." + (blacklist.matchAll() ? "all" : "any"));
            curTip.add(xlate("pneumaticcraft.playerFilter.blacklist").append(" (").append(suffix).append(")").withStyle(ChatFormatting.GOLD));
            blacklist.getDescription(player, curTip);
        }
    }

    @Override
    public boolean isLocationLimited() {
        return whitelist.isReal() || blacklist.isReal();
    }

    public void write(FriendlyByteBuf buf) {
        ModRecipeSerializers.AMADRON_OFFERS.get().toNetwork(buf, this);
    }

    public static class Serializer<T extends AmadronOffer> implements RecipeSerializer<T> {
        private final IFactory<T> factory;
        private final Codec<T> codec;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;

            codec = RecordCodecBuilder.create(inst -> inst.group(
                    ResourceLocation.CODEC.fieldOf("offer_id").forGetter(AmadronOffer::getOfferId),
                    AmadronTradeResource.CODEC.fieldOf("input").forGetter(AmadronOffer::getInput),
                    AmadronTradeResource.CODEC.fieldOf("output").forGetter(AmadronOffer::getOutput),
                    Codec.BOOL.optionalFieldOf("static", true).forGetter(AmadronOffer::isStaticOffer),
                    Codec.BOOL.optionalFieldOf("villager_trade", false).forGetter(AmadronOffer::isVillagerTrade),
                    Codec.INT.optionalFieldOf("level", 1).forGetter(AmadronOffer::getTradeLevel),
                    Codec.INT.optionalFieldOf("maxStock", -1).forGetter(AmadronOffer::getMaxStock),
                    Codec.INT.optionalFieldOf("inStock", -1).forGetter(AmadronOffer::getStock),
                    PlayerFilter.CODEC.optionalFieldOf("whitelist", PlayerFilter.YES).forGetter(AmadronOffer::getWhitelist),
                    PlayerFilter.CODEC.optionalFieldOf("blacklist", PlayerFilter.NO).forGetter(AmadronOffer::getBlacklist)
            ).apply(inst, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            return factory.create(
                    buffer.readResourceLocation(),
                    AmadronTradeResource.fromNetwork(buffer),
                    AmadronTradeResource.fromNetwork(buffer),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readByte(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    PlayerFilter.fromNetwork(buffer),
                    PlayerFilter.fromNetwork(buffer)
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, T recipe) {
            buf.writeResourceLocation(recipe.getOfferId());
            recipe.getInput().toNetwork(buf);
            recipe.getOutput().toNetwork(buf);
            buf.writeBoolean(recipe.isStaticOffer());
            buf.writeBoolean(recipe.isVillagerTrade());
            buf.writeByte(recipe.getTradeLevel());
            buf.writeVarInt(recipe.getMaxStock());
            buf.writeVarInt(recipe.getStock());
            recipe.getWhitelist().toNetwork(buf);
            recipe.getBlacklist().toNetwork(buf);
        }

        public interface IFactory<T extends AmadronRecipe> {
            T create(ResourceLocation offerId, AmadronTradeResource input, AmadronTradeResource output, boolean isStaticOffer,
                     boolean villagerTrade, int level, int maxStock, int inStock, PlayerFilter whitelist, PlayerFilter blacklist);
        }
    }
}
