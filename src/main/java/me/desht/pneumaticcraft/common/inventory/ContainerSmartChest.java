package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSmartChest extends ContainerPneumaticBase<TileEntitySmartChest> {
    public static final int N_COLS = 12;

    public ContainerSmartChest(int windowId, PlayerInventory inv, BlockPos pos) {
        super(ModContainers.SMART_CHEST.get(), windowId, inv, pos);

        for (int i = 0; i < TileEntitySmartChest.CHEST_SIZE; i++) {
            addSlot(new SlotItemHandler(te.getPrimaryInventory(), i, 8 + (i % N_COLS) * 18, 18 + (i / N_COLS) * 18));
        }

        addUpgradeSlots(187, 148);

        addPlayerSlots(inv, 130);
    }

    public ContainerSmartChest(int windowId, PlayerInventory inv, PacketBuffer buffer) {
        this(windowId, inv, getTilePos(buffer));
    }
}
