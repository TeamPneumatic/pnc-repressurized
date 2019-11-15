package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IHeatFrameCoolingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class HeatFrameCoolingRecipe implements IHeatFrameCoolingRecipe {
	public static final List<HeatFrameCoolingRecipe> recipes = new ArrayList<>();

    private final ResourceLocation id;
    public final Ingredient input;
    private final int inputAmount;
    public final ItemStack output;

    public HeatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int inputAmount, ItemStack output) {
        this.id = id;
        this.input = input;
        this.inputAmount = inputAmount;
        this.output = output;
    }

    public HeatFrameCoolingRecipe(ResourceLocation id, Ingredient input, ItemStack output) {
        this(id, input, 1, output);
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    public int getInputAmount() {
        return inputAmount;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeResourceLocation(id);
        input.write(buf);
        buf.writeVarInt(inputAmount);
        buf.writeItemStack(output);
    }
}
