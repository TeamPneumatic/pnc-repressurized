package pneumaticCraft.common.thirdparty.ic2;

import ic2.api.recipe.IRecipeInput;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

public class IC2RecipeInput implements IRecipeInput{
    private final ItemStack input;

    public IC2RecipeInput(ItemStack input){
        this.input = input;
    }

    @Override
    public boolean matches(ItemStack subject){
        return subject != null && input.isItemEqual(subject);
    }

    @Override
    public int getAmount(){
        return input.stackSize;
    }

    @Override
    public List<ItemStack> getInputs(){
        return Arrays.asList(new ItemStack[]{input});
    }

}
