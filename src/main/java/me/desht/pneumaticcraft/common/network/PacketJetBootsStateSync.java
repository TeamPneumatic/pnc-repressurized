package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent from server so that all clients know the jet boots state (enabled/active) of all other clients.
 * Allows us to play particles, do rotations, etc. with minimum traffic.
 */
public class PacketJetBootsStateSync {
    private final UUID playerId;
    private final JetBootsStateTracker.JetBootsState state;

    public PacketJetBootsStateSync(PlayerEntity player, JetBootsStateTracker.JetBootsState state) {
        this.playerId = player.getUniqueID();
        this.state = state;
    }

    PacketJetBootsStateSync(PacketBuffer buf) {
        playerId = new UUID(buf.readLong(), buf.readLong());
        state = new JetBootsStateTracker.JetBootsState(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeLong(playerId.getMostSignificantBits());
        buf.writeLong(playerId.getLeastSignificantBits());
        buf.writeBoolean(state.isEnabled());
        buf.writeBoolean(state.isActive());
        buf.writeBoolean(state.isBuilderMode());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> JetBootsStateTracker.getTracker(ClientUtils.getClientPlayer()).setJetBootsState(playerId, state));
        ctx.get().setPacketHandled(true);
    }

}
