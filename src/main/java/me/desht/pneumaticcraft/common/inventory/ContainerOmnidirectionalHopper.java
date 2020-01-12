package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerOmnidirectionalHopper extends ContainerPneumaticBase<TileEntityOmnidirectionalHopper> {

    public ContainerOmnidirectionalHopper(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public ContainerOmnidirectionalHopper(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.OMNIDIRECTIONAL_HOPPER.get(), windowId, playerInventory, pos);

        addUpgradeSlots(23, 29);

        for (int i = 0; i < TileEntityOmnidirectionalHopper.INVENTORY_SIZE; i++) {
            addSlot(new SlotItemHandler(te.getPrimaryInventory(), i, 68 + i * 18, 36));
        }

        addPlayerSlots(playerInventory, 84);
    }
}
