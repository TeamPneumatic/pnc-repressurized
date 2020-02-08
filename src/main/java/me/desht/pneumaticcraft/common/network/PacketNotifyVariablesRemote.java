package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when the Remote GUI is being opened
 * TODO: should be possible to include this data in the open gui message?
 */
public class PacketNotifyVariablesRemote {
    private String[] variables;

    public PacketNotifyVariablesRemote() {

    }

    public PacketNotifyVariablesRemote(String[] variables) {
        this.variables = variables;
    }

    public PacketNotifyVariablesRemote(PacketBuffer buffer) {
        variables = new String[buffer.readVarInt()];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = buffer.readString();
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(variables.length);
        Arrays.stream(variables).forEach(buf::writeString);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ClientUtils.getClientPlayer();
            if (player.openContainer instanceof ContainerRemote) {
                ((ContainerRemote) player.openContainer).variables = variables;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
