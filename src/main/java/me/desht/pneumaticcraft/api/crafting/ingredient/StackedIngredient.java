package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

/**
 * Like a vanilla Ingredient, but also compares the size of the input ingredient.  Useful for machine recipes which
 * can take multiples of an input item.
 */
public class StackedIngredient extends Ingredient {
    public static final StackedIngredient EMPTY = new StackedIngredient(Stream.empty());

    private StackedIngredient(Stream<? extends IItemList> itemLists) {
        super(itemLists);
    }

    public static Ingredient fromTag(ITag.INamedTag<Item> tag, int count) {
        return StackedIngredient.fromItemListStream(Stream.of(new StackedTagList(tag, count, tag.getName())));
    }

    public static Ingredient fromStacks(ItemStack... stacks) {
        return fromItemListStream(Arrays.stream(stacks).map(StackedItemList::new));
    }

    public static Ingredient fromItems(int count, IItemProvider... itemsIn) {
        return fromItemListStream(Arrays.stream(itemsIn).map((itemProvider) -> new StackedItemList(new ItemStack(itemProvider, count))));
    }

    public static Ingredient fromIngredient(int count, Ingredient wrappedIngredient) {
        List<ItemStack> l = new ArrayList<>();
        for (ItemStack stack : wrappedIngredient.getItems()) {
            l.add(ItemHandlerHelper.copyStackWithSize(stack, count));
        }
        return fromStacks(l.toArray(new ItemStack[0]));
    }

    public static StackedIngredient fromItemListStream(Stream<? extends Ingredient.IItemList> stream) {
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

    private static Ingredient.IItemList deserializeItemListWithCount(JsonObject json) {
        if (json.has("item") && json.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (json.has("item")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(JSONUtils.getAsString(json, "item"));
            Item item = Registry.ITEM.getOptional(resourcelocation1)
                    .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + resourcelocation1 + "'"));
            int count = json.has("count") ? JSONUtils.getAsInt(json, "count") : 1;
            return new Ingredient.SingleItemList(new ItemStack(item, count));
        } else if (json.has("tag")) {
            ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(json, "tag"));
//            ITag<Item> tag = ItemTags.getCollection().get(resourcelocation);
            ITag<Item> tag = TagCollectionManager.getInstance().getItems().getTag(resourcelocation);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown item tag '" + resourcelocation + "'");
            } else {
                int count = json.has("count") ? JSONUtils.getAsInt(json, "count") : 1;
                return new StackedTagList(tag, count, resourcelocation);
            }
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }

    public static class Serializer implements IIngredientSerializer<StackedIngredient> {
        public static final Serializer INSTANCE  = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("pneumaticcraft:stacked_item");

        @Override
        public StackedIngredient parse(PacketBuffer buffer) {
            return fromItemListStream(Stream.generate(() -> new Ingredient.SingleItemList(buffer.readItem())).limit(buffer.readVarInt()));
        }

        @Override
        public StackedIngredient parse(JsonObject json) {
            return fromItemListStream(Stream.of(deserializeItemListWithCount(json)));
        }

        @Override
        public void write(PacketBuffer buffer, StackedIngredient ingredient) {
            ItemStack[] items = ingredient.getItems();
            buffer.writeVarInt(items.length);

            for (ItemStack stack : items)
                buffer.writeItem(stack);
        }
    }

    public static class StackedItemList implements IItemList {
        private final ItemStack itemStack;

        public StackedItemList(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singletonList(itemStack);
        }

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("type", Serializer.ID.toString());
            json.addProperty("item", itemStack.getItem().getRegistryName().toString());
            json.addProperty("count", itemStack.getCount());
            return json;
        }
    }

    public static class StackedTagList implements IItemList {
        private final ITag<Item> tag;
        private final int count;
        private final ResourceLocation id;

        public StackedTagList(ITag<Item> tagIn, int count, ResourceLocation id) {
            this.tag = tagIn;
            this.count = count;
            this.id = id;
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> list = Lists.newArrayList();

            for (Item item : this.tag.getValues()) {
                list.add(new ItemStack(item, count));
            }

            if (list.size() == 0 && !net.minecraftforge.common.ForgeConfig.SERVER.treatEmptyTagsAsAir.get()) {
                list.add(new ItemStack(net.minecraft.block.Blocks.BARRIER).setHoverName(new net.minecraft.util.text.StringTextComponent("Empty Tag: " + id.toString())));
            }
            return list;
        }

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("type", Serializer.ID.toString());
            json.addProperty("tag", id.toString());
            json.addProperty("count", count);
            return json;
        }
    }
}
