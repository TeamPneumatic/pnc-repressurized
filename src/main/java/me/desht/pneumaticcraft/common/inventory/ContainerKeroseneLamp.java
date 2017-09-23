package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerKeroseneLamp extends ContainerPneumaticBase<TileEntityKeroseneLamp> {

    public ContainerKeroseneLamp(InventoryPlayer inventoryPlayer, TileEntityKeroseneLamp te) {
        super(te);

        addSlotToContainer(new SlotFullFluidContainer(te.getPrimaryInventory(), 0, 132, 22));
        addSlotToContainer(new SlotOutput(te.getPrimaryInventory(), 1, 132, 55));

        addPlayerSlots(inventoryPlayer, 84);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return te.isGuiUseableByPlayer(player);
    }
}
