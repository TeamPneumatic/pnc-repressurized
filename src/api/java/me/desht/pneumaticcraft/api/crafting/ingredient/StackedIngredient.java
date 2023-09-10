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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

/**
 * Like a vanilla Ingredient, but also compares the size of the input ingredient.  Useful for machine recipes which
 * can take multiples of an input item.
 */
public class StackedIngredient extends Ingredient {
    public static final StackedIngredient EMPTY = new StackedIngredient(Stream.empty());

    private StackedIngredient(Stream<? extends Value> itemLists) {
        super(itemLists);
    }

    public static Ingredient fromTag(TagKey<Item> tag, int count) {
        return StackedIngredient.fromItemListStream(Stream.of(new StackedTagList(tag, count)));
    }

    public static Ingredient fromStacks(ItemStack... stacks) {
        return fromItemListStream(Arrays.stream(stacks).map(StackedItemList::new));
    }

    public static Ingredient fromItems(int count, ItemLike... itemsIn) {
        return fromItemListStream(Arrays.stream(itemsIn).map((itemProvider) -> new StackedItemList(new ItemStack(itemProvider, count))));
    }

    public static Ingredient fromIngredient(int count, Ingredient wrappedIngredient) {
        List<ItemStack> l = new ArrayList<>();
        for (ItemStack stack : wrappedIngredient.getItems()) {
            l.add(ItemHandlerHelper.copyStackWithSize(stack, count));
        }
        return fromStacks(l.toArray(new ItemStack[0]));
    }

    public static StackedIngredient fromItemListStream(Stream<? extends Ingredient.Value> stream) {
        StackedIngredient ingredient = new StackedIngredient(stream);
        return ingredient.isEmpty() ? StackedIngredient.EMPTY : ingredient;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
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

    private static Ingredient.Value deserializeItemListWithCount(JsonObject json) {
        if (json.has("item") && json.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (json.has("item")) {
            Item item = ShapedRecipe.itemFromJson(json);
            int count = json.has("count") ? GsonHelper.getAsInt(json, "count") : 1;
            return new Ingredient.ItemValue(new ItemStack(item, count));
        } else if (json.has("tag")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, resourcelocation);
            int count = json.has("count") ? GsonHelper.getAsInt(json, "count") : 1;
            return new StackedTagList(tagKey, count);
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }

    public static class Serializer implements IIngredientSerializer<StackedIngredient> {
        public static final Serializer INSTANCE  = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("pneumaticcraft:stacked_item");

        @Override
        public StackedIngredient parse(FriendlyByteBuf buffer) {
            return fromItemListStream(Stream.generate(() -> new Ingredient.ItemValue(buffer.readItem())).limit(buffer.readVarInt()));
        }

        @Override
        public StackedIngredient parse(JsonObject json) {
            return fromItemListStream(Stream.of(deserializeItemListWithCount(json)));
        }

        @Override
        public void write(FriendlyByteBuf buffer, StackedIngredient ingredient) {
            ItemStack[] items = ingredient.getItems();
            buffer.writeVarInt(items.length);

            for (ItemStack stack : items)
                buffer.writeItem(stack);
        }
    }

    public record StackedItemList(ItemStack itemStack) implements Value {
        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singletonList(itemStack);
        }

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("type", Serializer.ID.toString());
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
            json.addProperty("item", rl == null ? "" : rl.toString());
            json.addProperty("count", itemStack.getCount());
            return json;
        }
    }

    public static class StackedTagList implements Value {
        private final TagKey<Item> tagKey;
        private final int count;

        public StackedTagList(TagKey<Item> tagIn, int count) {
            this.tagKey = tagIn;
            this.count = count;
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> list = Lists.newArrayList();

            Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(tagKey).forEach(item -> list.add(new ItemStack(item, count)));

            if (list.isEmpty()) {
                list.add(new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Tag: " + tagKey.location())));
            }
            return list;
        }

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("type", Serializer.ID.toString());
            json.addProperty("tag", tagKey.location().toString());
            json.addProperty("count", count);
            return json;
        }
    }
}
