package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPressureChamber extends ContainerPneumaticBase<TileEntityPressureChamberValve> {

    public ContainerPressureChamber(InventoryPlayer inventoryPlayer, TileEntityPressureChamberValve te) {
        super(te);

        addUpgradeSlots(48, 29);

        addPlayerSlots(inventoryPlayer, 84);
    }

}
