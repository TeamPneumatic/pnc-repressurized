package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ExplosionCraftingRecipeImpl extends ExplosionCraftingRecipe {
    private static final NonNullList<ItemStack> EMPTY_RESULT = NonNullList.create();

    private final Ingredient input;
    private final List<ItemStack> outputs;
    private final int lossRate;

    public ExplosionCraftingRecipeImpl(ResourceLocation id, Ingredient input, int lossRate, ItemStack... outputs) {
        super(id);

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

    public static NonNullList<ItemStack> tryToCraft(World world, ItemStack stack) {
        ExplosionCraftingRecipe recipe = PneumaticCraftRecipeType.EXPLOSION_CRAFTING.findFirst(world, r -> r.matches(stack));
        return recipe == null ? EMPTY_RESULT : createOutput(recipe, stack);
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

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeVarInt(outputs.size());
        outputs.forEach(buffer::writeItemStack);
        buffer.writeVarInt(lossRate);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.EXPLOSION_CRAFTING.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return PneumaticCraftRecipeType.EXPLOSION_CRAFTING;
    }

    @Override
    public String getGroup() {
        return PneumaticCraftRecipeType.EXPLOSION_CRAFTING.toString();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.TNT);
    }

    public static class Serializer<T extends ExplosionCraftingRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.deserialize(json.get("input"));
            int loss_rate = JSONUtils.getInt(json,"loss_rate", 0);
            JsonArray outputs = json.get("results").getAsJsonArray();
            NonNullList<ItemStack> results = NonNullList.create();
            for (JsonElement e : outputs) {
                results.add(ShapedRecipe.deserializeItem(e.getAsJsonObject()));
            }
            return factory.create(recipeId, input, loss_rate, results.toArray(new ItemStack[0]));
        }

        @Nullable
        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient input = Ingredient.read(buffer);
            int nOutputs = buffer.readVarInt();
            List<ItemStack> l = new ArrayList<>();
            for (int i = 0; i < nOutputs; i++) {
                l.add(buffer.readItemStack());
            }
            int lossRate = buffer.readVarInt();
            return factory.create(recipeId, input, lossRate, l.toArray(new ItemStack[0]));
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends ExplosionCraftingRecipe> {
            T create(ResourceLocation id, Ingredient input, int lossRate, ItemStack... result);
        }
    }
}
