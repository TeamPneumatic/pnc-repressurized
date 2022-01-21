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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

import net.minecraft.world.item.crafting.Ingredient.ItemValue;

/**
 * Like a vanilla ingredient, but requires the item must have no NBT whatsoever
 */
public class NoNBTIngredient extends Ingredient {

    private final ItemStack stack;

    public NoNBTIngredient(ItemStack stack) {
        super(Stream.of(new ItemValue(stack)));
        this.stack = stack;
    }

    public NoNBTIngredient(ItemLike item) {
        this(new ItemStack(item));
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && stack.getItem() == this.stack.getItem() && !stack.hasTag();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        json.addProperty("item", this.stack.getItem().getRegistryName().toString());
        json.addProperty("count", this.stack.getCount());
        return json;
    }

    @Nonnull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<NoNBTIngredient> {
        public static final IIngredientSerializer<NoNBTIngredient> INSTANCE = new NoNBTIngredient.Serializer();
        public static final ResourceLocation ID = new ResourceLocation("pneumaticcraft:no_nbt");

        @Override
        public NoNBTIngredient parse(FriendlyByteBuf buffer) {
            return new NoNBTIngredient(buffer.readItem());
        }

        @Override
        public NoNBTIngredient parse(JsonObject json) {
            return new NoNBTIngredient(ShapedRecipe.itemFromJson(json));
        }

        @Override
        public void write(FriendlyByteBuf buffer, NoNBTIngredient ingredient) {
            buffer.writeItem(ingredient.stack);
        }
    }
}
