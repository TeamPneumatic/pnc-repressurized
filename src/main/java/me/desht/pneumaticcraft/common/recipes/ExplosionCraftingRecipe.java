package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IExplosionCraftingRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ExplosionCraftingRecipe implements IExplosionCraftingRecipe {
    private static final NonNullList<ItemStack> EMPTY_RESULT = NonNullList.create();

//    public static final List<ExplosionCraftingRecipe> recipes = new ArrayList<>();

    private final ResourceLocation id;
    private final Ingredient input;
    private final int amount;
    private final List<ItemStack> outputs;
    private final int lossRate;

    public ExplosionCraftingRecipe(ResourceLocation id, Ingredient input, int amount, int lossRate, ItemStack... outputs) {
        this.id = id;
        this.input = input;
        this.amount = amount;
        this.outputs = Arrays.asList(outputs);
        this.lossRate = lossRate;
    }

    public ExplosionCraftingRecipe(ResourceLocation id, Ingredient input, ItemStack output, int lossRate) {
        this(id, input, 1, lossRate, output);
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public int getAmount() {
        return amount;
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
        return input.test(stack) && amount <= stack.getCount();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeResourceLocation(id);
        input.write(buf);
        buf.writeVarInt(amount);
        buf.writeVarInt(outputs.size());
        outputs.forEach(buf::writeItemStack);
        buf.writeVarInt(lossRate);
    }

    public static NonNullList<ItemStack> tryToCraft(ItemStack stack) {
        for (IExplosionCraftingRecipe recipe : PneumaticCraftRecipes.explosionCraftingRecipes.values()) {
            if (recipe.matches(stack)) {
                return createOutput(recipe, stack);
            }
        }
        return EMPTY_RESULT;
    }

    private static NonNullList<ItemStack> createOutput(IExplosionCraftingRecipe recipe, ItemStack stack) {
        Random rand = new Random();
        int lossRate = recipe.getLossRate();

        NonNullList<ItemStack> res = NonNullList.create();
        if (stack.getCount() >= 3 || rand.nextDouble() >= lossRate / 100D) {
            for (ItemStack s : recipe.getOutputs()) {
                ItemStack newStack = s.copy();
                if (stack.getCount() >= 3) {
                    newStack.setCount((int) (stack.getCount() * (rand.nextDouble() * Math.min(lossRate * 0.02D, 0.2D) + (Math.max(0.9D, 1D - lossRate * 0.01D) - lossRate * 0.01D))));
                }
                res.add(newStack);
            }
        }
        return res;
    }
}
