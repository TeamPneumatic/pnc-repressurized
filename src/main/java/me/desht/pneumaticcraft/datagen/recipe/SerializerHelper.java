package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

class SerializerHelper {
    static JsonElement serializeOneItemStack(@Nonnull ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", stack.getItem().getRegistryName().toString());
        if (stack.getCount() > 1) {
            json.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }

    static JsonElement serializeItemStacks(@Nonnull ItemStack... stacks) {
        JsonArray res = new JsonArray();
        for (ItemStack stack : stacks) {
            res.add(serializeOneItemStack(stack));
        }
        return res;
    }
}
