package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeNotifyDeal;
import me.desht.pneumaticcraft.common.network.PacketUtil;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
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

    public AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, PlayerEntity offeringPlayer) {
        this(id, input, output, offeringPlayer.getGameProfile().getName(), offeringPlayer.getGameProfile().getId());
    }

    public AmadronPlayerOffer(ResourceLocation id, AmadronTradeResource input, AmadronTradeResource output, String playerName, UUID playerId) {
        super(id, input, output, true, 0, -1);
        offeringPlayerName = playerName;
        offeringPlayerId = playerId;
        inStock = 0;
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
        AmadronPlayerOffer reversed = new AmadronPlayerOffer(reversedId, getOutput(), getInput(), offeringPlayerName, offeringPlayerId);
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
    public String getVendor() {
        return offeringPlayerName;
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

    boolean payout() {
        boolean madePayment = false;
        TileEntity returning = getReturningTileEntity();
        if (pendingPayments > 0) {
            int paying = Math.min(pendingPayments, 50);
            switch (getInput().getType()) {
                case ITEM:
                    paying = getInput().findSpaceInItemOutput(IOHelper.getInventoryForTE(returning), paying);
                    break;
                case FLUID:
                    paying = getInput().findSpaceInFluidOutput(IOHelper.getFluidHandlerForTE(returning), paying);
                    break;
            }
            if (paying > 0) {
                pendingPayments -= paying;
                madePayment = true;
                switch (getInput().getType()) {
                    case ITEM:
                        ItemStack deliveringItems = getInput().getItem();
                        int amount = deliveringItems.getCount() * paying;
                        List<ItemStack> stacks = new ArrayList<>();
                        while (amount > 0) {
                            ItemStack stack = ItemHandlerHelper.copyStackWithSize(deliveringItems, Math.min(amount, deliveringItems.getMaxStackSize()));
                            stacks.add(stack);
                            amount -= stack.getCount();
                        }
                        DroneRegistry.getInstance().deliverItemsAmazonStyle(returningPos, stacks.toArray(new ItemStack[0]));
                        break;
                    case FLUID:
                        FluidStack deliveringFluid = getInput().getFluid().copy();
                        deliveringFluid.setAmount(deliveringFluid.getAmount() * paying);
                        DroneRegistry.getInstance().deliverFluidAmazonStyle(returningPos, deliveringFluid);
                        break;
                }
            }
        }
        return madePayment;
    }

    /**
     * Return any unsold stock when an Amadron offer is removed.  If there's no space in the provider inventory
     * or the inventory is gone, items will be dumped on the ground.
     */
    public void returnStock() {
        while (inStock > 0) {
            int stock = Math.min(inStock, 64);
            inStock -= stock;
            switch (getOutput().getType()) {
                case ITEM:
                    ItemStack deliveringItems = getOutput().getItem();
                    int amount = deliveringItems.getCount() * stock;
                    List<ItemStack> stacks = new ArrayList<>();
                    while (amount > 0) {
                        ItemStack stack = ItemHandlerHelper.copyStackWithSize(deliveringItems, Math.min(amount, deliveringItems.getMaxStackSize()));
                        stacks.add(stack);
                        amount -= stack.getCount();
                    }
                    DroneRegistry.getInstance().deliverItemsAmazonStyle(providingPos, stacks.toArray(new ItemStack[0]));
                    break;
                case FLUID:
                    FluidStack deliveringFluid = getOutput().getFluid().copy();
                    deliveringFluid.setAmount(deliveringFluid.getAmount() * stock);
                    DroneRegistry.getInstance().deliverFluidAmazonStyle(providingPos, deliveringFluid);
                    break;
            }
        }
    }

    TileEntity getProvidingTileEntity() {
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

    GlobalPos getProvidingPos() {
        return providingPos;
    }

    @Override
    public void write(PacketBuffer buf) {
        input.writeToBuf(buf);
        output.writeToBuf(buf);
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
        AmadronPlayerOffer offer = new AmadronPlayerOffer(id,
                AmadronTradeResource.fromPacketBuf(buf), AmadronTradeResource.fromPacketBuf(buf),
                buf.readUtf(100), buf.readUUID()
        );
        if (buf.readBoolean()) {
            offer.setProvidingPosition(PacketUtil.readGlobalPos(buf));
        }
        if (buf.readBoolean()) {
            offer.setReturningPosition(PacketUtil.readGlobalPos(buf));
        }
        offer.inStock = buf.readVarInt();
        offer.pendingPayments = buf.readVarInt();
        return offer;
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
        AmadronOffer offer = AmadronOffer.fromJson(new ResourceLocation(JSONUtils.getAsString(json, "id")), json);
        AmadronPlayerOffer custom = new AmadronPlayerOffer(offer.getId(), offer.input, offer.output,
                json.get("offeringPlayerName").getAsString(), UUID.fromString(json.get("offeringPlayerId").getAsString()));
        custom.inStock = json.get("inStock").getAsInt();
        custom.pendingPayments = json.get("pendingPayments").getAsInt();
        if (json.has("providingPos")) {
            custom.providingPos = GlobalPosHelper.fromJson(json.get("providingPos").getAsJsonObject());
        }
        if (json.has("returningPos")) {
            custom.returningPos = GlobalPosHelper.fromJson(json.get("returningPos").getAsJsonObject());
        }
        return custom;
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
