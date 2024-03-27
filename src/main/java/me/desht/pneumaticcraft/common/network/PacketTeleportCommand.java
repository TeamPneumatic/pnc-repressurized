package me.desht.pneumaticcraft.common.network;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by GPS Tool and Area GPS Tool GUI when the Teleport button is pressed
 */
public record PacketTeleportCommand(BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("teleport_command");

    public static PacketTeleportCommand fromNetwork(FriendlyByteBuf buf) {
        return new PacketTeleportCommand(buf.readBlockPos());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketTeleportCommand message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            CommandSourceStack stack = player.createCommandSourceStack();
            String command = String.format("tp %d %d %d", message.pos().getX(), message.pos().getY(), message.pos().getZ());
            player.getServer().getCommands().performPrefixedCommand(stack, command);
        }));
    }
}
