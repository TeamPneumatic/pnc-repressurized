package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.EntityArmorInvWrapper;

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

        // Add the player's armor slots to the container.
        EntityArmorInvWrapper armorInvWrapper = new EntityArmorInvWrapper(inventoryPlayer.player);
        for (int i = 0; i < 4; i++) {
            addSlotToContainer(new SlotItemHandler(armorInvWrapper, i, 9, 62 - i * 18));
//            addSlotToContainer(new SlotPneumaticArmor(inventoryPlayer.player, inventoryPlayer, inventoryPlayer.getSizeInventory() - 1 - i,
//                    9, 8 + i * 18, i));
        }

        addPlayerSlots(inventoryPlayer, 84);
    }
}
