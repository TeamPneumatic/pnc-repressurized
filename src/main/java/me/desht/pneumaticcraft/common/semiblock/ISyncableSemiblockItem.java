package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

/**
 * Implement this interface on containers for semiblocks which need to be synced from the client in item form,
 * generally as a result of GUI configuration.
 * E.g. see Logistics Frames
 */
public interface ISyncableSemiblockItem {
    void syncSemiblockItemFromClient(PlayerEntity player, PacketBuffer payload);
}
