package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotCamouflage extends SlotItemHandler {
    private final ICamouflageableTE te;

    public SlotCamouflage(ICamouflageableTE te, int index, int xPosition, int yPosition) {
        super(te.getCamoInventory(), index, xPosition, yPosition);
        this.te = te;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof ItemBlock;
    }
}
