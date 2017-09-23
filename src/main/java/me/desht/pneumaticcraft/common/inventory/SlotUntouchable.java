package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class SlotUntouchable extends SlotBase implements IPhantomSlot {
    SlotUntouchable(IItemHandler handler, int slotIndex, int x, int y) {
        super(handler, slotIndex, x, y);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canAdjust() {
        return false;
    }

    @Override
    public boolean canShift() {
        return false;
    }
}