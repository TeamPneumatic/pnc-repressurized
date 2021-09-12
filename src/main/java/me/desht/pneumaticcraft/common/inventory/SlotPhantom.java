package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class SlotPhantom extends SlotItemHandler implements IPhantomSlot {

    // used for filters
    SlotPhantom(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPickup(PlayerEntity par1EntityPlayer) {
        return false;
    }

    @Override
    public boolean canAdjust() {
        return true;
    }

}
