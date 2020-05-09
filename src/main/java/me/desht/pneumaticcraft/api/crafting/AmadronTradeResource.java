package me.desht.pneumaticcraft.api.crafting;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Represents an Amadron trade resource. The input and output may be either an item or a fluid.
 */
public class AmadronTradeResource {
    public enum Type { ITEM, FLUID }

    private final Type type;
    private final ItemStack item;
    private final FluidStack fluid;

    private AmadronTradeResource(@Nonnull ItemStack stack) {
        item = stack;
        type = Type.ITEM;
        fluid = FluidStack.EMPTY;
    }

    private AmadronTradeResource(@Nonnull FluidStack stack) {
        item = ItemStack.EMPTY;
        type = Type.FLUID;
        fluid = stack;
    }

    /**
     * Checks if these two resources are equivalent: same resource, but don't check amounts.
     * @param other the trade resource to compare
     * @return true if the two are equivalent, false if not
     */
    public boolean equivalentTo(AmadronTradeResource other) {
        if (type != other.type) return false;
        switch (type) {
            case ITEM:
                return getItem().getItem() == other.getItem().getItem();
            case FLUID:
                return getFluid().getFluid() == other.getFluid().getFluid();
        }
        return false;
    }

    public static AmadronTradeResource of(ItemStack stack) {
        return new AmadronTradeResource(stack);
    }

    public static AmadronTradeResource of(FluidStack stack) {
        return new AmadronTradeResource(stack);
    }

    public static AmadronTradeResource fromPacketBuf(PacketBuffer pb) {
        Type type = Type.values()[pb.readByte()];
        switch (type) {
            case ITEM:
                return new AmadronTradeResource(pb.readItemStack());
            case FLUID:
                return new AmadronTradeResource(FluidStack.loadFluidStackFromNBT(pb.readCompoundTag()));
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

    public int countTradesInInventory(LazyOptional<IItemHandler> inv) {
        return countItemsInHandler(item, inv) / item.getCount();
    }

    public int findSpaceInItemOutput(LazyOptional<IItemHandler> inv, int wantedTradeCount) {
        return Math.min(wantedTradeCount, findSpaceInHandler(item, wantedTradeCount, inv));
    }

    public int countTradesInTank(LazyOptional<IFluidHandler> lazy) {
        return lazy.map(fluidHandler -> {
            FluidStack searchingFluid = fluid.copy();
            searchingFluid.setAmount(Integer.MAX_VALUE);
            FluidStack extracted = fluidHandler.drain(searchingFluid, IFluidHandler.FluidAction.SIMULATE);
            return extracted.getAmount() / fluid.getAmount();
        }).orElse(0);
    }

    public int findSpaceInFluidOutput(LazyOptional<IFluidHandler> lazy, int wantedTradeCount) {
        return lazy.map(fluidHandler -> {
            FluidStack providingFluid = fluid.copy();
            providingFluid.setAmount(providingFluid.getAmount() * wantedTradeCount);
            int amountFilled = fluidHandler.fill(providingFluid, IFluidHandler.FluidAction.SIMULATE);
            return amountFilled / fluid.getAmount();
        }).orElse(0);
    }

    public AmadronTradeResource validate() {
        Validate.isTrue(item != null && fluid != null);
        switch (type) {
            case ITEM:
                Validate.isTrue(item != null && !item.isEmpty());
                break;
            case FLUID:
                Validate.isTrue(fluid != null && !fluid.isEmpty());
                break;
        }
        return this;
    }

    public static AmadronTradeResource fromJson(JsonObject obj) throws CommandSyntaxException {
        Type type = Type.valueOf(obj.get("type").getAsString().toUpperCase());
        ResourceLocation rl = new ResourceLocation(obj.get("id").getAsString());
        int amount = obj.get("amount").getAsInt();
        switch (type) {
            case ITEM:
                Item item = ForgeRegistries.ITEMS.getValue(rl);
                ItemStack stack = new ItemStack(item, amount);
                if (obj.has("nbt")) {
                    stack.setTag(JsonToNBT.getTagFromJson(JSONUtils.getString(obj, "nbt")));
                }
                return new AmadronTradeResource(stack);
            case FLUID:
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
                FluidStack fluidStack = new FluidStack(fluid, amount);
                return new AmadronTradeResource(fluidStack);
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
                    res.addProperty("nbt", item.getTag().toString()); //NBTToJsonConverter.getObject(item.getTag()));
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

    public String getName() {
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
        if (!(o instanceof AmadronTradeResource)) return false;
        AmadronTradeResource that = (AmadronTradeResource) o;
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

    /**
     * Get the total number of matching items in the given (lazy) item handler. "Matching" means that the stacks are
     * the same item, AND if the item in the offer has any NBT, the stack's NBT must also match.
     *
     * @param item the item to look for
     * @param lazy the LazyOptional item handler
     * @return the total number of matching items
     */
    private static int countItemsInHandler(ItemStack item, LazyOptional<IItemHandler> lazy) {
        boolean matchNBT = item.hasTag();
        return lazy.map(handler -> IntStream.range(0, handler.getSlots())
                .filter(i -> PneumaticCraftUtils.doesItemMatchFilter(item, handler.getStackInSlot(i), false, matchNBT, false))
                .map(i -> handler.getStackInSlot(i).getCount())
                .sum()
        ).orElse(0);
    }

    /**
     * Check how many time we can insert the given itemstack into the (lazy) item handler.
     *
     * @param item the item stack, whose size may be > 1
     * @param multiplier the number of times we want to insert it
     * @param lazy the LazyOptional item handler
     * @return the number of times the stack can actually be inserted
     */
    private static int findSpaceInHandler(ItemStack item, int multiplier, LazyOptional<IItemHandler> lazy) {
        final int totalItems = item.getCount() * multiplier;
        return lazy.map(inv -> {
            int remaining = totalItems;
            for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
                if (inv.getStackInSlot(i).isEmpty() || ItemHandlerHelper.canItemStacksStack(inv.getStackInSlot(i), item)) {
                    remaining -= item.getMaxStackSize() - inv.getStackInSlot(i).getCount();
                }
            }
            return (totalItems - remaining) / item.getCount();
        }).orElse(0);
    }
}
