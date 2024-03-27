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

package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Fluid handler for items which are created from dropped block entities with saved fluid data.
 * Fluids are saved in "BlockEntityTag" -> "SavedTanks" -> "[name]", where "[name]" is the {@code tankName}
 * parameter passed to the constructor.
 */
public class FluidHandlerSavedItemStack implements IFluidHandlerItem {
    private final ItemStack holderStack;
    private final String tankName;
    private final Predicate<Fluid> fluidPredicate;
    private final FluidTank fluidTank;

    public FluidHandlerSavedItemStack(ItemStack holderStack, String tankName, int capacity, Predicate<Fluid> fluidPredicate) {
        this.holderStack = holderStack;
        this.tankName = tankName;
        this.fluidPredicate = fluidPredicate;

        FluidTank tank = deserializeTank(holderStack, tankName, capacity);
        fluidTank = tank == null ? new FluidTank(capacity) : tank;
    }

    public FluidHandlerSavedItemStack(ItemStack holderStack, String tankName, int capacity) {
        this(holderStack, tankName, capacity, fluid -> true);
    }

    /**
     * Serialize some tank data onto the handler's ItemStack.  Used by the item's fluid handler capability.  If the
     * tank is empty, it will be removed from the stack's NBT to keep it clean (helps with stackability)
     * <p>
     * Data is serialized under the "BlockEntityTag" sub-tag, so will
     * be automatically deserialized back into the block entity when this item (assuming it's from a block,
     * of course) is placed back down.
     *
     * @param tank the fluid tank
     * @param tagName name of the subtag in the itemstack's NBT to store the tank data
     */
    private void serializeTank(FluidTank tank, String tagName) {
        CompoundTag tag = holderStack.getOrCreateTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        CompoundTag subTag = tag.getCompound(NBTKeys.NBT_SAVED_TANKS);
        if (!tank.getFluid().isEmpty()) {
            subTag.put(tagName, tank.writeToNBT(new CompoundTag()));
        } else {
            subTag.remove(tagName);
        }
        if (!subTag.isEmpty()) {
            tag.put(NBTKeys.NBT_SAVED_TANKS, subTag);
        } else {
            // clean up NBT if possible
            tag.remove(NBTKeys.NBT_SAVED_TANKS);
            if (tag.isEmpty()) {
                Objects.requireNonNull(holderStack.getTag()).remove(NBTKeys.BLOCK_ENTITY_TAG);
                if (holderStack.getTag().isEmpty()) {
                    holderStack.setTag(null);
                }
            }
        }
    }

    /**
     * Deserialize some fluid tank data from an ItemStack into a fluid tank.  Used by the
     * item's fluid handler capability.
     *
     * @param stack the itemstack to load from
     * @param tagName name of the subtag in the itemstack's NBT which holds the saved tank data
     * @param capacity capacity of the created tank
     * @return the deserialized tank, or null
     */
    private FluidTank deserializeTank(ItemStack stack, String tagName, int capacity) {
        CompoundTag tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(NBTKeys.NBT_SAVED_TANKS)) {
            FluidTank tank = new FluidTank(capacity);
            CompoundTag subTag = tag.getCompound(NBTKeys.NBT_SAVED_TANKS);
            return tank.readFromNBT(subTag.getCompound(tagName));
        }
        return null;
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return holderStack;
    }

    @Override
    public int getTanks() {
        return fluidTank == null ? 0 : fluidTank.getTanks();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidTank == null ? FluidStack.EMPTY : fluidTank.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidTank == null ? 0 : fluidTank.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return fluidTank != null && fluidPredicate.test(stack.getFluid()) && fluidTank.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (fluidTank == null || !isFluidValid(0, resource)) {
            return 0;
        }
        int filled = fluidTank.fill(resource, doFill);
        if (filled > 0 && doFill == FluidAction.EXECUTE) {
            serializeTank(fluidTank, tankName);
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain) {
        if (fluidTank == null) {
            return FluidStack.EMPTY;
        }
        FluidStack drained = fluidTank.drain(resource, doDrain);
        if (!drained.isEmpty() && doDrain == FluidAction.EXECUTE) {
            serializeTank(fluidTank, tankName);
        }
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        if (fluidTank == null) {
            return FluidStack.EMPTY;
        }
        FluidStack drained = fluidTank.drain(maxDrain, doDrain);
        if (!drained.isEmpty() && doDrain == FluidAction.EXECUTE) {
            serializeTank(fluidTank, tankName);
        }
        return drained;
    }
}
