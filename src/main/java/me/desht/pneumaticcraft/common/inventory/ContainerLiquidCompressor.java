package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerLiquidCompressor extends ContainerPneumaticBase<TileEntityLiquidCompressor> {

    public ContainerLiquidCompressor(InventoryPlayer inventoryPlayer, TileEntityLiquidCompressor te) {
        super(te);

        addUpgradeSlots(11, 29);

        addSlotToContainer(new SlotFullFluidContainer(te.getPrimaryInventory(), 0, getFluidContainerOffset(), 22));
        addSlotToContainer(new SlotOutput(te.getPrimaryInventory(), 1, getFluidContainerOffset(), 55));

        addPlayerSlots(inventoryPlayer, 84);
    }

    protected int getFluidContainerOffset() {
        return 62;
    }

}
