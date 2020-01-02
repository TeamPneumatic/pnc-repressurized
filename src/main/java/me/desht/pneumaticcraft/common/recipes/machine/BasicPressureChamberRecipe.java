package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class BasicPressureChamberRecipe implements IPressureChamberRecipe {
    public static final ResourceLocation RECIPE_TYPE = RL("basic_pressure_chamber");

    private final ResourceLocation id;
    private final float pressureRequired;
    private final List<Ingredient> inputs;
    private final NonNullList<ItemStack> outputs;

    public BasicPressureChamberRecipe(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs) {
        this.id = id;
        this.inputs = ImmutableList.copyOf(inputs);
        this.outputs = NonNullList.from(ItemStack.EMPTY, outputs);
        this.pressureRequired = pressureRequired;
    }

    @Override
    public float getCraftingPressure() {
        return pressureRequired;
    }

    @Override
    public boolean isValidRecipe(@Nonnull ItemStackHandler chamberHandler) {
        List<Ingredient> missing = new ArrayList<>(inputs);

        for (int i = 0; i < chamberHandler.getSlots() && !missing.isEmpty(); i++) {
            ItemStack input = chamberHandler.getStackInSlot(i);

            int stackIndex = -1;
            for(int j = 0; j < missing.size(); j++) {
                Ingredient ingr = missing.get(j);
                if (ingr.test(input)) {
                    stackIndex = j;
                    break;
                }
            }
            if (stackIndex != -1)
                missing.remove(stackIndex);
            else return false;
        }
        return missing.isEmpty();
    }

    @Override
    public List<Ingredient> getInputsForDisplay() {
        return new ArrayList<>(inputs);
    }

    @Override
    public NonNullList<ItemStack> getResultForDisplay() {
        return outputs;
    }

    @Override
    public boolean isOutputItem(ItemStack stack) {
        for (ItemStack out: outputs) {
            if (ItemStack.areItemsEqual(out, stack)) return true;
        }
        return false;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ResourceLocation getRecipeType() {
        return RECIPE_TYPE;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull ItemStackHandler chamberHandler) {
        // remove the recipe's input items from the chamber
        for (Ingredient ingredient : inputs) {
            int nItems = ingredient.hasNoMatchingItems() ? 0 : ingredient.getMatchingStacks()[0].getCount();
            for (int i = 0; i < chamberHandler.getSlots() && nItems > 0; i++) {
                ItemStack itemInChamber = chamberHandler.getStackInSlot(i);
                if (ingredient.test(itemInChamber)) {
                    ItemStack extracted = chamberHandler.extractItem(i, nItems, false);
                    nItems -= extracted.getCount();
                }
            }
        }

        return outputs;
    }

    public static class Serializer extends AbstractRecipeSerializer<BasicPressureChamberRecipe> {
        @Override
        public BasicPressureChamberRecipe read(ResourceLocation recipeId, JsonObject json) {
            // TODO when we add support for data pack machine recipes
            return null;
        }

        @Nullable
        @Override
        public BasicPressureChamberRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            float pressure = buffer.readFloat();
            int nInputs = buffer.readVarInt();
            List<Ingredient> in = new ArrayList<>();
            for (int i = 0; i < nInputs; i++) {
                in.add(Ingredient.read(buffer));
            }
            int nOutputs = buffer.readVarInt();
            ItemStack[] out = new ItemStack[nOutputs];
            for (int i = 0; i < nOutputs; i++) {
                out[i] = buffer.readItemStack();
            }
            return new BasicPressureChamberRecipe(recipeId, in, pressure, out);
        }

        @Override
        public void write(PacketBuffer buffer, BasicPressureChamberRecipe recipe) {
            super.write(buffer, recipe);

            buffer.writeFloat(recipe.getCraftingPressure());
            buffer.writeVarInt(recipe.inputs.size());
            recipe.inputs.forEach(i -> i.write(buffer));
            buffer.writeVarInt(recipe.outputs.size());
            recipe.outputs.forEach(buffer::writeItemStack);
        }
    }
}
