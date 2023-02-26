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

package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.core.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PressureChamberRecipeImpl extends PressureChamberRecipe {
    private final float pressureRequired;
    private final List<Ingredient> inputs;
    private final NonNullList<ItemStack> outputs;

    public PressureChamberRecipeImpl(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs) {
        super(id);
        this.inputs = ImmutableList.copyOf(inputs);
        this.outputs = NonNullList.of(ItemStack.EMPTY, outputs);
        this.pressureRequired = pressureRequired;
    }

    @Override
    public float getCraftingPressure(IItemHandler chamberHandler, IntList ingredientSlots) {
        return pressureRequired;
    }

    @Override
    public float getCraftingPressureForDisplay() {
        return pressureRequired;
    }

    @Override
    public IntCollection findIngredients(IItemHandler chamberHandler) {
        // Ingredient doesn't override equals() and hashCode() but there's always the possibility
        // that some subclass might, so we'll use an identity set here.  We want to always treat
        // two equivalent ingredients in a recipe as different objects.
        Set<Ingredient> inputSet = Sets.newIdentityHashSet();
        inputSet.addAll(inputs);

        IntCollection slots = new IntArrayList();
        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            if (!chamberHandler.getStackInSlot(i).isEmpty()) {
                Iterator<Ingredient> iter = inputSet.iterator();
                while (iter.hasNext()) {
                    Ingredient ingr = iter.next();
                    if (ingr.test(chamberHandler.getStackInSlot(i))) {
                        iter.remove();
                        slots.add(i);
                        break;
                    }
                }
                if (slots.size() == inputs.size()) {
                    return slots;
                }
            }
        }
        return IntList.of();
    }

    @Override
    public List<Ingredient> getInputsForDisplay() {
        return new ArrayList<>(inputs);
    }

    @Override
    protected List<ItemStack> getSingleResultsForDisplay() {
        return outputs;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.PRESSURE_CHAMBER.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PRESSURE_CHAMBER.get();
    }

    @Override
    public String getGroup() {
        return "pneumaticcraft:pressure_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get());
    }

    @Override
    public boolean isValidInputItem(ItemStack stack) {
        ItemStack s2 = ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize());
        return inputs.stream().anyMatch(ingr -> ingr.test(s2));
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, IntList ingredientSlots, boolean simulate) {
        // remove the recipe's input items from the chamber
        for (Ingredient ingredient : inputs) {
            if (ingredient.isEmpty()) return NonNullList.create(); // sanity check
            int nItems = ingredient.getItems()[0].getCount();
            for (int i = 0; i < ingredientSlots.size() && nItems > 0; i++) {
                int slot = ingredientSlots.get(i);
                if (ingredient.test(chamberHandler.getStackInSlot(slot))) {
                    ItemStack extracted = chamberHandler.extractItem(slot, nItems, simulate);
                    nItems -= extracted.getCount();
                }
            }
        }

        return outputs;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeFloat(getCraftingPressureForDisplay());
        buffer.writeVarInt(inputs.size());
        inputs.forEach(i -> i.toNetwork(buffer));
        buffer.writeVarInt(outputs.size());
        outputs.forEach(buffer::writeItem);
    }

    public static class Serializer<T extends PressureChamberRecipe> implements RecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonArray inputs = json.get("inputs").getAsJsonArray();
            List<Ingredient> inputIngredients = new ArrayList<>();
            for (JsonElement e : inputs) {
                inputIngredients.add(Ingredient.fromJson(e.getAsJsonObject()));
            }
            float pressure = GsonHelper.getAsFloat(json, "pressure");
            JsonArray outputs = json.get("results").getAsJsonArray();
            NonNullList<ItemStack> results = NonNullList.create();
            for (JsonElement e : outputs) {
                results.add(ShapedRecipe.itemStackFromJson(e.getAsJsonObject()));
            }
            return factory.create(recipeId, inputIngredients, pressure, results.toArray(new ItemStack[0]));
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            float pressure = buffer.readFloat();
            int nInputs = buffer.readVarInt();
            List<Ingredient> in = new ArrayList<>();
            for (int i = 0; i < nInputs; i++) {
                in.add(Ingredient.fromNetwork(buffer));
            }
            int nOutputs = buffer.readVarInt();
            ItemStack[] out = new ItemStack[nOutputs];
            for (int i = 0; i < nOutputs; i++) {
                out[i] = buffer.readItem();
            }
            return factory.create(recipeId, in, pressure, out);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends PressureChamberRecipe> {
            T create(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs);
        }
    }
}
