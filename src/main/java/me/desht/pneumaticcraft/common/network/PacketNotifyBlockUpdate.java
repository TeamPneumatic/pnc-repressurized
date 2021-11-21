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
 * Sent by server when a block is dropped by shift-wrenching it, rotated by wrenching it, or if a pneumatic TE explodes
 * due to overpressure.
 * This happens server-side (block updates are triggered on the server), but the client needs to know too so that
 * neighbouring cached block shapes (pressure tubes especially, but potentially anything) can be recalculated.
 */
public class PacketNotifyBlockUpdate extends LocationIntPacket {
    public PacketNotifyBlockUpdate(BlockPos pos) {
        super(pos);
    }

    public PacketNotifyBlockUpdate(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) {
                World w = ClientUtils.getClientWorld();
                w.getBlockState(pos).updateNeighbourShapes(w, pos, Constants.BlockFlags.DEFAULT);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
