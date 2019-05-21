package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerGasLift extends ContainerPneumaticBase<TileEntityGasLift> {

    public ContainerGasLift(InventoryPlayer inventoryPlayer, TileEntityGasLift te) {
        super(te);

        addUpgradeSlots(11, 29);

        addSlotToContainer(new SlotItemHandler(te.getPrimaryInventory(), 0, 55, 48));

        addPlayerSlots(inventoryPlayer, 84);
    }
}
