package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicPressureChamberRecipe implements IPressureChamberRecipe {
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

        for (int i = 0; i < chamberHandler.getSlots(); i++) {
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
    public List<List<ItemStack>> getInputsForDisplay() {
        List<List<ItemStack>> res = new ArrayList<>();
        inputs.forEach(ingr -> {
            NonNullList<ItemStack> l = NonNullList.create();
            l.addAll(Arrays.asList(ingr.getMatchingStacks()));
            res.add(l);
        });
        return res;
    }

    @Override
    public NonNullList<ItemStack> getResultForDisplay() {
        return outputs;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeResourceLocation(getId());
        buf.writeFloat(getCraftingPressure());
        buf.writeVarInt(inputs.size());
        inputs.forEach(i -> i.write(buf));
        buf.writeVarInt(outputs.size());
        outputs.forEach(buf::writeItemStack);
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull ItemStackHandler chamberHandler) {
        // remove the recipe's input items from the chamber
        for (Ingredient ingredient : inputs) {
//                int amountLeft = ingredient.getItemAmount();
            for (int i = 0; i < chamberHandler.getSlots(); i++) {
                ItemStack itemInChamber = chamberHandler.getStackInSlot(i);
                if (ingredient.test(itemInChamber)) {
                    chamberHandler.extractItem(i, 1, false);
                }
            }
        }

        return outputs;
    }
}
