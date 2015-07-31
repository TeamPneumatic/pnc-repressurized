package pneumaticCraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class SemiBlockActiveProvider extends SemiBlockLogistics implements ISpecificProvider{
    public static String ID = "logisticFrameActiveProvider";

    @Override
    public int getColor(){
        return 0xFF93228c;
    }

    @Override
    public int getPriority(){
        return 0;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.LOGISTICS_PASSIVE_PROVIDER;
    }

    @Override
    public boolean canProvide(ItemStack providingStack){
        return passesFilter(providingStack);
    }

    @Override
    public boolean canProvide(FluidStack providingStack){
        return passesFilter(providingStack.getFluid());
    }

}
