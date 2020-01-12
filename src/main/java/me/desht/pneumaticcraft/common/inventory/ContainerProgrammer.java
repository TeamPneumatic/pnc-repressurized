package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.network.PacketSendNBTPacket;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerProgrammer extends ContainerPneumaticBase<TileEntityProgrammer> {

    private final boolean hiRes;

    public ContainerProgrammer(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.PROGRAMMER.get(), i, playerInventory, pos);

        // server side doesn't care about slot positioning, so doesn't care about screen res either
        this.hiRes = playerInventory.player.world.isRemote && ClientUtils.isScreenHiRes();
        int xBase = hiRes ? 270 : 95;
        int yBase = hiRes ? 430 : 174;

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, hiRes ? 676 : 326, 15) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return isProgrammableItem(stack);
            }
        });

        // Add the player's inventory slots to the container
        for (int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for (int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlot(new Slot(playerInventory, inventoryColumnIndex + inventoryRowIndex * 9 + 9, xBase + inventoryColumnIndex * 18, yBase + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlot(new Slot(playerInventory, actionBarSlotIndex, xBase + actionBarSlotIndex * 18, yBase + 58));
        }
    }

    public ContainerProgrammer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public boolean isHiRes() {
        return hiRes;
    }

    private static boolean isProgrammableItem(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof IProgrammable && ((IProgrammable) stack.getItem()).canProgram(stack);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        // update the client about contents of adjacent inventories so the programmer GUI knows what
        // puzzle pieces are available
        if (te.getWorld().getGameTime() % 20 == 0) {
            for (Direction d : Direction.VALUES) {
                TileEntity neighbor = te.getTileCache()[d.getIndex()].getTileEntity();
                if (neighbor != null && neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite()).isPresent()) {
                    sendToContainerListeners(new PacketSendNBTPacket(neighbor));
                }
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int slotIndex) {
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

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);

        if (playerIn.world.isRemote) GuiProgrammer.onCloseFromContainer();
    }
}
