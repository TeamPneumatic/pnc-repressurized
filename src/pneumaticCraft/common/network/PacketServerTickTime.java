package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketServerTickTime extends AbstractPacket<PacketServerTickTime>{
    private double tickTime;
    public static double tickTimeMultiplier = 1;

    public PacketServerTickTime(){}

    public PacketServerTickTime(double tickTime){
        this.tickTime = tickTime;
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        tickTime = buffer.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeDouble(tickTime);
    }

    @Override
    public void handleClientSide(PacketServerTickTime message, EntityPlayer player){
        tickTimeMultiplier = Math.min(1, 50D / Math.max(message.tickTime, 0.01));
    }

    @Override
    public void handleServerSide(PacketServerTickTime message, EntityPlayer player){}

}
