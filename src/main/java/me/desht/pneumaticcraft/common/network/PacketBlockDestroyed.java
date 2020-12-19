package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by server when a block is dropped by shift-wrenching it. This happens server-side but the client needs
 * to know too so neighbouring cached block shapes can be recalculated.
 */
public class PacketBlockDestroyed extends LocationIntPacket {
    public PacketBlockDestroyed(BlockPos pos) {
        super(pos);
    }

    public PacketBlockDestroyed(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) {
                World w = ClientUtils.getClientWorld();
                w.getBlockState(pos).updateNeighbours(w, pos, Constants.BlockFlags.DEFAULT);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
