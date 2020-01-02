package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AmadronOffer {
    public enum TradeType { PLAYER, PERIODIC, STATIC }

    protected AmadronTradeResource input;
    protected AmadronTradeResource output;
    // distinguishes base default trades vs. ones added later; only used server-side and only saved to JSON
    private String addedBy = null;

    public AmadronOffer(@Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output) {
        this(input, output, null);
    }

    public AmadronOffer(@Nonnull AmadronTradeResource input, @Nonnull AmadronTradeResource output, String addedBy) {
        Validate.notNull(input, "Input item/fluid can't be null!");
        Validate.notNull(output, "Output item/fluid can't be null!");
        input.validate();
        output.validate();
        this.input = input;
        this.output = output;
        this.addedBy = addedBy;
    }

    public AmadronTradeResource getInput() {
        return input;
    }

    public AmadronTradeResource getOutput() {
        return output;
    }

    public String getVendor() {
        return xlate("gui.amadron").getFormattedText();
    }

    public int getStock() {
        return -1;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public boolean passesQuery(String query) {
        String queryLow = query.toLowerCase();
        return getInput().getName().toLowerCase().contains(queryLow) || getVendor().toLowerCase().contains(queryLow);
    }

    public void onTrade(int tradingAmount, String buyingPlayer) {
    }

    public void writeToNBT(CompoundNBT tag) {
        tag.put("input", input.writeToNBT());
        tag.put("output", output.writeToNBT());
    }

    public static AmadronOffer loadFromNBT(CompoundNBT tag) {
        return new AmadronOffer(AmadronTradeResource.fromNBT(tag.getCompound("input")), AmadronTradeResource.fromNBT(tag.getCompound("output")));
    }

    public void writeToBuf(PacketBuffer buf) {
        input.writeToBuf(buf);
        output.writeToBuf(buf);
    }

    public static AmadronOffer readFromBuf(PacketBuffer buf) {
        return new AmadronOffer(AmadronTradeResource.fromPacketBuf(buf), AmadronTradeResource.fromPacketBuf(buf));
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.add("input", input.toJson());
        object.add("output", output.toJson());
        if (addedBy != null) object.addProperty("addedBy", addedBy);

        return object;
    }

    public static AmadronOffer fromJson(JsonObject object) {
        String addedBy = object.has("addedBy") ? object.get("addedBy").getAsString() : null;
        return new AmadronOffer(
                AmadronTradeResource.fromJson(object.getAsJsonObject("input")),
                AmadronTradeResource.fromJson(object.getAsJsonObject("output")),
                addedBy
        );
    }

    @Override
    public String toString() {
        return String.format("[in = %s, out = %s]", input.toString(), output.toString());
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
