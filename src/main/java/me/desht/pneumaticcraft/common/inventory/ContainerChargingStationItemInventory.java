package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.EntityArmorInvWrapper;

public class ContainerChargingStationItemInventory extends ContainerPneumaticBase<TileEntityChargingStation> {

    public ChargeableItemHandler chargeableItemHandler;

    public ContainerChargingStationItemInventory(InventoryPlayer inventoryPlayer, TileEntityChargingStation te) {
        super(te);
        if (te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).isEmpty())
            throw new IllegalArgumentException("instanciating ContainerPneumaticArmor with a charge item being empty!");
        chargeableItemHandler = te.getChargeableInventory();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlotToContainer(new SlotInventoryLimiting(chargeableItemHandler, i * 3 + j, 31 + j * 18, 24 + i * 18));
            }
        }

        addPlayerSlots(inventoryPlayer, 84);

        // Add the player's armor slots to the container.
        EntityArmorInvWrapper armorInvWrapper = new EntityArmorInvWrapper(inventoryPlayer.player);
        for (int i = 0; i < 4; i++) {
            // order is feet, legs, chest, head; so add slots from bottom up
            addSlotToContainer(new SlotItemHandler(armorInvWrapper, i, 9,  62 - i * 18));
        }

        addSlotToContainer(new SlotUntouchable(te.getPrimaryInventory(), 0, -50000, -50000)); //Allows the charging stack to sync.

    }
}
