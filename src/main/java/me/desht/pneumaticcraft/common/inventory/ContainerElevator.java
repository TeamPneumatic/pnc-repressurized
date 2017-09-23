package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerElevator extends ContainerPneumaticBase<TileEntityElevatorBase> {

    public ContainerElevator(InventoryPlayer inventoryPlayer, TileEntityElevatorBase te) {
        super(te);

        addUpgradeSlots(23, 29);

        // Add the camo slots.
        addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), 0, 77, 36));
        addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), 1, 77, 55));

        addPlayerSlots(inventoryPlayer, 84);

    }

}
