package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerLiquidHopper extends ContainerPneumaticBase<TileEntityLiquidHopper> {

    public ContainerLiquidHopper(InventoryPlayer inventoryPlayer, TileEntityLiquidHopper te) {
        super(te);

        addUpgradeSlots(48, 29);

        addPlayerSlots(inventoryPlayer, 84);
    }

//    @Override
//    public boolean canInteractWith(EntityPlayer player) {
//        return te.isGuiUseableByPlayer(player);
//    }

}
