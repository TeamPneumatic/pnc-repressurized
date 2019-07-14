package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.AreaShowManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to make a tile entity render its area of effect
 */
public class PacketShowArea extends LocationIntPacket {
    private BlockPos[] area;

    public PacketShowArea() {
    }

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
            area[i] = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(area.length);
        for (BlockPos pos : area) {
            buffer.writeInt(pos.getX());
            buffer.writeInt(pos.getY());
            buffer.writeInt(pos.getZ());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AreaShowManager.getInstance().showArea(area, 0x9000FFFF, getTileEntity(ctx));
        });
        ctx.get().setPacketHandled(true);
    }
}
