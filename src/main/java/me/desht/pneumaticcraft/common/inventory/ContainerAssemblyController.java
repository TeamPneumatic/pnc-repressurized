package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerAssemblyController extends ContainerPneumaticBase<TileEntityAssemblyController> {

    public ContainerAssemblyController(InventoryPlayer inventoryPlayer, TileEntityAssemblyController te) {
        super(te);

        // Add the burn slot.
        addSlotToContainer(new SlotItemSpecific(te.getPrimaryInventory(), Itemss.ASSEMBLY_PROGRAM, 0, 74, 38));

        addUpgradeSlots(13, 31);

        // add the upgrade slots
//        for (int i = 0; i < 2; i++) {
//            for (int j = 0; j < 2; j++) {
//                addSlotToContainer(new SlotInventoryLimiting(te, i * 2 + j + 1, 13 + j * 18, 31 + i * 18));
//            }
//        }

        addPlayerSlots(inventoryPlayer, 84);
    }

}
