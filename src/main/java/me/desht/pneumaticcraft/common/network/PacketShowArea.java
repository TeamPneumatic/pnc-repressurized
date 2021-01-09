package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to make a tile entity render its area of effect
 */
public class PacketShowArea extends LocationIntPacket {
    private final BlockPos[] area;

    public PacketShowArea(BlockPos pos, BlockPos... area) {
        super(pos);
        this.area = area;
    }

    public PacketShowArea(BlockPos pos, Set<BlockPos> area) {
        this(pos, area.toArray(new BlockPos[0]));
    }

    PacketShowArea(PacketBuffer buffer) {
        super(buffer);
        area = new BlockPos[buffer.readInt()];
        for (int i = 0; i < area.length; i++) {
            area[i] = buffer.readBlockPos();
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeInt(area.length);
        Arrays.stream(area).forEach(buffer::writeBlockPos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AreaRenderManager.getInstance().showArea(area, 0x9000FFFF, ClientUtils.getClientTE(pos)));
        ctx.get().setPacketHandled(true);
    }
}
