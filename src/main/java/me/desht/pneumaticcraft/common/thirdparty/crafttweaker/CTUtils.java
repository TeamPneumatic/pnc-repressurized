package me.desht.pneumaticcraft.common.thirdparty.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CTUtils {
    public static Ingredient toStackedIngredient(IIngredientWithAmount ingredient) {
        return StackedIngredient.fromIngredient(ingredient.getAmount(), ingredient.getIngredient().asVanillaIngredient());
    }

    public static List<Ingredient> toStackedIngredientList(IIngredientWithAmount[] ingredients) {
        return Arrays.stream(ingredients).map(CTUtils::toStackedIngredient).collect(Collectors.toList());
    }

    public static ItemStack[] toItemStacks(IItemStack[] stacks) {
        return Arrays.stream(stacks).map(IItemStack::getInternal).toArray(ItemStack[]::new);
    }

    public static FluidStack[] toFluidStacks(IFluidStack[] stacks) {
        return Arrays.stream(stacks).map(IFluidStack::getImmutableInternal).toArray(FluidStack[]::new);
    }

    public static FluidIngredient toFluidIngredient(CTFluidIngredient ingredient) {
        return ingredient.mapTo(FluidIngredient::of, (tag, amount) -> FluidIngredient.of(amount, tag), FluidIngredient::of);
    }
}
