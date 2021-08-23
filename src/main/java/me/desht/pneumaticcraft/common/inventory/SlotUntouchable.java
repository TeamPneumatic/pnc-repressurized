package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class SlotUntouchable extends SlotItemHandler implements IPhantomSlot {
    private boolean enabled = true;

    SlotUntouchable(IItemHandler handler, int slotIndex, int x, int y) {
        super(handler, slotIndex, x, y);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean mayPickup(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean canAdjust() {
        return false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isActive() {
        return enabled;
    }
}