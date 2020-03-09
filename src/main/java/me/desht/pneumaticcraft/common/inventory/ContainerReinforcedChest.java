package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityReinforcedChest;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerReinforcedChest extends ContainerPneumaticBase<TileEntityReinforcedChest> {
    public ContainerReinforcedChest(int windowId, PlayerInventory invPlayer, BlockPos pos) {
        super(ModContainers.REINFORCED_CHEST.get(), windowId, invPlayer, pos);

        for (int i = 0; i < TileEntityReinforcedChest.CHEST_SIZE; i++) {
            addSlot(new SlotItemHandler(te.getPrimaryInventory(), i, 8 + (i % 9) * 18, 18 + (i / 9) * 18));
        }
        addPlayerSlots(invPlayer, 104);
    }

    public ContainerReinforcedChest(int windowId, PlayerInventory invPlayer, PacketBuffer buffer) {
        this(windowId, invPlayer, getTilePos(buffer));
    }
}
