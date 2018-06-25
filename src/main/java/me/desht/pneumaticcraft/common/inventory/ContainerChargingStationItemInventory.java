package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerChargingStationItemInventory extends ContainerPneumaticBase<TileEntityChargingStation> {

    public ChargeableItemHandler chargeableItemHandler;

    public ContainerChargingStationItemInventory(InventoryPlayer inventoryPlayer, TileEntityChargingStation te) {
        super(te);
        if (te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).isEmpty())
            throw new IllegalArgumentException("instantiating ContainerChargingStationItemInventory with no chargeable item installed!");
        chargeableItemHandler = te.getChargeableInventory();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlotToContainer(new SlotInventoryLimiting(chargeableItemHandler, i * 3 + j, 31 + j * 18, 24 + i * 18){
                    @Override
                    public void onSlotChanged() {
                        // needed to force client sync in some circumstances
                        te.getChargeableInventory().saveInventory();
                    }
                });
            }
        }

        addPlayerSlots(inventoryPlayer, 84);

        addArmorSlots(inventoryPlayer, 9, 8);

        addSlotToContainer(new SlotUntouchable(te.getPrimaryInventory(), 0, -50000, -50000)); //Allows the charging stack to sync.

    }
}
