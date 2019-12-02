package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

/**
 * Like a vanilla ingredient, but also checks the size of the input ingredient
 */
public class StackedIngredient extends Ingredient {
    public static final StackedIngredient EMPTY = new StackedIngredient(Stream.empty());

    protected StackedIngredient(Stream<? extends IItemList> itemLists) {
        super(itemLists);
    }

    public static Ingredient fromTag(Tag<Item> tag, int count) {
        return StackedIngredient.fromItemListStream(Stream.of(new TagListStacked(tag, count)));
    }

    public static StackedIngredient fromItemListStream(Stream<? extends Ingredient.IItemList> stream) {
        StackedIngredient ingredient = new StackedIngredient(stream);
        return ingredient.hasNoMatchingItems() ? StackedIngredient.EMPTY : ingredient;
    }

    @Override
    public boolean test(@Nullable ItemStack checkingStack) {
        if (checkingStack == null || checkingStack.isEmpty()) {
            return super.test(checkingStack);
        } else {
            for (ItemStack stack : this.getMatchingStacks()) {
                if (stack.getItem() == checkingStack.getItem() && stack.getCount() <= checkingStack.getCount()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static Ingredient.IItemList deserializeItemListWithCount(JsonObject json) {
        if (json.has("item") && json.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (json.has("item")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(JSONUtils.getString(json, "item"));
            Item item = Registry.ITEM.getValue(resourcelocation1)
                    .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + resourcelocation1 + "'"));
            int count = json.has("count") ? JSONUtils.getInt(json, "count") : 1;
            return new Ingredient.SingleItemList(new ItemStack(item, count));
        } else if (json.has("tag")) {
            ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getString(json, "tag"));
            Tag<Item> tag = ItemTags.getCollection().get(resourcelocation);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown item tag '" + resourcelocation + "'");
            } else {
                int count = json.has("count") ? JSONUtils.getInt(json, "count") : 1;
                return new TagListStacked(tag, count);
            }
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }

    public static class Serializer implements IIngredientSerializer<StackedIngredient> {
        public static final Serializer INSTANCE  = new Serializer();
        public static final ResourceLocation ID = RL("stacked_item");

        @Override
        public StackedIngredient parse(PacketBuffer buffer) {
            return fromItemListStream(Stream.generate(() -> new Ingredient.SingleItemList(buffer.readItemStack())).limit(buffer.readVarInt()));
        }

        @Override
        public StackedIngredient parse(JsonObject json) {
            return fromItemListStream(Stream.of(deserializeItemListWithCount(json)));
        }

        @Override
        public void write(PacketBuffer buffer, StackedIngredient ingredient) {
            ItemStack[] items = ingredient.getMatchingStacks();
            buffer.writeVarInt(items.length);

            for (ItemStack stack : items)
                buffer.writeItemStack(stack);
        }
    }

    public static class TagListStacked implements Ingredient.IItemList {
        private final Tag<Item> tag;
        private final int count;

        public TagListStacked(Tag<Item> tagIn, int count) {
            this.tag = tagIn;
            this.count = count;
        }

        @Override
        public Collection<ItemStack> getStacks() {
            List<ItemStack> list = Lists.newArrayList();

            for (Item item : this.tag.getAllElements()) {
                list.add(new ItemStack(item, count));
            }

            if (list.size() == 0 && !net.minecraftforge.common.ForgeConfig.SERVER.treatEmptyTagsAsAir.get()) {
                list.add(new ItemStack(net.minecraft.block.Blocks.BARRIER).setDisplayName(new net.minecraft.util.text.StringTextComponent("Empty Tag: " + tag.getId().toString())));
            }
            return list;
        }

        @Override
        public JsonObject serialize() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("tag", this.tag.getId().toString());
            return jsonobject;
        }
    }
}
