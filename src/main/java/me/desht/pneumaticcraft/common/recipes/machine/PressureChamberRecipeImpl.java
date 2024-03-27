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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PressureChamberRecipeImpl extends PressureChamberRecipe {
    private final float pressureRequired;
    private final List<StackedIngredient> inputs;
    private final List<ItemStack> outputs;

    public PressureChamberRecipeImpl(List<StackedIngredient> inputs, float pressureRequired, List<ItemStack> outputs) {
        super();
        this.inputs = ImmutableList.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.pressureRequired = pressureRequired;
    }

    @Override
    public float getPressure() {
        return pressureRequired;
    }

    @Override
    public List<StackedIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<ItemStack> getOutputs() {
        return outputs;
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
    public List<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, IntList ingredientSlots, boolean simulate) {
        // remove the recipe's input items from the chamber
        for (Ingredient ingredient : inputs) {
            if (ingredient.isEmpty()) return NonNullList.create(); // sanity check
            int nItems = ingredient.getItems()[0].getCount();
            for (int i = 0; i < ingredientSlots.size() && nItems > 0; i++) {
                int slot = ingredientSlots.getInt(i);
                if (ingredient.test(chamberHandler.getStackInSlot(slot))) {
                    ItemStack extracted = chamberHandler.extractItem(slot, nItems, simulate);
                    nItems -= extracted.getCount();
                }
            }
        }

        return outputs;
    }

    public static class Serializer<T extends PressureChamberRecipe> implements RecipeSerializer<T> {
        private final Codec<T> codec;
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
            this.codec = RecordCodecBuilder.create(builder -> builder.group(
                    StackedIngredient.CODEC.listOf().fieldOf("inputs").forGetter(PressureChamberRecipe::getInputs),
                    Codec.FLOAT.fieldOf("pressure").forGetter(PressureChamberRecipe::getPressure),
                    ItemStack.ITEM_WITH_COUNT_CODEC.listOf().fieldOf("results").forGetter(PressureChamberRecipe::getOutputs)
            ).apply(builder, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            float pressure = buffer.readFloat();
            List<StackedIngredient> in = buffer.readList(StackedIngredient::fromNetwork);
            List<ItemStack> out = buffer.readList(FriendlyByteBuf::readItem);
            return factory.create(in, pressure, out);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            buffer.writeFloat(recipe.getCraftingPressureForDisplay());
            buffer.writeCollection(recipe.getInputs(), (buf, ingredient) -> ingredient.toNetwork(buf));
            buffer.writeCollection(recipe.getOutputs(), FriendlyByteBuf::writeItem);
        }

        public interface IFactory<T extends PressureChamberRecipe> {
            T create(List<StackedIngredient> inputs, float pressureRequired, List<ItemStack> outputs);
        }
    }
}
