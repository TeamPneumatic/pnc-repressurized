package me.desht.pneumaticcraft.common.util.fakeplayer;

import me.desht.pneumaticcraft.api.drone.IDrone;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class DroneItemHandler extends ItemStackHandler {
    private final IDrone holder;
    protected boolean heldItemChanged = false;

    public DroneItemHandler(int size, IDrone holder) {
        super(size);
        this.holder = holder;
    }

    private ItemStack oldStack = ItemStack.EMPTY;

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        if (slot == 0) {
            // We can't call getFakePlayer() here since we might be still in entity initialization,
            // i.e. the chunk is still being loaded.  Initializing a player at this stage
            // can cause an endless loop (player constructor tries to find a random spawn point,
            // which can lead to more chunk creation)
            heldItemChanged = true;
        }
    }

    /**
     * This should be called regularly, e.g. from the entity or tile entity update() method.  It ensures the fake
     * player has the appropriate attributes based on the held item, and can be overridden for extra functionality.
     */
    public void updateHeldItem() {
        if (heldItemChanged) {
            ItemStack newStack = getStackInSlot(0);
            if (!oldStack.isEmpty()) {
                holder.getFakePlayer().getAttributeMap().removeAttributeModifiers(oldStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
            }
            if (!newStack.isEmpty()) {
                holder.getFakePlayer().getAttributeMap().applyAttributeModifiers(newStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
            }
            oldStack = newStack.copy();

            heldItemChanged = false;
        }
    }
}
