package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class ContainerRefinery extends ContainerPneumaticBase<TileEntityRefinery> {

    public ContainerRefinery(InventoryPlayer inventoryPlayer, TileEntityRefinery te) {
        super(te);

        TileEntityRefinery refinery = te;
        refinery.onNeighborTileUpdate();
        while (refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity();
            addSyncedFields(refinery);
            refinery.onNeighborTileUpdate();
        }

        addPlayerSlots(inventoryPlayer, 84);

//        if (te.getTankInfo(EnumFacing.UP)[0].fluid != null && te.getTankInfo(EnumFacing.UP)[0].fluid.getFluid() == Fluids.OIL) {
//            AchievementHandler.giveAchievement(inventoryPlayer.player, new ItemStack(Fluids.getBucket(Fluids.OIL)));
//        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;//te.isUseableByPlayer(player);
        //return te.isGuiUseableByPlayer(player);
    }


    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotIndex) {
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
