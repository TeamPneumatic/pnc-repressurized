package igwmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 
 * @author MineMaarten
 */

public abstract class LocationIntPacket<REQ extends IMessage> extends AbstractPacket<REQ>{

    protected int x, y, z;

    public LocationIntPacket(){

    }

    public LocationIntPacket(int x, int y, int z){

        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf){

        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    public void fromBytes(ByteBuf buf){

        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    public NetworkRegistry.TargetPoint getTargetPoint(World world){

        return getTargetPoint(world, 64);
    }

    public NetworkRegistry.TargetPoint getTargetPoint(World world, double updateDistance){

        return new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, updateDistance);
    }
}
