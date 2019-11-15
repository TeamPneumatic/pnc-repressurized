package me.desht.pneumaticcraft.api.recipe;

import me.desht.pneumaticcraft.common.recipes.ExplosionCraftingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Random;

public interface IExplosionCraftingRecipe extends IModRecipe {

    Ingredient getInput();

    int getAmount();

    List<ItemStack> getOutputs();

    int getLossRate();

    boolean matches(ItemStack stack);

    /**
     * Get the output items for the given recipe and input item.  Note that the quantity of output items will differ
     * on each call due to the application of the randomised loss rate.
     *
     * @param recipe the recipe to check
     * @param stack the input itemstack
     * @return a list of output items
     */
    static NonNullList<ItemStack> createOutput(IExplosionCraftingRecipe recipe, ItemStack stack) {
        Random rand = new Random();
        int lossRate = recipe.getLossRate();

        NonNullList<ItemStack> res = NonNullList.create();
        if (stack.getCount() >= 3 || rand.nextDouble() >= lossRate / 100D) {
            for (ItemStack s : recipe.getOutputs()) {
                ItemStack newStack = s.copy();
                if (stack.getCount() >= 3) {
                    newStack.setCount((int) (stack.getCount() * (rand.nextDouble() * Math.min(lossRate * 0.02D, 0.2D) + (Math.max(0.9D, 1D - lossRate * 0.01D) - lossRate * 0.01D))));
                }
                if (!newStack.isEmpty()) res.add(newStack);
            }
        }
        return res;
    }

    /**
     * Create a basic explosion crafting recipe.  This uses in-world explosions to convert nearby items on the ground
     * (in item entity form) to one or more other items.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param inputAmount the quantity of input required & used
     * @param lossRate the item loss rate, as a percentage
     * @param outputs the output items
     * @return a basic Explosion Crafting recipe
     */
    static IExplosionCraftingRecipe basicRecipe(ResourceLocation id, Ingredient input, int inputAmount, int lossRate, ItemStack... outputs) {
        return new ExplosionCraftingRecipe(id, input, inputAmount, lossRate, outputs);
    }

    /**
     * Used for client-side sync'ing of recipes: do not call directly!
     * @param buf a packet buffer
     * @return a deserialised recipe
     */
    static IExplosionCraftingRecipe read(PacketBuffer buf) {
        ResourceLocation id = buf.readResourceLocation();
        Ingredient input = Ingredient.read(buf);
        int amount = buf.readVarInt();
        ItemStack out = buf.readItemStack();
        int lossRate = buf.readVarInt();
        return new ExplosionCraftingRecipe(id, input, amount, lossRate, out);
    }
}
