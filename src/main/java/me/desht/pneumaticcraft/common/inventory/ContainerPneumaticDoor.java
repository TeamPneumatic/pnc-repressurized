package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPneumaticDoor extends ContainerPneumaticBase<TileEntityPneumaticDoorBase> {

    public ContainerPneumaticDoor(InventoryPlayer inventoryPlayer, TileEntityPneumaticDoorBase te) {
        super(te);

        addUpgradeSlots(23, 29);

        // Add the camo slot.
        addSlotToContainer(new SlotInventoryLimiting(te, TileEntityPneumaticDoorBase.CAMO_SLOT, 77, 36));

        addPlayerSlots(inventoryPlayer, 84);
    }
}
