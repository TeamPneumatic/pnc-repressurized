package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidMixerRecipe extends PneumaticCraftRecipe {
    protected FluidMixerRecipe(ResourceLocation id) {
        super(id);
    }

    public abstract boolean matches(FluidStack fluid1, FluidStack fluid2);

    public abstract FluidIngredient getInput1();

    public abstract FluidIngredient getInput2();

    public abstract FluidStack getOutputFluid();

    public abstract ItemStack getOutputItem();

    public abstract int getProcessingTime();

    public abstract float getRequiredPressure();
}
