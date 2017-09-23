package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerAdvancedLiquidCompressor extends ContainerLiquidCompressor {

    public ContainerAdvancedLiquidCompressor(InventoryPlayer inventoryPlayer, TileEntityAdvancedLiquidCompressor te) {
        super(inventoryPlayer, te);
    }

    @Override
    protected int getFluidContainerOffset() {
        return 52;
    }

}
