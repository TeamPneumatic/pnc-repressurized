package pneumaticCraft.common.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

public class AmadronOffer{
    private final Object input;
    private final Object output;

    public AmadronOffer(Object input, Object output){
        if(input == null) throw new NullPointerException("Input item can't be null!");
        if(output == null) throw new NullPointerException("Output item can't be null!");
        if(input instanceof ItemStack) {
            if(((ItemStack)input).stackSize <= 0) throw new IllegalArgumentException("Input item needs to have a stacksize of > 0!");
        } else if(input instanceof FluidStack) {
            if(((FluidStack)input).amount <= 0) throw new IllegalArgumentException("Input fluid needs to have an amount of > 0!");
        } else {
            throw new IllegalArgumentException("Input must be of type ItemStack or FluidStack. Input: " + input);
        }
        if(output instanceof ItemStack) {
            if(((ItemStack)output).stackSize <= 0) throw new IllegalArgumentException("Output item needs to have a stacksize of > 0!");
        } else if(output instanceof FluidStack) {
            if(((FluidStack)output).amount <= 0) throw new IllegalArgumentException("Output fluid needs to have an amount of > 0!");
        } else {
            throw new IllegalArgumentException("Output must be of type ItemStack or FluidStack. Output: " + input);
        }
        this.input = input;
        this.output = output;
    }

    public Object getInput(){
        return input;
    }

    public Object getOutput(){
        return output;
    }

    public String getVendor(){
        return StatCollector.translateToLocal("gui.amadron");//Hardcoded for now until inter-player trading is implemented.
    }

    public boolean passesQuery(String query){
        String queryLow = query.toLowerCase();
        return getObjectName(getInput()).toLowerCase().contains(queryLow) || getObjectName(getOutput()).toLowerCase().contains(queryLow) || getVendor().toLowerCase().contains(queryLow);
    }

    private String getObjectName(Object object){
        return object instanceof ItemStack ? ((ItemStack)object).getDisplayName() : ((FluidStack)object).getLocalizedName();
    }

    public void writeToNBT(NBTTagCompound tag){
        NBTTagCompound subTag = new NBTTagCompound();
        if(input instanceof ItemStack) {
            ((ItemStack)input).writeToNBT(subTag);
            tag.setTag("inputItem", subTag);
        } else {
            ((FluidStack)input).writeToNBT(subTag);
            tag.setTag("inputFluid", subTag);
        }
        subTag = new NBTTagCompound();
        if(output instanceof ItemStack) {
            ((ItemStack)output).writeToNBT(subTag);
            tag.setTag("outputItem", subTag);
        } else {
            ((FluidStack)output).writeToNBT(subTag);
            tag.setTag("outputFluid", subTag);
        }
    }

    public static AmadronOffer loadFromNBT(NBTTagCompound tag){
        Object input;
        if(tag.hasKey("inputItem")) {
            input = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("inputItem"));
        } else {
            input = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("inputFluid"));
        }
        Object output;
        if(tag.hasKey("outputItem")) {
            output = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("outputItem"));
        } else {
            output = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("outputFluid"));
        }
        return new AmadronOffer(input, output);
    }

    @Override
    public int hashCode(){
        int code = getInput().hashCode();
        code = 31 * code + getOutput().hashCode();
        code = 31 * code + getVendor().hashCode();
        return code;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof AmadronOffer) {
            AmadronOffer offer = (AmadronOffer)o;
            if(offer.getInput().getClass() == getInput().getClass() && offer.getOutput().getClass() == getOutput().getClass()) {
                if(offer.getInput() instanceof ItemStack) {
                    ItemStack s1 = (ItemStack)offer.getInput();
                    ItemStack s2 = (ItemStack)getInput();
                    if(!ItemStack.areItemStacksEqual(s1, s2)) return false;
                } else {
                    FluidStack s1 = (FluidStack)offer.getInput();
                    FluidStack s2 = (FluidStack)getInput();
                    if(!s1.isFluidEqual(s2) || s1.amount != s2.amount) return false;
                }
                if(offer.getOutput() instanceof ItemStack) {
                    ItemStack s1 = (ItemStack)offer.getOutput();
                    ItemStack s2 = (ItemStack)getOutput();
                    if(!ItemStack.areItemStacksEqual(s1, s2)) return false;
                } else {
                    FluidStack s1 = (FluidStack)offer.getOutput();
                    FluidStack s2 = (FluidStack)getOutput();
                    if(!s1.isFluidEqual(s2) || s1.amount != s2.amount) return false;
                }
                return getVendor().equals(offer.getVendor());
            }
        }
        return false;
    }
}
