package me.desht.pneumaticcraft.common.network;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by GPS Tool and Area GPS Tool GUI when the Teleport button is pressed
 */
public class PacketTeleportCommand {
    private final BlockPos targetPos;

    public PacketTeleportCommand(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    public PacketTeleportCommand(FriendlyByteBuf buf) {
        this.targetPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(targetPos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null && sender.getServer() != null) {
                CommandSourceStack stack = sender.createCommandSourceStack();
                String command = String.format("tp %d %d %d", targetPos.getX(), targetPos.getY(), targetPos.getZ());
                sender.getServer().getCommands().performPrefixedCommand(stack, command);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
