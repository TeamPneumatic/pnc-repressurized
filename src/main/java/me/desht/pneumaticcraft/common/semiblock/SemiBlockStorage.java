package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class SemiBlockStorage extends SemiBlockLogistics implements ISpecificProvider, ISpecificRequester {

    public static final String ID = "logistics_frame_storage";

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

    @Override
    public ITextComponent getDisplayName() {
        return new ItemStack(ModItems.LOGISTICS_FRAME_STORAGE).getDisplayName();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerLogistics(ModContainerTypes.LOGISTICS_FRAME_STORAGE, i, playerInventory, getPos());
    }
}
