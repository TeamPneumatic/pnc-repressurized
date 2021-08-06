package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

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
        this.outputs = NonNullList.from(ItemStack.EMPTY, outputs);
        this.pressureRequired = pressureRequired;
    }

    @Override
    public float getCraftingPressure(IItemHandler chamberHandler, List<Integer> ingredientSlots) {
        return pressureRequired;
    }

    @Override
    public float getCraftingPressureForDisplay() {
        return pressureRequired;
    }

    @Override
    public Collection<Integer> findIngredients(IItemHandler chamberHandler) {
        // Ingredient doesn't override equals() and hashCode() but there's always the possibility
        // that some subclass might, so we'll use an identity set here.  We want to always treat
        // two equivalent ingredients in a recipe as different objects.
        Set<Ingredient> inputSet = Sets.newIdentityHashSet();
        inputSet.addAll(inputs);

        List<Integer> slots = new ArrayList<>();
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
        return Collections.emptyList();
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
    public IRecipeType<?> getType() {
        return PneumaticCraftRecipeType.PRESSURE_CHAMBER;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESSURE_CHAMBER.get();
    }

    @Override
    public String getGroup() {
        return ModBlocks.PRESSURE_CHAMBER_WALL.get().getRegistryName().getPath();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get());
    }

    @Override
    public boolean isValidInputItem(ItemStack stack) {
        ItemStack s2 = ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize());
        return inputs.stream().anyMatch(ingr -> ingr.test(s2));
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, List<Integer> ingredientSlots, boolean simulate) {
        // remove the recipe's input items from the chamber
        for (Ingredient ingredient : inputs) {
            if (ingredient.hasNoMatchingItems()) return NonNullList.create(); // sanity check
            int nItems = ingredient.getMatchingStacks()[0].getCount();
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
    public void write(PacketBuffer buffer) {
        buffer.writeFloat(getCraftingPressureForDisplay());
        buffer.writeVarInt(inputs.size());
        inputs.forEach(i -> i.write(buffer));
        buffer.writeVarInt(outputs.size());
        outputs.forEach(buffer::writeItemStack);
    }

    public static class Serializer<T extends PressureChamberRecipe>
            extends ForgeRegistryEntry<IRecipeSerializer<?>>
            implements IRecipeSerializer<T> {

        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json) {
            JsonArray inputs = json.get("inputs").getAsJsonArray();
            List<Ingredient> inputIngredients = new ArrayList<>();
            for (JsonElement e : inputs) {
                inputIngredients.add(Ingredient.deserialize(e.getAsJsonObject()));
            }
            float pressure = JSONUtils.getFloat(json, "pressure");
            JsonArray outputs = json.get("results").getAsJsonArray();
            NonNullList<ItemStack> results = NonNullList.create();
            for (JsonElement e : outputs) {
                results.add(ShapedRecipe.deserializeItem(e.getAsJsonObject()));
            }
            return factory.create(recipeId, inputIngredients, pressure, results.toArray(new ItemStack[0]));
        }

        @Nullable
        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
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
            return factory.create(recipeId, in, pressure, out);
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends PressureChamberRecipe> {
            T create(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs);
        }
    }
}
