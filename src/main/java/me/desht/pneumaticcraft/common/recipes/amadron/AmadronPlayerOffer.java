package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.amadron.AmadronPlayerFilter;
import me.desht.pneumaticcraft.common.amadron.AmadronUtil;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeNotifyDeal;
import me.desht.pneumaticcraft.common.network.PacketUtil;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Extended Amadron offer used for player-player trading.
 */
public class AmadronPlayerOffer extends AmadronOffer {
    private final String offeringPlayerName;
    private UUID offeringPlayerId;
    private GlobalPos providingPos;
    private GlobalPos returningPos;
    private int pendingPayments;
    private TileEntity cachedInput, cachedOutput;

    private AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, String playerName, UUID playerId, AmadronPlayerFilter whitelist, AmadronPlayerFilter blacklist) {
        super(id, input, output, true, 0, -1, 0, whitelist, blacklist);
        offeringPlayerName = playerName;
        offeringPlayerId = playerId;
        inStock = 0;
    }

    public AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, PlayerEntity offeringPlayer, AmadronPlayerFilter whitelist, AmadronPlayerFilter blacklist) {
        this(id, input, output, offeringPlayer.getGameProfile().getName(), offeringPlayer.getGameProfile().getId(), whitelist, blacklist);
    }

    public AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, PlayerEntity player) {
        this(id, input, output, player, AmadronPlayerFilter.YES, AmadronPlayerFilter.NO);
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

    /**
     * Create an offer which is the reverse of this offer.  Used for Amadron restocking and returning of unsold stock
     * when a player offer is removed.  Note that the reversed offer has "_rev" appended to its ID; this new offer is
     * available via {@link AmadronOfferManager#getOffer(ResourceLocation)} (assuming the original offer is) but will
     * never appear in the active offers list.  If this is called on an already-reversed offer, the original offer will
     * be returned.
     * @return a new Amadron offer with the input and output swapped
     */
    public AmadronPlayerOffer getReversedOffer() {
        ResourceLocation reversedId = getReversedId(getId());
        AmadronPlayerOffer reversed = new AmadronPlayerOffer(reversedId, getOutput(), getInput(), offeringPlayerName, offeringPlayerId, whitelist, blacklist);
        reversed.providingPos = providingPos;
        reversed.returningPos = returningPos;
        reversed.inStock = this.inStock;
        reversed.pendingPayments = this.pendingPayments;
        return reversed;
    }

    public void updatePlayerId() {
        PlayerEntity player = PneumaticCraftUtils.getPlayerFromName(offeringPlayerName);
        if (player != null) offeringPlayerId = player.getGameProfile().getId();
    }

    public void addPayment(int payment) {
        pendingPayments += payment;
    }

    @Override
    public ITextComponent getVendorName() {
        return new StringTextComponent(offeringPlayerName);
    }

    public UUID getPlayerId() {
        return offeringPlayerId;
    }

    @Override
    public void onTrade(int tradingAmount, String buyingPlayer) {
        PlayerEntity player = PneumaticCraftUtils.getPlayerFromId(offeringPlayerId);
        if (player != null && PNCConfig.Common.Amadron.notifyOfDealMade) {
            NetworkHandler.sendToPlayer(new PacketAmadronTradeNotifyDeal(this, tradingAmount, buyingPlayer), (ServerPlayerEntity) player);
        }
    }

    @Override
    public boolean isRemovableBy(PlayerEntity player) {
        return getPlayerId().equals(player.getUUID());
    }

    public void notifyRestock() {
        PlayerEntity player = PneumaticCraftUtils.getPlayerFromId(getPlayerId());
        if (player != null) {
            player.displayClientMessage(xlate("pneumaticcraft.message.amadron.amadronRestocked", getDescription(), getStock()), false);
        }
    }

    public boolean payout() {
        TileEntity returning = getReturningTileEntity();
        if (pendingPayments > 0) {
            final int pay0 = Math.min(pendingPayments, 50);
            int paying = getInput().apply(
                    itemStack -> getInput().findSpaceInItemOutput(IOHelper.getInventoryForTE(returning), pay0),
                    fluidStack -> getInput().findSpaceInFluidOutput(IOHelper.getFluidHandlerForTE(returning), pay0)
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

    public TileEntity getProvidingTileEntity() {
        if (cachedInput == null || cachedInput.isRemoved()) {
            if (providingPos != null) {
                cachedInput = GlobalPosHelper.getTileEntity(providingPos);
            }
        }
        return cachedInput;
    }

    TileEntity getReturningTileEntity() {
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
    public void write(PacketBuffer buf) {
        super.write(buf);

        buf.writeUtf(offeringPlayerName);
        buf.writeUUID(offeringPlayerId);
        buf.writeBoolean(providingPos != null);
        if (providingPos != null) {
            PacketUtil.writeGlobalPos(buf, providingPos);
        }
        buf.writeBoolean(returningPos != null);
        if (returningPos != null) {
            PacketUtil.writeGlobalPos(buf, returningPos);
        }
        buf.writeVarInt(inStock);
        buf.writeVarInt(pendingPayments);
    }

    public static AmadronPlayerOffer playerOfferFromBuf(ResourceLocation id, PacketBuffer buf) {
        AmadronRecipe recipe = ModRecipes.AMADRON_OFFERS.get().fromNetwork(id, buf);

        if (recipe instanceof AmadronOffer) {
            AmadronOffer offer = (AmadronOffer) recipe;
            AmadronPlayerOffer playerOffer = new AmadronPlayerOffer(offer.getId(),
                    offer.getInput(), offer.getOutput(), buf.readUtf(100), buf.readUUID(),
                    offer.whitelist, offer.blacklist);
            if (buf.readBoolean()) {
                playerOffer.setProvidingPosition(PacketUtil.readGlobalPos(buf));
            }
            if (buf.readBoolean()) {
                playerOffer.setReturningPosition(PacketUtil.readGlobalPos(buf));
            }
            playerOffer.inStock = buf.readVarInt();
            playerOffer.pendingPayments = buf.readVarInt();
            return playerOffer;
        }
        return null;  // shouldn't happen
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        super.toJson(json);
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

    public static AmadronPlayerOffer fromJson(JsonObject json) throws CommandSyntaxException {
        ResourceLocation id = new ResourceLocation(JSONUtils.getAsString(json, "id"));
        AmadronRecipe recipe = ModRecipes.AMADRON_OFFERS.get().fromJson(id, json);
        if (recipe instanceof AmadronOffer) {
            AmadronOffer offer = (AmadronOffer) recipe;
            AmadronPlayerOffer playerOffer = new AmadronPlayerOffer(offer.getId(), offer.getInput(), offer.getOutput(),
                    json.get("offeringPlayerName").getAsString(), UUID.fromString(json.get("offeringPlayerId").getAsString()),
                    offer.whitelist, offer.blacklist);

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
        return null;
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
        if (o instanceof AmadronPlayerOffer) {
            AmadronPlayerOffer offer = (AmadronPlayerOffer) o;
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
                new ResourceLocation(s.replaceFirst("_rev$", "")):
                new ResourceLocation(s + "_rev");
    }
}
