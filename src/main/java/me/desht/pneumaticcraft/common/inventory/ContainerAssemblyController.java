package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerAssemblyController extends ContainerPneumaticBase<TileEntityAssemblyController> {

    public ContainerAssemblyController(InventoryPlayer inventoryPlayer, TileEntityAssemblyController te) {
        super(te);

        addSlotToContainer(new SlotItemSpecific(te.getPrimaryInventory(), Itemss.ASSEMBLY_PROGRAM, 0, 74, 38));

        addUpgradeSlots(13, 31);

        addPlayerSlots(inventoryPlayer, 84);
    }

}
