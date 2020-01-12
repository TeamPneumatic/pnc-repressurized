package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerSecurityStationMain extends ContainerPneumaticBase<TileEntitySecurityStation> {

    public ContainerSecurityStationMain(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerSecurityStationMain(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.SECURITY_STATION_MAIN.get(), windowId, playerInventory, pos);

        //add the network slots
        for (int i = 0; i < TileEntitySecurityStation.INV_ROWS; i++) {
            for (int j = 0; j < TileEntitySecurityStation.INV_COLS; j++) {
                addSlot(new SlotItemSpecific(te.getPrimaryInventory(), stack -> stack.getItem() instanceof ItemNetworkComponent, j + i * 5, 17 + j * 18, 22 + i * 18));
            }
        }

        addUpgradeSlots(128, 62);

        addPlayerSlots(playerInventory, 157);
    }
}
