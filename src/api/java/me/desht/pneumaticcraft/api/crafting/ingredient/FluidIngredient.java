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

package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A fluid ingredient matcher, with fluid tag support.  Can also match items; it checks if the item contains the
 * desired fluid.
 */
public class FluidIngredient extends Ingredient {
    private List<FluidEntry> fluids;
    private ItemStack[] cachedStacks;
    private final Value[] fluidValues;

    public static final Codec<FluidIngredient> FLUID_CODEC = codec(true);
    public static final Codec<FluidIngredient> FLUID_CODEC_NON_EMPTY = codec(false);

    public static final FluidIngredient EMPTY = FluidIngredient.empty();

    private static FluidIngredient empty() {
        FluidIngredient ingr = new FluidIngredient(new Value[0]);
        ingr.fluids = List.of();
        ingr.cachedStacks = new ItemStack[0];
        return ingr;
    }

    private FluidIngredient(Value[] fluidValues) {
        super(Stream.empty(), PneumaticRegistry.getInstance().getCustomIngredientTypes().fluidType());
        this.fluidValues = fluidValues;
    }

    private FluidIngredient(Stream<Value> fluidValues) {
        this(fluidValues.toArray(Value[]::new));
    }

    public static FluidIngredient of(int amount, Fluid... fluids) {
        return new FluidIngredient(Arrays.stream(fluids).map(f -> new FluidValue(new FluidStack(f, amount), true)));
    }

    public static FluidIngredient of(int amount, TagKey<Fluid> tag) {
        return new FluidIngredient(Stream.of(new FluidTagValue(tag, amount, true)));
    }

    public static FluidIngredient ofFluidStream(Stream<FluidIngredient> fluidIngredientStream) {
        return new FluidIngredient(fluidIngredientStream
                .flatMap(fluidIngredient -> Arrays.stream(fluidIngredient.fluidValues)));
    }

    protected List<FluidEntry> getFluidEntryList() {
        if (fluids == null) {
            fluids = Arrays.stream(fluidValues)
                    .flatMap(val -> val.getFluids().stream())
                    .distinct()
                    .toList();
        }
        return fluids;
    }

    @Override
    public boolean isEmpty() {
        return getFluidEntryList().isEmpty() || getFluidEntryList().stream().allMatch(entry -> entry.fluidStack.isEmpty() || entry.fluidStack.getFluid() == Fluids.EMPTY);
    }

    /**
     * Test the given item against this ingredient. The item must be a fluid container item (providing the
     * {@link Capabilities.FluidHandler} ITEM capability) containing fluid which matches
     * this ingredient, AND it must be a container item
     * ({@link net.neoforged.neoforge.common.extensions.IItemExtension#hasCraftingRemainingItem(ItemStack)} must return true).
     *
     * @param stack the itemstack to test
     * @return true if the fluid in the given itemstack matches this ingredient
     */
    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && stack.hasCraftingRemainingItem() && FluidUtil.getFluidContained(stack).map(this::testFluid).orElse(false);
    }

    @Override
    public ItemStack[] getItems() {
        if (cachedStacks == null) {
            List<ItemStack> tankList = new ArrayList<>();
            for (FluidEntry entry : getFluidEntryList()) {
                FluidStack fluidStack = new FluidStack(entry.fluidStack(), 1000);
                ItemStack bucket = FluidUtil.getFilledBucket(fluidStack);
                if (!bucket.isEmpty()) tankList.add(bucket);
                Stream.of("small", "medium", "large", "huge")
                        .map(tankName -> BuiltInRegistries.BLOCK.get(PneumaticRegistry.RL(tankName + "_tank")))
                        .filter(tankBlock -> tankBlock != Blocks.AIR)
                        .forEach(tankBlock -> maybeAddTank(tankList, tankBlock, fluidStack));
            }
            cachedStacks = tankList.toArray(new ItemStack[0]);
        }
        return cachedStacks;
    }

    private void maybeAddTank(List<ItemStack> l, Block tankBlock, FluidStack stack) {
        ItemStack tank = new ItemStack(tankBlock);
        IFluidHandlerItem handler = tank.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null) {
            handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            l.add(handler.getContainer());
        }
    }

    /**
     * Test the given fluid stack against this ingredient. The fluid must match, and the fluid stack amount must be at
     * least as large. In addition, if the ingredient specifies any NBT, that must also match.
     *
     * @param toMatch the fluid stack to test
     * @return true if the fluid stack matches, false otherwise
     */
    public boolean testFluid(FluidStack toMatch) {
        return getFluidEntryList().stream().anyMatch(entry ->
                toMatch.getFluid() == entry.fluidStack().getFluid()
                        && toMatch.getAmount() >= entry.fluidStack().getAmount()
                        && matchNBT(toMatch, entry.fluidStack, entry.strictNbt)
        );
    }

    /**
     * Test the given fluid against this ingredient. Just a fluid match; no amount or NBT matching is done.
     *
     * @param fluid the fluid to test
     * @return true if the fluid matches, false otherwise
     */
    public boolean testFluid(Fluid fluid) {
        return fluid == Fluids.EMPTY ?
                getFluidEntryList().isEmpty() :
                getFluidEntryList().stream().anyMatch(entry -> entry.fluidStack().getFluid() == fluid);
    }

    private boolean matchNBT(FluidStack toTest, FluidStack stack, boolean strictNbt) {
        if (!toTest.hasTag()) {
            return !strictNbt || !stack.hasTag();
        }

        if (strictNbt) {
            // exact match of all fields is required
            return NbtUtils.compareNbt(stack.getTag(), toTest.getTag(), true);
        } else {
            // match only the fields which are actually present in the ingredient
            CompoundTag nbt = stack.getTag();
            return nbt != null && nbt.getAllKeys().stream().allMatch(key -> NbtUtils.compareNbt(nbt.get(key), toTest.getTag().get(key), true));
        }
    }

    public int getAmount() {
        return getFluidEntryList().isEmpty() ? 0 : getFluidEntryList().get(0).fluidStack().getAmount();
    }

    public List<FluidStack> getFluidStacks() {
        return getFluidEntryList().stream().map(entry -> new FluidStack(entry.fluidStack(), getAmount())).toList();
    }

    private static Codec<FluidIngredient> codec(boolean allowEmpty) {
        Codec<FluidIngredient.Value[]> codec = Codec.list(FluidIngredient.Value.CODEC).comapFlatMap(
                values -> !allowEmpty && values.isEmpty() ?
                        DataResult.error(() -> "Fluid array cannot be empty, at least one fluid must be defined") :
                        DataResult.success(values.toArray(new FluidIngredient.Value[0])),
                List::of
        );
        return ExtraCodecs.either(codec, FluidIngredient.Value.CODEC).flatComapMap(
                either -> either.map(FluidIngredient::new, fluidValue -> new FluidIngredient(new FluidIngredient.Value[]{fluidValue})),
                fluidIngredient -> {
                    if (fluidIngredient.fluidValues.length == 1) {
                        return DataResult.success(Either.right(fluidIngredient.fluidValues[0]));
                    } else {
                        return fluidIngredient.fluidValues.length == 0 && !allowEmpty ?
                                DataResult.error(() -> "Fluid array cannot be empty, at least one fluid must be defined") :
                                DataResult.success(Either.left(fluidIngredient.fluidValues));
                    }
                }
        );
    }

    public interface Value {
        Codec<Value> CODEC = ExtraCodecs.xor(FluidIngredient.FluidValue.CODEC, FluidIngredient.FluidTagValue.CODEC)
                .xmap(either -> either.map(fluidValue -> fluidValue, fluidTagValue -> fluidTagValue), value -> {
                    if (value instanceof FluidIngredient.FluidTagValue fluidTagValue) {
                        return Either.right(fluidTagValue);
                    } else if (value instanceof FluidIngredient.FluidValue fluidValue) {
                        return Either.left(fluidValue);
                    } else {
                        throw new UnsupportedOperationException("This is neither an fluid value nor a tag value.");
                    }
                });

        Collection<FluidEntry> getFluids();
    }

    public record FluidValue(FluidStack fluidStack, BiFunction<FluidStack,FluidStack,Boolean> comparator, boolean strictNbt) implements Value {
        static final Codec<FluidIngredient.FluidValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        FluidStack.CODEC.fieldOf("fluid").forGetter(FluidValue::fluidStack),
                        Codec.BOOL.optionalFieldOf("strict", true).forGetter(FluidValue::strictNbt)
                ).apply(instance, FluidIngredient.FluidValue::new)
        );

        public FluidValue(FluidStack fluidStack, boolean strictNbt) {
            this(fluidStack, (f1, f2) -> f1.isFluidEqual(f2) && f1.getAmount() == f2.getAmount(), strictNbt);
        }

        @Override
        public Collection<FluidEntry> getFluids() {
            return List.of(new FluidEntry(fluidStack, strictNbt));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FluidValue val && comparator().apply(this.fluidStack, val.fluidStack);
        }
    }

    public record FluidTagValue(TagKey<Fluid> tag, int amount, boolean strictNbt) implements Value {
        static final Codec<FluidIngredient.FluidTagValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(val -> val.tag),
                        Codec.INT.optionalFieldOf("amount", 1000).forGetter(val -> val.amount),
                        Codec.BOOL.optionalFieldOf("strict", true).forGetter(FluidTagValue::strictNbt)
                ).apply(instance, FluidIngredient.FluidTagValue::new)
        );

        @Override
        public Collection<FluidEntry> getFluids() {
            List<FluidEntry> list = Lists.newArrayList();
            for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(this.tag)) {
                list.add(new FluidEntry(new FluidStack(holder, amount), strictNbt));
            }
            if (list.isEmpty()) {
                list.add(new FluidEntry(new FluidStack(Fluids.EMPTY, 0), true));
            }
            return list;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FluidTagValue tagValue && tagValue.tag.location().equals(this.tag.location());
        }
    }

    public record FluidEntry(FluidStack fluidStack, boolean strictNbt) {
    }
}
