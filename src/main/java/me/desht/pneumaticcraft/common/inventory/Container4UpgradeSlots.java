package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.InventoryPlayer;

public class Container4UpgradeSlots<Tile extends TileEntityBase> extends ContainerPneumaticBase<Tile> {

    public Container4UpgradeSlots(InventoryPlayer inventoryPlayer, Tile te) {
        super(te);

        addUpgradeSlots(48, 29);
        addPlayerSlots(inventoryPlayer, 84);
    }
}
