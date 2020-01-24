package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import me.desht.pneumaticcraft.common.recipes.MachineRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ExplosionCraftingRecipe implements IExplosionCraftingRecipe {
    private static final NonNullList<ItemStack> EMPTY_RESULT = NonNullList.create();

    private final ResourceLocation id;
    private final Ingredient input;
    private final List<ItemStack> outputs;
    private final int lossRate;

    public ExplosionCraftingRecipe(ResourceLocation id, Ingredient input/*, int amount*/, int lossRate, ItemStack... outputs) {
        this.id = id;
        this.input = input;
        this.outputs = Arrays.asList(outputs);
        this.lossRate = lossRate;
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public int getAmount() {
        return input.getMatchingStacks().length > 0 ? input.getMatchingStacks()[0].getCount() : 0;
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

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ResourceLocation getRecipeType() {
        return MachineRecipeHandler.Category.EXPLOSION_CRAFTING.getId();
    }

    public static NonNullList<ItemStack> tryToCraft(ItemStack stack) {
        for (IExplosionCraftingRecipe recipe : PneumaticCraftRecipes.explosionCraftingRecipes.values()) {
            if (recipe.matches(stack)) {
                return createOutput(recipe, stack);
            }
        }
        return EMPTY_RESULT;
    }

    /**
     * Get the output items for the given recipe and input item.  Note that the quantity of output items will differ
     * on each call due to the application of the randomised loss rate.
     *
     * @param recipe the recipe to check
     * @param stack the input itemstack
     * @return a list of output items
     */
    private static NonNullList<ItemStack> createOutput(IExplosionCraftingRecipe recipe, ItemStack stack) {
        Random rand = new Random();
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

    public static class Serializer extends AbstractRecipeSerializer<ExplosionCraftingRecipe> {
        @Override
        public ExplosionCraftingRecipe read(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.deserialize(json.get("input"));
            ItemStack result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            int loss_rate = JSONUtils.getInt(json,"loss_rate", 0);
            return new ExplosionCraftingRecipe(recipeId, input, loss_rate, result);
        }

        @Nullable
        @Override
        public ExplosionCraftingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient input = Ingredient.read(buffer);
            int nOutputs = buffer.readVarInt();
            List<ItemStack> l = new ArrayList<>();
            for (int i = 0; i < nOutputs; i++) {
                l.add(buffer.readItemStack());
            }
            int lossRate = buffer.readVarInt();
            return new ExplosionCraftingRecipe(recipeId, input, lossRate, l.toArray(new ItemStack[0]));
        }

        @Override
        public void write(PacketBuffer buffer, ExplosionCraftingRecipe recipe) {
            super.write(buffer, recipe);

            recipe.input.write(buffer);
            buffer.writeVarInt(recipe.outputs.size());
            recipe.outputs.forEach(buffer::writeItemStack);
            buffer.writeVarInt(recipe.lossRate);
        }
    }
}
