package pneumaticCraft.common.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

public class SlotFullFluidContainer extends Slot{

    public SlotFullFluidContainer(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_){
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
    }

    @Override
    public boolean isItemValid(ItemStack stack){
        return stack != null && (FluidContainerRegistry.getFluidForFilledItem(stack) != null || stack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem)stack.getItem()).getFluid(stack) != null);
    }
}
