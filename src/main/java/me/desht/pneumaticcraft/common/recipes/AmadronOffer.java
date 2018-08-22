package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.network.PacketSyncAmadronOffers;
import me.desht.pneumaticcraft.common.util.JsonToNBTConverter;
import me.desht.pneumaticcraft.common.util.NBTToJsonConverter;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

public class AmadronOffer {
    public enum TradeType { PLAYER, PERIODIC, STATIC }

    protected Object input;
    protected Object output;
    // distinguishes base default trades vs. ones added later; only used server-side and only saved to JSON
    private String addedBy = null;

    public AmadronOffer(Object input, Object output) {
        this(input, output, null);
    }

    public AmadronOffer(Object input, Object output, String addedBy) {
        Validate.notNull(input, "Input item/fluid can't be null!");
        Validate.notNull(output, "Output item/fluid can't be null!");
        if (input instanceof ItemStack) {
            Validate.isTrue(!((ItemStack) input).isEmpty(), "Input item cannot be empty!");
        } else if (input instanceof FluidStack) {
            Validate.isTrue(((FluidStack) input).amount > 0, "Input fluid cannot be empty!");
        } else {
            throw new IllegalArgumentException("Input must be of type ItemStack or FluidStack. Input: " + input);
        }
        if (output instanceof ItemStack) {
            Validate.isTrue(!((ItemStack) output).isEmpty(), "Output item cannot be empty!");
        } else if (output instanceof FluidStack) {
            Validate.isTrue(((FluidStack) output).amount > 0, "Output fluid cannot be empty!");
        } else {
            throw new IllegalArgumentException("Output must be of type ItemStack or FluidStack. Output: " + input);
        }
        this.input = input;
        this.output = output;
        this.addedBy = addedBy;
    }

    public Object getInput() {
        return input;
    }

    public Object getOutput() {
        return output;
    }

    public String getVendor() {
        return I18n.translateToLocal("gui.amadron");
    }

    public int getStock() {
        return -1;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public boolean passesQuery(String query) {
        String queryLow = query.toLowerCase();
        return getObjectName(getInput()).toLowerCase().contains(queryLow)
                || getObjectName(getOutput()).toLowerCase().contains(queryLow)
                || getVendor().toLowerCase().contains(queryLow);
    }

    private String getObjectName(Object object) {
        return object instanceof ItemStack ? ((ItemStack) object).getDisplayName() : ((FluidStack) object).getLocalizedName();
    }

    public void onTrade(int tradingAmount, String buyingPlayer) {
    }

    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        if (input instanceof ItemStack) {
            ((ItemStack) input).writeToNBT(subTag);
            tag.setTag("inputItem", subTag);
        } else {
            ((FluidStack) input).writeToNBT(subTag);
            tag.setTag("inputFluid", subTag);
        }
        subTag = new NBTTagCompound();
        if (output instanceof ItemStack) {
            ((ItemStack) output).writeToNBT(subTag);
            tag.setTag("outputItem", subTag);
        } else {
            ((FluidStack) output).writeToNBT(subTag);
            tag.setTag("outputFluid", subTag);
        }
    }

    public static AmadronOffer loadFromNBT(NBTTagCompound tag) {
        Object input;
        if (tag.hasKey("inputItem")) {
            input = new ItemStack(tag.getCompoundTag("inputItem"));
        } else {
            input = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("inputFluid"));
        }
        Object output;
        if (tag.hasKey("outputItem")) {
            output = new ItemStack(tag.getCompoundTag("outputItem"));
        } else {
            output = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("outputFluid"));
        }
        return new AmadronOffer(input, output);
    }

    public void writeToBuf(ByteBuf buf) {
        PacketSyncAmadronOffers.writeFluidOrItemStack(getInput(), buf);
        PacketSyncAmadronOffers.writeFluidOrItemStack(getOutput(), buf);
    }

    public static AmadronOffer readFromBuf(ByteBuf buf) {
        return new AmadronOffer(PacketSyncAmadronOffers.readFluidOrItemStack(buf), PacketSyncAmadronOffers.readFluidOrItemStack(buf));
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        JsonObject inputObject = new JsonObject();
        if (input instanceof ItemStack) {
            ItemStack stack = (ItemStack) input;
            ResourceLocation name = stack.getItem().getRegistryName();
            inputObject.addProperty("id", name == null ? "" : name.toString());
            inputObject.addProperty("damage", stack.getItemDamage());
            inputObject.addProperty("amount", stack.getCount());
            if (stack.hasTagCompound()) {
                //noinspection ConstantConditions
                inputObject.add("nbt", NBTToJsonConverter.getObject(stack.getTagCompound()));
            }
        } else {
            inputObject.addProperty("id", ((FluidStack) input).getFluid().getName());
            inputObject.addProperty("amount", ((FluidStack) input).amount);
        }
        object.add("input", inputObject);

        JsonObject outputObject = new JsonObject();
        if (output instanceof ItemStack) {
            ItemStack stack = (ItemStack) output;
            ResourceLocation name = stack.getItem().getRegistryName();
            outputObject.addProperty("id",  name == null ? "" : name.toString());
            outputObject.addProperty("damage", stack.getItemDamage());
            outputObject.addProperty("amount", stack.getCount());
            if (stack.hasTagCompound()) {
                //noinspection ConstantConditions
                outputObject.add("nbt", NBTToJsonConverter.getObject(stack.getTagCompound()));
            }
        } else {
            outputObject.addProperty("id", ((FluidStack) output).getFluid().getName());
            outputObject.addProperty("amount", ((FluidStack) output).amount);
        }
        object.add("output", outputObject);

        if (addedBy != null) object.addProperty("addedBy", addedBy);
        return object;
    }

    public static AmadronOffer fromJson(JsonObject object) {
        JsonObject inputObject = object.getAsJsonObject("input");
        Object input;
        if (inputObject.has("damage")) {
            Item item = Item.getByNameOrId(inputObject.get("id").getAsString());
            if (item != null) {
                input = new ItemStack(item, inputObject.get("amount").getAsInt(), inputObject.get("damage").getAsInt());
                if (inputObject.has("nbt")) {
                    ((ItemStack) input).setTagCompound(JsonToNBTConverter.getTag(inputObject.getAsJsonObject("nbt")));
                }
            } else {
                Log.error("Invalid Amadron Offer input item. Invalid item name: " + inputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        } else {
            Fluid fluid = FluidRegistry.getFluid(inputObject.get("id").getAsString());
            if (fluid != null) {
                input = new FluidStack(fluid, inputObject.get("amount").getAsInt());
            } else {
                Log.error("Invalid Amadron Offer input fluid. Invalid fluid name: " + inputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        }

        JsonObject outputObject = object.getAsJsonObject("output");
        Object output;
        if (outputObject.has("damage")) {
            Item item = Item.getByNameOrId(outputObject.get("id").getAsString());
            if (item != null) {
                output = new ItemStack(item, outputObject.get("amount").getAsInt(), outputObject.get("damage").getAsInt());
                if (outputObject.has("nbt")) {
                    ((ItemStack) output).setTagCompound(JsonToNBTConverter.getTag(outputObject.getAsJsonObject("nbt")));
                }
            } else {
                Log.error("Invalid Amadron Offer output item. Invalid item name: " + outputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        } else {
            Fluid fluid = FluidRegistry.getFluid(outputObject.get("id").getAsString());
            if (fluid != null) {
                output = new FluidStack(fluid, outputObject.get("amount").getAsInt());
            } else {
                Log.error("Invalid Amadron Offer output fluid. Invalid fluid name: " + outputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        }

        String addedBy = object.has("addedBy") ? object.get("addedBy").getAsString() : null;
        return new AmadronOffer(input, output, addedBy);
    }

    @Override
    public int hashCode() {
        int code = getObjectHashCode(getInput());
        code = 31 * code + getObjectHashCode(getOutput());
        code = 31 * code + getVendor().hashCode();
        return code;
    }

    @Override
    public String toString() {
        return String.format("[in = %s, out = %s]", input, output);
    }

    private int getObjectHashCode(Object o) {
        if (o instanceof FluidStack) {
            return o.hashCode();
        } else {
            ItemStack stack = (ItemStack) o;
            ResourceLocation name = stack.getItem().getRegistryName();
            return (name == null ? 0 : name.hashCode()) + stack.getCount() * 19 + stack.getMetadata() * 37;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AmadronOffer) {
            AmadronOffer offer = (AmadronOffer) o;
            if (offer.getInput().getClass() == getInput().getClass() && offer.getOutput().getClass() == getOutput().getClass()) {
                if (offer.getInput() instanceof ItemStack) {
                    ItemStack s1 = (ItemStack) offer.getInput();
                    ItemStack s2 = (ItemStack) getInput();
                    if (!ItemStack.areItemStacksEqual(s1, s2)) return false;
                } else {
                    FluidStack s1 = (FluidStack) offer.getInput();
                    FluidStack s2 = (FluidStack) getInput();
                    if (!s1.isFluidEqual(s2) || s1.amount != s2.amount) return false;
                }
                if (offer.getOutput() instanceof ItemStack) {
                    ItemStack s1 = (ItemStack) offer.getOutput();
                    ItemStack s2 = (ItemStack) getOutput();
                    if (!ItemStack.areItemStacksEqual(s1, s2)) return false;
                } else {
                    FluidStack s1 = (FluidStack) offer.getOutput();
                    FluidStack s2 = (FluidStack) getOutput();
                    if (!s1.isFluidEqual(s2) || s1.amount != s2.amount) return false;
                }
                return getVendor().equals(offer.getVendor());
            }
        }
        return false;
    }
}
