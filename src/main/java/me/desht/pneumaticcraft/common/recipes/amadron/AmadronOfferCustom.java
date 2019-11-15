package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeNotifyDeal;
import me.desht.pneumaticcraft.common.network.PacketUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class AmadronOfferCustom extends AmadronOffer {
    private final String offeringPlayerName;
    private String offeringPlayerId;
    private GlobalPos providingPos;
    private GlobalPos returningPos;
    private int inStock;
    private int maxTrades = -1;
    private int pendingPayments;
    private TileEntity cachedInput, cachedOutput;

    public AmadronOfferCustom(TradeResource input, TradeResource output, PlayerEntity offeringPlayer) {
        this(input, output, offeringPlayer.getGameProfile().getName(), offeringPlayer.getGameProfile().getId().toString());
    }

    public AmadronOfferCustom(TradeResource input, TradeResource output, String playerName, String playerId) {
        super(input, output);
        offeringPlayerName = playerName;
        offeringPlayerId = playerId;
    }

    public AmadronOfferCustom setProvidingPosition(GlobalPos pos) {
        providingPos = pos;
        cachedInput = null;
        return this;
    }

    public AmadronOfferCustom setReturningPosition(GlobalPos pos) {
        returningPos = pos;
        cachedOutput = null;
        return this;
    }

    public AmadronOfferCustom invert() {
        TradeResource temp = input;
        input = output;
        output = temp;
        return this;
    }

    public AmadronOfferCustom copy() {
        CompoundNBT tag = new CompoundNBT();
        writeToNBT(tag);
        return loadFromNBT(tag);
    }

    public void updatePlayerId() {
        PlayerEntity player = PneumaticCraftUtils.getPlayerFromName(offeringPlayerName);
        if (player != null) offeringPlayerId = player.getGameProfile().getId().toString();
    }

    public void addStock(int stock) {
        inStock += stock;
    }

    @Override
    public int getStock() {
        return inStock;
    }

    public void addPayment(int payment) {
        pendingPayments += payment;
    }

    public void setMaxTrades(int maxTrades) {
        this.maxTrades = maxTrades;
    }

    @Override
    public String getVendor() {
        return offeringPlayerName;
    }

    public String getPlayerId() {
        return offeringPlayerId;
    }

    @Override
    public void onTrade(int tradingAmount, String buyingPlayer) {
        PlayerEntity player = PneumaticCraftUtils.getPlayerFromId(offeringPlayerId);
        if (player != null && PNCConfig.Common.Amadron.notifyOfDealMade) {
            NetworkHandler.sendToPlayer(new PacketAmadronTradeNotifyDeal(this, tradingAmount, buyingPlayer), (ServerPlayerEntity) player);
        }
    }

    void payout() {
        TileEntity returning = getReturningTileEntity();
        TileEntity provider = getProvidingTileEntity();
        if (pendingPayments > 0) {
            int paying = Math.min(pendingPayments, 50);
            paying = ContainerAmadron.capShoppingAmount(this, paying,
                    AmadronOfferManager.getItemHandler(provider), AmadronOfferManager.getItemHandler(returning),
                    AmadronOfferManager.getFluidHandler(provider), AmadronOfferManager.getFluidHandler(returning),
                    null);
            if (paying > 0) {
                pendingPayments -= paying;
                switch (getInput().getType()) {
                    case ITEM:
                        ItemStack deliveringItems = getInput().item;
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
                        FluidStack deliveringFluid = getInput().fluid.copy();
                        deliveringFluid.setAmount(deliveringFluid.getAmount() * paying);
                        DroneRegistry.getInstance().deliverFluidAmazonStyle(returningPos, deliveringFluid);
                        break;
                }
            }
        }
    }

    public void returnStock() {
        TileEntity provider = getProvidingTileEntity();
        TileEntity returning = getReturningTileEntity();
        invert();
        while (inStock > 0) {
            int stock = Math.min(inStock, 50);
            stock = ContainerAmadron.capShoppingAmount(this, stock,
                    AmadronOfferManager.getItemHandler(returning), AmadronOfferManager.getItemHandler(provider),
                    AmadronOfferManager.getFluidHandler(returning), AmadronOfferManager.getFluidHandler(provider),
                    null);
            if (stock > 0) {
                inStock -= stock;
                switch (getInput().getType()) {
                    case ITEM:
                        ItemStack deliveringItems = getInput().item;
                        int amount = deliveringItems.getCount() * stock;
                        List<ItemStack> stacks = new ArrayList<>();
                        while (amount > 0) {
                            ItemStack stack = deliveringItems.copy();
                            stack.setCount(Math.min(amount, stack.getMaxStackSize()));
                            stacks.add(stack);
                            amount -= stack.getCount();
                        }
                        DroneRegistry.getInstance().deliverItemsAmazonStyle(providingPos, stacks.toArray(new ItemStack[0]));
                        break;
                    case FLUID:
                        FluidStack deliveringFluid = getInput().fluid.copy();
                        deliveringFluid.setAmount(deliveringFluid.getAmount() * stock);
                        DroneRegistry.getInstance().deliverFluidAmazonStyle(providingPos, deliveringFluid);
                        break;
                }
            } else {
                break;
            }
        }
    }

    public TileEntity getProvidingTileEntity() {
        if (cachedInput == null || cachedInput.isRemoved()) {
            if (providingPos != null) {
                cachedInput = PneumaticCraftUtils.getTileEntity(providingPos);
            }
        }
        return cachedInput;
    }

    public TileEntity getReturningTileEntity() {
        if (cachedOutput == null || cachedOutput.isRemoved()) {
            if (returningPos != null) {
                cachedOutput = PneumaticCraftUtils.getTileEntity(returningPos);
            }
        }
        return cachedOutput;
    }

    public GlobalPos getProvidingPos() {
        return providingPos;
    }

    public GlobalPos getReturningPos() {
        return returningPos;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putString("offeringPlayerId", offeringPlayerId);
        tag.putString("offeringPlayerName", offeringPlayerName);
        tag.putInt("inStock", inStock);
        tag.putInt("maxTrades", maxTrades);
        tag.putInt("pendingPayments", pendingPayments);
        if (providingPos != null) {
            tag.put("providingPos", PneumaticCraftUtils.serializeGlobalPos(providingPos));
        }
        if (returningPos != null) {
            tag.put("returningPos", PneumaticCraftUtils.serializeGlobalPos(returningPos));
        }
    }

    public static AmadronOfferCustom loadFromNBT(CompoundNBT tag) {
        AmadronOffer offer = AmadronOffer.loadFromNBT(tag);
        AmadronOfferCustom custom = new AmadronOfferCustom(offer.getInput(), offer.getOutput(), tag.getString("offeringPlayerName"), tag.getString("offeringPlayerId"));
        custom.inStock = tag.getInt("inStock");
        custom.maxTrades = tag.getInt("maxTrades");
        custom.pendingPayments = tag.getInt("pendingPayments");
        if (tag.contains("providingPos")) {
            custom.setProvidingPosition(PneumaticCraftUtils.deserializeGlobalPos(tag.getCompound("providingPos")));
        }
        if (tag.contains("returningPos")) {
            custom.setProvidingPosition(PneumaticCraftUtils.deserializeGlobalPos(tag.getCompound("returningPos")));
        }
        return custom;
    }

    public void writeToBuf(PacketBuffer buf) {
        super.writeToBuf(buf);
        buf.writeString(offeringPlayerName);
        buf.writeString(offeringPlayerId);
        buf.writeBoolean(providingPos != null);
        if (providingPos != null) {
            PacketUtil.writeGlobalPos(buf, providingPos);
        }
        buf.writeBoolean(returningPos != null);
        if (returningPos != null) {
            PacketUtil.writeGlobalPos(buf, returningPos);
        }
        buf.writeInt(inStock);
        buf.writeInt(maxTrades);
        buf.writeInt(pendingPayments);
    }

    public static AmadronOfferCustom loadFromBuf(PacketBuffer buf) {
        AmadronOfferCustom offer = new AmadronOfferCustom(
                TradeResource.fromPacketBuf(buf), TradeResource.fromPacketBuf(buf),
                buf.readString(), buf.readString()
        );
        if (buf.readBoolean()) {
            offer.setProvidingPosition(PacketUtil.readGlobalPos(buf));
        }
        if (buf.readBoolean()) {
            offer.setReturningPosition(PacketUtil.readGlobalPos(buf));
        }
        offer.inStock = buf.readInt();
        offer.maxTrades = buf.readInt();
        offer.pendingPayments = buf.readInt();
        return offer;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("offeringPlayerName", offeringPlayerName);
        json.addProperty("offeringPlayerId", offeringPlayerId);
        json.addProperty("inStock", inStock);
        json.addProperty("maxTrades", maxTrades);
        json.addProperty("pendingPayments", pendingPayments);
        if (providingPos != null) {
            json.addProperty("providingDimension", providingPos.getDimension().getRegistryName().toString());
            json.addProperty("providingX", providingPos.getPos().getX());
            json.addProperty("providingY", providingPos.getPos().getY());
            json.addProperty("providingZ", providingPos.getPos().getZ());
        }
        if (returningPos != null) {
            json.addProperty("returningDimension", returningPos.getDimension().getRegistryName().toString());
            json.addProperty("returningX", returningPos.getPos().getX());
            json.addProperty("returningY", returningPos.getPos().getY());
            json.addProperty("returningZ", returningPos.getPos().getZ());
        }
        return json;
    }

    public static AmadronOfferCustom fromJson(JsonObject json) {
        AmadronOffer offer = AmadronOffer.fromJson(json);
        if (offer != null) {
            AmadronOfferCustom custom = new AmadronOfferCustom(offer.input, offer.output, json.get("offeringPlayerName").getAsString(), json.get("offeringPlayerId").getAsString());
            custom.inStock = json.get("inStock").getAsInt();
            custom.maxTrades = json.get("maxTrades").getAsInt();
            custom.pendingPayments = json.get("pendingPayments").getAsInt();
            if (json.has("providingDimension")) {
                ResourceLocation rl = new ResourceLocation(json.get("providingDimension").getAsString());
                custom.providingPos = GlobalPos.of(DimensionType.byName(rl), new BlockPos(json.get("providingX").getAsInt(), json.get("providingY").getAsInt(), json.get("providingZ").getAsInt()));
            }
            if (json.has("returningDimension")) {
                ResourceLocation rl = new ResourceLocation(json.get("returningDimension").getAsString());
                custom.providingPos = GlobalPos.of(DimensionType.byName(rl), new BlockPos(json.get("returningX").getAsInt(), json.get("returningY").getAsInt(), json.get("returningZ").getAsInt()));
            }
            return custom;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return super.toString() + " - " + offeringPlayerName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AmadronOfferCustom) {
            AmadronOfferCustom offer = (AmadronOfferCustom) o;
            return super.equals(o) && offer.offeringPlayerId.equals(offeringPlayerId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + offeringPlayerId.hashCode();
    }
}
