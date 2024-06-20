package me.desht.pneumaticcraft.common.network;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by GPS Tool and Area GPS Tool GUI when the Teleport button is pressed
 */
public record PacketTeleportCommand(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketTeleportCommand> TYPE = new Type<>(RL("teleport_command"));

    public static final StreamCodec<FriendlyByteBuf, PacketTeleportCommand> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketTeleportCommand::pos,
            PacketTeleportCommand::new
    );

    @Override
    public Type<PacketTeleportCommand> type() {
        return TYPE;
    }

    public static void handle(PacketTeleportCommand message, IPayloadContext ctx) {
        CommandSourceStack stack = ctx.player().createCommandSourceStack();
        String command = String.format("tp %d %d %d", message.pos().getX(), message.pos().getY(), message.pos().getZ());
        stack.getServer().getCommands().performPrefixedCommand(stack, command);
    }
}
