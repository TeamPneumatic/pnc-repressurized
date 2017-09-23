package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerSecurityStationHacking extends ContainerPneumaticBase<TileEntitySecurityStation> {

    public ContainerSecurityStationHacking(InventoryPlayer inventoryPlayer, TileEntitySecurityStation te) {
        super(te);

        //add the network slots
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                addSlotToContainer(new SlotUntouchable(te.getPrimaryInventory(), j + i * 5, 13 + j * 31, 18 + i * 31));
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

}
