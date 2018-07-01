package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Sent to inform the client that other players are flying and should have their
 * model rotated accordingly.
 */
public class PacketMarkPlayerFlying extends AbstractPacket<PacketMarkPlayerFlying> {
    private static final Set<UUID> rotatedPlayers = new HashSet<>();

    private UUID playerID;
    private boolean flying;

    public PacketMarkPlayerFlying() {
    }

    public PacketMarkPlayerFlying(EntityPlayer player, boolean flying) {
        this.playerID = player.getUniqueID();
        this.flying = flying;
    }

    @Override
    public void handleClientSide(PacketMarkPlayerFlying message, EntityPlayer player) {
        if (message.flying) {
            rotatedPlayers.add(message.playerID);
        } else {
            rotatedPlayers.remove(message.playerID);
        }
    }

    @Override
    public void handleServerSide(PacketMarkPlayerFlying message, EntityPlayer player) {

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long l1 = buf.readLong();
        long l2 = buf.readLong();
        playerID = new UUID(l1, l2);
        flying = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(playerID.getMostSignificantBits());
        buf.writeLong(playerID.getLeastSignificantBits());
        buf.writeBoolean(flying);
    }

    public static boolean shouldPlayerBeRotated(EntityPlayer player) {
        return rotatedPlayers.contains(player.getUniqueID());
    }
}
