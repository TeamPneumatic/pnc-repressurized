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
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.amadron.AmadronUtil;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.DroneRegistry;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeNotifyDeal;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.playerfilter.PlayerFilter;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Extended Amadron offer used for player-player trading.
 */
public class AmadronPlayerOffer extends AmadronOffer {
    public static final StreamCodec<RegistryFriendlyByteBuf, AmadronPlayerOffer> STREAM_CODEC = StreamCodec.of(
            (buf, playerOffer) -> playerOffer.write(buf),
            AmadronPlayerOffer::playerOfferFromBuf
    );

    private final String offeringPlayerName;
    private UUID offeringPlayerId;
    private GlobalPos providingPos;
    private GlobalPos returningPos;
    private int pendingPayments;
    private BlockEntity cachedInput, cachedOutput;

    private AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, String playerName, UUID playerId, PlayerFilter whitelist, PlayerFilter blacklist) {
        super(id, input, output, true, false, 0, -1, -1, whitelist, blacklist);
        offeringPlayerName = playerName;
        offeringPlayerId = playerId;
        inStock = 0;
    }

    public AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, Player offeringPlayer, PlayerFilter whitelist, PlayerFilter blacklist) {
        this(id, input, output, offeringPlayer.getGameProfile().getName(), offeringPlayer.getGameProfile().getId(), whitelist, blacklist);
    }

    public AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, Player player) {
        this(id, input, output, player, PlayerFilter.YES, PlayerFilter.NO);
    }

    public AmadronPlayerOffer setProvidingPosition(GlobalPos pos) {
        providingPos = pos;
        cachedInput = null;
        return this;
    }

    public AmadronPlayerOffer setReturningPosition(GlobalPos pos) {
        returningPos = pos;
        cachedOutput = null;
        return this;
    }

    @Override
    public OfferType getOfferType() {
        return OfferType.PLAYER;
    }

    /**
     * Create an offer which is the reverse of this offer.  Used for Amadron restocking and returning of unsold stock
     * when a player offer is removed.  Note that the reversed offer has "_rev" appended to its ID; this new offer is
     * available via {@link AmadronOfferManager#getOffer(ResourceLocation)} (assuming the original offer is) but will
     * never appear in the active offers list.  If this is called on an already-reversed offer, the original offer will
     * be returned.
     * @return a new Amadron offer with the input and output swapped
     */
    public AmadronPlayerOffer getReversedOffer() {
        ResourceLocation reversedId = getReversedId(getOfferId());
        AmadronPlayerOffer reversed = new AmadronPlayerOffer(reversedId, getOutput(), getInput(), offeringPlayerName, offeringPlayerId, whitelist, blacklist);
        reversed.providingPos = providingPos;
        reversed.returningPos = returningPos;
        reversed.inStock = this.inStock;
        reversed.pendingPayments = this.pendingPayments;
        return reversed;
    }

    public void updatePlayerId() {
        ServerPlayer player = PneumaticCraftUtils.getPlayerFromName(offeringPlayerName);
        if (player != null) offeringPlayerId = player.getGameProfile().getId();
    }

    public void addPayment(int payment) {
        pendingPayments += payment;
    }

    @Override
    public Component getVendorName() {
        return Component.literal(offeringPlayerName);
    }

    public UUID getPlayerId() {
        return offeringPlayerId;
    }

    @Override
    public void onTrade(int tradingAmount, String buyingPlayer) {
        ServerPlayer player = PneumaticCraftUtils.getPlayerFromId(offeringPlayerId);
        if (player != null && ConfigHelper.common().amadron.notifyOfDealMade.get()) {
            NetworkHandler.sendToPlayer(new PacketAmadronTradeNotifyDeal(this, tradingAmount, buyingPlayer), player);
        }
    }

    @Override
    public boolean isRemovableBy(Player player) {
        return getPlayerId().equals(player.getUUID());
    }

    public void notifyRestock() {
        ServerPlayer player = PneumaticCraftUtils.getPlayerFromId(getPlayerId());
        if (player != null) {
            player.displayClientMessage(xlate("pneumaticcraft.message.amadron.amadronRestocked", getDescription(), getStock()), false);
        }
    }

    public boolean payout() {
        BlockEntity returning = getReturningTileEntity();
        if (pendingPayments > 0) {
            final int pay0 = Math.min(pendingPayments, 50);
            int paying = getInput().apply(
                    itemStack -> IOHelper.getInventoryForBlock(returning).map(h -> getInput().findSpaceInItemOutput(h, pay0)).orElse(0),
                    fluidStack -> IOHelper.getFluidHandlerForBlock(returning).map(h -> getInput().findSpaceInFluidOutput(h, pay0)).orElse(0)
            );
            if (paying > 0) {
                pendingPayments -= paying;
                getInput().accept(
                        itemStack -> DroneRegistry.getInstance().deliverItemsAmazonStyle(returningPos, AmadronUtil.buildStacks(itemStack, paying)),
                        fluidStack -> DroneRegistry.getInstance().deliverFluidAmazonStyle(returningPos, AmadronUtil.buildFluidStack(fluidStack, paying)));
                return true;
            }
        }
        return false;
    }

    /**
     * Return any unsold stock when an Amadron offer is removed.  If there's no space in the provider inventory
     * or the inventory is gone, items will be dumped on the ground.
     */
    public void returnStock() {
        while (inStock > 0) {
            int stock = Math.min(inStock, 64);
            inStock -= stock;
            getOutput().accept(
                    itemStack -> DroneRegistry.getInstance().deliverItemsAmazonStyle(providingPos, AmadronUtil.buildStacks(itemStack, stock)),
                    fluidStack -> DroneRegistry.getInstance().deliverFluidAmazonStyle(providingPos, AmadronUtil.buildFluidStack(fluidStack, stock))
            );
        }
    }

    public BlockEntity getProvidingTileEntity() {
        if (cachedInput == null || cachedInput.isRemoved()) {
            if (providingPos != null) {
                cachedInput = GlobalPosHelper.getTileEntity(providingPos);
            }
        }
        return cachedInput;
    }

    BlockEntity getReturningTileEntity() {
        if (cachedOutput == null || cachedOutput.isRemoved()) {
            if (returningPos != null) {
                cachedOutput = GlobalPosHelper.getTileEntity(returningPos);
            }
        }
        return cachedOutput;
    }

    public GlobalPos getProvidingPos() {
        return providingPos;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);

        buf.writeUtf(offeringPlayerName);
        buf.writeUUID(offeringPlayerId);
        buf.writeOptional(Optional.ofNullable(providingPos), GlobalPos.STREAM_CODEC);
        buf.writeOptional(Optional.ofNullable(returningPos), GlobalPos.STREAM_CODEC);
        buf.writeVarInt(inStock);
        buf.writeVarInt(pendingPayments);
    }

    public static AmadronPlayerOffer playerOfferFromBuf(RegistryFriendlyByteBuf buf) {
        AmadronRecipe recipe = ModRecipeSerializers.AMADRON_OFFERS.get().streamCodec().decode(buf);

        if (recipe instanceof AmadronOffer offer) {
            AmadronPlayerOffer playerOffer = new AmadronPlayerOffer(offer.getOfferId(),
                    offer.getInput(), offer.getOutput(), buf.readUtf(100), buf.readUUID(),
                    offer.whitelist, offer.blacklist);
            playerOffer.setProvidingPosition(buf.readOptional(GlobalPos.STREAM_CODEC).orElse(null));
            playerOffer.setReturningPosition(buf.readOptional(GlobalPos.STREAM_CODEC).orElse(null));
            playerOffer.inStock = buf.readVarInt();
            playerOffer.pendingPayments = buf.readVarInt();
            return playerOffer;
        }

        throw new IllegalStateException("recipe isn't an amadron offer?! impossible!");
    }

    public JsonObject toJson() {
        JsonObject json = ModRecipeSerializers.AMADRON_OFFERS.get().codec()
                .encode(this, JsonOps.INSTANCE, JsonOps.INSTANCE.mapBuilder())
                .build(new JsonObject())
                .resultOrPartial(s -> Log.error("can't create json: " + s))
                .orElseThrow()
                .getAsJsonObject();

        json.addProperty("offeringPlayerName", offeringPlayerName);
        json.addProperty("offeringPlayerId", offeringPlayerId.toString());
        json.addProperty("inStock", inStock);
        json.addProperty("pendingPayments", pendingPayments);
        if (providingPos != null) {
            json.add("providingPos", GlobalPosHelper.toJson(providingPos));
        }
        if (returningPos != null) {
            json.add("returningPos", GlobalPosHelper.toJson(returningPos));
        }
        return json;
    }

    public static AmadronPlayerOffer fromJson(JsonObject json) {
        AmadronOffer baseOffer = ModRecipeSerializers.AMADRON_OFFERS.get().codec()
                .decode(JsonOps.INSTANCE, JsonOps.INSTANCE.getMap(json).getOrThrow())
                .resultOrPartial(err -> { throw new JsonSyntaxException(err); })
                .orElseThrow(() -> new JsonSyntaxException("invalid json syntax"));

        AmadronPlayerOffer playerOffer = new AmadronPlayerOffer(baseOffer.getOfferId(), baseOffer.getInput(), baseOffer.getOutput(),
                json.get("offeringPlayerName").getAsString(), UUID.fromString(json.get("offeringPlayerId").getAsString()),
                baseOffer.whitelist, baseOffer.blacklist);

        playerOffer.inStock = json.get("inStock").getAsInt();
        playerOffer.pendingPayments = json.get("pendingPayments").getAsInt();
        if (json.has("providingPos")) {
            playerOffer.providingPos = GlobalPosHelper.fromJson(json.get("providingPos").getAsJsonObject());
        }
        if (json.has("returningPos")) {
            playerOffer.returningPos = GlobalPosHelper.fromJson(json.get("returningPos").getAsJsonObject());
        }
        return playerOffer;
    }

    @Override
    public boolean equivalentTo(AmadronPlayerOffer otherOffer) {
        return super.equivalentTo(otherOffer) && offeringPlayerId.equals(otherOffer.offeringPlayerId);
    }

    @Override
    public String toString() {
        return super.toString() + " - " + offeringPlayerName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AmadronPlayerOffer offer) {
            return super.equals(o) && offer.offeringPlayerId.equals(offeringPlayerId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + offeringPlayerId.hashCode();
    }

    public static ResourceLocation getReversedId(ResourceLocation id) {
        String s = id.toString();
        return s.endsWith("_rev") ?
                ResourceLocation.parse(s.replaceFirst("_rev$", "")):
                ResourceLocation.parse(s + "_rev");
    }
}
