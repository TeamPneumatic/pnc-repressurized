package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerAirCannon extends ContainerPneumaticBase<TileEntityAirCannon> {

    public ContainerAirCannon(InventoryPlayer inventoryPlayer, TileEntityAirCannon te) {
        super(te);

        addUpgradeSlots(8, 29);

        // add the gps slot
        addSlotToContainer(new SlotItemSpecific(te.getPrimaryInventory(), Itemss.GPS_TOOL, 1, 51, 29));

        // add the cannoned slot.
        addSlotToContainer(new SlotItemHandler(te.getPrimaryInventory(), 0, 79, 40));

        addPlayerSlots(inventoryPlayer, 84);

    }

}
