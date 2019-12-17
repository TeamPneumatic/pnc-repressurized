package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.util.JsonToNBTConverter;
import me.desht.pneumaticcraft.common.util.NBTToJsonConverter;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AmadronOffer {
    public enum TradeType { PLAYER, PERIODIC, STATIC }

    protected TradeResource input;
    protected TradeResource output;
    // distinguishes base default trades vs. ones added later; only used server-side and only saved to JSON
    private String addedBy = null;

    public AmadronOffer(@Nonnull TradeResource input, @Nonnull TradeResource output) {
        this(input, output, null);
    }

    public AmadronOffer(@Nonnull TradeResource input, @Nonnull TradeResource output, String addedBy) {
        Validate.notNull(input, "Input item/fluid can't be null!");
        Validate.notNull(output, "Output item/fluid can't be null!");
        input.validate();
        output.validate();
        this.input = input;
        this.output = output;
        this.addedBy = addedBy;
    }

    public TradeResource getInput() {
        return input;
    }

    public TradeResource getOutput() {
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
        return new AmadronOffer(TradeResource.fromNBT(tag.getCompound("input")), TradeResource.fromNBT(tag.getCompound("output")));
    }

    public void writeToBuf(PacketBuffer buf) {
        input.writeToBuf(buf);
        output.writeToBuf(buf);
    }

    public static AmadronOffer readFromBuf(PacketBuffer buf) {
        return new AmadronOffer(TradeResource.fromPacketBuf(buf), TradeResource.fromPacketBuf(buf));
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
                TradeResource.fromJson(object.getAsJsonObject("input")),
                TradeResource.fromJson(object.getAsJsonObject("output")),
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

    public static class TradeResource {
        public enum Type { ITEM, FLUID;}

        final Type type;
        final ItemStack item;
        final FluidStack fluid;

        TradeResource(ItemStack stack) {
            item = stack;
            type = Type.ITEM;
            fluid = FluidStack.EMPTY;
        }

        TradeResource(FluidStack stack) {
            item = ItemStack.EMPTY;
            type = Type.FLUID;
            fluid = stack;
        }

        public static TradeResource of(ItemStack stack) {
            return new TradeResource(stack);
        }

        public static TradeResource of(FluidStack stack) {
            return new TradeResource(stack);
        }

        public static TradeResource fromNBT(CompoundNBT tag) {
            Type type = Type.valueOf(tag.getString("type"));
            switch (type) {
                case ITEM:
                    return new TradeResource(ItemStack.read(tag.getCompound("resource")));
                case FLUID:
                    return new TradeResource(FluidStack.loadFluidStackFromNBT(tag.getCompound("resource")));
            }
            throw new IllegalStateException("bad trade resource type: " + type);
        }

        public static TradeResource fromPacketBuf(PacketBuffer pb) {
            Type type = Type.values()[pb.readByte()];
            switch (type) {
                case ITEM:
                    return new TradeResource(pb.readItemStack());
                case FLUID:
                    return new TradeResource(FluidStack.loadFluidStackFromNBT(pb.readCompoundTag()));
            }
            throw new IllegalStateException("bad trade resource type: " + type);
        }

        public Type getType() {
            return type;
        }

        public ItemStack getItem() {
            return type == Type.ITEM ? item : ItemStack.EMPTY;
        }

        public FluidStack getFluid() {
            return type == Type.FLUID ? fluid : FluidStack.EMPTY;
        }

        public int countTradesInInventory(IItemHandler inv) {
            int count = 0;
            for (int i = 0; i < inv.getSlots(); i++) {
                if (ItemStack.areItemsEqual(inv.getStackInSlot(i), item)) {
                    count += inv.getStackInSlot(i).getCount();
                }
            }
            return count / item.getCount();
        }

        public int countTradesInTank(IFluidHandler fluidHandler) {
            FluidStack searchingFluid = fluid.copy();
            searchingFluid.setAmount(Integer.MAX_VALUE);
            FluidStack extracted = fluidHandler.drain(searchingFluid, IFluidHandler.FluidAction.SIMULATE);
            return extracted.getAmount() / fluid.getAmount();
        }

        public int findSpaceInOutput(IItemHandler inv, int wantedTradeCount) {
            ItemStack providingItem = ItemHandlerHelper.copyStackWithSize(item, item.getCount() * wantedTradeCount);
            ItemStack remainder = ItemHandlerHelper.insertItem(inv, providingItem.copy(), true);
            if (!remainder.isEmpty()) {
                return (providingItem.getCount() - remainder.getCount()) / item.getCount();
            } else {
                return wantedTradeCount;
            }
        }

        public int findSpaceInOutput(IFluidHandler fluidHandler, int wantedTradeCount) {
            FluidStack providingFluid = fluid.copy();
            providingFluid.setAmount(providingFluid.getAmount() * wantedTradeCount);
            int amountFilled = fluidHandler.fill(providingFluid, IFluidHandler.FluidAction.SIMULATE);
            return amountFilled / fluid.getAmount();
        }

        public void validate() {
            switch (type) {
                case ITEM:
                    Validate.isTrue(!item.isEmpty());
                    break;
                case FLUID:
                    Validate.isTrue(!fluid.isEmpty());
                    break;
            }
        }

        public static TradeResource fromJson(JsonObject obj) {
            Type type = Type.valueOf(obj.get("type").getAsString());
            ResourceLocation rl = new ResourceLocation(obj.get("id").getAsString());
            int amount = obj.get("amount").getAsInt();
            switch (type) {
                case ITEM:
                    Item item = ForgeRegistries.ITEMS.getValue(rl);
                    ItemStack stack = new ItemStack(item, amount);
                    if (obj.has("nbt")) {
                        stack.setTag(JsonToNBTConverter.getTag(obj.getAsJsonObject("nbt")));
                    }
                    return new TradeResource(stack);
                case FLUID:
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
                    FluidStack fluidStack = new FluidStack(fluid, amount);
                    return new TradeResource(fluidStack);
                default:
                    return null;
            }
        }

        public JsonObject toJson() {
            JsonObject res = new JsonObject();
            res.addProperty("type", type.name());
            switch (type) {
                case ITEM:
                    ResourceLocation name = item.getItem().getRegistryName();
                    res.addProperty("id", name == null ? "" : name.toString());
                    res.addProperty("amount", item.getCount());
                    if (item.hasTag()) {
                        res.add("nbt", NBTToJsonConverter.getObject(item.getTag()));
                    }
                    break;
                case FLUID:
                    res.addProperty("id", fluid.getFluid().getRegistryName().toString());
                    res.addProperty("amount", fluid.getAmount());
                    break;
            }
            return res;
        }

        public void writeToBuf(PacketBuffer pb) {
            pb.writeByte(type.ordinal());
            switch (type) {
                case ITEM:
                    pb.writeItemStack(item);
                    break;
                case FLUID:
                    pb.writeCompoundTag(fluid.writeToNBT(new CompoundNBT()));
                    break;
            }
        }

        private String getName() {
            switch (type) {
                case ITEM:
                    return item.getDisplayName().getFormattedText();
                case FLUID:
                    return fluid.getDisplayName().getFormattedText();
                default:
                    return null;
            }
        }

        public int getAmount() {
            switch (type) {
                case ITEM: return item.getCount();
                case FLUID: return fluid.getAmount();
            }
            return 0;
        }

        public CompoundNBT writeToNBT() {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("type", type.toString());
            CompoundNBT subTag = type == Type.ITEM ? item.write(new CompoundNBT()) : fluid.writeToNBT(new CompoundNBT());
            tag.put("resource", subTag);
            return tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TradeResource)) return false;
            TradeResource that = (TradeResource) o;
            return type == Type.ITEM && ItemStack.areItemStacksEqual(item, that.item)
                    || type == Type.FLUID && fluid.equals(that.fluid) && fluid.getAmount() == that.fluid.getAmount();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, item, fluid);
        }

        @Override
        public String toString() {
            switch (type) {
                case ITEM:
                    return item.getCount() + " x " + item.getDisplayName().getFormattedText();
                case FLUID:
                    return fluid.getAmount() + "mB " + fluid.getDisplayName().getFormattedText();
            }
            return super.toString();
        }
    }
}
