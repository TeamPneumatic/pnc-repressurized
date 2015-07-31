package pneumaticCraft.common.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class AmadronOffer{
    private final Object input;
    private final Object output;

    public AmadronOffer(Object input, Object output){
        if(input == null) throw new NullPointerException("Input item can't be null!");
        if(output == null) throw new NullPointerException("Output item can't be null!");
        if(!(input instanceof ItemStack) && !(input instanceof FluidStack)) throw new IllegalArgumentException("Input must be of type ItemStack or FluidStack. Input: " + input);
        if(!(output instanceof ItemStack) && !(output instanceof FluidStack)) throw new IllegalArgumentException("Output must be of type ItemStack or FluidStack. Output: " + input);
        this.input = input;
        this.output = output;
    }

    public Object getInput(){
        return input;
    }

    public Object getOutput(){
        return output;
    }
}
