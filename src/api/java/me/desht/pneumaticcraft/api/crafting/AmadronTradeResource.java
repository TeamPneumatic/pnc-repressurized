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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Represents an Amadron trade resource. The input and output may be either an item or a fluid.
 */
public record AmadronTradeResource(Either<ItemStack,FluidStack> resource) {
    public static final Codec<AmadronTradeResource> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.either(ItemStack.CODEC, FluidStack.CODEC).fieldOf("resource").forGetter(AmadronTradeResource::resource)
    ).apply(inst, AmadronTradeResource::new));

    public static StreamCodec<RegistryFriendlyByteBuf, AmadronTradeResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.either(ItemStack.STREAM_CODEC, FluidStack.STREAM_CODEC), AmadronTradeResource::resource,
            AmadronTradeResource::new
    );

    public boolean isEmpty() {
        return resource.map(ItemStack::isEmpty, FluidStack::isEmpty);
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
        return new AmadronTradeResource(Either.left(stack));
    }

    public static AmadronTradeResource of(FluidStack stack) {
        return new AmadronTradeResource(Either.right(stack));
    }

    /**
     * Get the item for this trade resource
     * @return the itemstack, or ItemStack.EMPTY if the resource is a fluid
     */
    public ItemStack getItem() {
        return resource.left().orElse(ItemStack.EMPTY);
    }

    /**
     * Get the fluid for this trade resource
     * @return the fluidstack, or FluidStack.EMPTY if the resource is an item
     */
    public FluidStack getFluid() {
        return resource.right().orElse(FluidStack.EMPTY);
    }

    /**
     * Run something against the resource, dependent on whether it's an item or a fluid
     * @param cItemStack consumer which is called when the resource is an item
     * @param cFluidStack consumer which is called when the resource is a fluid
     */
    public void accept(Consumer<ItemStack> cItemStack, Consumer<FluidStack> cFluidStack) {
        resource.ifLeft(cItemStack).ifRight(cFluidStack);
    }

    /**
     * Run something against the resource, dependent on whether it's an item or a fluid, returning a result
     * @param fItemStack function which is called when the resource is an item
     * @param fFluidStack function which is called when the resource is a fluid
     */
    public <T> T apply(Function<ItemStack,T> fItemStack, Function<FluidStack,T> fFluidStack) {
        return resource.map(fItemStack, fFluidStack);
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

    public int countTradesInInventory(IItemHandler inv) {
        return resource.left().map(item -> countItemsInHandler(item, inv) / item.getCount()).orElse(0);
    }

    public int findSpaceInItemOutput(IItemHandler inv, int wantedTradeCount) {
        return resource.left().map(item -> Math.min(wantedTradeCount, findSpaceInHandler(item, wantedTradeCount, inv))).orElse(0);
    }

    public int countTradesInTank(IFluidHandler fluidHandler) {
        return resource.right().map(fluid -> {
            FluidStack searchingFluid = fluid.copy();
            searchingFluid.setAmount(Integer.MAX_VALUE);
            FluidStack extracted = fluidHandler.drain(searchingFluid, IFluidHandler.FluidAction.SIMULATE);
            return extracted.getAmount() / fluid.getAmount();
        }).orElse(0);
    }

    public int findSpaceInFluidOutput(IFluidHandler fluidHandler, int wantedTradeCount) {
        return resource.right().map(fluid -> {
            FluidStack providingFluid = fluid.copy();
            providingFluid.setAmount(providingFluid.getAmount() * wantedTradeCount);
            int amountFilled = fluidHandler.fill(providingFluid, IFluidHandler.FluidAction.SIMULATE);
            return amountFilled / fluid.getAmount();
        }).orElse(0);
    }

    public AmadronTradeResource validate() {
        resource.ifLeft(stack -> Validate.isTrue(!stack.isEmpty()))
                .ifRight(fluidStack -> Validate.isTrue(!fluidStack.isEmpty()));
        return this;
    }

    public String getName() {
        return resource.map(
                itemStack -> itemStack.getHoverName().getString(),
                fluidStack -> fluidStack.getHoverName().getString()
        );
    }

    public ResourceLocation getId() {
        return resource.map(
                itemStack -> BuiltInRegistries.ITEM.getKey(itemStack.getItem()),
                fluidStack -> BuiltInRegistries.FLUID.getKey(fluidStack.getFluid())
        );
    }

    public int getAmount() {
        return resource.map(ItemStack::getCount, FluidStack::getAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmadronTradeResource that)) return false;
        return resource.map(
                itemStack -> ItemStack.matches(itemStack, that.getItem()),
                fluidStack -> FluidStack.matches(fluidStack, that.getFluid())
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
                fluidStack -> fluidStack.getAmount() + "mB " + fluidStack.getHoverName().getString()
        );
    }

    /**
     * Get the total number of matching items in the given (lazy) item handler. "Matching" means that the stacks are
     * the same item, AND if the item in the offer has any component data, the stack's components must also match.
     *
     * @param item the item to look for
     * @param itemHandler the item handler
     * @return the total number of matching items
     */
    private static int countItemsInHandler(ItemStack item, IItemHandler itemHandler) {
        boolean matchComponents = !item.getComponentsPatch().isEmpty();
        IItemRegistry registry = PneumaticRegistry.getInstance().getItemRegistry();
        return IntStream.range(0, itemHandler.getSlots())
                .filter(i -> registry.doesItemMatchFilter(item, itemHandler.getStackInSlot(i), false, matchComponents, false))
                .map(i -> itemHandler.getStackInSlot(i).getCount())
                .sum();
    }

    /**
     * Check how many times we can insert the given itemstack into the (lazy) item handler.
     *
     * @param item the item stack, whose size may be > 1
     * @param multiplier the number of times we want to insert it
     * @param itemHandler the item handler
     * @return the number of times the stack can actually be inserted
     */
    private static int findSpaceInHandler(ItemStack item, int multiplier, IItemHandler itemHandler) {
        final int totalItems = item.getCount() * multiplier;

        int remaining = totalItems;
        for (int i = 0; i < itemHandler.getSlots() && remaining > 0; i++) {
            if (itemHandler.getStackInSlot(i).isEmpty() || ItemStack.isSameItemSameComponents(itemHandler.getStackInSlot(i), item)) {
                remaining -= item.getMaxStackSize() - itemHandler.getStackInSlot(i).getCount();
            }
        }
        return (totalItems - remaining) / item.getCount();
    }
}
