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

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class PressureChamberRecipeImpl extends PressureChamberRecipe {
    private final float pressureRequired;
    private final List<SizedIngredient> inputs;
    private final List<ItemStack> outputs;
    private List<List<ItemStack>> displayStacks = null;

    public PressureChamberRecipeImpl(List<SizedIngredient> inputs, float pressureRequired, List<ItemStack> outputs) {
        super();
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.pressureRequired = pressureRequired;
    }

    @Override
    public float getPressure() {
        return pressureRequired;
    }

    @Override
    public List<SizedIngredient> getInputs() {
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
        Set<SizedIngredient> inputSet = Sets.newIdentityHashSet();
        inputSet.addAll(inputs);

        IntCollection slots = new IntArrayList();
        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            if (!chamberHandler.getStackInSlot(i).isEmpty()) {
                Iterator<SizedIngredient> iter = inputSet.iterator();
                while (iter.hasNext()) {
                    SizedIngredient ingr = iter.next();
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
    public List<List<ItemStack>> getInputsForDisplay() {
        if (displayStacks == null) {
            displayStacks = inputs.stream().map(ingredient -> Arrays.asList(ingredient.getItems())).toList();
        }
        return displayStacks;
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
        ItemStack s2 = stack.copyWithCount(stack.getMaxStackSize());
        return inputs.stream().anyMatch(ingr -> ingr.test(s2));
    }

    @Nonnull
    @Override
    public List<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, IntList ingredientSlots, boolean simulate) {
        // remove the recipe's input items from the chamber
        for (SizedIngredient ingredient : inputs) {
//            if (ingredient.isEmpty()) return NonNullList.create(); // sanity check
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


    public interface IFactory<T extends PressureChamberRecipe> {
        T create(List<SizedIngredient> inputs, float pressureRequired, List<ItemStack> outputs);
    }

    public static class Serializer<T extends PressureChamberRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(IFactory<T> factory) {
            codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    SizedIngredient.FLAT_CODEC.listOf().fieldOf("inputs").forGetter(PressureChamberRecipe::getInputs),
                    Codec.floatRange(-1f, 5f).fieldOf("pressure").forGetter(PressureChamberRecipe::getPressure),
                    ItemStack.CODEC.listOf().fieldOf("results").forGetter(PressureChamberRecipe::getOutputs)
            ).apply(builder, factory::create));
            streamCodec = StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), PressureChamberRecipe::getInputs,
                    ByteBufCodecs.FLOAT, PressureChamberRecipe::getPressure,
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), PressureChamberRecipe::getOutputs,
                    factory::create
            );
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
