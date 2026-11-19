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
import me.desht.pneumaticcraft.api.crafting.recipe.PneumaticCraftRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nonnull;
import java.util.*;

public class PressureChamberRecipeImpl extends PressureChamberRecipe {
    private final float pressureRequired;
    private final List<SizedIngredient> inputs;
    private final List<ItemStackTemplate> outputs;
    private List<List<ItemStack>> displayStacks = null;

    public PressureChamberRecipeImpl(List<SizedIngredient> inputs, float pressureRequired, List<ItemStackTemplate> outputs) {
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
        return outputs.stream().map(ItemStackTemplate::create).toList();
    }

    public List<ItemStackTemplate> getOutputTemplates() {
        return outputs;
    }

    @Override
    public float getCraftingPressure(ResourceHandler<ItemResource> chamberHandler, IntList ingredientSlots) {
        return pressureRequired;
    }

    @Override
    public float getCraftingPressureForDisplay() {
        return pressureRequired;
    }

    @Override
    public IntCollection findIngredients(ResourceHandler<ItemResource> chamberHandler) {
        // Ingredient doesn't override equals() and hashCode() but there's always the possibility
        // that some subclass might, so we'll use an identity set here.  We want to always treat
        // two equivalent ingredients in a recipe as different objects.
        Set<SizedIngredient> inputSet = Sets.newIdentityHashSet();
        inputSet.addAll(inputs);

        IntCollection slots = new IntArrayList();
        for (int i = 0; i < chamberHandler.size(); i++) {
            if (!IOHelper.getStack(chamberHandler, i).isEmpty()) {
                Iterator<SizedIngredient> iter = inputSet.iterator();
                while (iter.hasNext()) {
                    SizedIngredient ingr = iter.next();
                    if (ingr.test(IOHelper.getStack(chamberHandler, i))) {
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
    public List<List<ItemStack>> getInputsForDisplay(HolderLookup.Provider provider) {
        if (displayStacks == null) {
            displayStacks = inputs.stream()
                    .map(ingredient -> ingredient.ingredient().items().map(item -> new ItemStack(item, ingredient.count())).toList())
                    .toList();
        }
        return displayStacks;
    }

    @Override
    protected List<ItemStack> getSingleResultsForDisplay(HolderLookup.Provider provider) {
        return getOutputs();
    }

    @Override
    public RecipeType<? extends PneumaticCraftRecipe> getType() {
        return ModRecipeTypes.PRESSURE_CHAMBER.get();
    }

    @Override
    public RecipeSerializer<? extends PneumaticCraftRecipe> getSerializer() {
        return ModRecipeSerializers.PRESSURE_CHAMBER.get();
    }

    @Override
    public String group() {
        return "pneumaticcraft:pressure_chamber";
    }


    @Override
    public boolean isValidInputItem(ItemStack stack) {
        ItemStack s2 = stack.copyWithCount(stack.getMaxStackSize());
        return inputs.stream().anyMatch(ingr -> ingr.test(s2));
    }

    @Nonnull
    @Override
    public List<ItemStack> craftRecipe(@Nonnull ResourceHandler<ItemResource> chamberHandler, IntList ingredientSlots, boolean simulate) {
        // remove the recipe's input items from the chamber
        for (SizedIngredient ingredient : inputs) {
//            if (ingredient.isEmpty()) return NonNullList.create(); // sanity check
            int nItems = ingredient.count();
            for (int i = 0; i < ingredientSlots.size() && nItems > 0; i++) {
                int slot = ingredientSlots.getInt(i);
                if (ingredient.test(IOHelper.getStack(chamberHandler, slot))) {
                    ItemStack extracted = IOHelper.extract(chamberHandler, slot, nItems, simulate);
                    nItems -= extracted.getCount();
                }
            }
        }

        return getOutputs();
    }


    public interface IFactory<T extends PressureChamberRecipeImpl> {
        T create(List<SizedIngredient> inputs, float pressureRequired, List<ItemStackTemplate> outputs);
    }

    public static <T extends PressureChamberRecipeImpl> RecipeSerializer<T> serializer(IFactory<T> factory) {
        MapCodec<T> codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                SizedIngredient.NESTED_CODEC.listOf().fieldOf("inputs").forGetter(PressureChamberRecipe::getInputs),
                Codec.floatRange(-1f, 20f).fieldOf("pressure").forGetter(PressureChamberRecipe::getPressure),
                ItemStackTemplate.CODEC.listOf().fieldOf("results").forGetter(PressureChamberRecipeImpl::getOutputTemplates)
        ).apply(builder, factory::create));
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), PressureChamberRecipe::getInputs,
                ByteBufCodecs.FLOAT, PressureChamberRecipe::getPressure,
                ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), PressureChamberRecipeImpl::getOutputTemplates,
                factory::create
        );
        return new RecipeSerializer<>(codec, streamCodec);
    }

}
