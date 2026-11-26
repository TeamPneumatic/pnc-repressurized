/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

/**
 * @author MineMaarten
 * @author Dynious
 * @author desht
 */
public class IOHelper {
    public enum ExtractCount {
        /**
         * Extract exactly the items specified, including amount.  If the exact number isn't available, extract nothing.
         */
        EXACT,
        /**
         * Extract the first matching ItemStack in the inventory, but not more than the given amount.
         */
        FIRST_MATCHING,
        /**
         * Extract up to the number of items specified but not more.
         */
        UP_TO
    }

    public static Optional<ResourceHandler<ItemResource>> getInventoryForBlock(BlockEntity te, Direction facing) {
        return getCap(te, Capabilities.Item.BLOCK, facing);
    }

    public static Optional<ResourceHandler<ItemResource>> getInventoryForBlock(BlockEntity te) {
        return getInventoryForBlock(te, null);
    }

    public static Optional<ResourceHandler<ItemResource>> getSidedInventoryForEntity(Entity entity, Direction dir) {
        ResourceHandler<ItemResource> handler = entity.getCapability(PNCCapabilities.ENTITY_AUTOMATION, dir);
        if (handler == null) {
            handler = entity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, dir);
        }
        if (handler == null) {
            handler = entity.getCapability(Capabilities.Item.ENTITY, null);
        }
        return Optional.ofNullable(handler);
    }

    public static Optional<ResourceHandler<ItemResource>> getInventoryForEntity(Entity entity) {
        return Optional.ofNullable(entity.getCapability(Capabilities.Item.ENTITY, null));
    }

    public static Optional<ResourceHandler<FluidResource>> getFluidHandlerForBlock(BlockEntity te, Direction facing) {
        return getCap(te, Capabilities.Fluid.BLOCK, facing);
    }

    public static Optional<ResourceHandler<FluidResource>> getFluidHandlerForBlock(BlockEntity te) {
        return getFluidHandlerForBlock(te, null);
    }

    /**
     * Get a fluid handler for an item stack. The stack itself is never modified; if the handler needs to change the
     * item (e.g. filling an empty bucket), the change is lost. Use this for read-only queries; to actually move
     * fluid, use an {@link ItemAccess} which can hold the modified item, e.g. {@link ItemAccess#forHandlerIndex}.
     *
     * @param stack the item stack
     * @return the fluid handler, if any
     */
    public static Optional<ResourceHandler<FluidResource>> getFluidHandlerForItem(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.ofNullable(ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM));
    }

    public static Optional<ResourceHandler<FluidResource>> getFluidHandlerForEntity(Entity entity, Direction dir) {
        return Optional.ofNullable(entity.getCapability(Capabilities.Fluid.ENTITY, dir));
    }

    public static Optional<EnergyHandler> getEnergyStorageForBlock(BlockEntity te, Direction side) {
        return getCap(te, Capabilities.Energy.BLOCK, side);
    }

    public static Optional<EnergyHandler> getEnergyStorageForBlock(BlockEntity te) {
        return getCap(te, Capabilities.Energy.BLOCK, null);
    }

    public static Optional<EnergyHandler> getEnergyStorageForItem(ItemStack item) {
        return item.isEmpty() ? Optional.empty() : Optional.ofNullable(ItemAccess.forStack(item).getCapability(Capabilities.Energy.ITEM));
    }

    public static <T> Optional<T> getCap(ItemStack stack, ItemCapability<T, Void> cap) {
        return Optional.ofNullable(stack.getCapability(cap));
    }

    public static <T> Optional<T> getCapV(Entity entity, EntityCapability<T, Void> cap) {
        return Optional.ofNullable(entity.getCapability(cap, null));
    }

    public static <T> Optional<T> getCap(Entity entity, EntityCapability<T, Direction> cap) {
        return Optional.ofNullable(entity.getCapability(cap, null));
    }

    /**
     * Get the stack in a slot of an item handler. For PNC's own item handlers ({@link BaseItemStackHandler}), this is
     * the stored stack itself, like the legacy {@code ItemStackHandler#getStackInSlot()}: code which modifies it
     * directly (e.g. drone AI) relies on that. For any other handler, it's a copy.
     *
     * @param handler the item handler
     * @param slot the slot
     * @return the stack in the slot
     */
    public static ItemStack getStack(ResourceHandler<ItemResource> handler, int slot) {
        return handler instanceof BaseItemStackHandler b ? b.getStackInSlot(slot) : ItemUtil.getStack(handler, slot);
    }

    /**
     * Directly set the stack in a slot of an item handler, bypassing any insertion rules.
     *
     * @param handler the item handler, which must be directly modifiable (see {@link #modifierFor(ResourceHandler)})
     * @param slot the slot
     * @param stack the new stack
     */
    public static void setStack(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        modifierFor(handler).set(slot, ItemResource.of(stack), stack.getCount());
    }

    /**
     * Get a modifier which can directly set the contents of an item handler.
     *
     * @param handler a {@link StacksResourceHandler}, an {@link IndexModifier}, or a {@link DelegatingResourceHandler}
     *                which wraps either of those
     * @return the modifier
     * @throws IllegalArgumentException if the handler can't be modified directly
     */
    @SuppressWarnings("unchecked")
    public static IndexModifier<ItemResource> modifierFor(ResourceHandler<ItemResource> handler) {
        if (handler instanceof IndexModifier<?> m) {
            return (IndexModifier<ItemResource>) m;
        } else if (handler instanceof StacksResourceHandler<?, ?> stacks) {
            return ((StacksResourceHandler<ItemStack, ItemResource>) stacks)::set;
        } else if (handler instanceof DelegatingResourceHandler<ItemResource> delegating) {
            return modifierFor(delegating.getDelegate());
        }
        throw new IllegalArgumentException("item handler can't be modified directly: " + handler);
    }

    /**
     * Insert an item stack into a specific slot of an item handler.
     *
     * @param handler the item handler
     * @param slot the slot
     * @param stack the stack to insert; not modified
     * @param simulate true to only simulate the insertion
     * @return the items which could not be inserted
     */
    public static ItemStack insert(ResourceHandler<ItemResource> handler, int slot, ItemStack stack, boolean simulate) {
        return ItemUtil.insertItemReturnRemaining(handler, slot, stack, simulate, null);
    }

    /**
     * Insert an item stack into an item handler, trying each slot in turn.
     *
     * @param handler the item handler
     * @param stack the stack to insert; not modified
     * @param simulate true to only simulate the insertion
     * @return the items which could not be inserted
     */
    public static ItemStack insert(@Nullable ResourceHandler<ItemResource> handler, ItemStack stack, boolean simulate) {
        if (handler == null) return stack;
        return ItemUtil.insertItemReturnRemaining(handler, stack, simulate, null);
    }

    /**
     * Insert an item stack into an item handler, preferring slots which already contain the item.
     *
     * @param handler the item handler
     * @param stack the stack to insert; not modified
     * @param simulate true to only simulate the insertion
     * @return the items which could not be inserted
     */
    public static ItemStack insertStacked(@Nullable ResourceHandler<ItemResource> handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) return stack;
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) tx.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    /**
     * Extract items from a specific slot of an item handler.
     *
     * @param handler the item handler
     * @param slot the slot
     * @param amount the maximum number of items to extract
     * @param simulate true to only simulate the extraction
     * @return the extracted items
     */
    public static ItemStack extract(ResourceHandler<ItemResource> handler, int slot, int amount, boolean simulate) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(slot, resource, amount, tx);
            if (!simulate) tx.commit();
            return resource.toStack(extracted);
        }
    }

    /**
     * Extract a specific number of the given item from the given item handler
     *
     * @param handler the item handler
     * @param requestedStack the item to search for, including the number of items; this stack is not modified
     * @param countType how to interpret the item count of requestedStack
     * @param simulate true if extraction should only be simulated
     * @param matchNBT if true, require an exact match of item NBT
     *
     * @return the extracted item stack, or ItemStack.EMPTY if nothing was extracted
     */
    public static ItemStack extract(ResourceHandler<ItemResource> handler, ItemStack requestedStack, ExtractCount countType, boolean simulate, boolean matchNBT) {
        if (requestedStack.isEmpty()) return requestedStack;

        if (handler != null) {
            int itemsFound = 0;
            IntList slotsOfInterest = new IntArrayList();
            for (int slot = 0; slot < handler.size() && itemsFound < requestedStack.getCount(); slot++) {
                ItemResource resource = handler.getResource(slot);
                if (!resource.isEmpty() && matchStacks(resource, requestedStack, matchNBT)) {
                    int count = handler.getAmountAsInt(slot);
                    if (countType == ExtractCount.FIRST_MATCHING) {
                        return extract(handler, slot, Math.min(requestedStack.getCount(), count), simulate);
                    }
                    itemsFound += count;
                    slotsOfInterest.add(slot);
                }
            }
            if (countType == ExtractCount.UP_TO || itemsFound >= requestedStack.getCount()) {
                ItemResource exported = ItemResource.EMPTY;
                int itemsNeeded = requestedStack.getCount();
                int totalExtracted = 0;
                try (Transaction tx = Transaction.openRoot()) {
                    for (int slot : slotsOfInterest) {
                        ItemResource resource = handler.getResource(slot);
                        if (!resource.isEmpty() && matchStacks(resource, requestedStack, matchNBT)) {
                            int itemsSubtracted = Math.min(itemsNeeded, handler.getAmountAsInt(slot));
                            if (itemsSubtracted > 0) {
                                exported = resource;
                            }
                            itemsNeeded -= itemsSubtracted;
                            if (itemsSubtracted > 0) {
                                totalExtracted += handler.extract(slot, resource, itemsSubtracted, tx);
                            }
                        }
                    }
                    if (!simulate) tx.commit();
                }
                return exported.toStack(totalExtracted);
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean matchStacks(ItemResource resource, ItemStack stack, boolean matchNBT) {
        return matchNBT ? resource.matches(stack) : resource.is(stack.getItem());
    }

    @Nonnull
    public static ItemStack insert(BlockEntity tile, ItemStack itemStack, boolean simulate) {
        for (Direction side : Direction.values()) {
            ItemStack inserted = getInventoryForBlock(tile, side)
                    .map(handler -> insert(handler, itemStack, simulate))
                    .orElse(ItemStack.EMPTY);
            if (inserted.getCount() < itemStack.getCount()) return inserted;
        }
        return itemStack;
    }

    @Nonnull
    public static ItemStack insert(BlockEntity tile, ItemStack itemStack, Direction side, boolean simulate) {
        return getInventoryForBlock(tile, side).map(handler -> insert(handler, itemStack, simulate)).orElse(itemStack);
    }

    /**
     * Try to transfer a single item between two item handlers
     *
     * @param input the input handler
     * @param output the output handler
     * @return true if an item was transferred
     */
    public static boolean transferOneItem(ResourceHandler<ItemResource> input, ResourceHandler<ItemResource> output) {
        if (input == null || output == null) return false;

        try (Transaction tx = Transaction.openRoot()) {
            if (ResourceHandlerUtil.moveStacking(input, output, r -> true, 1, tx) == 1) {
                tx.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * Insert an item into an item handler, dropping any excess in the world
     * @param world the world
     * @param stack the stack to insert
     * @param handler the item handler
     * @param dropPos position to drop excess items at
     * @param simulate true if only simulating (excess will not be dropped)
     */
    public static void insertOrDrop(Level world, ItemStack stack, ResourceHandler<ItemResource> handler, Vec3 dropPos, boolean simulate) {
        ItemStack remainder = insert(handler, stack, simulate);
        if (!remainder.isEmpty() && !simulate) {
            ItemEntity item = new ItemEntity(world, dropPos.x(), dropPos.y(), dropPos.z(), remainder);
            world.addFreshEntity(item);
        }
    }

    /**
     * Count the number of items in the given handler which match the given predicate
     * @param cap the item handler capability
     * @param pred a matching predicate
     * @return the number of matching items
     */
    public static int countItems(ResourceHandler<ItemResource> cap, Predicate<ItemStack> pred) {
        return IntStream.range(0, cap.size())
                .filter(i -> !cap.getResource(i).isEmpty() && pred.test(getStack(cap, i)))
                .map(cap::getAmountAsInt)
                .sum();
    }

    /**
     * Insert energy into an energy handler.
     *
     * @param handler the energy handler
     * @param amount the amount to insert
     * @param simulate true to only simulate the insertion
     * @return the amount inserted
     */
    public static int insertEnergy(@Nullable EnergyHandler handler, int amount, boolean simulate) {
        if (handler == null || amount <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(amount, tx);
            if (!simulate) tx.commit();
            return inserted;
        }
    }

    /**
     * Extract energy from an energy handler.
     *
     * @param handler the energy handler
     * @param amount the amount to extract
     * @param simulate true to only simulate the extraction
     * @return the amount extracted
     */
    public static int extractEnergy(@Nullable EnergyHandler handler, int amount, boolean simulate) {
        if (handler == null || amount <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(amount, tx);
            if (!simulate) tx.commit();
            return extracted;
        }
    }

    /**
     * {@return the capability from a capability cache, or null if there's no cache or no capability}
     */
    @Nullable
    public static <T> T getCapability(@Nullable BlockCapabilityCache<T, Direction> cache) {
        return cache == null ? null : cache.getCapability();
    }

    public static <T> Optional<T> getCap(BlockEntity te, BlockCapability<T,Direction> cap, Direction face) {
        return te == null || te.getLevel() == null ?
                Optional.empty() :
                Optional.ofNullable(te.getLevel().getCapability(cap, te.getBlockPos(), te.getBlockState(), te, face));
    }
}
