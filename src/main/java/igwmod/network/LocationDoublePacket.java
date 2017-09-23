package igwmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 
 * @author MineMaarten
 */

public abstract class LocationDoublePacket<REQ extends IMessage> extends AbstractPacket<REQ>{

    protected double x, y, z;

    public LocationDoublePacket(){

    }

    public LocationDoublePacket(double x, double y, double z){

        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf){

        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    @Override
    public void fromBytes(ByteBuf buf){

        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    public NetworkRegistry.TargetPoint getTargetPoint(World world){

        return getTargetPoint(world, 64);
    }

    public NetworkRegistry.TargetPoint getTargetPoint(World world, double updateDistance){

        return new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, updateDistance);
    }
}
