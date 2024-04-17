package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidFilter implements INBTSerializable<CompoundTag> {
    private final NonNullList<FluidStack> fluidStacks;

    public FluidFilter() {
        this(AbstractLogisticsFrameEntity.FLUID_FILTER_SLOTS);
    }

    public FluidFilter(int size) {
        fluidStacks = NonNullList.withSize(size, FluidStack.EMPTY);
    }

    public FluidFilter(FriendlyByteBuf packetBuffer) {
        fluidStacks = NonNullList.create();
        int size = packetBuffer.readVarInt();
        for (int i = 0; i < size; i++) {
            fluidStacks.add(packetBuffer.readFluidStack());
        }
    }

    public int size() {
        return fluidStacks.size();
    }

    public void setFluid(int filterIndex, FluidStack stack) {
        if (filterIndex >= 0 && filterIndex < fluidStacks.size()) {
            fluidStacks.set(filterIndex, stack);
        }
    }

    public FluidStack getFluid(int filterIndex) {
        return filterIndex >= 0 && filterIndex < fluidStacks.size() ? fluidStacks.get(filterIndex) : FluidStack.EMPTY;
    }

    public boolean matchFluid(Fluid fluid) {
        return fluidStacks.stream()
                .anyMatch(filterStack -> !filterStack.isEmpty() && filterStack.getFluid() == fluid);
    }

    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeCollection(fluidStacks, FriendlyByteBuf::writeFluidStack);
    }

    @Override
    public CompoundTag serializeNBT() {
        return Util.make(new CompoundTag(), t -> {
            ListTag list = new ListTag();
            for (int i = 0; i < fluidStacks.size(); i++) {
                FluidStack f = fluidStacks.get(i);
                if (!f.isEmpty()) {
                    CompoundTag t1 = new CompoundTag();
                    t1.putInt("Slot", i);
                    f.writeToNBT(t1);
                    list.add(t1);
                }
            }
            t.put("fluids", list);
        });
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        fluidStacks.replaceAll(ignored -> FluidStack.EMPTY);

        ListTag list = nbt.getList("fluids", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            int slot = t.getInt("Slot");
            if (slot >= 0 && slot < fluidStacks.size()) {
                fluidStacks.set(slot, FluidStack.loadFluidStackFromNBT(t));
            }
        }
    }
}
