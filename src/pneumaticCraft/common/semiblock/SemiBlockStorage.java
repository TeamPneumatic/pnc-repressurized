package pneumaticCraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.proxy.CommonProxy;

public class SemiBlockStorage extends SemiBlockLogistics implements ISpecificProvider, ISpecificRequester{

    public static final String ID = "logisticFrameStorage";

    @Override
    public int getColor(){
        return 0xFFFFFF00;
    }

    @Override
    public int getPriority(){
        return 1;
    }

    @Override
    public int getGuiID(){
        return CommonProxy.GUI_ID_LOGISTICS_STORAGE;
    }

    @Override
    public int amountRequested(ItemStack stack){
        return passesFilter(stack) ? stack.stackSize : 0;
    }

    @Override
    public boolean canProvide(ItemStack providingStack){
        return passesFilter(providingStack);
    }

    @Override
    public int amountRequested(FluidStack stack){
        return passesFilter(stack.getFluid()) ? stack.amount : 0;
    }

    @Override
    public boolean canProvide(FluidStack providingStack){
        return passesFilter(providingStack.getFluid());
    }

}
