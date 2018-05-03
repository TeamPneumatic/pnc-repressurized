package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerChargingStation extends ContainerPneumaticBase<TileEntityChargingStation> {

    public ContainerChargingStation(InventoryPlayer inventoryPlayer, TileEntityChargingStation te) {
        super(te);

        // add the cannoned slot.
        addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), 0, 91, 39) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });

        addUpgradeSlots(42, 29);

        addArmorSlots(inventoryPlayer, 9, 8);

        addPlayerSlots(inventoryPlayer, 84);
    }
}
