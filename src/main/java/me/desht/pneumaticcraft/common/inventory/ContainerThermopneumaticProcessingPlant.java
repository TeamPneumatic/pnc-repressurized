package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerThermopneumaticProcessingPlant extends
        ContainerPneumaticBase<TileEntityThermopneumaticProcessingPlant> {

    public ContainerThermopneumaticProcessingPlant(InventoryPlayer inventoryPlayer,
                                                   TileEntityThermopneumaticProcessingPlant te) {
        super(te);

        // add upgrade slots
        for (int i = 0; i < 4; i++)
            addSlotToContainer(new SlotInventoryLimiting(te.getUpgradesInventory(), i, 80 + 18 * i, 93));

        addSlotToContainer(new SlotItemHandler(te.getPrimaryInventory(), 0, 46, 14));

        addPlayerSlots(inventoryPlayer, 115);

    }

//    @Override
//    public boolean canInteractWith(EntityPlayer player) {
//        return te.isUseableByPlayer(player);
//        //return te.isGuiUseableByPlayer(player);
//    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        //  te.closeGUI();
    }
}
