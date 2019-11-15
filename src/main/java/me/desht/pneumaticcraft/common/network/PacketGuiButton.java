package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent when a GUI button is clicked.
 */
public class PacketGuiButton {
    private String tag;

    public PacketGuiButton() {
    }

    public PacketGuiButton(String tag) {
        this.tag = tag;
    }

    public PacketGuiButton(PacketBuffer buffer) {
        tag = buffer.readString();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeString(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player.openContainer instanceof IGUIButtonSensitive) {
                ((IGUIButtonSensitive) player.openContainer).handleGUIButtonPress(tag, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
