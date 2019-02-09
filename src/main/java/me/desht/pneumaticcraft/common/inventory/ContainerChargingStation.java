package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerChargingStation extends ContainerPneumaticBase<TileEntityChargingStation> {

    public ContainerChargingStation(InventoryPlayer inventoryPlayer, TileEntityChargingStation te) {
        super(te);

        // add the cannoned slot.
        addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), 0, 91, 39) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });

        addUpgradeSlots(42, 29);

        addArmorSlots(inventoryPlayer, 9, 8);

        addPlayerSlots(inventoryPlayer, 94);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        Slot srcSlot = inventorySlots.get(slot);
        if (srcSlot == null || !srcSlot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack srcStack = srcSlot.getStack().copy();
        ItemStack copyOfSrcStack = srcStack.copy();

        if (slot == 0 && srcStack.getItem() instanceof ItemArmor) {
            // chargeable slot - move to armor if appropriate, player inv otherwise
            if (!mergeItemStack(srcStack, 5, 9, false)
                    && !mergeItemStack(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else if (slot >= 5 && slot < 9 && srcStack.getItem() instanceof IPressurizable) {
            // armor slots - try to move to the charging slot if possible
            if (!mergeItemStack(srcStack, 0, 1, false)
                    && !mergeItemStack(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else if (slot < playerSlotsStart) {
            if (!mergeItemStack(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else {
            if (!mergeItemStack(srcStack, 0, playerSlotsStart, false))
                return ItemStack.EMPTY;
        }

        srcSlot.putStack(srcStack);
        srcSlot.onSlotChange(srcStack, copyOfSrcStack);
        srcSlot.onTake(player, srcStack);

        return copyOfSrcStack;
    }
}
