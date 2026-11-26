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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.lang3.Validate;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Represents an Amadron trade resource. The input and output may be either an item or a fluid.
 */
public record AmadronTradeResource(Either<ItemStackTemplate,FluidStackTemplate> resource) {
    public static final Codec<AmadronTradeResource> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.either(ItemStackTemplate.CODEC, FluidStackTemplate.CODEC).fieldOf("resource").forGetter(AmadronTradeResource::resource)
    ).apply(inst, AmadronTradeResource::new));

    public static StreamCodec<RegistryFriendlyByteBuf, AmadronTradeResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.either(ItemStackTemplate.STREAM_CODEC, FluidStackTemplate.STREAM_CODEC), AmadronTradeResource::resource,
            AmadronTradeResource::new
    );

    /**
     * Trade resources are never empty; kept for compatibility.
     * @return false
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Checks if these two resources are equivalent: same resource, but don't check amounts.
     * @param other the trade resource to compare
     * @return true if the two are equivalent, false if not
     */
    public boolean equivalentTo(AmadronTradeResource other) {
        return resource.map(
                item -> other.resource.left().map(o -> o.item().value() == item.item().value()).orElse(false),
                fluid -> other.resource.right().map(o -> o.fluid().value() == fluid.fluid().value()).orElse(false)
        );
    }

    /**
     * Create an item resource from a (non-empty) item stack
     * @param stack the stack
     * @return a new trade resource
     * @throws IllegalArgumentException if the stack is empty
     */
    public static AmadronTradeResource of(ItemStack stack) {
        Validate.isTrue(!stack.isEmpty(), "item resource may not be empty");
        return of(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    /**
     * Create a fluid resource from a (non-empty) fluid stack
     * @param stack the stack
     * @return a new trade resource
     * @throws IllegalArgumentException if the stack is empty
     */
    public static AmadronTradeResource of(FluidStack stack) {
        Validate.isTrue(!stack.isEmpty(), "fluid resource may not be empty");
        return of(FluidStackTemplate.fromNonEmptyStack(stack));
    }

    public static AmadronTradeResource of(ItemStackTemplate item) {
        return new AmadronTradeResource(Either.left(item));
    }

    public static AmadronTradeResource of(FluidStackTemplate fluid) {
        return new AmadronTradeResource(Either.right(fluid));
    }

    /**
     * Get the item for this trade resource
     * @return the itemstack, or ItemStack.EMPTY if the resource is a fluid
     */
    public ItemStack getItem() {
        return resource.left().map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    /**
     * Get the fluid for this trade resource
     * @return the fluidstack, or FluidStack.EMPTY if the resource is an item
     */
    public FluidStack getFluid() {
        return resource.right().map(FluidStackTemplate::create).orElse(FluidStack.EMPTY);
    }

    /**
     * Run something against the resource, dependent on whether it's an item or a fluid
     * @param cItemStack consumer which is called when the resource is an item
     * @param cFluidStack consumer which is called when the resource is a fluid
     */
    public void accept(Consumer<ItemStack> cItemStack, Consumer<FluidStack> cFluidStack) {
        resource.ifLeft(item -> cItemStack.accept(item.create())).ifRight(fluid -> cFluidStack.accept(fluid.create()));
    }

    /**
     * Run something against the resource, dependent on whether it's an item or a fluid, returning a result
     * @param fItemStack function which is called when the resource is an item
     * @param fFluidStack function which is called when the resource is a fluid
     */
    public <T> T apply(Function<ItemStack,T> fItemStack, Function<FluidStack,T> fFluidStack) {
        return resource.map(item -> fItemStack.apply(item.create()), fluid -> fFluidStack.apply(fluid.create()));
    }

    /**
     * The total space required for this resource, in stacks for an item resource, and in mB for a fluid resource
     * @param units number of offer units
     * @return total space required
     */
    public int totalSpaceRequired(int units) {
        return resource.map(
                item -> (((item.count() * units) - 1) / item.create().getMaxStackSize()) + 1,
                fluid -> fluid.amount() * units
        );
    }

    public int countTradesInInventory(ResourceHandler<ItemResource> inv) {
        return resource.left().map(item -> countItemsInHandler(item.create(), inv) / item.count()).orElse(0);
    }

    public int findSpaceInItemOutput(ResourceHandler<ItemResource> inv, int wantedTradeCount) {
        return resource.left().map(item -> Math.min(wantedTradeCount, findSpaceInHandler(item.create(), wantedTradeCount, inv))).orElse(0);
    }

    public int countTradesInTank(ResourceHandler<FluidResource> fluidHandler) {
        return resource.right().map(fluid -> {
            // simulated: the transaction is not committed
            try (Transaction tx = Transaction.openRoot()) {
                return fluidHandler.extract(FluidResource.of(fluid), Integer.MAX_VALUE, tx) / fluid.amount();
            }
        }).orElse(0);
    }

    public int findSpaceInFluidOutput(ResourceHandler<FluidResource> fluidHandler, int wantedTradeCount) {
        return resource.right().map(fluid -> {
            // simulated: the transaction is not committed
            try (Transaction tx = Transaction.openRoot()) {
                return fluidHandler.insert(FluidResource.of(fluid), fluid.amount() * wantedTradeCount, tx) / fluid.amount();
            }
        }).orElse(0);
    }

    public AmadronTradeResource validate() {
        // templates can't be empty
        return this;
    }

    public String getName() {
        return resource.map(
                item -> item.create().getHoverName().getString(),
                fluid -> fluid.create().getHoverName().getString()
        );
    }

    public Identifier getId() {
        return resource.map(
                item -> BuiltInRegistries.ITEM.getKey(item.item().value()),
                fluid -> BuiltInRegistries.FLUID.getKey(fluid.fluid().value())
        );
    }

    public int getAmount() {
        return resource.map(ItemStackTemplate::count, FluidStackTemplate::amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmadronTradeResource that)) return false;
        return resource.equals(that.resource);
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }

    @Override
    public String toString() {
        return resource.map(
                item -> item.count() + " x " + item.create().getHoverName().getString(),
                fluid -> fluid.amount() + "mB " + fluid.create().getHoverName().getString()
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
    private static int countItemsInHandler(ItemStack item, ResourceHandler<ItemResource> itemHandler) {
        boolean matchComponents = !item.getComponentsPatch().isEmpty();
        IItemRegistry registry = PneumaticRegistry.getInstance().getItemRegistry();
        return IntStream.range(0, itemHandler.size())
                .filter(i -> registry.doesItemMatchFilter(item, ItemUtil.getStack(itemHandler, i), false, matchComponents, false))
                .map(i -> ItemUtil.getStack(itemHandler, i).getCount())
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
    private static int findSpaceInHandler(ItemStack item, int multiplier, ResourceHandler<ItemResource> itemHandler) {
        final int totalItems = item.getCount() * multiplier;

        int remaining = totalItems;
        for (int i = 0; i < itemHandler.size() && remaining > 0; i++) {
            if (ItemUtil.getStack(itemHandler, i).isEmpty() || ItemStack.isSameItemSameComponents(ItemUtil.getStack(itemHandler, i), item)) {
                remaining -= item.getMaxStackSize() - ItemUtil.getStack(itemHandler, i).getCount();
            }
        }
        return (totalItems - remaining) / item.getCount();
    }
}
