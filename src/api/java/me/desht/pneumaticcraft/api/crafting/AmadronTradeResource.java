/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Represents an Amadron trade resource. The input and output may be either an item or a fluid.
 */
public class AmadronTradeResource {
    public enum Type { ITEM, FLUID }

    private final Either<ItemStack,FluidStack> resource;

    private AmadronTradeResource(@Nonnull ItemStack stack) {
        resource = Either.left(stack);
    }

    private AmadronTradeResource(@Nonnull FluidStack stack) {
        resource = Either.right(stack);
    }

    public boolean isEmpty() {
        return resource.map(ItemStack::isEmpty, FluidStack::isEmpty);
    }

    /**
     * Get the type of this resource. Here for historical reasons; there are better alternatives - see
     * {@link #accept(Consumer, Consumer)} and {@link #apply(Function, Function)} for general purpose methods to call on
     * a trade resource. This method will be removed in 1.17+.
     *
     * @return the resource type
     * @deprecated don't use this; this class is designed to be type-agnostic
     */
    @Deprecated
    public Type getType() {
        // TODO 1.17 remove this method
        return resource.map(item -> Type.ITEM, fluidStack -> Type.FLUID);
    }

    /**
     * Checks if these two resources are equivalent: same resource, but don't check amounts.
     * @param other the trade resource to compare
     * @return true if the two are equivalent, false if not
     */
    public boolean equivalentTo(AmadronTradeResource other) {
        return resource.map(
                itemStack -> itemStack.getItem() == other.getItem().getItem(),
                fluidStack -> fluidStack.getFluid() == other.getFluid().getFluid()
        );
    }

    public static AmadronTradeResource of(ItemStack stack) {
        return new AmadronTradeResource(stack);
    }

    public static AmadronTradeResource of(FluidStack stack) {
        return new AmadronTradeResource(stack);
    }

    public static AmadronTradeResource fromPacketBuf(PacketBuffer pb) {
        Type type = pb.readEnum(Type.class);
        switch (type) {
            case ITEM: return new AmadronTradeResource(pb.readItem());
            case FLUID: return new AmadronTradeResource(FluidStack.loadFluidStackFromNBT(pb.readNbt()));
        }
        throw new IllegalStateException("bad trade resource type: " + type);
    }

    public ItemStack getItem() {
        return resource.left().orElse(ItemStack.EMPTY);
    }

    public FluidStack getFluid() {
        return resource.right().orElse(FluidStack.EMPTY);
    }

    public void accept(Consumer<ItemStack> cStack, Consumer<FluidStack> cFluid) {
        resource.ifLeft(cStack).ifRight(cFluid);
    }

    public <T> T apply(Function<ItemStack,T> fStack, Function<FluidStack,T> fFluid) {
        return resource.map(fStack, fFluid);
    }

    /**
     * The total space required for this resource, in stacks for an item resource, and in mB for a fluid resource
     * @param units number of offer units
     * @return total space required
     */
    public int totalSpaceRequired(int units) {
        return resource.map(
                itemStack -> (((itemStack.getCount() * units) - 1) / itemStack.getMaxStackSize()) + 1,
                fluidStack -> fluidStack.getAmount() * units
        );
    }

    public int countTradesInInventory(LazyOptional<IItemHandler> inv) {
        return resource.left().map(item -> countItemsInHandler(item, inv) / item.getCount()).orElse(0);
    }

    public int findSpaceInItemOutput(LazyOptional<IItemHandler> inv, int wantedTradeCount) {
        return resource.left().map(item -> Math.min(wantedTradeCount, findSpaceInHandler(item, wantedTradeCount, inv))).orElse(0);
    }

    public int countTradesInTank(LazyOptional<IFluidHandler> lazy) {
        return resource.right().map(fluid -> lazy.map(fluidHandler -> {
            FluidStack searchingFluid = fluid.copy();
            searchingFluid.setAmount(Integer.MAX_VALUE);
            FluidStack extracted = fluidHandler.drain(searchingFluid, IFluidHandler.FluidAction.SIMULATE);
            return extracted.getAmount() / fluid.getAmount();
        }).orElse(0)).orElse(0);
    }

    public int findSpaceInFluidOutput(LazyOptional<IFluidHandler> lazy, int wantedTradeCount) {
        return resource.right().map(fluid -> lazy.map(fluidHandler -> {
            FluidStack providingFluid = fluid.copy();
            providingFluid.setAmount(providingFluid.getAmount() * wantedTradeCount);
            int amountFilled = fluidHandler.fill(providingFluid, IFluidHandler.FluidAction.SIMULATE);
            return amountFilled / fluid.getAmount();
        }).orElse(0)).orElse(0);
    }

    public AmadronTradeResource validate() {
        resource.ifLeft(stack -> Validate.isTrue(!stack.isEmpty()))
                .ifRight(fluidStack -> Validate.isTrue(!fluidStack.isEmpty()));
        return this;
    }

    public static AmadronTradeResource fromJson(JsonObject obj) throws CommandSyntaxException {
        Type type = Type.valueOf(obj.get("type").getAsString().toUpperCase(Locale.ROOT));
        ResourceLocation rl = new ResourceLocation(obj.get("id").getAsString());
        int amount = obj.get("amount").getAsInt();
        switch (type) {
            case ITEM:
                Item item = ForgeRegistries.ITEMS.getValue(rl);
                if (item == null || item == Items.AIR) throw new JsonSyntaxException("unknown item " + rl + "!");
                ItemStack itemStack = new ItemStack(item, amount);
                if (obj.has("nbt")) {
                    itemStack.setTag(JsonToNBT.parseTag(JSONUtils.getAsString(obj, "nbt")));
                }
                return new AmadronTradeResource(itemStack);
            case FLUID:
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
                if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("unknown fluid " + rl + "!");
                FluidStack fluidStack = new FluidStack(fluid, amount);
                return new AmadronTradeResource(fluidStack);
            default:
                throw new JsonSyntaxException("amadron offer " + rl + " : invalid type!");
        }
    }

    public JsonObject toJson() {
        JsonObject res = new JsonObject();
        resource.ifLeft(item -> {
            res.addProperty("type", Type.ITEM.name());
            ResourceLocation name = item.getItem().getRegistryName();
            res.addProperty("id", name == null ? "" : name.toString());
            res.addProperty("amount", item.getCount());
            if (item.hasTag()) {
                res.addProperty("nbt", item.getTag().toString()); //NBTToJsonConverter.getObject(item.getTag()));
            }
        }).ifRight(fluid -> {
            res.addProperty("type", Type.FLUID.name());
            res.addProperty("id", fluid.getFluid().getRegistryName().toString());
            res.addProperty("amount", fluid.getAmount());
        });
        return res;
    }

    public void writeToBuf(PacketBuffer pb) {
        resource.ifLeft(pStack -> {
                    pb.writeEnum(Type.ITEM);
                    pb.writeItem(pStack);
                })
                .ifRight(fluidStack -> {
                    pb.writeEnum(Type.FLUID);
                    pb.writeNbt(fluidStack.writeToNBT(new CompoundNBT()));
                });
    }

    public String getName() {
        return resource.map(
                itemStack -> itemStack.getHoverName().getString(),
                fluidStack -> fluidStack.getDisplayName().getString()
        );
    }

    public ResourceLocation getId() {
        return resource.map(
                itemStack -> itemStack.getItem().getRegistryName(),
                fluidStack -> fluidStack.getFluid().getRegistryName()
        );
    }

    public int getAmount() {
        return resource.map(ItemStack::getCount, FluidStack::getAmount);
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT tag = new CompoundNBT();
        resource.ifLeft(itemStack -> {
                    tag.putString("type", Type.ITEM.toString());
                    tag.put("resource", itemStack.save(new CompoundNBT()));
                })
                .ifRight(fluidStack -> {
                    tag.putString("type", Type.FLUID.toString());
                    tag.put("resource", fluidStack.writeToNBT(new CompoundNBT()));
                });
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmadronTradeResource)) return false;
        AmadronTradeResource that = (AmadronTradeResource) o;
        return resource.map(
                itemStack -> ItemStack.matches(itemStack, that.getItem()),
                fluidStack -> fluidStack.isFluidStackIdentical(that.getFluid())
        );
    }

    @Override
    public int hashCode() {
        return resource.map(Objects::hash, Objects::hash);
    }

    @Override
    public String toString() {
        return resource.map(
                itemStack -> itemStack.getCount() + " x " + itemStack.getHoverName().getString(),
                fluidStack -> fluidStack.getAmount() + "mB " + fluidStack.getDisplayName().getString()
        );
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
        IItemRegistry registry = PneumaticRegistry.getInstance().getItemRegistry();
        return lazy.map(handler -> IntStream.range(0, handler.getSlots())
                .filter(i -> registry.doesItemMatchFilter(item, handler.getStackInSlot(i), false, matchNBT, false))
                .map(i -> handler.getStackInSlot(i).getCount())
                .sum()
        ).orElse(0);
    }

    /**
     * Check how many times we can insert the given itemstack into the (lazy) item handler.
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
