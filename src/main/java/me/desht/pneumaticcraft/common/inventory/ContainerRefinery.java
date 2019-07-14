package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class ContainerRefinery extends ContainerPneumaticBase<TileEntityRefinery> {

    public ContainerRefinery(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerRefinery(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.REFINERY, i, playerInventory, pos);

        TileEntityRefinery refinery = te;
        refinery.onNeighborTileUpdate();
        while (refinery.getTileCache()[Direction.UP.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getTileCache()[Direction.UP.ordinal()].getTileEntity();
            addSyncedFields(refinery);
            refinery.onNeighborTileUpdate();
        }

        addPlayerSlots(playerInventory, 84);
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return te.isGuiUseableByPlayer(player);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int slotIndex) {
        // Refinery itself has no item slots, but this allows shift-clicking items between player's inventory & hotbar
        ItemStack stack = ItemStack.EMPTY;
        Slot srcSlot = inventorySlots.get(slotIndex);

        if (srcSlot != null && srcSlot.getHasStack()) {
            ItemStack stackInSlot = srcSlot.getStack();
            stack = stackInSlot.copy();

            if (slotIndex < 27) {
                if (!mergeItemStack(stackInSlot, 27, 36, false)) return ItemStack.EMPTY;
                srcSlot.onSlotChange(stackInSlot, stack);
            } else {
                if (!mergeItemStack(stackInSlot, 0, 27, false)) return ItemStack.EMPTY;
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
