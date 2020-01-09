package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerUVLightBox extends ContainerPneumaticBase<TileEntityUVLightBox> {

    public ContainerUVLightBox(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerUVLightBox(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.UV_LIGHT_BOX, i, playerInventory, pos);

        addSlot(new SlotItemSpecific(te.getPrimaryInventory(), ModItems.EMPTY_PCB, 0, 71, 36));

        addUpgradeSlots(21, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
