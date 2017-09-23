package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.AreaShowManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class PacketShowArea extends LocationIntPacket<PacketShowArea> {
    private BlockPos[] area;

    public PacketShowArea() {
    }

    public PacketShowArea(BlockPos pos, BlockPos... area) {
        super(pos);
        this.area = area;
    }

    public PacketShowArea(BlockPos pos, Set<BlockPos> area) {
        this(pos, area.toArray(new BlockPos[area.size()]));
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

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        area = new BlockPos[buffer.readInt()];
        for (int i = 0; i < area.length; i++) {
            area[i] = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }
    }

    @Override
    public void handleClientSide(PacketShowArea message, EntityPlayer player) {
        AreaShowManager.getInstance().showArea(message.area, 0x00FFFF, message.getTileEntity(player.world));
    }

    @Override
    public void handleServerSide(PacketShowArea message, EntityPlayer player) {
    }

}
