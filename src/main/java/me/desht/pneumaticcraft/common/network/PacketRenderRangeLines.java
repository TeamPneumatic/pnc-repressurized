package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.tileentity.IRangeLineShower;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to tell the client-side tile entity to render its area of effect
 */
public class PacketRenderRangeLines extends LocationIntPacket {

    public PacketRenderRangeLines() {
    }

    public PacketRenderRangeLines(TileEntity te) {
        super(te.getPos());
    }

    public PacketRenderRangeLines(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ClientUtils.getClientTE(pos);
            if (te instanceof IRangeLineShower) {
                ((IRangeLineShower) te).showRangeLines();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
