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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Like a vanilla Ingredient, but also compares the size of the input ingredient.  Useful for machine recipes which
 * can take multiples of an input item.
 */
public class StackedIngredient extends Ingredient {
    public static final StackedIngredient EMPTY = new StackedIngredient(new Value[0]);

    public static final Codec<StackedIngredient> CODEC = codec(true);
    public static final Codec<StackedIngredient> CODEC_NONEMPTY = codec(false);

    public final StackedIngredient.Value[] stackedValues;
    @Nullable
    private ItemStack[] itemStacks;

    private StackedIngredient(Stream<? extends Value> values) {
        this(values, PneumaticRegistry.getInstance().getCustomIngredientTypes().stackedItemType());
    }

    private StackedIngredient(StackedIngredient.Value[] values) {
        this(values, PneumaticRegistry.getInstance().getCustomIngredientTypes().stackedItemType());
    }

    protected StackedIngredient(Stream<? extends StackedIngredient.Value> values, Supplier<? extends IngredientType<?>> type) {
        super(Stream.of(), type);

        stackedValues = values.toArray(Value[]::new);
    }

    private StackedIngredient(StackedIngredient.Value[] values, Supplier<? extends IngredientType<?>> type) {
        super(Stream.of(values), type);

        stackedValues = values;
    }

    public static StackedIngredient fromTag(int count, TagKey<Item> tag) {
        return StackedIngredient.fromItemListStream(Stream.of(new StackedTagValue(tag, count)));
    }

    public static StackedIngredient fromStacks(ItemStack... stacks) {
        return fromItemListStream(Arrays.stream(stacks).map(StackedItemValue::new));
    }

    public static StackedIngredient fromItems(int count, ItemLike... itemsIn) {
        return fromItemListStream(Arrays.stream(itemsIn).map((itemProvider) -> new StackedItemValue(new ItemStack(itemProvider, count))));
    }

    public static StackedIngredient fromIngredient(int count, Ingredient wrappedIngredient) {
        List<ItemStack> l = new ArrayList<>();
        for (ItemStack stack : wrappedIngredient.getItems()) {
            l.add(ItemHandlerHelper.copyStackWithSize(stack, count));
        }
        return fromStacks(l.toArray(new ItemStack[0]));
    }

    private static StackedIngredient fromItemListStream(Stream<? extends StackedIngredient.Value> stream) {
        StackedIngredient ingredient = new StackedIngredient(stream);
        return ingredient.isEmpty() ? StackedIngredient.EMPTY : ingredient;
    }

    @Override
    public boolean test(@Nullable ItemStack checkingStack) {
        if (checkingStack == null || checkingStack.isEmpty()) {
            return super.test(checkingStack);
        } else {
            return Arrays.stream(this.getItems())
                    .anyMatch(stack -> stack.getItem() == checkingStack.getItem() && stack.getCount() <= checkingStack.getCount());
        }
    }

    @Override
    public ItemStack[] getItems() {
        if (this.itemStacks == null) {
            this.itemStacks = Arrays.stream(this.stackedValues)
                    .flatMap(value -> value.getItems().stream())
                    .distinct()
                    .toArray(ItemStack[]::new);
        }

        return this.itemStacks;
    }

    @Override
    public boolean isEmpty() {
        return stackedValues.length == 0 || Arrays.stream(getItems()).allMatch(ItemStack::isEmpty);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StackedIngredient ingredient && Arrays.equals(this.stackedValues, ingredient.stackedValues);
    }

    public static StackedIngredient fromNetwork(FriendlyByteBuf buf) {
        var size = buf.readVarInt();
        if (size == -1) {
            return buf.readWithCodecTrusted(net.minecraft.nbt.NbtOps.INSTANCE, CODEC);
        }
        return new StackedIngredient(Stream.generate(() -> new StackedIngredient.StackedItemValue(buf.readItem())).limit(size));
    }

    private static Codec<StackedIngredient> codec(boolean allowEmpty) {
        Codec<StackedIngredient.Value[]> codec = Codec.list(StackedIngredient.Value.CODEC).comapFlatMap(
                values -> !allowEmpty && values.isEmpty() ?
                        DataResult.error(() -> "Item array cannot be empty, at least one item must be defined") :
                        DataResult.success(values.toArray(new Value[0])), List::of);

        return ExtraCodecs.either(codec, StackedIngredient.Value.CODEC).flatComapMap(
                either -> either.map(StackedIngredient::new, value -> new StackedIngredient(new Value[]{value})),
                ingredient -> {
                    if (ingredient.stackedValues.length == 1) {
                        return DataResult.success(Either.right(ingredient.stackedValues[0]));
                    } else {
                        return ingredient.stackedValues.length == 0 && !allowEmpty ?
                                DataResult.error(() -> "Item array cannot be empty, at least one item must be defined") :
                                DataResult.success(Either.left(ingredient.stackedValues));
                    }
                });
    }

    public interface Value extends Ingredient.Value {
        Codec<StackedIngredient.Value> CODEC = ExtraCodecs.xor(StackedItemValue.CODEC, StackedTagValue.CODEC)
                .xmap(either -> either.map(itemValue -> itemValue, tagValue -> tagValue), val -> {
                    if (val instanceof StackedIngredient.StackedTagValue tagValue) {
                        return Either.right(tagValue);
                    } else if (val instanceof StackedIngredient.StackedItemValue itemValue) {
                        return Either.left(itemValue);
                    } else {
                        throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
                    }
                });
    }

    public record StackedItemValue(ItemStack itemStack, BiFunction<ItemStack, ItemStack, Boolean> comparator) implements Value {
        static final Codec<StackedItemValue> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                        ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("item").forGetter(val -> val.itemStack)
                ).apply(builder, StackedItemValue::new)
        );

        public StackedItemValue(ItemStack itemStack) {
            this(itemStack, StackedItemValue::areStacksEqual);
        }

        private static boolean areStacksEqual(ItemStack left, ItemStack right) {
            return left.getItem().equals(right.getItem()) && left.getCount() == right.getCount();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof StackedItemValue s && comparator.apply(s.itemStack, this.itemStack);
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singletonList(itemStack);
        }
    }

    public static class StackedTagValue implements Value {
        static final Codec<StackedIngredient.StackedTagValue> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                        TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(val -> val.tagKey),
                        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("count").forGetter(val -> val.count)
                ).apply(builder, StackedIngredient.StackedTagValue::new)
        );

        private final TagKey<Item> tagKey;
        private final int count;

        public StackedTagValue(TagKey<Item> tagIn, int count) {
            this.tagKey = tagIn;
            this.count = count;
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> list = Lists.newArrayList();

            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tagKey)) {
                list.add(new ItemStack(holder, count));
            }

            if (list.isEmpty()) {
                list.add(new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Tag: " + tagKey.location())));
            }
            return list;
        }
    }
}
