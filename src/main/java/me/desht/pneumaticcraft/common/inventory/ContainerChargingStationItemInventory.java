package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerChargingStationItemInventory extends ContainerPneumaticBase<TileEntityChargingStation> {

    public ContainerChargingStationItemInventory(InventoryPlayer inventoryPlayer, TileEntityChargingStation te) {
        super(te);

        if (te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).isEmpty())
            throw new IllegalArgumentException("instantiating ContainerChargingStationItemInventory with no chargeable item installed!");

//        ChargeableItemHandler chargeableItemHandler = te.getChargeableInventory();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlotToContainer(new UpgradeSlot(te, i * 3 + j, 31 + j * 18, 24 + i * 18));
            }
        }

        addPlayerSlots(inventoryPlayer, 84);
        addArmorSlots(inventoryPlayer, 9, 8);
    }

    private static class UpgradeSlot extends SlotInventoryLimiting {
        UpgradeSlot(TileEntityChargingStation te, int slotIndex, int posX, int posY) {
            super(te.getChargeableInventory(), slotIndex, posX, posY);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            ((ChargeableItemHandler) getItemHandler()).writeToNBT();
        }
    }
}
