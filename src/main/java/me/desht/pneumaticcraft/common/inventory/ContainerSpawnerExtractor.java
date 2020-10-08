package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySpawnerExtractor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerSpawnerExtractor extends ContainerPneumaticBase<TileEntitySpawnerExtractor> {
    public ContainerSpawnerExtractor(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public ContainerSpawnerExtractor(int windowId, PlayerInventory inv, BlockPos pos) {
        super(ModContainers.SPAWNER_EXTRACTOR.get(), windowId, inv, pos);

        addUpgradeSlots(23, 29);

        addPlayerSlots(inv, 84);
    }
}
