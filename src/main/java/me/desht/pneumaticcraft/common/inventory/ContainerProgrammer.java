package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.common.network.PacketSendNBTPacket;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerProgrammer extends ContainerPneumaticBase<TileEntityProgrammer> {

    public ContainerProgrammer(InventoryPlayer inventoryPlayer, TileEntityProgrammer te) {
        super(te);

        addSlotToContainer(new SlotItemHandler(te.getPrimaryInventory(), 0, 326, 15) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return isProgrammableItem(stack);
            }
        });

        // Add the player's inventory slots to the container
        for (int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for (int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 95 + inventoryColumnIndex * 18, 174 + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 95 + actionBarSlotIndex * 18, 232));
        }
    }

    private static boolean isProgrammableItem(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof IProgrammable && ((IProgrammable) stack.getItem()).canProgram(stack);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (te.getWorld().getTotalWorldTime() % 20 == 0) {
            for (EnumFacing d : EnumFacing.VALUES) {
                TileEntity neighbor = te.getWorld().getTileEntity(te.getPos().offset(d));
                if (neighbor != null && neighbor.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite())) {
                    sendToContainerListeners(new PacketSendNBTPacket(neighbor));
                }
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot srcSlot = inventorySlots.get(slotIndex);

        if (srcSlot != null && srcSlot.getHasStack()) {
            ItemStack stackInSlot = srcSlot.getStack();
            stack = stackInSlot.copy();

            if (slotIndex == 0) {
                if (!mergeItemStack(stackInSlot, 1, 36, false)) return ItemStack.EMPTY;
                srcSlot.onSlotChange(stackInSlot, stack);
            } else if (isProgrammableItem(stack)) {
                if (!mergeItemStack(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                srcSlot.onSlotChange(stackInSlot, stack);
            }
            if (stackInSlot.isEmpty()) {
                srcSlot.putStack(ItemStack.EMPTY);
            } else {
                srcSlot.onSlotChanged();
            }

            if (stackInSlot.getCount() == stack.getCount()) return ItemStack.EMPTY;

            srcSlot.onTake(par1EntityPlayer, stackInSlot);
        }

        return stack;
    }
}
