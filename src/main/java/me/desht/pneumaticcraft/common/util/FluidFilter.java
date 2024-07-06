package me.desht.pneumaticcraft.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class FluidFilter {
    public static final Codec<FluidFilter> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        FluidStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(filter -> filter.fluidStacks)
    ).apply(builder, FluidFilter::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFilter> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), filter -> filter.fluidStacks,
            FluidFilter::new
    );

    private final NonNullList<FluidStack> fluidStacks;

    private FluidFilter(List<FluidStack> stacks) {
        this.fluidStacks = NonNullList.createWithCapacity(stacks.size());
        fluidStacks.addAll(stacks);
    }

    public FluidFilter(int size) {
        fluidStacks = NonNullList.withSize(size, FluidStack.EMPTY);
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
}
