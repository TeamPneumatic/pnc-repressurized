package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerUniversalSensor extends ContainerPneumaticBase<TileEntityUniversalSensor> {

    public ContainerUniversalSensor(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public ContainerUniversalSensor(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.UNIVERSAL_SENSOR, windowId, playerInventory, pos);

        addUpgradeSlots(19, 108);

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 29, 72));

        addPlayerSlots(playerInventory, 157);
    }
}
