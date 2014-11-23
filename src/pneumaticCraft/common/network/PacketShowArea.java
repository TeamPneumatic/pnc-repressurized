package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.AreaShowManager;

public class PacketShowArea extends LocationIntPacket<PacketShowArea>{
    private ChunkPosition[] area;

    public PacketShowArea(){}

    public PacketShowArea(int x, int y, int z, ChunkPosition... area){
        super(x, y, z);
        this.area = area;
    }

    public PacketShowArea(int x, int y, int z, Set<ChunkPosition> area){
        this(x, y, z, area.toArray(new ChunkPosition[area.size()]));
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(area.length);
        for(ChunkPosition pos : area) {
            buffer.writeInt(pos.chunkPosX);
            buffer.writeInt(pos.chunkPosY);
            buffer.writeInt(pos.chunkPosZ);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        area = new ChunkPosition[buffer.readInt()];
        for(int i = 0; i < area.length; i++) {
            area[i] = new ChunkPosition(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }
    }

    @Override
    public void handleClientSide(PacketShowArea message, EntityPlayer player){
        AreaShowManager.getInstance().showArea(message.area, 0x00FFFF, message.getTileEntity(player.worldObj));
    }

    @Override
    public void handleServerSide(PacketShowArea message, EntityPlayer player){}

}
