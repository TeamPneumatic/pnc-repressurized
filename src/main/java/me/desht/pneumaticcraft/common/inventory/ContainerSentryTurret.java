package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSentryTurret extends ContainerPneumaticBase<TileEntitySentryTurret> {

    public ContainerSentryTurret(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerSentryTurret(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.SENTRY_TURRET, windowId, playerInventory, pos);

        // Add the hopper slots.
        for (int i = 0; i < 4; i++)
            addSlot(new SlotItemHandler(te.getPrimaryInventory(), i, 80 + i * 18, 29));

        addUpgradeSlots(23, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
