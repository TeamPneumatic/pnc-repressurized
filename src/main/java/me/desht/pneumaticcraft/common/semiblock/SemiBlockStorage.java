package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class SemiBlockStorage extends SemiBlockLogistics implements ISpecificProvider, ISpecificRequester {

    public static final String ID = "logistic_frame_storage";

    @DescSynced
    @GuiSynced
    private int minItemOrderSize;
    @DescSynced
    @GuiSynced
    private int minFluidOrderSize;

    @Override
    public int getColor() {
        return 0xFFFFFF00;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.LOGISTICS_STORAGE;
    }

    @Override
    public int amountRequested(ItemStack stack) {
        return passesFilter(stack) ? stack.getMaxStackSize() : 0;
    }

    @Override
    public boolean canProvide(ItemStack providingStack) {
        return passesFilter(providingStack);
    }

    @Override
    public int amountRequested(FluidStack stack) {
        return passesFilter(stack.getFluid()) ? stack.amount : 0;
    }

    @Override
    public boolean canProvide(FluidStack providingStack) {
        return passesFilter(providingStack.getFluid());
    }
}
