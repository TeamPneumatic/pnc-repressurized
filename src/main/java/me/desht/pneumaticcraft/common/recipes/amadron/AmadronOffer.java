package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AmadronOffer {
    private final ResourceLocation offerId;  // unique offer identifier
    protected final AmadronTradeResource input;
    protected final AmadronTradeResource output;

    public AmadronOffer(ResourceLocation id, @Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output) {
        this.offerId = id;
        this.input = input.validate();
        this.output = output.validate();
    }

    public ResourceLocation getOfferId() {
        return offerId;
    }

    public AmadronTradeResource getInput() {
        return input;
    }

    public AmadronTradeResource getOutput() {
        return output;
    }

    public boolean equivalentTo(AmadronPlayerOffer offer) {
        return input.equivalentTo(offer.getInput()) && output.equivalentTo(offer.getOutput());
    }

    public String getVendor() {
        return xlate("gui.amadron").getFormattedText();
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

    public void writeToNBT(CompoundNBT tag) {
        tag.putString("id", offerId.toString());
        tag.put("input", input.writeToNBT());
        tag.put("output", output.writeToNBT());
    }

    public static AmadronOffer loadFromNBT(CompoundNBT tag) {
        return new AmadronOffer(
                new ResourceLocation(tag.getString("id")),
                AmadronTradeResource.fromNBT(tag.getCompound("input")),
                AmadronTradeResource.fromNBT(tag.getCompound("output"))
        );
    }

    public void writeToBuf(PacketBuffer buf) {
        buf.writeResourceLocation(offerId);
        input.writeToBuf(buf);
        output.writeToBuf(buf);
    }

    public static AmadronOffer readFromBuf(PacketBuffer buf) {
        return new AmadronOffer(
                buf.readResourceLocation(),
                AmadronTradeResource.fromPacketBuf(buf),
                AmadronTradeResource.fromPacketBuf(buf)
        );
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("id", offerId.toString());
        object.add("input", input.toJson());
        object.add("output", output.toJson());

        return object;
    }

    public static AmadronOffer fromJson(JsonObject object) throws CommandSyntaxException {
        return new AmadronOffer(
                new ResourceLocation(JSONUtils.getString(object, "id")),
                AmadronTradeResource.fromJson(object.getAsJsonObject("input")),
                AmadronTradeResource.fromJson(object.getAsJsonObject("output"))
        );
    }

    @Override
    public String toString() {
        return String.format("[id = %s, in = %s, out = %s]", offerId.toString(), input.toString(), output.toString());
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

}
