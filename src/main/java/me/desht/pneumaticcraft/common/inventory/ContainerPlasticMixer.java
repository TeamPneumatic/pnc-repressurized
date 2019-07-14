package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPlasticMixer extends ContainerPneumaticBase<TileEntityPlasticMixer> {

    public ContainerPlasticMixer(PlayerInventory inventoryPlayer, TileEntityPlasticMixer te) {
        super(te);

        addUpgradeSlots(11, 29);

        addSlotToContainer(new SlotItemHandler(te.getInventoryCap(), TileEntityPlasticMixer.INV_INPUT, 98, 26));
        addSlotToContainer(new SlotOutput(te.getInventoryCap(), TileEntityPlasticMixer.INV_OUTPUT, 98, 58));
        for (int i = 0; i < 3; i++) {
            addSlotToContainer(new SlotItemHandler(te.getInventoryCap(), TileEntityPlasticMixer.INV_DYE_RED + i, 128, 20 + i * 18));
        }

        addPlayerSlots(inventoryPlayer, 84);
    }

}
