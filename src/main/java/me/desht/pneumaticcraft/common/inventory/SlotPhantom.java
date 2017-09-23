package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class SlotPhantom extends SlotBase implements IPhantomSlot {

    // used for filters
    SlotPhantom(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean canShift() {
        return true;
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
        return false;
    }

    @Override
    public boolean canAdjust() {
        return true;
    }

}
