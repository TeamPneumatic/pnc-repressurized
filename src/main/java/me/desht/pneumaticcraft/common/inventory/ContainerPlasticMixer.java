package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPlasticMixer extends ContainerPneumaticBase<TileEntityPlasticMixer> {

    public ContainerPlasticMixer(InventoryPlayer inventoryPlayer, TileEntityPlasticMixer te) {
        super(te);

        addUpgradeSlots(11, 29);

        addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), TileEntityPlasticMixer.INV_INPUT, 98, 26));
        addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), TileEntityPlasticMixer.INV_OUTPUT, 98, 58));
        for (int i = 0; i < 3; i++) {
            addSlotToContainer(new SlotInventoryLimiting(te.getPrimaryInventory(), TileEntityPlasticMixer.INV_DYE_RED + i, 127, 22 + i * 18));
        }

        addPlayerSlots(inventoryPlayer, 84);
    }

}
