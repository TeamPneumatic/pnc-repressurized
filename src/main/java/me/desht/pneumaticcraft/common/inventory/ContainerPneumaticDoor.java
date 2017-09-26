package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerPneumaticDoor extends ContainerPneumaticBase<TileEntityPneumaticDoorBase> {

    public ContainerPneumaticDoor(InventoryPlayer inventoryPlayer, TileEntityPneumaticDoorBase te) {
        super(te);

        addUpgradeSlots(23, 29);

        // Add the camo slot.
        addSlotToContainer(new SlotInventoryLimiting(te, TileEntityPneumaticDoorBase.CAMO_SLOT, 77, 36) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });

        addPlayerSlots(inventoryPlayer, 84);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        // work around a problem where shift-clicking out of the camo slot
        // doesn't call onSlotChanged() in the item handler
        ItemStack stack = super.transferStackInSlot(player, slot);
        if (!stack.isEmpty() && getSlot(slot) instanceof SlotInventoryLimiting) {
            te.cacheCamo();
            te.getWorld().markBlockRangeForRenderUpdate(te.getPos(), te.getPos());
            te.markDirty();
        }
        return stack;
    }
}
