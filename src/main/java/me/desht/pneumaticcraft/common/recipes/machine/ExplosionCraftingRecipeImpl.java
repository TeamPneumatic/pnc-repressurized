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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.recipe.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ExplosionCraftingRecipeImpl extends ExplosionCraftingRecipe {
    private static final NonNullList<ItemStack> EMPTY_RESULT = NonNullList.create();

    private final Ingredient input;
    private final List<ItemStack> outputs;
    private final int lossRate;

    public ExplosionCraftingRecipeImpl(Ingredient input, int lossRate, List<ItemStack> outputs) {
        this.input = input;
        this.outputs = outputs;
        this.lossRate = lossRate;
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public int getAmount() {
        return input.getItems().length > 0 ? input.getItems()[0].getCount() : 0;
    }

    @Override
    public List<ItemStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getLossRate() {
        return lossRate;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack) && stack.getCount() >= getAmount();
    }

    public static NonNullList<ItemStack> tryToCraft(Level world, ItemStack stack) {
        return ModRecipeTypes.EXPLOSION_CRAFTING.get().findFirst(world, r -> r.matches(stack)).map(holder -> {
            ExplosionCraftingRecipe recipe = holder.value();
            return recipe.getAmount() == 0 ? EMPTY_RESULT : createOutput(recipe, stack);
        }).orElse(EMPTY_RESULT);
    }

    /**
     * Get the output items for the given recipe and input item.  Note that the quantity of output items will differ
     * on each call due to the application of the randomised loss rate.
     *
     * @param recipe the recipe to check
     * @param stack the input itemstack
     * @return a list of output items
     */
    private static NonNullList<ItemStack> createOutput(ExplosionCraftingRecipe recipe, ItemStack stack) {
        Random rand = ThreadLocalRandom.current();
        int lossRate = recipe.getLossRate();

        NonNullList<ItemStack> res = NonNullList.create();
        int inputCount = Math.round((float)stack.getCount() / recipe.getAmount());
        if (inputCount >= 3 || rand.nextDouble() >= lossRate / 100D) {
            for (ItemStack s : recipe.getOutputs()) {
                ItemStack newStack = s.copy();
                if (inputCount >= 3) {
                    newStack.setCount((int) (inputCount * (rand.nextDouble() * Math.min(lossRate * 0.02D, 0.2D) + (Math.max(0.9D, 1D - lossRate * 0.01D) - lossRate * 0.01D))));
                }
                res.add(newStack);
            }
        }
        return res;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.EXPLOSION_CRAFTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.EXPLOSION_CRAFTING.get();
    }

    @Override
    public String getGroup() {
        return PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.TNT);
    }

    public static class Serializer<T extends ExplosionCraftingRecipe> implements RecipeSerializer<T> {
        private final Codec<T> codec;
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
            this.codec = RecordCodecBuilder.create(builder -> builder.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(ExplosionCraftingRecipe::getInput),
                    ExtraCodecs.intRange(0, 99).fieldOf("loss_rate").forGetter(ExplosionCraftingRecipe::getLossRate),
                    ItemStack.ITEM_WITH_COUNT_CODEC.listOf().fieldOf("results").forGetter(ExplosionCraftingRecipe::getOutputs)
            ).apply(builder, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            List<ItemStack> l = buffer.readList(FriendlyByteBuf::readItem);
            int lossRate = buffer.readVarInt();
            return factory.create(input, lossRate, l);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.getInput().toNetwork(buffer);
            buffer.writeCollection(recipe.getOutputs(), FriendlyByteBuf::writeItem);
            buffer.writeVarInt(recipe.getLossRate());
        }

        public interface IFactory<T extends ExplosionCraftingRecipe> {
            T create(Ingredient input, int lossRate, List<ItemStack> result);
        }
    }
}
