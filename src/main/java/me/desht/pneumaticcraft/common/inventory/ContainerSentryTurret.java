package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerSentryTurret extends ContainerPneumaticBase<TileEntitySentryTurret> {

    public ContainerSentryTurret(InventoryPlayer inventoryPlayer, TileEntitySentryTurret te) {
        super(te);

        // Add the hopper slots.
        for (int i = 0; i < 4; i++)
            addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), i, 80 + i * 18, 29));

        addUpgradeSlots(23, 29);

        addPlayerSlots(inventoryPlayer, 84);
    }

//    @Override
//    public boolean canInteractWith(EntityPlayer player) {
//        return te.isUseableByPlayer(player);
//    }
}
